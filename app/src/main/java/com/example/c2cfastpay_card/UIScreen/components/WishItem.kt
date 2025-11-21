package com.example.c2cfastpay_card.UIScreen.components

import java.util.UUID

data class WishItem(
    val title: String = "",
    val price: String = "",
    val payment: String = "",
    val imageUri: String = "", // 這裡之後會存 "https://..." 的雲端網址
    val uuid: String = UUID.randomUUID().toString(), // 文件 ID

    // 您的詳細資料欄位
    val description: String = "",
    val specs: String = "",
    val notes: String = "",
    val other: String = "",

    // 【新增】擁有者資訊 (C2C 核心)
    val ownerId: String = "",       // 許願者的 Firebase UID
    val ownerName: String = "",     // 許願者的暱稱
    val ownerEmail: String = "",    // 許願者的信箱

    val timestamp: Long = System.currentTimeMillis() // 許願時間
)