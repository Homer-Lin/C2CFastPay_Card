package com.example.c2cfastpay_card.UIScreen.components

import android.content.Context
import android.util.Log
import com.example.c2cfastpay_card.data.Like
import com.example.c2cfastpay_card.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MatchRepository(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * 右滑喜歡 (Like) - 核心功能
     * 回傳 true 代表配對成功
     */
    suspend fun likeProduct(targetProduct: ProductItem): Boolean {
        val myId = getCurrentUserId()
        if (myId == null) {
            Log.e("MatchDebug", "錯誤：找不到目前使用者 ID (myId is null)")
            return false
        }

        // 檢查對方 ID 是否正常
        if (targetProduct.ownerId.isBlank()) {
            Log.e("MatchDebug", "錯誤：對方的商品 ownerId 是空的！無法配對。商品 ID: ${targetProduct.id}")
            return false
        }

        Log.d("MatchDebug", "開始執行 Like: 我 ($myId) -> 喜歡 -> 他 (${targetProduct.ownerId}) 的商品 (${targetProduct.title})")

        try {
            // 1. 取得我的名字 (為了寫入 Like 資料)
            val mySnapshot = db.collection("users").document(myId).get().await()
            val me = mySnapshot.toObject(User::class.java)
            val myName = me?.name ?: "未知用戶"

            // 2. 寫入 Like 資料
            val like = Like(
                id = "${myId}_${targetProduct.id}", // 確保唯一性
                likerId = myId,
                likerName = myName,
                productId = targetProduct.id,
                productOwnerId = targetProduct.ownerId
            )

            db.collection("likes")
                .document(like.id)
                .set(like)
                .await()
            Log.d("MatchDebug", "Like 資料寫入成功")

            // 3. 檢查配對 (Mutual Like)
            Log.d("MatchDebug", "開始檢查對方是否喜歡過我...")

            val mutualLikeSnapshot = db.collection("likes")
                .whereEqualTo("likerId", targetProduct.ownerId) // 對方是按讚者
                .whereEqualTo("productOwnerId", myId)     // 我是商品主人
                .limit(1)
                .get()
                .await()

            if (!mutualLikeSnapshot.isEmpty) {
                Log.d("MatchDebug", "找到配對了！對方也喜歡我！準備建立聊天室...")

                // A. 找出對方喜歡我的哪個商品
                val theirLikeDoc = mutualLikeSnapshot.documents.first()
                val myProductIdTheyLiked = theirLikeDoc.getString("productId")
                Log.d("MatchDebug", "對方喜歡我的商品 ID: $myProductIdTheyLiked")

                if (myProductIdTheyLiked != null) {
                    val myProductDoc = db.collection("products").document(myProductIdTheyLiked).get().await()

                    if (myProductDoc.exists()) {

                        // --- 安全讀取價格 (避免崩潰) ---
                        val originPrice = myProductDoc.get("price")
                        val safePrice = when (originPrice) {
                            is Number -> originPrice.toDouble()
                            is String -> originPrice.toDoubleOrNull() ?: 0.0
                            else -> 0.0
                        }

                        // B. 準備我的商品資料快照
                        val myProductData = hashMapOf<String, Any>(
                            "id" to myProductDoc.id,
                            "title" to (myProductDoc.getString("title") ?: "我的商品"),
                            "imageUrl" to (myProductDoc.getString("imageUri") ?: myProductDoc.getString("imageUrl") ?: ""),
                            "ownerId" to myId,
                            "price" to safePrice
                        )

                        // C. 準備對方的商品資料快照
                        val targetProductPrice = targetProduct.price.toDoubleOrNull() ?: 0.0
                        val targetProductData = hashMapOf<String, Any>(
                            "id" to targetProduct.id,
                            "title" to targetProduct.title,
                            "imageUrl" to targetProduct.imageUri,
                            "ownerId" to targetProduct.ownerId,
                            "price" to targetProductPrice
                        )

                        // D. 建立詳細配對紀錄
                        createMatchWithDetails(myId, targetProduct.ownerId, myProductData, targetProductData)
                        return true
                    } else {
                        Log.e("MatchDebug", "錯誤：雖然配對成功，但在資料庫找不到『我被喜歡的商品』(ID: $myProductIdTheyLiked)")
                    }
                }
            } else {
                Log.d("MatchDebug", "目前尚未配對 (對方還沒按喜歡，或查詢不到)")
            }

        } catch (e: Exception) {
            Log.e("MatchDebug", "發生錯誤: ${e.message}", e)
            if (e.message?.contains("index") == true) {
                Log.e("MatchDebug", "請去 Logcat 點擊 Firebase 連結建立索引！")
            }
        }

        return false
    }

    /**
     * 建立詳細配對紀錄
     */
    private suspend fun createMatchWithDetails(
        myId: String,
        otherId: String,
        product1Map: HashMap<String, Any>,
        product2Map: HashMap<String, Any>
    ) {
        val userIds = listOf(myId, otherId).sorted()
        val matchId = "${userIds[0]}_${userIds[1]}"

        val matchData = hashMapOf(
            "id" to matchId,
            "users" to userIds,
            "lastMessage" to "配對成功！開始聊天吧",
            "timestamp" to FieldValue.serverTimestamp(),
            "product1" to product1Map,
            "product2" to product2Map
        )

        db.collection("matches")
            .document(matchId)
            .set(matchData)
            .await()

        Log.d("MatchDebug", "聊天室建立完成！Match ID: $matchId")
    }

    /**
     * 讀取配對列表
     */
    suspend fun getMatches(): List<MatchItem> {
        val myId = getCurrentUserId() ?: return emptyList()

        try {
            val snapshot = db.collection("matches")
                .whereArrayContains("users", myId)
                .get()
                .await()

            return snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null

                val p1 = data["product1"] as? Map<String, Any>
                val p2 = data["product2"] as? Map<String, Any>

                if (p1 == null || p2 == null) return@mapNotNull null

                val p1OwnerId = p1["ownerId"] as? String
                val otherProductMap = if (p1OwnerId == myId) p2 else p1

                MatchItem(
                    id = doc.getString("id") ?: "",
                    productId = otherProductMap["id"] as? String ?: "",
                    productTitle = otherProductMap["title"] as? String ?: "未知商品",
                    productImageUrl = otherProductMap["imageUrl"] as? String ?: "",
                    timestamp = doc.getTimestamp("timestamp")?.seconds ?: 0L
                )
            }
        } catch (e: Exception) {
            Log.e("MatchDebug", "讀取配對列表失敗", e)
            return emptyList()
        }
    }
}