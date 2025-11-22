package com.example.c2cfastpay_card.UIScreen.Screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.c2cfastpay_card.data.Message
import com.example.c2cfastpay_card.UIScreen.components.ChatRepository
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val matchId: String
) : ViewModel() {

    // 1. 取得當前用戶 ID (用來判斷訊息是「我發的」還是「對方發的」)
    val myUserId = Firebase.auth.currentUser?.uid ?: ""

    // 2. 即時監聽訊息列表
    //    當 Firestore 有新訊息時，這個 StateFlow 會自動更新
    val messages: StateFlow<List<Message>> = chatRepository.getMessagesFlow(matchId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 3. 發送訊息
    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            chatRepository.sendMessage(matchId, text)
        }
    }
}

// Factory: 用來傳遞參數 (matchId) 給 ViewModel
class ChatViewModelFactory(
    private val chatRepository: ChatRepository,
    private val matchId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(chatRepository, matchId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}