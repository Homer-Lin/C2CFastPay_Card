package com.example.c2cfastpay_card.UIScreen.Screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.c2cfastpay_card.UIScreen.components.ChatRepository
import com.example.c2cfastpay_card.UIScreen.components.MatchDetails
import com.example.c2cfastpay_card.data.Message
// --- 【修正重點】移除 .ktx，改用標準 FirebaseAuth ---
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val matchId: String
) : ViewModel() {

    // --- 【修正重點】改用 getInstance() 寫法，保證不會報錯 ---
    val myUserId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _matchDetails = MutableStateFlow<MatchDetails?>(null)
    val matchDetails: StateFlow<MatchDetails?> = _matchDetails.asStateFlow()

    init {
        loadMessages()
        loadMatchDetails()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            chatRepository.getMessagesFlow(matchId).collect { msgList ->
                _messages.value = msgList
            }
        }
    }

    private fun loadMatchDetails() {
        viewModelScope.launch {
            val details = chatRepository.getMatchDetails(matchId)
            _matchDetails.value = details
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            chatRepository.sendMessage(matchId, text)
        }
    }
}

class ChatViewModelFactory(
    private val repository: ChatRepository,
    private val matchId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(repository, matchId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}