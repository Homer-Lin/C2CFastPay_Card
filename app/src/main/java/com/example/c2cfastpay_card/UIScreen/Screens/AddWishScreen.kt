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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.c2cfastpay_card.UIScreen.components.WishRepository
import com.example.c2cfastpay_card.navigation.Screen
// 1. 確保有導入 WishItem 和 UUID
import com.example.c2cfastpay_card.UIScreen.components.WishItem
import java.util.UUID
// 如果 WishColorScheme 報錯，請改回 C2CFastPay_CardTheme 或確認 Theme.kt 有定義
import com.example.c2cfastpay_card.ui.theme.WishColorScheme
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

    // --- 狀態管理 ---
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
    var selectedTradeMethod by remember { mutableStateOf("") }
    // --- 狀態管理結束 ---

    MaterialTheme(colorScheme = WishColorScheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "新增許願",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {navController.navigate(Screen.Sale.route) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回"
                            )
                        }
                    },
                    actions = {
                        // 這裡的導航建議使用 Screen.AddProduct.route
                        Button(
                            onClick = { navController.navigate(Screen.AddProduct.route) },
                            colors = ButtonDefaults.buttonColors(Color(0xFF487F81))
                        ) {
                            Text(
                                "我要上架",
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // --- 圖片選擇區域 ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "選擇圖片",
                                modifier = Modifier.size(50.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "點擊上傳圖片",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- 輸入欄位 ---
                OutlinedTextField(
                    value = wishTitle,
                    onValueChange = { wishTitle = it },
                    label = { Text("許願商品名稱*") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = wishDescription,
                    onValueChange = { wishDescription = it },
                    label = { Text("許願商品描述") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = wishSpecs,
                    onValueChange = { wishSpecs = it },
                    label = { Text("許願商品規格") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = wishPrice,
                    onValueChange = { wishPrice = it },
                    label = { Text("期望價格*") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    leadingIcon = { Text("NT$") }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 交易方式
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedTradeMethod.ifEmpty { "請選擇交易方式*" },
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

                OutlinedTextField(
                    value = wishNotes,
                    onValueChange = { wishNotes = it },
                    label = { Text("注意事項") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = wishOtherInfo,
                    onValueChange = { wishOtherInfo = it },
                    label = { Text("其他") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )

                Spacer(modifier = Modifier.height(32.dp))

                // --- 新增許願按鈕 (關鍵修改部分) ---
                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            val isValid = wishTitle.isNotBlank() &&
                                    wishPrice.isNotBlank() &&
                                    wishPrice.toDoubleOrNull() != null &&
                                    selectedTradeMethod.isNotBlank()

                            if (!isValid) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "請填寫所有必填 (*) 欄位並確認價格格式正確", Toast.LENGTH_SHORT).show()
                                }
                                return@launch
                            }

                            try {
                                // 1. 儲存圖片到內部空間 (可選，為了取得路徑)
                                // 如果您希望上傳到 Firebase Storage，WishRepository.addWish 已經有處理
                                // 這裡我們只需要取得 URI 字串傳給它
                                val finalImageUriString = imageUri?.let { uri ->
                                    saveImageToInternalStorage(context, uri)
                                } ?: ""

                                // 2. 【修正】建立 WishItem 物件
                                // 包含所有新欄位，並手動生成 UUID 防止 null 錯誤
                                val newWish = WishItem(
                                    title = wishTitle,
                                    description = wishDescription, // 新增欄位
                                    specs = wishSpecs,             // 新增欄位
                                    price = wishPrice,
                                    payment = selectedTradeMethod,
                                    notes = wishNotes,             // 新增欄位
                                    other = wishOtherInfo,         // 新增欄位
                                    imageUri = finalImageUriString,
                                    uuid = UUID.randomUUID().toString() // 【關鍵】生成 UUID
                                )

                                // 3. 【修正】呼叫新版 addWish
                                wishRepository.addWish(newWish)

                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "願望已成功新增", Toast.LENGTH_SHORT).show()
                                    navController.navigate(Screen.WishList.route) {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        "新增許願", fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}