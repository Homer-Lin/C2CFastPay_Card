package com.example.c2cfastpay_card.UIScreen.Screens

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.c2cfastpay_card.UIScreen.components.ProductItem
import com.example.c2cfastpay_card.UIScreen.components.ProductRepository
import kotlinx.coroutines.launch

class AddProductViewModel(private val repository: ProductRepository) : ViewModel() {

    var isLoading by mutableStateOf(false)
    var uploadStatus by mutableStateOf<String?>(null)

    fun submitProduct(
        title: String,
        description: String,
        story: String,
        price: String,
        stock: String,
        condition: String,
        logistics: Set<String>,
        photoUris: List<Uri>,
        onSuccess: () -> Unit
    ) {
        if (isLoading) return
        isLoading = true
        uploadStatus = null

        viewModelScope.launch {
            try {
                // 1. 整理物流字串
                val logisticsStr = logistics.joinToString("、")

                // 2. 處理圖片 (取第一張當作封面圖，主要是為了相容舊的 imageUri 欄位)
                val firstImageUri = photoUris.firstOrNull()?.toString() ?: ""

                // 3. 建立 ProductItem 物件
                val newProduct = ProductItem(
                    title = title,
                    description = description,
                    story = story,
                    price = price,
                    stock = stock,
                    condition = condition,
                    payment = logisticsStr,
                    imageUri = firstImageUri, // 舊欄位：存第一張
                    // ★★★ 修正重點：不需要在這裡設定 images 欄位，因為 Repository 會處理上傳後的網址 ★★★
                    // 這裡的 ProductItem 只是暫存資料載體
                )

                // 4. 呼叫 Repository 進行上架 (★★★ 關鍵：把 photoUris 傳進去 ★★★)
                // Repository 內部會負責把這份 List<Uri> 上傳到 Firebase Storage，
                // 拿到下載網址後，再填回 ProductItem 的 images 欄位寫入資料庫。
                repository.addProduct(newProduct, photoUris)

                // 5. 成功
                isLoading = false
                uploadStatus = "上架成功！"
                onSuccess()

            } catch (e: Exception) {
                isLoading = false
                uploadStatus = "上架失敗: ${e.message}"
            }
        }
    }

    fun clearStatus() {
        uploadStatus = null
    }
}

class AddProductViewModelFactory(private val repository: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddProductViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}