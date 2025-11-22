package com.example.c2cfastpay_card.UIScreen.Screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.c2cfastpay_card.data.CartItem
import com.example.c2cfastpay_card.UIScreen.components.CartRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CartViewModel(private val cartRepository: CartRepository) : ViewModel() {

    // 1. 即時監聽購物車列表 (從 Repository 的 Flow 轉為 StateFlow)
    val cartItems: StateFlow<List<CartItem>> = cartRepository.getCartItemsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 2. 計算總金額 (自動隨 cartItems 更新)
    //    假設 price 是字串，我們轉成 Int 來計算
    val totalPrice: StateFlow<Int> = cartItems.map { items ->
        items.sumOf { (it.productPrice.toIntOrNull() ?: 0) * it.quantity }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // 3. 刪除商品
    fun removeItem(itemId: String) {
        viewModelScope.launch {
            cartRepository.removeFromCart(itemId)
        }
    }

    // 4. 結帳 (目前先印 Log，未來接金流)
    fun checkout() {
        // TODO: 實作結帳邏輯 (例如建立訂單)
        android.util.Log.d("CartViewModel", "準備結帳: ${cartItems.value.size} 件商品")
    }
}

// Factory 用來注入 Repository
class CartViewModelFactory(private val repository: CartRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CartViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}