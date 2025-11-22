package com.example.c2cfastpay_card.UIScreen.components

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.c2cfastpay_card.data.User
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProductRepository(private val context: Context) { // Context 雖然這裡暫時沒用到，但保留架構

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage

    // 取得當前用戶 ID
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * 1. 上架商品 (包含圖片上傳)
     */
    suspend fun addProduct(product: ProductItem) {
        val userId = getCurrentUserId() ?: throw Exception("尚未登入")

        // A. 取得使用者資料 (為了填寫 ownerName)
        val userSnapshot = db.collection("users").document(userId).get().await()
        val user = userSnapshot.toObject(User::class.java)
        val userName = user?.name ?: "未知賣家"
        val userEmail = user?.email ?: ""

        // B. 處理圖片上傳
        var finalImageUrl = ""
        if (product.imageUri.isNotEmpty()) {
            // 如果是 content:// 開頭的本地路徑，就上傳到 Storage
            if (product.imageUri.startsWith("content://") || product.imageUri.startsWith("file://")) {
                finalImageUrl = uploadImageToStorage(Uri.parse(product.imageUri))
            } else {
                // 如果已經是網址 (雖然上架時不太可能)，直接使用
                finalImageUrl = product.imageUri
            }
        }

        // C. 準備要寫入的資料 (補上擁有者資訊)
        val newProduct = product.copy(
            imageUri = finalImageUrl, // 換成雲端網址
            ownerId = userId,
            ownerName = userName,
            ownerEmail = userEmail,
            timestamp = System.currentTimeMillis()
        )

        // D. 寫入 Firestore
        // 使用 product.id 作為文件 ID，方便之後搜尋或刪除
        db.collection("products")
            .document(newProduct.id)
            .set(newProduct)
            .await()

        Log.d("ProductRepository", "商品上架成功: ${newProduct.title}")
    }

    /**
     * 輔助函式：上傳圖片到 Firebase Storage 並回傳下載網址
     */
    private suspend fun uploadImageToStorage(uri: Uri): String {
        val userId = getCurrentUserId() ?: return ""
        // 檔名：images/用戶ID/隨機ID.jpg
        val filename = "images/$userId/${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child(filename)

        // 上傳
        ref.putFile(uri).await()

        // 取得下載網址
        val downloadUrl = ref.downloadUrl.await()
        return downloadUrl.toString()
    }

    /**
     * 2. 取得「所有」商品 (改為 Flow 以便即時更新)
     * 這裡可以用來在首頁顯示
     */
    fun getAllProducts(): Flow<List<ProductItem>> = flow {
        try {
            val snapshot = db.collection("products")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val products = snapshot.toObjects(ProductItem::class.java)
            emit(products)
        } catch (e: Exception) {
            // 【新增】錯誤處理：印出錯誤 Log，並回傳空列表，防止崩潰
            Log.e("ProductRepository", "讀取商品失敗", e)
            emit(emptyList())
        }
    }

    /**
     * 3. 取得「配對用」商品
     * 規則：只顯示「別人」的商品 (排除自己)
     */
    suspend fun getProductsForMatching(): List<ProductItem> {
        val userId = getCurrentUserId() ?: return emptyList()

        val snapshot = db.collection("products")
            .whereNotEqualTo("ownerId", userId) // 過濾掉自己的
            .get()
            .await()

        // 注意：Firestore 的 whereNotEqualTo 可能需要建立索引，
        // 如果 Logcat 報錯說需要 Index，請點擊錯誤訊息裡的連結去建立。

        return snapshot.toObjects(ProductItem::class.java)
            .shuffled() // 隨機排序，增加配對趣味性
    }

    suspend fun getProductById(productId: String): ProductItem? {
        return try {
            val document = db.collection("products")
                .document(productId)
                .get()
                .await()
            document.toObject(ProductItem::class.java)
        } catch (e: Exception) {
            Log.e("ProductRepository", "找不到商品: $productId", e)
            null
        }
    }
}