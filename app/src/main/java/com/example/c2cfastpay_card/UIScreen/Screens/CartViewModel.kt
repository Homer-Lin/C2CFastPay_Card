package com.example.c2cfastpay_card.UIScreen.Screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.c2cfastpay_card.data.CartItem
import com.example.c2cfastpay_card.UIScreen.components.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CartViewModel(private val cartRepository: CartRepository) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    init {
        viewModelScope.launch {
            cartRepository.getCartItemsFlow().collect { items ->
                _cartItems.value = items
            }
        }
    }

    // 計算總金額 (只算勾選的)
    val totalPrice: StateFlow<Int> = _cartItems.combine(_cartItems) { items, _ ->
        items.filter { it.isChecked }
            .sumOf {
                // 防止價格字串轉失敗
                val price = it.productPrice.replace(",", "").toIntOrNull() ?: 0
                price * it.quantity
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // 切換勾選
    fun toggleItemChecked(item: CartItem) {
        val newItem = item.copy(isChecked = !item.isChecked)
        updateRepo(newItem)
    }

    // 增加數量
    fun increaseQuantity(item: CartItem) {
        // Log 幫助檢查：如果按了沒反應，看看 Logcat 有沒有這行，還是被庫存擋住了
        if (item.quantity < item.stock) {
            val newItem = item.copy(quantity = item.quantity + 1)
            updateRepo(newItem)
        } else {
            Log.w("CartViewModel", "庫存不足，無法增加 (庫存: ${item.stock}, 目前: ${item.quantity})")
        }
    }

    // 減少數量
    fun decreaseQuantity(item: CartItem) {
        if (item.quantity > 1) {
            val newItem = item.copy(quantity = item.quantity - 1)
            updateRepo(newItem)
        }
    }

    fun removeItem(itemId: String) {
        viewModelScope.launch {
            cartRepository.removeFromCart(itemId)
        }
    }

    fun checkout() {
        val itemsToBuy = _cartItems.value.filter { it.isChecked }
        Log.d("CartViewModel", "結帳商品數: ${itemsToBuy.size}")
    }

    private fun updateRepo(item: CartItem) {
        viewModelScope.launch {
            cartRepository.updateCartItem(item)
        }
    }
}

class CartViewModelFactory(private val repository: CartRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CartViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}