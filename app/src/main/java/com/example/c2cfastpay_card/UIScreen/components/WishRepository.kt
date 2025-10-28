package com.example.c2cfastpay_card.UIScreen.components

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.core.net.toUri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.c2cfastpay_card.data.dataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class WishRepository(private val context: Context) {

    private val gson = Gson()
    private val KEY_WISH_LIST = stringPreferencesKey("wish_list")
    private val cartWishList = mutableStateListOf<com.example.c2cfastpay_card.UIScreen.components.WishItem>()

    // 複製圖片到本地，並回傳其 Uri 字串
    private fun saveImageToLocal(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return ""
        val fileName = "wish_image_${UUID.randomUUID()}.jpg"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { output ->
            inputStream.copyTo(output)
        }
        return file.toUri().toString()
    }

    suspend fun saveWishData(
        title: String,
        description: String,
        specs: String,
        price: String,
        payment: String,
        notes: String,
        other: String,
        imageUri: Uri?
    ) {
        val imagePath = imageUri?.let { saveImageToLocal(it) } ?: ""
        val newWish = WishItem(
            title,
            description,
            specs,
            price,
            payment,
            notes,
            other,
            imagePath
        )

        context.dataStore.edit { prefs ->
            val currentJson = prefs[KEY_WISH_LIST] ?: "[]"
            val listType = object : TypeToken<MutableList<WishItem>>() {}.type
            val currentList: MutableList<WishItem> = gson.fromJson(currentJson, listType)
            currentList.add(newWish)
            prefs[KEY_WISH_LIST] = gson.toJson(currentList)
        }
    }

    suspend fun getWishList(): List<WishItem> {
        val prefs = context.dataStore.data.first()
        val json = prefs[KEY_WISH_LIST] ?: "[]"
        val type = object : TypeToken<List<WishItem>>() {}.type
        return gson.fromJson(json, type)
    }

    suspend fun clearWishList() {
        context.dataStore.edit { prefs ->
            prefs[KEY_WISH_LIST] = "[]"
        }
    }

    // 購物車相關方法
    fun addToCart(item: WishItem) {
        if (!cartWishList.contains(item)) {
            cartWishList.add(item)
        }
    }

    fun removeFromCart(item: com.example.c2cfastpay_card.UIScreen.components.WishItem) {
        cartWishList.remove(item)
    }

    fun getCart(): List<com.example.c2cfastpay_card.UIScreen.components.WishItem> {
        return cartWishList
    }
}