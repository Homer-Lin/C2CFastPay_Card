package com.example.c2cfastpay_card.UIScreen.Screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.gson.Gson
// 請記得 import 你的 DraftProduct
// import com.example.c2cfastpay_card.model.DraftProduct

// 暫時定義在這裡方便你複製，之後建議移到 model 檔案
data class DraftProduct(
    val imageUri: String = "",
    val title: String = "",
    val description: String = "",
    val story: String = "",
    val condition: String = "",
    val fromAI: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(navController: NavController, draftJson: String? = null) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // --- 1. 狀態變數 (UI 資料) ---
    // 圖片列表 (支援多張)
    var photoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // 文字欄位
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") } // 文案
    var story by remember { mutableStateOf("") }   // 故事 (選填)
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("1") }

    // 新舊狀態 (下拉選單)
    val statusOptions = listOf("全新", "二手")
    var selectedStatus by remember { mutableStateOf(statusOptions[0]) }
    var statusExpanded by remember { mutableStateOf(false) }

    // 物流方式 (多選)
    val logisticOptions = listOf("7-11", "全家", "面交")
    var selectedLogistics by remember { mutableStateOf(setOf<String>()) }

    // --- 2. 接收外部資料 (AI 或 Step1) ---
    LaunchedEffect(draftJson) {
        if (!draftJson.isNullOrEmpty() && draftJson != "null") {
            try {
                // 解碼 JSON (防止 URL 特殊字元問題)
                val decodedJson = java.net.URLDecoder.decode(draftJson, "UTF-8")
                val draft = Gson().fromJson(decodedJson, DraftProduct::class.java)

                // 填入圖片
                if (draft.imageUri.isNotEmpty()) {
                    val uri = draft.imageUri.toUri()
                    if (!photoUris.contains(uri)) {
                        photoUris = photoUris + uri
                    }
                }

                // 填入文字資料 (如果是 AI 來的)
                if (draft.fromAI) {
                    title = draft.title
                    content = draft.description
                    story = draft.story
                    if (draft.condition in statusOptions) {
                        selectedStatus = draft.condition
                    }
                }
            } catch (e: Exception) {
                Log.e("AddProductScreen", "解析失敗", e)
            }
        }
    }

    // 圖片選擇器
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        photoUris = photoUris + uris
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("上架商品", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF9F9F9)) // 微微灰背景，讓卡片更明顯
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- 圖片區塊 (Horizontal Scroll) ---
            Text("商品圖片", modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), color = Color.Gray)
            LazyRow(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 顯示已選圖片
                items(photoUris) { uri ->
                    Box(modifier = Modifier.size(110.dp)) {
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        // 刪除按鈕
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(20.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .clickable { photoUris = photoUris - uri },
                            tint = Color.White
                        )
                    }
                }
                // 新增圖片按鈕
                item {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                            .clickable { galleryLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.Gray)
                            Text("新增照片", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 輸入欄位區 ---

            // 標題
            MyTextField(value = title, onValueChange = { title = it }, label = "商品標題")

            Spacer(modifier = Modifier.height(16.dp))

            // 文案
            MyTextField(
                value = content,
                onValueChange = { content = it },
                label = "商品文案",
                singleLine = false,
                modifier = Modifier.height(120.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 故事 (選填)
            MyTextField(
                value = story,
                onValueChange = { story = it },
                label = "商品故事 (選填)",
                singleLine = false,
                modifier = Modifier.height(100.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 價格與庫存 (並排)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MyTextField(
                    value = price,
                    onValueChange = { if (it.all { c -> c.isDigit() }) price = it },
                    label = "價格",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                MyTextField(
                    value = stock,
                    onValueChange = { if (it.all { c -> c.isDigit() }) stock = it },
                    label = "庫存",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 新舊狀態 (下拉選單) ---
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = !statusExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedStatus,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("新舊狀態") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    statusOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedStatus = option
                                statusExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 物流方式 (多選 Chips) ---
            Text("物流方式 (可多選)", modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), color = Color.Gray)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                logisticOptions.forEach { option ->
                    val isSelected = selectedLogistics.contains(option)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val current = selectedLogistics.toMutableSet()
                            if (isSelected) current.remove(option) else current.add(option)
                            selectedLogistics = current
                        },
                        label = { Text(option) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF759E9F), // 你的主題色
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,           // 1. 補上 enabled
                            selected = isSelected,    // 2. 補上 selected
                            borderColor = if(isSelected) Color.Transparent else Color.Gray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 確認按鈕 ---
            Button(
                onClick = {
                    // 驗證
                    if (title.isBlank() || content.isBlank() || price.isBlank() || selectedLogistics.isEmpty()) {
                        Toast.makeText(context, "請填寫完整資訊 (標題、文案、價格、物流)", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // TODO: 這裡之後接資料庫，現在先顯示成功
                    // 未來可以把 photoUris 轉字串, selectedLogistics 轉字串存入 DB
                    Toast.makeText(context, "商品上架成功 (UI測試)", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF759E9F))
            ) {
                Text("確認上架", fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

// 封裝一個通用的 TextField 讓 UI 程式碼更乾淨
@Composable
fun MyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.Gray) },
        modifier = modifier,
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color(0xFF759E9F),
            unfocusedBorderColor = Color.LightGray
        )
    )
}