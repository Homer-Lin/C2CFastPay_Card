package com.example.c2cfastpay_card.UIScreen.Screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // 使用 AutoMirrored
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.c2cfastpay_card.R
import com.example.c2cfastpay_card.UIScreen.components.WishRepository
import com.example.c2cfastpay_card.navigation.Screen // 匯入 Screen
import com.example.c2cfastpay_card.ui.theme.WishColorScheme // <-- 匯入 Wish 配色
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.c2cfastpay_card.utils.saveImageToInternalStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWishScreen(navController: NavController) {

    val context = LocalContext.current
    val wishRepository = remember { WishRepository(context) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // --- 狀態管理 (來自您原本的 Compose 程式碼) ---
    var wishTitle by remember { mutableStateOf("") }
    var wishDescription by remember { mutableStateOf("") }
    var wishSpecs by remember { mutableStateOf("") }
    var wishPrice by remember { mutableStateOf("") }
    var wishNotes by remember { mutableStateOf("") }
    var wishOtherInfo by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }
    val tradeMethods = listOf("面交", "宅配", "超商取貨")
    var expanded by remember { mutableStateOf(false) }
    var selectedTradeMethod by remember { mutableStateOf("") } // 預設空字串或第一個選項
    // --- 狀態管理結束 ---

    MaterialTheme(colorScheme = WishColorScheme) { // <-- 套用 Wish 配色主題
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "新增許願",
                            color = MaterialTheme.colorScheme.onSurface, // 使用主題顏色
                            fontSize = 20.sp
                        )
                    },
                    navigationIcon = {
                        // 返回按鈕
                        IconButton(onClick = {navController.navigate(Screen.Sale.route) }) {
                            // 直接使用 Icon，Compose 會處理 AutoMirrored
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回"
                            )
                        }
                    },
                    actions = {
                        // 上架商品按鈕
                        Button(
                            onClick = { navController.navigate("add_product") }, // 確保傳遞 null 或空字串
                            // 可以考慮使用 Sale 的主題色或其他強調色
                            colors = ButtonDefaults.buttonColors(Color(0xFF487F81))
                        ) {
                            Text(
                                "我要上架",
                                color = Color.White, // 使用主題顏色
                                fontSize = 18.sp
                            )
                        }
                    },
                    // 可以選擇性地為 TopAppBar 添加背景色
//                    colors = TopAppBarDefaults.topAppBarColors(
//                        containerColor = MaterialTheme.colorScheme.primaryContainer
//                    )
                )
            }
        ) { innerPadding ->
            // --- 可滾動的表單內容 ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding) // 套用 Scaffold 的 padding
                    .padding(horizontal = 16.dp) // 加入左右邊距
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(24.dp)) // 頂部間距

                // --- 圖片選擇區域 ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp) // 給定一個高度
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant, // 使用主題的淺色背景
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "許願圖片",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // 顯示預設圖示和文字
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "選擇圖片",
                                modifier = Modifier.size(50.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) // 使用主題的灰色調
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "點擊上傳圖片",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f) // 使用主題的文字顏色
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp)) // 間距

                // --- 輸入欄位 (使用 OutlinedTextField) ---

                // 許願商品名稱 (帶*號)
                OutlinedTextField(
                    value = wishTitle,
                    onValueChange = { wishTitle = it },
                    label = { Text("許願商品名稱*") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 許願商品描述
                OutlinedTextField(
                    value = wishDescription,
                    onValueChange = { wishDescription = it },
                    label = { Text("許願商品描述") },
                    modifier = Modifier.fillMaxWidth().height(120.dp), // 給定多行高度
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 許願商品規格
                OutlinedTextField(
                    value = wishSpecs,
                    onValueChange = { wishSpecs = it },
                    label = { Text("許願商品規格") },
                    modifier = Modifier.fillMaxWidth().height(120.dp), // 給定多行高度
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 許願商品價格 (帶*號)
                OutlinedTextField(
                    value = wishPrice,
                    onValueChange = { wishPrice = it },
                    label = { Text("期望價格*") }, // 修改標籤文字
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    leadingIcon = { Text("NT$") } // 加上貨幣符號提示
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 交易方式下拉選單
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedTradeMethod.ifEmpty { "請選擇交易方式*" }, // 提示文字
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("交易方式*") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                            .clickable { expanded = !expanded }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }) {
                        tradeMethods.forEach { method ->
                            DropdownMenuItem(text = { Text(method) }, onClick = {
                                selectedTradeMethod = method
                                expanded = false
                            })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 注意事項
                OutlinedTextField(
                    value = wishNotes,
                    onValueChange = { wishNotes = it },
                    label = { Text("注意事項") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 其他
                OutlinedTextField(
                    value = wishOtherInfo,
                    onValueChange = { wishOtherInfo = it },
                    label = { Text("其他") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )

                Spacer(modifier = Modifier.height(32.dp)) // 底部按鈕前的間距

                // --- 新增許願按鈕 ---
                Button(
                    onClick = {
                        // 保留您原有的驗證和儲存邏輯
                        scope.launch(Dispatchers.IO) {
                            val isValid = wishTitle.isNotBlank() &&
                                    wishPrice.isNotBlank() && // 價格也不能為空
                                    wishPrice.toDoubleOrNull() != null && // 價格必須是數字
                                    selectedTradeMethod.isNotBlank()

                            if (!isValid) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "請填寫所有必填 (*) 欄位並確認價格格式正確", Toast.LENGTH_SHORT).show()
                                }
                                return@launch
                            }

                            try {
                                // --- 處理圖片 URI (這部分不變) ---
                                val finalImageUriString = imageUri?.let { uri ->
                                    saveImageToInternalStorage(context, uri)
                                }
                                // --- 處理結束 ---

                                // --- 修改開始：將 String? 轉換回 Uri? ---
                                // 我們的 saveImageToInternalStorage 回傳的是 String?
                                // 但 wishRepository.saveWishData 預期的是 Uri?
                                // 所以我們需要將 String? 解析(parse)回 Uri?
                                val finalImageUri: Uri? = finalImageUriString?.let { uriString ->
                                    Uri.parse(uriString) // Uri.parse() 會將 "file:///..." 字串轉回 Uri 物件
                                }
                                // --- 修改結束 ---

                                // --- 傳遞轉換後的 finalImageUri ---
                                wishRepository.saveWishData(
                                    wishTitle,
                                    wishDescription,
                                    wishSpecs,
                                    wishPrice,
                                    selectedTradeMethod,
                                    wishNotes,
                                    wishOtherInfo,
                                    finalImageUri // <-- 使用轉換後的永久 Uri (或 null)
                                )
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "願望已成功新增", Toast.LENGTH_SHORT).show()
                                    navController.navigate(Screen.WishList.route) {
                                        // 清除返回堆疊，避免用戶按返回又回到新增頁面
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "儲存失敗：${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    // --- 使用主題主要色 ---
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary // Wish 主色
                    )
                ) {
                    Text(
                        "新增許願", fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSecondary // Wish 主色上的文字顏色
                    )
                }

                Spacer(modifier = Modifier.height(16.dp)) // 確保滾動到底部時有空間
            }
        }
    }
}