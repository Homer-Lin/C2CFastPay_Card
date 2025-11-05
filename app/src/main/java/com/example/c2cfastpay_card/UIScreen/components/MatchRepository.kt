package com.example.c2cfastpay_card.UIScreen.components

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.c2cfastpay_card.data.dataStore // 確保 import dataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class MatchRepository(private val context: Context) {

    // 1. 定義一個新的 DataStore Key
    private val MATCHES_KEY = stringPreferencesKey("matches_list")

    // 2. 獲取所有配對 (供歷史紀錄頁面使用)
    suspend fun getMatches(): List<MatchItem> {
        val json = context.dataStore.data
            .map { preferences ->
                preferences[MATCHES_KEY] ?: "[]"
            }.first()
        return try {
            Gson().fromJson(json, object : TypeToken<List<MatchItem>>() {}.type)
        } catch (e: Exception) {
            Log.e("MatchRepository", "Error parsing matches JSON", e)
            emptyList()
        }
    }

    // 3. 新增一個配對
    suspend fun addMatch(matchItem: MatchItem) {
        try {
            val currentMatches = getMatches().toMutableList()
            currentMatches.add(0, matchItem) // 加到列表頂部
            val json = Gson().toJson(currentMatches)
            context.dataStore.edit { preferences ->
                preferences[MATCHES_KEY] = json
            }
            Log.d("MatchRepository", "Match added: ${matchItem.productTitle}")
        } catch (e: Exception) {
            Log.e("MatchRepository", "Error adding match", e)
        }
    }
}