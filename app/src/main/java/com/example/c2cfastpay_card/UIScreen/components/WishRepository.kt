package com.example.c2cfastpay_card.UIScreen.components

import android.content.Context
import android.util.Log
import com.example.c2cfastpay_card.model.WishItem // ★ 請確認您的 WishItem 是在哪個 package
// 如果 WishItem 還在 components 資料夾，就改成: import com.example.c2cfastpay_card.UIScreen.components.WishItem

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class WishRepository(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val wishCollection = db.collection("wishes") // 假設集合名稱叫 wishes

    /**
     * 取得願望列表 (即時監聽 + 搜尋過濾)
     */
    fun getWishListFlow(searchQuery: String = ""): Flow<List<WishItem>> = callbackFlow {
        var query: Query = wishCollection

        // 簡單的搜尋邏輯：如果有輸入關鍵字，就篩選標題
        // 注意：Firestore 的全文檢索能力有限，這裡用簡單的範圍搜尋
        if (searchQuery.isNotBlank()) {
            query = query.whereGreaterThanOrEqualTo("title", searchQuery)
                .whereLessThanOrEqualTo("title", searchQuery + "\uf8ff")
        } else {
            // 沒搜尋時，依照時間排序 (最新的在上面)
            query = query.orderBy("timestamp", Query.Direction.DESCENDING)
        }

        val listener = query.addSnapshotListener { snapshot: QuerySnapshot?, e: FirebaseFirestoreException? ->
            if (e != null) {
                Log.w("WishRepository", "Listen failed.", e)
                trySend(emptyList())
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val allWishes = snapshot.toObjects(WishItem::class.java)
                trySend(allWishes)
            }
        }

        awaitClose { listener.remove() }
    }

    /**
     * 根據 UUID 取得單一願望 (給詳情頁用)
     */
    suspend fun getWishByUuid(uuid: String): WishItem? {
        return try {
            // 因為我們是用 uuid 欄位存 ID，而不是 Document ID，所以要用 whereEqualTo 查
            val snapshot = wishCollection
                .whereEqualTo("uuid", uuid)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                snapshot.documents.first().toObject(WishItem::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("WishRepository", "Get wish failed", e)
            null
        }
    }
}