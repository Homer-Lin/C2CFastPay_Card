package com.example.c2cfastpay_card.UIScreen.Screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.c2cfastpay_card.UIScreen.components.MatchRepository
import com.example.c2cfastpay_card.UIScreen.components.ProductItem
import com.example.c2cfastpay_card.UIScreen.components.ProductRepository
import com.example.c2cfastpay_card.UIScreen.components.SwipeDirection
import com.example.c2cfastpay_card.UIScreen.components.toMatchItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CardStackViewModel(
    private val productRepository: ProductRepository,
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val _cards = MutableStateFlow<List<ProductItem>>(emptyList())
    val cards: StateFlow<List<ProductItem>> = _cards.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadPotentialMatches()
    }

    fun loadPotentialMatches() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. 先取得已滑過的 ID 列表
                val swipedIds = matchRepository.getSwipedProductIds()

                // 2. 傳入 Repository 進行過濾
                val newCards = productRepository.getProductsForMatching(swipedIds)

                _cards.value = newCards
                Log.d("CardStackViewModel", "成功載入 ${newCards.size} 張未滑過的卡片")

            } catch (e: Exception) {
                Log.e("CardStackViewModel", "載入卡片失敗", e)
                _cards.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 左滑 (Pass)
    fun swipeLeft(product: ProductItem) {
        _cards.update { list -> list.filterNot { it.id == product.id } }

        viewModelScope.launch {
            // 記錄到資料庫
            matchRepository.recordSwipe(product.id, SwipeDirection.LEFT)
        }
    }

    // 右滑 (Like)
    fun swipeRight(product: ProductItem) {
        _cards.update { list -> list.filterNot { it.id == product.id } }

        viewModelScope.launch {
            try {
                // 記錄並檢查配對 (likeProduct 內部已包含 recordSwipe)
                val isMatched = matchRepository.likeProduct(product)
                if (isMatched) {
                    Log.d("CardStackViewModel", "HOST: 配對成功！")
                }
            } catch (e: Exception) {
                Log.e("CardStackViewModel", "儲存喜歡失敗", e)
            }
        }
    }
}

// ViewModel Factory 保持不變 (用來注入 Repository)
class CardStackViewModelFactory(
    private val productRepository: ProductRepository,
    private val matchRepository: MatchRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardStackViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CardStackViewModel(productRepository, matchRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}