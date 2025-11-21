package com.example.c2cfastpay_card.UIScreen.Screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.c2cfastpay_card.UIScreen.components.MatchRepository
import com.example.c2cfastpay_card.UIScreen.components.ProductItem
import com.example.c2cfastpay_card.UIScreen.components.ProductRepository
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

    // 使用 StateFlow 來管理卡片列表 UI 狀態
    private val _cards = MutableStateFlow<List<ProductItem>>(emptyList())
    val cards: StateFlow<List<ProductItem>> = _cards.asStateFlow()

    // 載入狀態
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 初始化時載入資料
    init {
        loadPotentialMatches()
    }

    fun loadPotentialMatches() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 【修正點】使用新版 Repository 的函式
                // 原本是: productRepository.getProductList().first() ...
                // 現在改為: getProductsForMatching() (直接回傳 List，且已過濾掉自己的商品)
                val newCards = productRepository.getProductsForMatching()

                _cards.value = newCards
                Log.d("CardStackViewModel", "成功載入 ${newCards.size} 張卡片")

            } catch (e: Exception) {
                Log.e("CardStackViewModel", "載入卡片失敗", e)
                // 這裡可以處理錯誤，例如顯示空列表或錯誤訊息
                _cards.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 左滑 (不喜歡 / Pass)
    fun swipeLeft(product: ProductItem) {
        // 從列表中移除該卡片
        _cards.update { currentList ->
            currentList.filterNot { it.id == product.id }
        }
    }

    // 右滑 (喜歡 / Like)
    fun swipeRight(product: ProductItem) {
        viewModelScope.launch {
            try {
                // 1. 從列表中移除該卡片 (UI 先反應)
                _cards.update { currentList ->
                    currentList.filterNot { it.id == product.id }
                }

                // 2. 呼叫 Repository 執行雲端操作
                val isMatched = matchRepository.likeProduct(product)

                if (isMatched) {
                    // TODO: 這裡可以觸發一個 UI 事件，通知 View 顯示「配對成功」動畫
                    Log.d("CardStackViewModel", "HOST: 配對成功！")
                }

            } catch (e: Exception) {
                Log.e("CardStackViewModel", "儲存喜歡失敗", e)
                // 這裡可以考慮是否把卡片加回去，或者提示網路錯誤
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