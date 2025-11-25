package com.example.c2cfastpay_card.UIScreen.components

import android.content.Context
import android.util.Log
import com.example.c2cfastpay_card.data.Message
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * 1. 【新增】定義 MatchDetails 資料類別
 * 這會解決 ChatScreen 和 ChatViewModel 找不到型別的錯誤
 */
data class MatchDetails(
    val product1: Map<String, Any>,
    val product2: Map<String, Any>
)

class ChatRepository(private val context: Context) {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * 2. 【新增】讀取配對詳細資料
     * 用於 ChatScreen 頂部顯示雙方商品資訊
     */
    suspend fun getMatchDetails(matchId: String): MatchDetails? {
        return try {
            val doc = db.collection("matches").document(matchId).get().await()
            if (doc.exists()) {
                // 安全轉型：從 Firestore 讀取 Map
                val p1 = doc.get("product1") as? Map<String, Any>
                val p2 = doc.get("product2") as? Map<String, Any>

                if (p1 != null && p2 != null) {
                    MatchDetails(product1 = p1, product2 = p2)
                } else {
                    Log.w("ChatRepository", "配對資料不完整: 缺少 product1 或 product2")
                    null
                }
            } else {
                Log.w("ChatRepository", "找不到配對文件: $matchId")
                null
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "讀取 MatchDetails 失敗", e)
            null
        }
    }

    /**
     * 3. 發送訊息 (保持原樣)
     * 除了新增訊息，還要更新聊天室的「最後訊息」和「時間」
     */
    suspend fun sendMessage(matchId: String, messageText: String) {
        val userId = getCurrentUserId() ?: return

        // 建立新訊息物件
        val newMessage = Message(
            senderId = userId,
            text = messageText,
            timestamp = System.currentTimeMillis()
        )

        try {
            // A. 將訊息寫入 matches/{matchId}/messages/{messageId}
            db.collection("matches")
                .document(matchId)
                .collection("messages")
                .document(newMessage.id)
                .set(newMessage)
                .await()

            // B. 更新外部聊天室的摘要 (給列表頁顯示用)
            val updateData = mapOf(
                "lastMessage" to messageText,
                "timestamp" to newMessage.timestamp
            )
            db.collection("matches")
                .document(matchId)
                .update(updateData)
                .await()

            Log.d("ChatRepository", "訊息發送成功: $messageText")
        } catch (e: Exception) {
            Log.e("ChatRepository", "發送失敗", e)
        }
    }

    /**
     * 4. 即時監聽特定聊天室的訊息 (Flow) (保持原樣)
     */
    fun getMessagesFlow(matchId: String): Flow<List<Message>> = callbackFlow {
        // 監聽 matches/{matchId}/messages 集合
        val registration = db.collection("matches")
            .document(matchId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING) // 舊訊息在上面，新訊息在下面
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("ChatRepository", "Listen failed.", e)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val messages = snapshot.toObjects(Message::class.java)
                    trySend(messages)
                }
            }

        awaitClose { registration.remove() }
    }
}