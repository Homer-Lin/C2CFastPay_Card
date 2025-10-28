package com.example.c2cfastpay_card.UIScreen.Screens // 確保 package 名稱正確

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.c2cfastpay_card.UIScreen.components.ProductItem
import com.example.c2cfastpay_card.UIScreen.components.ProductRepository
import com.example.c2cfastpay_card.UIScreen.components.WishRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update // <-- 加入 import for update
import android.util.Log // <-- 加入 Log import

class CardStackViewModel(
    private val productRepository: ProductRepository,
    private val wishRepository: WishRepository
) : ViewModel() {

    // 使用 StateFlow 來保存待顯示的卡片列表
    private val _cards = MutableStateFlow<List<ProductItem>>(emptyList())
    val cards = _cards.asStateFlow()

    // --- 新增：載入狀態 ---
    private val _isLoading = MutableStateFlow(false) // 預設為 false
    val isLoading = _isLoading.asStateFlow()

//    init {
//        // ViewModel 啟動時，自動載入並計算配對
//        loadPotentialMatches()
//    }

    fun loadPotentialMatches() {
        // --- 加入 Log ---
        Log.d("CardStackDebug", "loadPotentialMatches() called. Current isLoading: ${_isLoading.value}")
        // --- Log 結束 ---

        if (_isLoading.value) {
            Log.d("CardStackDebug", "Already loading, returning.") // <-- Log: 檢查是否因重複呼叫而返回
            return
        }

        // --- 加入 Log ---
        Log.d("CardStackDebug", "Setting isLoading to true")
        _isLoading.update { true }
        // --- Log 結束 ---
        viewModelScope.launch {
            Log.d("CardStackDebug", "Coroutine launched for loading data.") // <-- Log: 確認 Coroutine 啟動
            try { // 加入 try-finally 確保 loading 狀態會被重設
                // 1. 取得所有願望和所有商品
                val allWishes = wishRepository.getWishList() // 使用 getWishList()
                val allProducts = productRepository.getProductList() // 使用 getProductList()
                Log.d("CardStackDebug", "Data loaded: ${allWishes.size} wishes, ${allProducts.size} products") // <-- Log: 確認資料讀取

                // 2. 建立願望標題的關鍵字集合 (轉小寫以便比對)
                val wishKeywords = allWishes
                    .map { it.title.lowercase().trim() } // 取得標題、轉小寫、去除前後空白
                    .filter { it.isNotEmpty() } // 過濾掉空標題
                    .toSet() // 轉成 Set 避免重複

                // 3. 執行簡易配對演算法
                val matchedProducts = mutableListOf<ProductItem>()
                if (wishKeywords.isNotEmpty()) {
                    for (product in allProducts) {
                        val productTitle = product.title.lowercase().trim()

                        // 檢查商品標題是否包含 *任何一個* 願望關鍵字
                        for (keyword in wishKeywords) {
                            if (productTitle.contains(keyword)) {
                                matchedProducts.add(product)
                                break // 找到一個符合就跳出內層迴圈，避免重複加入
                            }
                        }
                    }
                } else {
                    // 如果沒有任何願望，可以選擇顯示所有商品或隨機商品
                    // 暫時顯示所有商品，並打亂順序
                    matchedProducts.addAll(allProducts)
                }
                // 4. 更新 StateFlow，並打亂順序
                _cards.value = matchedProducts.shuffled()
                Log.d("CardStackDebug", "Matched products count: ${matchedProducts.size}") // <-- Log: 確認配對結果
            } finally {
                // --- 修改：更新載入狀態 ---
                Log.d("CardStackDebug", "Setting isLoading to false in finally block")
                _isLoading.update { false } // 載入完成 (無論成功或失敗)，設為 false
                // --- 修改結束 ---
            }
        }
    }
}

/**
 * 建立一個 ViewModel Factory，因為 CardStackViewModel 需要傳入 Repositories
 */
class CardStackViewModelFactory(
    private val productRepo: ProductRepository,
    private val wishRepo: WishRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardStackViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CardStackViewModel(productRepo, wishRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}