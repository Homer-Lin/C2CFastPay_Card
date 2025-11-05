package com.example.c2cfastpay_card.UIScreen.Screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.c2cfastpay_card.UIScreen.components.ProductItem
import com.example.c2cfastpay_card.UIScreen.components.ProductRepository
// import com.example.c2cfastpay_card.UIScreen.components.WishRepository // <-- 1. 移除 WishRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CardStackViewModel(
    private val productRepository: ProductRepository
    // private val wishRepository: WishRepository // <-- 2. 移除
) : ViewModel() {

    private val _cards = MutableStateFlow<List<ProductItem>>(emptyList())
    val cards = _cards.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadPotentialMatches() {
        Log.d("CardStackDebug", "loadPotentialMatches() called. Current isLoading: ${_isLoading.value}")
        if (_isLoading.value) {
            Log.d("CardStackDebug", "Already loading, returning.")
            return
        }
        Log.d("CardStackDebug", "Setting isLoading to true")
        _isLoading.update { true }

        viewModelScope.launch {
            Log.d("CardStackDebug", "Coroutine launched for loading data.")
            try {
                // --- 3. 修改邏輯：只載入所有商品 ---
                val allProducts = productRepository.getProductList()
                Log.d("CardStackDebug", "Data loaded: ${allProducts.size} products")

                // (移除所有 wishKeywords 和配對演算法)

                _cards.value = allProducts.shuffled() // 直接顯示所有商品並打亂順序
                Log.d("CardStackDebug", "Matched products count: ${allProducts.size}")
                // --- 修改結束 ---

            } finally {
                Log.d("CardStackDebug", "Setting isLoading to false in finally block")
                _isLoading.update { false }
            }
        }
    }
}

/**
 * 4. 修改 ViewModel Factory
 */
class CardStackViewModelFactory(
    private val productRepo: ProductRepository
    // private val wishRepo: WishRepository // <-- 5. 移除
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardStackViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // 6. 移除 wishRepo 的傳遞
            return CardStackViewModel(productRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}