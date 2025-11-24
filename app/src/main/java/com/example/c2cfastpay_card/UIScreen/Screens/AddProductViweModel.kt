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
    var uploadStatus by mutableStateOf<String?>(null) // 用來傳遞成功或錯誤訊息給 UI

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
                // 1. 整理物流字串 (例如 "7-11, 全家")
                val logisticsStr = logistics.joinToString("、")

                // 2. 處理圖片 (目前 ProductItem 只支援單張圖，我們先取第一張)
                val firstImageUri = photoUris.firstOrNull()?.toString() ?: ""

                // 3. 建立 ProductItem 物件
                val newProduct = ProductItem(
                    title = title,
                    description = description,
                    story = story,
                    price = price,
                    stock = stock,
                    condition = condition,
                    payment = logisticsStr, // 暫時用 payment 欄位存物流方式
                    imageUri = firstImageUri
                    // ownerId, timestamp 等資訊會在 Repository 層補上
                )

                // 4. 呼叫 Repository 進行上架 (含圖片上傳)
                repository.addProduct(newProduct)

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

// Factory 用來注入 Repository
class AddProductViewModelFactory(private val repository: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddProductViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}