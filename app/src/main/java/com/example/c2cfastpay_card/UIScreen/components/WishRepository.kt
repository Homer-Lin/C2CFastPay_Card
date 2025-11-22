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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class WishRepository(private val context: Context) {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage

    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * 1. 新增願望 (包含圖片上傳)
     */
    suspend fun addWish(wish: WishItem) {
        val userId = getCurrentUserId() ?: throw Exception("尚未登入")

        // A. 取得使用者資料
        val userSnapshot = db.collection("users").document(userId).get().await()
        val user = userSnapshot.toObject(User::class.java)
        val userName = user?.name ?: "未知用戶"
        val userEmail = user?.email ?: ""

        // B. 處理圖片上傳
        var finalImageUrl = ""
        if (wish.imageUri.isNotEmpty()) {
            if (wish.imageUri.startsWith("content://") || wish.imageUri.startsWith("file://")) {
                finalImageUrl = uploadImageToStorage(Uri.parse(wish.imageUri))
            } else {
                finalImageUrl = wish.imageUri
            }
        }

        // C. 準備資料
        val newWish = wish.copy(
            imageUri = finalImageUrl,
            ownerId = userId,
            ownerName = userName,
            ownerEmail = userEmail,
            timestamp = System.currentTimeMillis()
        )

        // D. 寫入 Firestore
        db.collection("wishes")
            .document(newWish.uuid)
            .set(newWish)
            .await()

        Log.d("WishRepository", "許願成功: ${newWish.title}")
    }

    /**
     * 2. 取得許願清單 (即時監聽 Flow，支援搜尋)
     * 【關鍵修改】這裡必須加入 searchQuery 參數
     */
    fun getWishListFlow(searchQuery: String = ""): Flow<List<WishItem>> = callbackFlow {
        val userId = getCurrentUserId()

        var baseQuery = db.collection("wishes")

        // 如果搜尋關鍵字不為空，則加入篩選條件
        val finalQuery = if (searchQuery.isNotBlank()) {
            // 使用前綴搜尋
            baseQuery.whereGreaterThanOrEqualTo("title", searchQuery)
                .whereLessThanOrEqualTo("title", searchQuery + "\uf8ff")
                .orderBy("title", Query.Direction.ASCENDING)
        } else {
            // 否則使用原本的時間排序
            baseQuery.orderBy("timestamp", Query.Direction.DESCENDING)
        }

        val registration = finalQuery
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("WishRepository", "Listen failed.", e)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val wishes = snapshot.toObjects(WishItem::class.java)
                    trySend(wishes)
                }
            }

        awaitClose { registration.remove() }
    }

    /**
     * 3. 根據 UUID 取得單一願望
     */
    suspend fun getWishByUuid(uuid: String): WishItem? {
        try {
            val doc = db.collection("wishes").document(uuid).get().await()
            return doc.toObject(WishItem::class.java)
        } catch (e: Exception) {
            Log.e("WishRepository", "找不到願望: $uuid", e)
            return null
        }
    }

    /**
     * 4. 刪除願望
     */
    suspend fun deleteWish(wishId: String) {
        db.collection("wishes")
            .document(wishId)
            .delete()
            .await()
    }

    private suspend fun uploadImageToStorage(uri: Uri): String {
        val userId = getCurrentUserId() ?: return ""
        val filename = "wishes/$userId/${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child(filename)

        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }
}