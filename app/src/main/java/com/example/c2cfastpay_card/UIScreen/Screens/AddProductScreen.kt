package com.example.c2cfastpay_card.UIScreen.Screens

import android.net.Uri
import android.util.Log // 【新增】Log Import
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
import androidx.compose.material3.MaterialTheme
import com.example.c2cfastpay_card.ui.theme.SaleColorScheme
import com.example.c2cfastpay_card.utils.saveImageToInternalStorage
import androidx.core.net.toUri // 【新增】toUri Import

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(navController: NavController, wishJson: String? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val productRepository = remember { ProductRepository(context) }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()


    // --- 【修改：還原為 LaunchedEffect 邏輯】 ---

    // 1. 將所有狀態的預設值都設為空字串或 null
    var productName by remember { mutableStateOf("") }
    var productDescription by remember { mutableStateOf("") } // 您的 WishItem 中沒有 description
    var productSpecs by remember { mutableStateOf("") } // 您的 WishItem 中沒有 specs
    var productPrice by remember { mutableStateOf("") }
    var selectedTradeMethod by remember { mutableStateOf("") } // 預設空字串
    var productNotes by remember { mutableStateOf("") } // 您的 WishItem 中沒有 notes
    var productOtherInfo by remember { mutableStateOf("") } // 您的 WishItem 中沒有 other
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // 2. 使用 LaunchedEffect(wishJson)
    //    這會在 wishJson 參數「改變」時 (例如從 null 變為 "{...json...}") 觸發
    LaunchedEffect(wishJson) {
        Log.d("DataFlowDebug", "步驟 6: AddProductScreen 收到 wishJson = $wishJson")
        val actualWishJson = if (wishJson == "null" || wishJson.isNullOrEmpty()) {
            null
        } else {
            wishJson
        }

        if (actualWishJson != null) {
            try {
                // 3. 解析 JSON
                val wishItem = Gson().fromJson(actualWishJson, WishItem::class.java)
                Log.d("DataFlowDebug", "步驟 7: 成功解析 JSON，準備設定狀態...")
                // 4. 【強制更新】所有狀態
                //    (我們只更新 WishItem 中有的欄位)
                productName = wishItem.title
                productPrice = wishItem.price
                productDescription = wishItem.description
                productSpecs = wishItem.specs
                selectedTradeMethod = wishItem.payment
                productNotes = wishItem.notes
                productOtherInfo = wishItem.other

                if (wishItem.imageUri.isNotEmpty()) {
                    imageUri = wishItem.imageUri.toUri()
                }

                // (註：您的 WishItem 中沒有 description, specs, notes, other，
                //    所以它們會保持為空字串，這也是您在 AddProductScreen.kt 中
                //    的原始邏輯)

            } catch (e: Exception) {
                Log.e("AddProductScreen", "Failed to parse wishJson: $wishJson", e)
                Log.e("DataFlowDebug", "步驟 7 失敗: 解析 JSON 錯誤", e)
            }
        } else {
            // 【重要】如果 wishJson 變回 null (例如用戶點了返回又點了上架)
            // 我們需要重置表單，否則會顯示上一個「快速上架」的資料
            Log.d("DataFlowDebug", "步驟 7 失敗: wishJson 為 null，重置表單")
            productName = ""
            productPrice = ""
            selectedTradeMethod = ""
            imageUri = null
            productDescription = ""
            productSpecs = ""
            productNotes = ""
            productOtherInfo = ""
        }
    }
    // --- 【修改結束】 ---

    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            imageUri = uri
        }
    val tradeMethods = listOf("面交", "宅配", "超商取貨")
    var expanded by remember { mutableStateOf(false) }

    MaterialTheme(colorScheme = SaleColorScheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "上架商品",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigate(Screen.Sale.route) }) {
                            Image(
                                painter = painterResource(id = R.drawable.a_1_back_buttom),
                                contentDescription = "返回"
                            )
                        }
                    },
                    actions = {
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
                            contentDescription = "商品圖片",
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

                // --- 輸入欄位 (使用 OutlinedTextField) ---

                // 商品名稱 (帶*號)
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("上架商品名稱*") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 商品描述
                OutlinedTextField(
                    value = productDescription,
                    onValueChange = { productDescription = it },
                    label = { Text("商品描述") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 商品規格
                OutlinedTextField(
                    value = productSpecs,
                    onValueChange = { productSpecs = it },
                    label = { Text("商品規格") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 商品價格 (帶*號)
                OutlinedTextField(
                    value = productPrice,
                    onValueChange = { productPrice = it },
                    label = { Text("商品價格*") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    leadingIcon = { Text("NT$") }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 交易方式下拉選單
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

                Spacer(modifier = Modifier.height(32.dp))

                // --- 上架按鈕 ---
                Button(
                    onClick = {
                        // (上架邏輯保持不變)
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
                                val finalImageUriString = imageUri?.let { uri ->
                                    saveImageToInternalStorage(context, uri)
                                } ?: ""

                                val newProduct = ProductItem(
                                    title = productName,
                                    description = productDescription,
                                    specs = productSpecs,
                                    price = productPrice,
                                    payment = selectedTradeMethod,
                                    notes = productNotes,
                                    other = productOtherInfo,
                                    imageUri = finalImageUriString
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        "上架商品", fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSecondary
                    )

                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}