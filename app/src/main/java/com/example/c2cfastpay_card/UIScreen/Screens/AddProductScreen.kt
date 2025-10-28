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
import com.example.c2cfastpay_card.UIScreen.components.WishItem
import com.example.c2cfastpay_card.UIScreen.components.ProductItem
import com.example.c2cfastpay_card.UIScreen.components.ProductRepository
import com.example.c2cfastpay_card.navigation.Screen
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material3.MaterialTheme // <-- 匯入 MaterialTheme
import com.example.c2cfastpay_card.ui.theme.SaleColorScheme // <-- 匯入 Sale 配色



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(navController: NavController, wishJson: String? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val productRepository = remember { ProductRepository(context) }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // --- 狀態管理 (來自您原本的 Compose 程式碼) ---
    val actualWishJson = if (wishJson == "null" || wishJson.isNullOrEmpty()) {
        null
    } else {
        wishJson
    }
    val wishItem = try {
        actualWishJson?.let { Gson().fromJson(it, WishItem::class.java) }
    } catch (e: Exception) {
        // 可以加入錯誤處理，例如 Logcat 輸出
        // Log.e("AddProductScreen", "Error parsing wishJson: ${e.message}")
        null // 解析失敗也視為 null
    }

    var productName by remember { mutableStateOf(wishItem?.title ?: "") }
    var productDescription by remember { mutableStateOf(wishItem?.description ?: "") }
    var productSpecs by remember { mutableStateOf(wishItem?.specs ?: "") }
    var productPrice by remember { mutableStateOf(wishItem?.price ?: "") }
    var selectedTradeMethod by remember { mutableStateOf(wishItem?.payment ?: "") } // 預設空字串或第一個選項
    var productNotes by remember { mutableStateOf(wishItem?.notes ?: "") }
    var productOtherInfo by remember { mutableStateOf(wishItem?.other ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(wishItem?.imageUri?.let { Uri.parse(it) }) }
    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            imageUri = uri
        }
    val tradeMethods = listOf("面交", "宅配", "超商取貨")
    var expanded by remember { mutableStateOf(false) }
    // --- 狀態管理結束 ---
    MaterialTheme(colorScheme = SaleColorScheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "上架商品",
                            color = MaterialTheme.colorScheme.onSurface, // 匹配 XML 顏色
                            fontSize = 20.sp
                        )
                    },
                    navigationIcon = {
                        // 返回按鈕 (匹配 XML)
                        IconButton(onClick = { navController.navigate(Screen.Sale.route) }) {
                            Image(
                                painter = painterResource(id = R.drawable.a_1_back_buttom), // 使用 XML 的 drawable
                                contentDescription = "返回"
                            )
                        }
                    },
                    actions = {
                        // 我要許願按鈕 (匹配 XML 的 ImageButton11)
                        Button(onClick = { navController.navigate(Screen.AddWish.route) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))) {
                            Text("我要許願" ,
                                color = Color.White,
                                fontSize = 18.sp)
                        }
                    },
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

                // --- 圖片選擇區域 (匹配 XML 的 imageView18 + imageButton9) ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp) // 給定一個高度
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant, // 淺色背景
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "商品圖片",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // 顯示預設圖示和上傳按鈕 (類似 XML)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "選擇圖片",
                                modifier = Modifier.size(50.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) // 灰色
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // 您可以放一個 Text 或 ImageButton 來模擬 XML 的上傳按鈕
                            Text(
                                "點擊上傳圖片",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp)) // 間距

                // --- 輸入欄位 (使用 OutlinedTextField) ---

                // 商品名稱 (帶*號)
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("上架商品名稱*") }, // 匹配 XML 文字
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 商品描述
                OutlinedTextField(
                    value = productDescription,
                    onValueChange = { productDescription = it },
                    label = { Text("商品描述") }, // 匹配 XML 文字
                    modifier = Modifier.fillMaxWidth().height(120.dp), // 給定多行高度
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 商品規格
                OutlinedTextField(
                    value = productSpecs,
                    onValueChange = { productSpecs = it },
                    label = { Text("商品規格") }, // 匹配 XML 文字
                    modifier = Modifier.fillMaxWidth().height(120.dp), // 給定多行高度
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 商品價格 (帶*號)
                OutlinedTextField(
                    value = productPrice,
                    onValueChange = { productPrice = it },
                    label = { Text("商品價格*") }, // 匹配 XML 文字
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    leadingIcon = { Text("NT$") } // 加上貨幣符號提示
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 交易方式下拉選單 (保留您原有的)
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
                    value = productNotes,
                    onValueChange = { productNotes = it },
                    label = { Text("注意事項") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 其他資訊
                OutlinedTextField(
                    value = productOtherInfo,
                    onValueChange = { productOtherInfo = it },
                    label = { Text("其他資訊") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )

                Spacer(modifier = Modifier.height(32.dp)) // 底部按鈕前的間距

                // --- 上架按鈕 ---
                Button(
                    onClick = {
                        // 保留您原有的驗證和儲存邏輯
                        if (productName.isBlank() || productPrice.isBlank() || selectedTradeMethod.isBlank()) {
                            Toast.makeText(context, "請填寫所有必填欄位 (*)", Toast.LENGTH_SHORT)
                                .show()
                            return@Button
                        }
                        if (productPrice.toDoubleOrNull() == null) {
                            Toast.makeText(context, "價格必須是數字", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        scope.launch(Dispatchers.IO) {
                            try {
                                val newProduct = ProductItem(
                                    title = productName,
                                    description = productDescription,
                                    specs = productSpecs,
                                    price = productPrice,
                                    payment = selectedTradeMethod,
                                    notes = productNotes,
                                    other = productOtherInfo,
                                    imageUri = imageUri?.toString() ?: ""
                                )
                                productRepository.addProduct(newProduct)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "商品已成功上架", Toast.LENGTH_SHORT)
                                        .show()
                                    navController.navigate(Screen.Sale.route) {
                                        popUpTo(Screen.Sale.route) { inclusive = true }
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        "上架失敗：${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    // --- 使用主題次要色 ---
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        "上架商品", fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSecondary
                    )

                }

                Spacer(modifier = Modifier.height(16.dp)) // 確保滾動到底部時有空間
            }
        }
    }
}
