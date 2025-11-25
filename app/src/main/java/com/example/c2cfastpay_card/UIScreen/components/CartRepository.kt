package com.example.c2cfastpay_card.UIScreen.components

import android.content.Context
import android.util.Log
import com.example.c2cfastpay_card.data.CartItem
// ★★★ 修正 Import：改用標準類別，不要用 ktx ★★★
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CartRepository(private val context: Context) {

    // ★★★ 修正初始化：改用 getInstance() ★★★
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * 1. 加入購物車
     */
    suspend fun addToCart(cartItem: CartItem): Boolean {
        val userId = getCurrentUserId()
        if (userId == null) {
            Log.e("CartRepository", "尚未登入")
            return false
        }

        val userCartRef = db.collection("users").document(userId).collection("cart")

        try {
            // 1. 先搜尋購物車裡面，有沒有這個 productId 的商品
            val querySnapshot = userCartRef
                .whereEqualTo("productId", cartItem.productId)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                // --- A. 沒買過 -> 新增一筆 ---
                val newDoc = userCartRef.document() // 產生新 ID
                val newItem = cartItem.copy(id = newDoc.id, quantity = 1) // 確保數量是 1
                newDoc.set(newItem).await()
                Log.d("CartRepository", "新商品加入購物車: ${cartItem.productTitle}")
                return true
            } else {
                // --- B. 買過了 -> 只要數量 +1 ---
                val existingDoc = querySnapshot.documents.first()
                val existingItem = existingDoc.toObject(CartItem::class.java)

                if (existingItem != null) {
                    val currentQty = existingItem.quantity
                    // 檢查庫存 (以傳進來的 cartItem.stock 為準，因為那是從詳情頁抓的最新資料)
                    if (currentQty + 1 <= cartItem.stock) {
                        existingDoc.reference.update("quantity", currentQty + 1).await()
                        Log.d("CartRepository", "商品數量累加: ${existingItem.productTitle}")
                        return true
                    } else {
                        Log.w("CartRepository", "庫存不足，無法累加")
                        return false // 回傳失敗
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CartRepository", "加入購物車失敗", e)
            return false
        }
        return false
    }

    /**
     * 2. 取得購物車列表 (即時監聽 Flow)
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
            // .orderBy("addedAt") // 暫時註解，避免 crash
            .addSnapshotListener { snapshot: QuerySnapshot?, e: FirebaseFirestoreException? ->
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

    /**
     * 4. 更新購物車 (更新數量或勾選狀態)
     */
    suspend fun updateCartItem(item: CartItem) {
        val userId = getCurrentUserId() ?: return
        try {
            if (item.id.isNotEmpty()) {
                db.collection("users")
                    .document(userId)
                    .collection("cart")
                    .document(item.id)
                    .set(item) // 直接覆蓋更新
                    .await()
                Log.d("CartRepository", "更新購物車成功: ${item.productTitle}")
            }
        } catch (e: Exception) {
            Log.e("CartRepository", "更新失敗", e)
        }
    }

    suspend fun checkout(itemsToBuy: List<CartItem>): Result<String> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("未登入"))

        if (itemsToBuy.isEmpty()) return Result.failure(Exception("購物車是空的"))

        return try {
            db.runTransaction { transaction ->
                // 1. 檢查買家餘額
                val userRef = db.collection("users").document(userId)
                val userSnapshot = transaction.get(userRef)
                val currentPoints = userSnapshot.getLong("points") ?: 0L

                val totalAmount = itemsToBuy.sumOf {
                    (it.productPrice.replace(",", "").toLongOrNull() ?: 0L) * it.quantity
                }

                if (currentPoints < totalAmount) {
                    throw FirebaseFirestoreException(
                        "餘額不足！(您有 $currentPoints，需支付 $totalAmount)",
                        FirebaseFirestoreException.Code.ABORTED
                    )
                }

                // 2. 處理每個商品 (扣庫存 + 加賣家錢)
                itemsToBuy.forEach { item ->
                    val price = (item.productPrice.replace(",", "").toLongOrNull() ?: 0L)
                    val cost = price * item.quantity

                    // A. 扣庫存
                    val productRef = db.collection("products").document(item.productId)
                    val productSnapshot = transaction.get(productRef)
                    val currentStockStr = productSnapshot.getString("stock") ?: "0"
                    val currentStock = currentStockStr.toIntOrNull() ?: 0

                    if (currentStock < item.quantity) {
                        throw FirebaseFirestoreException(
                            "商品【${item.productTitle}】庫存不足",
                            FirebaseFirestoreException.Code.ABORTED
                        )
                    }
                    transaction.update(productRef, "stock", (currentStock - item.quantity).toString())

                    // B. ★★★ 加賣家錢 (新增邏輯) ★★★
                    if (item.sellerId.isNotBlank()) {
                        val sellerRef = db.collection("users").document(item.sellerId)
                        // 注意：Transaction 內讀取必須在寫入之前，但因為我們可能對同一個賣家加錢多次
                        // 這裡使用 FieldValue.increment 直接寫入，不需要先讀取，這樣更安全且避免讀寫順序問題
                        transaction.update(sellerRef, "points", FieldValue.increment(cost))
                    }
                }

                // 3. 扣買家錢
                transaction.update(userRef, "points", currentPoints - totalAmount)

                // 4. 建立訂單紀錄
                val orderRef = db.collection("orders").document()
                val orderData = hashMapOf(
                    "id" to orderRef.id,
                    "buyerId" to userId,
                    "totalAmount" to totalAmount,
                    "timestamp" to com.google.firebase.Timestamp.now(),
                    "status" to "COMPLETED",
                    "items" to itemsToBuy
                )
                transaction.set(orderRef, orderData)

                // 5. 移除購物車
                itemsToBuy.forEach { item ->
                    val cartItemRef = db.collection("users").document(userId).collection("cart").document(item.id)
                    transaction.delete(cartItemRef)
                }

            }.await()

            Result.success("結帳成功！")
        } catch (e: Exception) {
            Log.e("CartRepository", "結帳失敗", e)
            Result.failure(e)
        }
    }
}