package com.example.c2cfastpay_card.model // 請確認 package 路徑

import java.util.UUID

data class WishItem(
    val uuid: String = UUID.randomUUID().toString(), // 文件 ID

    // 基本資訊
    val title: String = "",
    val price: String = "",
    val payment: String = "", // 物流方式
    val imageUri: String = "",

    // 詳細資訊 (對應詳情頁 UI)
    val description: String = "",
    val qty: String = "1",          // ★ 新增：欲購數量
    val condition: String = "皆可", // ★ 新增：新舊狀態
    val memo: String = "",          // ★ 新增：備註 (取代 notes)

    // 擁有者資訊 (C2C 核心)
    val ownerId: String = "",       // 許願者的 Firebase UID
    val ownerName: String = "",     // ★ 關鍵：許願者暱稱
    val ownerEmail: String = "",

    val timestamp: Long = System.currentTimeMillis()
)