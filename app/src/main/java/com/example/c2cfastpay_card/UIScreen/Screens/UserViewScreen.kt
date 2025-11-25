package com.example.c2cfastpay_card.UIScreen.Screens

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c2cfastpay_card.data.User
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableSharedFlow // 新增
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow // 新增
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow // 新增
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val storage = Firebase.storage

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 用來發送單次提示訊息 (Toast)
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    init {
        fetchUserData()
    }

    private fun fetchUserData() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val snapshot = db.collection("users").document(uid).get().await()
                _user.value = snapshot.toObject(User::class.java)
            } catch (e: Exception) {
                Log.e("UserViewModel", "讀取失敗", e)
            }
        }
    }
    fun addPoints(amount: Int) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch

                // 使用原子操作增加數值
                db.collection("users").document(userId)
                    .update("points", FieldValue.increment(amount.toLong()))
                    .await()
                fetchUserData()
                _toastMessage.emit("儲值成功！增加 $amount 點")

            } catch (e: Exception) {
                Log.e("UserViewModel", "儲值失敗", e)
                _toastMessage.emit("儲值失敗: ${e.message}")
            }
        }
    }

    fun uploadAvatar(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val ref = storage.reference.child("users/$uid/avatar.jpg")
                ref.putFile(uri).await()
                val downloadUrl = ref.downloadUrl.await().toString()

                db.collection("users").document(uid)
                    .update("avatarUrl", downloadUrl)
                    .await()

                fetchUserData()
                _toastMessage.emit("大頭貼更新成功")
            } catch (e: Exception) {
                Log.e("UserViewModel", "上傳失敗", e)
                _toastMessage.emit("上傳失敗，請稍後再試")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 【修改】更新暱稱 (加入唯一性檢查)
    fun updateName(newName: String) {
        val uid = auth.currentUser?.uid ?: return
        val trimmedName = newName.trim()

        if (trimmedName.isBlank()) {
            viewModelScope.launch { _toastMessage.emit("暱稱不能為空") }
            return
        }

        if (trimmedName == _user.value?.name) return // 沒改動

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. 檢查是否有其他人使用此名稱
                val query = db.collection("users")
                    .whereEqualTo("name", trimmedName)
                    .get()
                    .await()

                if (!query.isEmpty) {
                    _toastMessage.emit("此帳號名稱已被使用，請換一個")
                } else {
                    // 2. 無人使用，執行更新
                    db.collection("users").document(uid)
                        .update("name", trimmedName)
                        .await()
                    fetchUserData()
                    _toastMessage.emit("暱稱修改成功")
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "更新暱稱失敗", e)
                _toastMessage.emit("更新失敗: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendResetPasswordEmail() {
        val email = auth.currentUser?.email
        if (email != null) {
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    viewModelScope.launch { _toastMessage.emit("重設信已發送至信箱") }
                }
                .addOnFailureListener {
                    viewModelScope.launch { _toastMessage.emit("發送失敗: ${it.message}") }
                }
        }
    }


    fun logout(onSuccess: () -> Unit) {
        auth.signOut()
        onSuccess()
    }
}