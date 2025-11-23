package com.example.c2cfastpay_card

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ProductFlowViewModel : ViewModel() {
    // 使用 mutableStateOf 讓外部可以修改 .value
    // 這裡儲存照片列表
    val photoUris = mutableStateOf<List<Uri>>(emptyList())

    // 這裡儲存是否有照片 (用來控制按鈕啟用)
    val hasCameraPhoto = mutableStateOf(false)

    // --- 以下是 AI 聊天可能需要的狀態 (預留) ---
    var userInput = mutableStateOf("")
    var productName = mutableStateOf("")
    var aiDescription = mutableStateOf("")
    var finalStory = mutableStateOf("")
    var currentImageUri = mutableStateOf<Uri?>(null)
    // ... 其他您需要的變數
}