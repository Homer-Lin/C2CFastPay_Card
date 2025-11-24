package com.example.c2cfastpay_card.UIScreen.components

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.c2cfastpay_card.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProductRepository(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * 上架商品 (支援多圖上傳)
     */
    suspend fun addProduct(product: ProductItem, imageUris: List<Uri> = emptyList()) {
        val userId = getCurrentUserId() ?: throw Exception("尚未登入")

        // 1. 取得使用者資料 (填寫 ownerName)
        // 這裡加個 try-catch 避免如果找不到 user 資料導致上架崩潰
        val userName = try {
            val userSnapshot = db.collection("users").document(userId).get().await()
            val user = userSnapshot.toObject(User::class.java)
            user?.name ?: "未知賣家"
        } catch (e: Exception) {
            "未知賣家"
        }
        // 為了簡化，Email 暫時不強制讀取，若 userSnapshot 失敗則留空
        val userEmail = auth.currentUser?.email ?: ""

        // 2. 上傳圖片 (處理多張圖)
        val uploadedImageUrls = product.images.toMutableList()

        // 相容：如果 imageUri 有值且不是網址 (是本地路徑)，也要上傳
        if (product.imageUri.isNotEmpty()
            && !product.imageUri.startsWith("http")
            && !product.imageUri.startsWith("https")) {

            val url = uploadImageToStorage(Uri.parse(product.imageUri))
            if (url.isNotEmpty()) {
                uploadedImageUrls.add(0, url) // 加在第一張
            }
        }

        // 處理新傳入的 List<Uri>
        for (uri in imageUris) {
            val url = uploadImageToStorage(uri)
            if (url.isNotEmpty()) uploadedImageUrls.add(url)
        }

        // 確保 imageUri (首圖) 有值，給列表頁顯示用
        val finalMainImage = if (uploadedImageUrls.isNotEmpty()) uploadedImageUrls[0] else ""

        // 3. 準備寫入資料
        val newProduct = product.copy(
            imageUri = finalMainImage, // 更新首圖為網址
            images = uploadedImageUrls,
            ownerId = userId,
            ownerName = userName,
            ownerEmail = userEmail,
            timestamp = System.currentTimeMillis()
        )

        // 4. 寫入 Firestore
        db.collection("products")
            .document(newProduct.id)
            .set(newProduct)
            .await()

        Log.d("ProductRepository", "商品上架成功: ${newProduct.title}")
    }

    // 上傳圖片輔助函式
    private suspend fun uploadImageToStorage(uri: Uri): String {
        return try {
            val userId = getCurrentUserId() ?: "guest"
            val filename = "images/$userId/${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child(filename)
            ref.putFile(uri).await()
            return ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("ProductRepository", "圖片上傳失敗: $uri", e)
            ""
        }
    }

    // 取得所有商品 (Flow)
    fun getAllProducts(searchQuery: String = ""): Flow<List<ProductItem>> = flow {
        var query: Query = db.collection("products")
            .orderBy("timestamp", Query.Direction.DESCENDING)

        if (searchQuery.isNotBlank()) {
            // Firestore 的簡單搜尋 (注意：這只支援前綴搜尋)
            query = db.collection("products")
                .orderBy("title")
                .startAt(searchQuery)
                .endAt(searchQuery + "\uf8ff")
        }

        try {
            val snapshot = query.get().await()
            emit(snapshot.toObjects(ProductItem::class.java))
        } catch (e: Exception) {
            Log.e("ProductRepository", "讀取商品失敗", e)
            emit(emptyList())
        }
    }

    suspend fun getProductsForMatching(): List<ProductItem> {
        val userId = getCurrentUserId()
        return try {
            // 從 Firestore 抓取所有商品
            val snapshot = db.collection("products")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await()

            val allProducts = snapshot.toObjects(ProductItem::class.java)

            // 過濾邏輯：如果有登入，就排除掉「ownerId」是自己的商品
            // 這樣才不會配對到自己賣的東西
            if (userId != null) {
                allProducts.filter { it.ownerId != userId }
            } else {
                allProducts
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "讀取配對商品失敗", e)
            emptyList()
        }
    }

    // ★★★【新增】取得單一商品詳情 (ProductDetailScreen 需要) ★★★
    suspend fun getProductById(productId: String): ProductItem? {
        return try {
            val snapshot = db.collection("products").document(productId).get().await()
            snapshot.toObject(ProductItem::class.java)
        } catch (e: Exception) {
            Log.e("ProductRepository", "找不到商品: $productId", e)
            null
        }
    }

    // 取得我的商品
    suspend fun getMyProducts(): List<ProductItem> {
        val userId = getCurrentUserId() ?: return emptyList()
        return try {
            val snapshot = db.collection("products")
                .whereEqualTo("ownerId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await()
            snapshot.toObjects(ProductItem::class.java)
        } catch (e: Exception) {
            Log.e("ProductRepository", "讀取我的商品失敗", e)
            emptyList()
        }
    }

    // 刪除商品
    suspend fun deleteProduct(productId: String) {
        try {
            db.collection("products").document(productId).delete().await()
        } catch (e: Exception) {
            Log.e("ProductRepository", "刪除失敗", e)
        }
    }

    // 相容舊函式
    suspend fun getProductList(): List<ProductItem> = emptyList()
}