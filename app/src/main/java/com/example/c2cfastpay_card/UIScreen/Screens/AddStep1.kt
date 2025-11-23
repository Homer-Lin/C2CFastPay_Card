package com.example.c2cfastpay_card.UIScreen.Screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.c2cfastpay_card.ProductFlowViewModel
import com.example.c2cfastpay_card.R
import com.example.c2cfastpay_card.model.DraftProduct
import com.google.gson.Gson
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AddProductStepOne(
    navController: NavController,
    flowViewModel: ProductFlowViewModel
) {
    val context = LocalContext.current

    // 用來暫存「即將拍攝」的那張照片的 URI
    val tempPhotoUri = remember { mutableStateOf<Uri?>(null) }

    // 從 ViewModel 取得目前已選的照片列表
    val currentUris = flowViewModel.photoUris.value

    // --- 1. 定義拍照與選圖的 Launcher ---

    // (A) 拍照回調
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempPhotoUri.value?.let { uri ->
                val oldUris = flowViewModel.photoUris.value
                flowViewModel.photoUris.value = oldUris + uri
                flowViewModel.hasCameraPhoto.value = true
            }
        }
    }

    // (B) 建立暫存檔案並取得 URI 的函式
    fun createUriForCamera(): Uri? {
        return try {
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val imageFile = File.createTempFile("product_${System.currentTimeMillis()}", ".jpg", storageDir)
            // 注意：這裡的 authority 必須跟 AndroidManifest.xml 裡的 provider 設定一致
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // (C) 權限請求 Launcher (解決點擊沒反應的關鍵)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 權限拿到後，直接執行拍照
            val uri = createUriForCamera()
            if (uri != null) {
                tempPhotoUri.value = uri
                cameraLauncher.launch(uri)
            }
        } else {
            Toast.makeText(context, "需要相機權限才能拍照上架喔", Toast.LENGTH_SHORT).show()
        }
    }

    // (D) 相簿選圖 Launcher
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) {
            val oldUris = flowViewModel.photoUris.value
            flowViewModel.photoUris.value = oldUris + uris
            flowViewModel.hasCameraPhoto.value = true
        }
    }

    // --- 2. 觸發拍照的函式 ---
    fun onCameraClick() {
        val permission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            // 已有權限 -> 開拍
            val uri = createUriForCamera()
            if (uri != null) {
                tempPhotoUri.value = uri
                cameraLauncher.launch(uri)
            }
        } else {
            // 無權限 -> 請求權限
            permissionLauncher.launch(permission)
        }
    }

    // --- 3. UI 佈局 ---
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)
    ) {
        // 底部 Banner
        Image(
            painter = painterResource(id = R.drawable.banner_in_choose_photo),
            contentDescription = "Banner",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .wrapContentHeight()
                .wrapContentWidth(),
            contentScale = ContentScale.Fit
        )

        // 主內容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 工具列
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 34.dp, bottom = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(50.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img), // 返回箭頭圖示
                        contentDescription = "返回",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "選擇照片",
                    color = Color(0xFF759E9F),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(1f))

                Image(
                    painter = painterResource(id = R.drawable.question),
                    contentDescription = "說明",
                    modifier = Modifier
                        .size(30.dp)
                        .padding(end = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                "上傳商品圖",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 圖片預覽區
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(100.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(6) { index ->
                    val uris = flowViewModel.photoUris.value
                    Card(
                        modifier = Modifier
                            .size(90.dp)
                            .drawBehind {
                                if (index >= uris.size) {
                                    drawRect(
                                        color = Color.LightGray,
                                        style = Stroke(width = 4f),
                                    )
                                }
                            },
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        if (index < uris.size) {
                            Image(
                                painter = rememberAsyncImagePainter(uris[index]),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 拍照與選圖按鈕
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { onCameraClick() },
                    modifier = Modifier.weight(1f).padding(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4E5D3))
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "拍攝照片", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("拍攝照片", color = Color.Black, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f).padding(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4E5D3))
                ) {
                    Icon(Icons.Filled.Photo, contentDescription = "選擇照片", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("選擇照片", color = Color.Black, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // =========================
            //   下一步：手動 or AI
            // =========================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ① 手動上架商品
                Button(
                    onClick = {
                        val uris = flowViewModel.photoUris.value
                        if (uris.isNotEmpty()) {
                            // 傳遞第一張圖 (若需多張，建議透過 SharedViewModel 或 Repository，因 URL 長度有限制)
                            val firstImageUri = uris.first().toString()

                            val draft = DraftProduct(imageUri = firstImageUri, fromAI = false)
                            val json = Gson().toJson(draft)
                            val encodedJson = URLEncoder.encode(json, StandardCharsets.UTF_8.toString())

                            navController.navigate("add_product?draftJson=$encodedJson")
                        } else {
                            navController.navigate("add_product")
                        }
                    },
                    enabled = currentUris.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE9D7C0)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("手動上架商品", color = Color.Black, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ② AI 上架助手
                Button(
                    onClick = {
                        val uris = flowViewModel.photoUris.value
                        if (uris.isNotEmpty()) {
                            val firstImageUri = uris.first().toString()
                            val encodedUri = URLEncoder.encode(firstImageUri, StandardCharsets.UTF_8.toString())
                            navController.navigate("ai_chat?imageUri=$encodedUri")
                        }
                    },
                    enabled = currentUris.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4E5D3)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("呼叫 AI 上架助手", color = Color.Black, fontSize = 18.sp)
                }
            }
        }
    }
}