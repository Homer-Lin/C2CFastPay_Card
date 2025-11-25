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

        // 1. 取得使用者資料
        val userName = try {
            val userSnapshot = db.collection("users").document(userId).get().await()
            val user = userSnapshot.toObject(User::class.java)
            user?.name ?: "未知賣家"
        } catch (e: Exception) {
            "未知賣家"
        }
        val userEmail = auth.currentUser?.email ?: ""

        // 2. 準備要上傳的圖片清單
        val uploadedImageUrls = product.images.toMutableList()

        // ★★★ 修正重點：先整理出要上傳的 Uri 列表，並去除重複 ★★★
        val urisToUpload = imageUris.distinct().toMutableList()

        // 檢查主圖 (product.imageUri) 是否需要加入上傳列表
        // 如果它不是網址(是本地路徑)，且不在 imageUris 列表中，才需要加進去
        if (product.imageUri.isNotEmpty()
            && !product.imageUri.startsWith("http")
            && !product.imageUri.startsWith("https")) {

            val mainUri = Uri.parse(product.imageUri)
            if (!urisToUpload.contains(mainUri)) {
                urisToUpload.add(0, mainUri) // 加在最前面
            }
        }

        // 3. 執行上傳 (只對整理好的列表跑迴圈)
        for (uri in urisToUpload) {
            val url = uploadImageToStorage(uri)
            if (url.isNotEmpty()) {
                uploadedImageUrls.add(url)
            }
        }

        // 4. 設定最終的主圖網址 (取上傳後的第一張)
        val finalMainImage = if (uploadedImageUrls.isNotEmpty()) uploadedImageUrls[0] else ""

        // 5. 準備寫入資料
        val newProduct = product.copy(
            imageUri = finalMainImage, // 更新首圖為網址
            images = uploadedImageUrls,
            ownerId = userId,
            ownerName = userName,
            ownerEmail = userEmail,
            timestamp = System.currentTimeMillis()
        )

        // 6. 寫入 Firestore
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

    suspend fun getProductsForMatching(swipedIds: List<String> = emptyList()): List<ProductItem> {
        val userId = getCurrentUserId()
        return try {
            val snapshot = db.collection("products")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await()

            val allProducts = snapshot.toObjects(ProductItem::class.java)

            allProducts.filter { product ->
                // 1. 排除自己的商品
                val isNotMine = userId == null || product.ownerId != userId
                // 2. 排除已經滑過的商品 (swipedIds)
                val isNotSwiped = !swipedIds.contains(product.id)

                isNotMine && isNotSwiped
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "讀取配對商品失敗", e)
            emptyList()
        }
    }

    suspend fun getProductById(productId: String): ProductItem? {
        return try {
            val snapshot = db.collection("products").document(productId).get().await()
            snapshot.toObject(ProductItem::class.java)
        } catch (e: Exception) {
            Log.e("ProductRepository", "找不到商品: $productId", e)
            null
        }
    }

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

    suspend fun deleteProduct(productId: String) {
        try {
            db.collection("products").document(productId).delete().await()
        } catch (e: Exception) {
            Log.e("ProductRepository", "刪除失敗", e)
        }
    }

    suspend fun getProductList(): List<ProductItem> = emptyList()
}