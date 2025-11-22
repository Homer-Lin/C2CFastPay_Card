package com.example.c2cfastpay_card.UIScreen.components

import android.content.Context
import android.util.Log
import com.example.c2cfastpay_card.data.CartItem
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CartRepository(private val context: Context) {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * 1. 加入購物車
     */
    suspend fun addToCart(cartItem: CartItem) {
        val userId = getCurrentUserId() ?: throw Exception("尚未登入")

        // 寫入路徑： users/{userId}/cart/{cartItemId}
        db.collection("users")
            .document(userId)
            .collection("cart")
            .document(cartItem.id)
            .set(cartItem)
            .await()

        Log.d("CartRepository", "加入購物車成功: ${cartItem.productTitle}")
    }

    /**
     * 2. 取得購物車列表 (即時監聽 Flow)
     * 這樣當您在詳情頁加入商品，購物車頁面會立刻更新
     */
    fun getCartItemsFlow(): Flow<List<CartItem>> = callbackFlow {
        val userId = getCurrentUserId()
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = db.collection("users")
            .document(userId)
            .collection("cart")
            .orderBy("addedAt") // 依照加入時間排序
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("CartRepository", "Listen failed.", e)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val items = snapshot.toObjects(CartItem::class.java)
                    trySend(items)
                }
            }

        awaitClose { registration.remove() }
    }

    /**
     * 3. 從購物車移除商品
     */
    suspend fun removeFromCart(cartItemId: String) {
        val userId = getCurrentUserId() ?: return

        db.collection("users")
            .document(userId)
            .collection("cart")
            .document(cartItemId)
            .delete()
            .await()

        Log.d("CartRepository", "移除購物車商品: $cartItemId")
    }
}