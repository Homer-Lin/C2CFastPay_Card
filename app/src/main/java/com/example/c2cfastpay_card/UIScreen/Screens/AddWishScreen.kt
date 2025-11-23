package com.example.c2cfastpay_card.UIScreen.Screens

import android.net.Uri
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
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.c2cfastpay_card.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWishScreen(navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // --- 定義顏色 ---
    val mainOrangeColor = Color(0xFFFF9800) // 主題橘色
    val buttonGreenColor = Color(0xFF487F81) // 右上角按鈕用的綠色

    // --- 1. 狀態變數 ---
    var photoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // 欄位變數
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") } // 願望描述
    var price by remember { mutableStateOf("") }       // 願付價格
    var quantity by remember { mutableStateOf("1") }   // 欲購數量
    var note by remember { mutableStateOf("") }        // 備註 (新增的)

    // 新舊狀態 (新增"皆可")
    val statusOptions = listOf("全新", "二手", "皆可")
    var selectedStatus by remember { mutableStateOf(statusOptions[0]) }
    var statusExpanded by remember { mutableStateOf(false) }

    // 物流方式
    val logisticOptions = listOf("7-11", "全家", "面交")
    var selectedLogistics by remember { mutableStateOf(setOf<String>()) }

    // 圖片選擇器
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        photoUris = photoUris + uris
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("新增願望", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                // ★★★ 右上角按鈕：樣式依照要求 (綠色框/底)，文字改為"我要上架"以便切換 ★★★
                actions = {
                    Button(
                        onClick = {
                            // 這裡導航去 "上架第一步" (AddStep1)
                            navController.navigate(Screen.AddStep1.route)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonGreenColor // 指定的綠色 0xFF487F81
                        ),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .height(36.dp)
                    ) {
                        Text(
                            text = "我要上架", // 切換回上架模式
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                .background(Color(0xFFFFF8E1)) // 背景改為非常淡的橘黃色，或是保持 F9F9F9 也可以
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- 圖片區塊 (參考圖片) ---
            Text("參考圖片 (選填)", modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), color = Color.Gray)
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

            // --- 輸入欄位區 (使用橘色主題 TextField) ---

            // 標題
            WishTextField(value = title, onValueChange = { title = it }, label = "願望標題", themeColor = mainOrangeColor)

            Spacer(modifier = Modifier.height(16.dp))

            // 願望描述
            WishTextField(
                value = description,
                onValueChange = { description = it },
                label = "願望描述 (例如：希望是哪一年的款式...)",
                singleLine = false,
                modifier = Modifier.height(120.dp),
                themeColor = mainOrangeColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 願付價格 與 欲購數量
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WishTextField(
                    value = price,
                    onValueChange = { if (it.all { c -> c.isDigit() }) price = it },
                    label = "願付價格",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                    themeColor = mainOrangeColor
                )
                WishTextField(
                    value = quantity,
                    onValueChange = { if (it.all { c -> c.isDigit() }) quantity = it },
                    label = "欲購數量",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                    themeColor = mainOrangeColor
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
                    label = { Text("接受狀態") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        // ★ 設定聚焦顏色為橘色
                        focusedBorderColor = mainOrangeColor,
                        focusedLabelColor = mainOrangeColor
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

            // --- 備註 (新增欄位) ---
            WishTextField(
                value = note,
                onValueChange = { note = it },
                label = "備註",
                singleLine = false,
                modifier = Modifier.height(80.dp),
                themeColor = mainOrangeColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- 物流方式 (橘色 Chips) ---
            Text("希望物流方式", modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), color = Color.Gray)
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
                            selectedContainerColor = mainOrangeColor, // ★ 選中變成橘色
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

            // --- 確認按鈕 (橘色) ---
            Button(
                onClick = {
                    if (title.isBlank() || description.isBlank() || price.isBlank() || selectedLogistics.isEmpty()) {
                        Toast.makeText(context, "請填寫完整資訊", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    Toast.makeText(context, "願望已送出", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = mainOrangeColor) // ★ 按鈕橘色
            ) {
                Text("確認許願", fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

// 專屬的橘色系 TextField
@Composable
fun WishTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    themeColor: Color
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
            focusedBorderColor = themeColor, // 傳入橘色
            unfocusedBorderColor = Color.LightGray,
            focusedLabelColor = themeColor   // Label 也變橘色
        )
    )
}