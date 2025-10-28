package com.example.c2cfastpay_card.UIScreen.components

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.c2cfastpay_card.UIScreen.components.ProductItem
import com.example.c2cfastpay_card.data.dataStore
import kotlinx.coroutines.flow.first

class ProductRepository(private val context: Context) {
    private val gson = com.google.gson.Gson()
    private val KEY_PRODUCT_LIST = stringPreferencesKey("product_list")

    suspend fun addProduct(product: ProductItem) {
        context.dataStore.edit { prefs ->
            val currentJson = prefs[KEY_PRODUCT_LIST] ?: "[]"
            val type = object : TypeToken<MutableList<ProductItem>>() {}.type
            val currentList: MutableList<ProductItem> = gson.fromJson(currentJson, type)
            currentList.add(product)
            prefs[KEY_PRODUCT_LIST] = gson.toJson(currentList)
        }
    }

    suspend fun getProductList(): List<ProductItem> {
        val prefs = context.dataStore.data.first()
        val json = prefs[KEY_PRODUCT_LIST] ?: "[]"
        val type = object : TypeToken<List<ProductItem>>() {}.type
        return gson.fromJson(json, type)
    }
    suspend fun clearAllProducts() {
        context.dataStore.edit { prefs ->
            prefs[KEY_PRODUCT_LIST] = "[]"
        }
    }

}
