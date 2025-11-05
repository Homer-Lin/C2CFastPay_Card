package com.example.c2cfastpay_card.UIScreen.Screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.c2cfastpay_card.UIScreen.components.ProductItem
import com.example.c2cfastpay_card.UIScreen.components.ProductRepository
import com.example.c2cfastpay_card.UIScreen.components.MatchRepository
import com.example.c2cfastpay_card.UIScreen.components.toMatchItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CardStackViewModel(
    private val productRepository: ProductRepository,
    private val matchRepository: MatchRepository // <-- 確保建構子接收 MatchRepository
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
                val allProducts = productRepository.getProductList() //
                Log.d("CardStackDebug", "Data loaded: ${allProducts.size} products")
                _cards.value = allProducts.shuffled()
                Log.d("CardStackDebug", "Matched products count: ${allProducts.size}")
            } finally {
                Log.d("CardStackDebug", "Setting isLoading to false in finally block")
                _isLoading.update { false }
            }
        }
    }

    fun swipeLeft(product: ProductItem) {
        Log.d("CardStackDebug", "Swiped Left on: ${product.title}")
        _cards.update { currentList ->
            // 透過 ID 過濾，而不是依賴物件相等性
            currentList.filterNot { it.id == product.id }
        }
    }

    fun swipeRight(product: ProductItem) {
        Log.d("CardStackDebug", "Swiped Right (Liked): ${product.title}")
        viewModelScope.launch {
            matchRepository.addMatch(product.toMatchItem())
            _cards.update { currentList ->
                // 透過 ID 過濾，而不是依賴物件相等性
                currentList.filterNot { it.id == product.id }


            }
        }
    }
}

class CardStackViewModelFactory(
    private val productRepo: ProductRepository,
    private val matchRepo: MatchRepository // <-- 確保接收 MatchRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardStackViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CardStackViewModel(productRepo, matchRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}