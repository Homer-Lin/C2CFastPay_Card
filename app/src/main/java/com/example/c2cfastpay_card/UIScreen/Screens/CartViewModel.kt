package com.example.c2cfastpay_card.UIScreen.Screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.c2cfastpay_card.data.CartItem
import com.example.c2cfastpay_card.UIScreen.components.CartRepository
import kotlinx.coroutines.flow.MutableSharedFlow // 新增
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow // 新增
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow // 新增
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CartViewModel(private val cartRepository: CartRepository) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    // 新增 Loading 狀態
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 新增 Toast 訊息 (用來顯示結帳成功或失敗)
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            cartRepository.getCartItemsFlow().collect { items ->
                _cartItems.value = items
            }
        }
    }

    val totalPrice: StateFlow<Int> = _cartItems.combine(_cartItems) { items, _ ->
        items.filter { it.isChecked }
            .sumOf {
                val price = it.productPrice.replace(",", "").toIntOrNull() ?: 0
                price * it.quantity
            }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    fun toggleItemChecked(item: CartItem) {
        val newItem = item.copy(isChecked = !item.isChecked)
        updateRepo(newItem)
    }

    fun increaseQuantity(item: CartItem) {
        if (item.quantity < item.stock) {
            val newItem = item.copy(quantity = item.quantity + 1)
            updateRepo(newItem)
        }
    }

    fun decreaseQuantity(item: CartItem) {
        if (item.quantity > 1) {
            val newItem = item.copy(quantity = item.quantity - 1)
            updateRepo(newItem)
        }
    }

    fun removeItem(itemId: String) {
        viewModelScope.launch { cartRepository.removeFromCart(itemId) }
    }

    // ★★★ 修改：執行結帳邏輯 ★★★
    fun checkout() {
        val itemsToBuy = _cartItems.value.filter { it.isChecked }
        if (itemsToBuy.isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true

            // 呼叫 Repository 的 Transaction
            val result = cartRepository.checkout(itemsToBuy)

            _isLoading.value = false

            if (result.isSuccess) {
                _toastMessage.emit("結帳成功！感謝您的購買")
            } else {
                _toastMessage.emit("結帳失敗: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    private fun updateRepo(item: CartItem) {
        viewModelScope.launch { cartRepository.updateCartItem(item) }
    }
}

// Factory 保持不變
class CartViewModelFactory(private val repository: CartRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CartViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}