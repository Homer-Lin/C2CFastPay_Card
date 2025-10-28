package com.example.c2cfastpay_card.UIScreen.components
import java.util.UUID // <-- 1. 加入 import

data class WishItem(
//    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val specs: String,
    val price: String,
    val payment: String,
    val notes: String,
    val other: String,
    val imageUri: String
)
