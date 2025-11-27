package com.example.c2cfastpay_card.UIScreen.components

import android.content.Context
import android.util.Log
import com.example.c2cfastpay_card.data.Like
import com.example.c2cfastpay_card.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// 1. 新增滑動方向列舉
enum class SwipeDirection { LEFT, RIGHT }

// 2. 新增滑動紀錄資料模型
data class SwipeRecord(
    val userId: String = "",
    val productId: String = "",
    val direction: String = "", // "LEFT" or "RIGHT"
    val timestamp: Long = System.currentTimeMillis()
)

class MatchRepository(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * ★ 新增：記錄滑動 (不論左滑右滑都記)
     * 用來防止卡片重複出現
     */
    suspend fun recordSwipe(productId: String, direction: SwipeDirection) {
        val myId = getCurrentUserId() ?: return

        val swipeData = SwipeRecord(
            userId = myId,
            productId = productId,
            direction = direction.name
        )

        // 使用 "userId_productId" 當作 ID，確保每個商品只會被記錄一次
        val docId = "${myId}_${productId}"

        try {
            db.collection("swipes").document(docId).set(swipeData).await()
            Log.d("MatchRepository", "已記錄滑動: $productId -> $direction")
        } catch (e: Exception) {
            Log.e("MatchRepository", "記錄滑動失敗", e)
        }
    }

    /**
     * ★ 新增：取得「我已經滑過」的所有商品 ID 列表
     */
    suspend fun getSwipedProductIds(): List<String> {
        val myId = getCurrentUserId() ?: return emptyList()

        return try {
            val snapshot = db.collection("swipes")
                .whereEqualTo("userId", myId)
                .get()
                .await()

            // 取出所有 productId
            snapshot.documents.mapNotNull { it.getString("productId") }
        } catch (e: Exception) {
            Log.e("MatchRepository", "取得已滑過列表失敗", e)
            emptyList()
        }
    }

    /**
     * 右滑喜歡 (Like) - 核心功能
     */
    suspend fun likeProduct(targetProduct: ProductItem): Boolean {
        val myId = getCurrentUserId()
        if (myId == null) return false

        if (targetProduct.ownerId.isBlank()) return false

        Log.d("MatchDebug", "開始執行 Like: 我 ($myId) -> 喜歡 -> 他 (${targetProduct.ownerId})")

        try {
            // 1. 取得我的名字
            val mySnapshot = db.collection("users").document(myId).get().await()
            val me = mySnapshot.toObject(User::class.java)
            val myName = me?.name ?: "未知用戶"

            // 2. 寫入 Like 資料
            val like = Like(
                id = "${myId}_${targetProduct.id}",
                likerId = myId,
                likerName = myName,
                productId = targetProduct.id,
                productOwnerId = targetProduct.ownerId
            )

            db.collection("likes").document(like.id).set(like).await()

            // ★ 順便記錄 Swipe (雖然 ViewModel 會呼叫，但這裡雙重確保也好)
            recordSwipe(targetProduct.id, SwipeDirection.RIGHT)

            // 3. 檢查配對 (Mutual Like)
            val mutualLikeSnapshot = db.collection("likes")
                .whereEqualTo("likerId", targetProduct.ownerId)
                .whereEqualTo("productOwnerId", myId)
                .limit(1)
                .get()
                .await()

            if (!mutualLikeSnapshot.isEmpty) {
                Log.d("MatchDebug", "配對成功！")

                val theirLikeDoc = mutualLikeSnapshot.documents.first()
                val myProductIdTheyLiked = theirLikeDoc.getString("productId")

                if (myProductIdTheyLiked != null) {
                    val myProductDoc = db.collection("products").document(myProductIdTheyLiked).get().await()

                    if (myProductDoc.exists()) {
                        val originPrice = myProductDoc.get("price")
                        val safePrice = when (originPrice) {
                            is Number -> originPrice.toDouble()
                            is String -> originPrice.toDoubleOrNull() ?: 0.0
                            else -> 0.0
                        }

                        val myProductData = hashMapOf<String, Any>(
                            "id" to myProductDoc.id,
                            "title" to (myProductDoc.getString("title") ?: "我的商品"),
                            "imageUrl" to (myProductDoc.getString("imageUri") ?: myProductDoc.getString("imageUrl") ?: ""),
                            "ownerId" to myId,
                            "price" to safePrice
                        )

                        val targetProductPrice = targetProduct.price.toDoubleOrNull() ?: 0.0
                        val targetProductData = hashMapOf<String, Any>(
                            "id" to targetProduct.id,
                            "title" to targetProduct.title,
                            "imageUrl" to targetProduct.imageUri,
                            "ownerId" to targetProduct.ownerId,
                            "price" to targetProductPrice
                        )

                        createMatchWithDetails(myId, targetProduct.ownerId, myProductData, targetProductData)
                        return true
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("MatchDebug", "Like 失敗: ${e.message}", e)
        }

        return false
    }

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

        db.collection("matches").document(matchId).set(matchData).await()
    }

    // 保留原本的 getMatches
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
            return emptyList()
        }
    }
}