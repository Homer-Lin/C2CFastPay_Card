package com.example.c2cfastpay_card.UIScreen.Screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.c2cfastpay_card.UIScreen.components.MatchItem
import com.example.c2cfastpay_card.UIScreen.components.MatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 1. 建立 HistoryViewModel
class HistoryViewModel(
    private val matchRepository: MatchRepository // 接收 MatchRepository
) : ViewModel() {

    // 2. 建立一個私有的、可變的 StateFlow
    private val _matches = MutableStateFlow<List<MatchItem>>(emptyList())

    // 3. 對外暴露一個公開的、不可變的 StateFlow，供 UI 觀察
    val matches: StateFlow<List<MatchItem>> = _matches.asStateFlow()

    // 4. 建立 init 區塊，在 ViewModel 建立時自動執行
    init {
        loadMatches() // 呼叫載入資料的函式
    }

    // 5. 建立一個私有函式來載入資料
    private fun loadMatches() {
        // 6. 啟動一個協程
        viewModelScope.launch {
            // 7. 呼叫 MatchRepository 中「正確的」 suspend 函式
            val matchList = matchRepository.getMatches()
            // 8. 更新 StateFlow 的值
            _matches.update { matchList }
        }
    }
}

// 9. ViewModel Factory 保持不變
class HistoryViewModelFactory(
    private val matchRepository: MatchRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(matchRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}