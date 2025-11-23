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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.example.c2cfastpay_card.navigation.Screen
import coil.compose.rememberAsyncImagePainter
import com.google.gson.Gson
// import com.example.c2cfastpay_card.model.DraftProduct (請解開您的 import)

// 暫時定義，請使用您專案原本的
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

    // --- 1. 狀態變數 ---
    var photoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var story by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("1") }

    val statusOptions = listOf("全新", "二手")
    var selectedStatus by remember { mutableStateOf(statusOptions[0]) }
    var statusExpanded by remember { mutableStateOf(false) }

    val logisticOptions = listOf("7-11", "全家", "面交")
    var selectedLogistics by remember { mutableStateOf(setOf<String>()) }

    // --- 2. 接收外部資料 ---
    LaunchedEffect(draftJson) {
        if (!draftJson.isNullOrEmpty() && draftJson != "null") {
            try {
                val decodedJson = java.net.URLDecoder.decode(draftJson, "UTF-8")
                val draft = Gson().fromJson(decodedJson, DraftProduct::class.java)

                if (draft.imageUri.isNotEmpty()) {
                    val uri = draft.imageUri.toUri()
                    if (!photoUris.contains(uri)) photoUris = photoUris + uri
                }
                if (draft.fromAI) {
                    title = draft.title
                    content = draft.description
                    story = draft.story
                    if (draft.condition in statusOptions) selectedStatus = draft.condition
                }
            } catch (e: Exception) {
                Log.e("AddProductScreen", "解析失敗", e)
            }
        }
    }

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
                // ★★★ 這裡新增了右上角的許願按鈕 ★★★
                actions = {
                    Button(
                        onClick = {
                            // 請確認這是您 Navigation 中定義的 route 名稱 (如 Screen.AddWish.route)
                            navController.navigate(Screen.AddWish.route)

                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800) // 橘色背景
                        ),
                        shape = RoundedCornerShape(50), // 圓角
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .height(36.dp) // 高度稍微設小一點，比較精緻
                    ) {
                        Text(
                            text = "我要許願",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                // ★★★ 結束 ★★★
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
                .background(Color(0xFFF9F9F9))
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- 圖片區塊 ---
            Text("商品圖片", modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), color = Color.Gray)
            LazyRow(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
            MyTextField(value = title, onValueChange = { title = it }, label = "商品標題")
            Spacer(modifier = Modifier.height(16.dp))
            MyTextField(
                value = content,
                onValueChange = { content = it },
                label = "商品文案",
                singleLine = false,
                modifier = Modifier.height(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            MyTextField(
                value = story,
                onValueChange = { story = it },
                label = "商品故事 (選填)",
                singleLine = false,
                modifier = Modifier.height(100.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

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

            // --- 新舊狀態 ---
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

            // --- 物流方式 ---
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
                            selectedContainerColor = Color(0xFF759E9F),
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if(isSelected) Color.Transparent else Color.Gray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 確認按鈕 ---
            Button(
                onClick = {
                    if (title.isBlank() || content.isBlank() || price.isBlank() || selectedLogistics.isEmpty()) {
                        Toast.makeText(context, "請填寫完整資訊 (標題、文案、價格、物流)", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    Toast.makeText(context, "商品上架成功", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF759E9F))
            ) {
                Text("確認上架", fontSize = 18.sp, color = Color.White)
            }
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

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