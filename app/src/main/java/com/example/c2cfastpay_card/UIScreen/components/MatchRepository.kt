package com.example.c2cfastpay_card.UIScreen.components

import android.content.Context
import android.util.Log
import com.example.c2cfastpay_card.data.Like
import com.example.c2cfastpay_card.data.User
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MatchRepository(private val context: Context) {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * 核心功能：右滑喜歡 (Like)
     * 回傳值：Boolean (true 代表配對成功！ false 代表只是單向喜歡)
     */
    suspend fun likeProduct(product: ProductItem): Boolean {
        val myId = getCurrentUserId() ?: return false

        // 1. 取得我的名字 (為了寫入 Like 資料)
        val mySnapshot = db.collection("users").document(myId).get().await()
        val me = mySnapshot.toObject(User::class.java)
        val myName = me?.name ?: "未知用戶"

        // 2. 建立 Like 物件
        val like = Like(
            id = "${myId}_${product.id}", // 確保唯一性：我對某個商品只能喜歡一次
            likerId = myId,
            likerName = myName,
            productId = product.id,
            productOwnerId = product.ownerId
        )

        // 3. 寫入 Firestore "likes" 集合
        db.collection("likes")
            .document(like.id)
            .set(like)
            .await()
        Log.d("MatchRepository", "已送出喜歡: ${product.title}")

        // 4. 【關鍵邏輯】檢查是否「配對成功」(Mutual Like)
        // 檢查對方是否也喜歡過「我的任何一個商品」？
        // (這是一種簡化的配對邏輯：只要雙方互相喜歡對方的"某個"東西，就算配對)
        // 或者更嚴格：我喜歡他的 A，他喜歡我的 B (以物易物) -> 這比較複雜。

        // 我們先做「人對人」的興趣檢查：
        // 查詢：是否有任何一筆 Like，是「對方 (product.ownerId)」喜歡「我 (myId)」的商品？
        val mutualLikeSnapshot = db.collection("likes")
            .whereEqualTo("likerId", product.ownerId) // 對方是按讚者
            .whereEqualTo("productOwnerId", myId)     // 我是商品主人
            .limit(1) // 只要有一筆就夠了
            .get()
            .await()

        if (!mutualLikeSnapshot.isEmpty) {
            // --- 配對成功！ ---
            Log.d("MatchRepository", "🎉 配對成功！對方也喜歡你的商品")
            createMatch(myId, product.ownerId, product)
            return true
        }

        return false
    }

    /**
     * 建立配對紀錄 (Match) -> 這就是未來的「聊天室」
     */
    private suspend fun createMatch(myId: String, otherId: String, product: ProductItem) {
        // 聊天室 ID：將兩個 UID 排序後組合，確保 A+B 和 B+A 是同一個 ID
        val userIds = listOf(myId, otherId).sorted()
        val matchId = "${userIds[0]}_${userIds[1]}"

        val matchData = hashMapOf(
            "id" to matchId,
            "users" to userIds, // 參與者列表
            "lastMessage" to "配對成功！開始聊天吧",
            "timestamp" to System.currentTimeMillis(),
            // 也可以記錄是因為哪個商品配對的
            "matchedProductImage" to product.imageUri
        )

        db.collection("matches")
            .document(matchId)
            .set(matchData) // 使用 set (merge) 避免覆蓋舊聊天紀錄
            .await()
    }

    suspend fun getMatches(): List<MatchItem> {
        val myId = getCurrentUserId() ?: return emptyList()

        try {
            // 查詢：所有「users 欄位包含我」的文件 (也就是我參與的配對)
            val snapshot = db.collection("matches")
                .whereArrayContains("users", myId)
                .get()
                .await()

            // 將 Firestore 資料轉換為 MatchItem
            return snapshot.documents.mapNotNull { doc ->
                MatchItem(
                    id = doc.getString("id") ?: "",
                    productId = "", // 暫時留空
                    productTitle = doc.getString("matchedProductTitle") ?: "未知商品",
                    productImageUrl = doc.getString("matchedProductImage") ?: "",
                    // productPrice 在 MatchItem 定義中如果是 String，就用 getString
                    // 如果您的 MatchItem 還沒有 price 欄位，這裡可以先不填
                    timestamp = doc.getLong("timestamp") ?: 0L
                )
            }
        } catch (e: Exception) {
            Log.e("MatchRepository", "讀取配對失敗", e)
            return emptyList()
        }
    // (原本的 getMatches 函式如果是讀本地的，這裡要暫時移除或改寫成讀取 "matches" 集合)
    }
}