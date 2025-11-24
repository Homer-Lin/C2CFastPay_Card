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
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow // 【修正1】新增 shadow 的 import
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.c2cfastpay_card.UIScreen.components.ProductRepository
import com.example.c2cfastpay_card.navigation.Screen
import com.google.gson.Gson

// --- 定義主題色 (藍綠色系) ---
val SalePrimary = Color(0xFF487F81)      // 主色：藍綠色
val SaleLight = Color(0xFFE0F2F1)        // 淺色背景
val SaleText = Color(0xFF191C1C)         // 深色文字
val SaleBackground = Color(0xFFF4F7F7)   // 頁面背景灰

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(navController: NavController, draftJson: String? = null) {
    val context = LocalContext.current

    // 初始化 ViewModel
    val productRepository = remember { ProductRepository(context) }
    val viewModel: AddProductViewModel = viewModel(
        factory = AddProductViewModelFactory(productRepository)
    )

    val scrollState = rememberScrollState()

    // --- 狀態變數 ---
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

    // 監聽 ViewModel 狀態
    LaunchedEffect(viewModel.uploadStatus) {
        viewModel.uploadStatus?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearStatus()
        }
    }

    // 自動帶入資料邏輯
    LaunchedEffect(draftJson) {
        if (!draftJson.isNullOrEmpty() && draftJson != "null") {
            try {
                // 這裡使用了下方的 WishDataDTO
                val wishData = Gson().fromJson(draftJson, WishDataDTO::class.java)
                if (wishData.title.isNotEmpty()) title = wishData.title
                if (wishData.description.isNotEmpty()) content = wishData.description
                if (wishData.price.isNotEmpty()) price = wishData.price
                if (wishData.qty.isNotEmpty()) stock = wishData.qty
                if (wishData.imageUri.isNotEmpty()) {
                    val uri = wishData.imageUri.toUri()
                    if (!photoUris.contains(uri)) photoUris = photoUris + uri
                }
                selectedStatus = if (wishData.condition == "全新") "全新" else "二手"
                if (wishData.payment.isNotEmpty()) {
                    val newLogistics = mutableSetOf<String>()
                    if (wishData.payment.contains("7-11")) newLogistics.add("7-11")
                    if (wishData.payment.contains("全家")) newLogistics.add("全家")
                    if (wishData.payment.contains("面交")) newLogistics.add("面交")
                    if (newLogistics.isNotEmpty()) selectedLogistics = newLogistics
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
        containerColor = SaleBackground, // 設定背景色
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "上架商品",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SaleText
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SaleText)
                    }
                },
                actions = {
                    // 許願按鈕 (維持橘色以做區分)
                    Button(
                        onClick = { navController.navigate(Screen.AddWish.route) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .height(36.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("我要許願", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- 1. 圖片上傳區 ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier
                                .width(4.dp)
                                .height(16.dp)
                                .background(SalePrimary, CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("商品圖片", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SaleText)
                            Text(" (第一張將成為封面)", fontSize = 12.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 已選圖片
                            items(photoUris) { uri ->
                                Box(modifier = Modifier.size(100.dp)) {
                                    Image(
                                        painter = rememberAsyncImagePainter(uri),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
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
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                            .padding(2.dp)
                                            .clickable { photoUris = photoUris - uri },
                                        tint = Color.White
                                    )
                                }
                            }
                            // 新增按鈕 (虛線風格)
                            item {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SaleLight.copy(alpha = 0.3f))
                                        .border(2.dp, SalePrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)) // 模擬虛線
                                        .clickable { galleryLauncher.launch("image/*") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = SalePrimary, modifier = Modifier.size(32.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("新增", fontSize = 12.sp, color = SalePrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- 2. 商品資訊表單 ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp) // 統一間距
                    ) {
                        // 標題 (有 Icon)
                        BeautifulTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = "商品標題",
                            icon = Icons.Default.Title,
                            placeholder = "例如：全新 switch 遊戲片"
                        )

                        // 價格與庫存 (並排)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            BeautifulTextField(
                                value = price,
                                onValueChange = { if (it.all { c -> c.isDigit() }) price = it },
                                label = "價格",
                                icon = Icons.Default.AttachMoney,
                                keyboardType = KeyboardType.Number,
                                modifier = Modifier.weight(1f)
                            )
                            BeautifulTextField(
                                value = stock,
                                onValueChange = { if (it.all { c -> c.isDigit() }) stock = it },
                                label = "庫存",
                                icon = Icons.Default.Inventory2,
                                keyboardType = KeyboardType.Number,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // 新舊狀態 (下拉選單)
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
                                leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = SalePrimary) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = SalePrimary,
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = statusExpanded,
                                onDismissRequest = { statusExpanded = false },
                                modifier = Modifier.background(Color.White)
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

                        // 物流方式 (Chips)
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = SalePrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("物流方式", color = Color.Gray, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
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
                                        label = { Text(option, fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal) },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SalePrimary,
                                            selectedLabelColor = Color.White,
                                            selectedLeadingIconColor = Color.White,
                                            containerColor = Color.White
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = if(isSelected) Color.Transparent else Color.LightGray
                                        ),
                                        shape = RoundedCornerShape(50)
                                    )
                                }
                            }
                        }

                        // 商品文案 (多行，強制填滿寬度)
                        BeautifulTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = "商品文案",
                            singleLine = false,
                            minLines = 4, // 預設高度
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = "詳細描述您的商品"
                        )

                        // 商品故事 (多行，強制填滿寬度)
                        BeautifulTextField(
                            value = story,
                            onValueChange = { story = it },
                            label = "商品故事 (選填)",
                            singleLine = false,
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = "分享這個商品背後的故事"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- 3. 確認按鈕 ---
                Button(
                    onClick = {
                        if (title.isBlank() || content.isBlank() || price.isBlank() || selectedLogistics.isEmpty()) {
                            Toast.makeText(context, "請填寫完整資訊 (標題、文案、價格、物流)", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.submitProduct(
                            title = title,
                            description = content,
                            story = story,
                            price = price,
                            stock = stock,
                            condition = selectedStatus,
                            logistics = selectedLogistics,
                            photoUris = photoUris,
                            onSuccess = { navController.popBackStack() }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp)), // 增加陰影
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SalePrimary,
                        disabledContainerColor = Color.Gray
                    ),
                    enabled = !viewModel.isLoading
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("上架中...", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("確認上架", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(50.dp))
            }

            // 全螢幕 Loading 遮罩
            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = SalePrimary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("商品上架處理中...", fontWeight = FontWeight.Bold, color = SaleText)
                        }
                    }
                }
            }
        }
    }
}

// --- 美化版通用輸入框 ---
@Composable
fun BeautifulTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier.fillMaxWidth(), // 預設填滿寬度
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        placeholder = { Text(placeholder, color = Color.LightGray) },
        leadingIcon = if (icon != null) {
            { Icon(icon, contentDescription = null, tint = SalePrimary) }
        } else null,
        modifier = modifier, // 使用傳入的 modifier
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = SalePrimary, // 聚焦時使用主題色
            unfocusedBorderColor = Color.LightGray,
            focusedLabelColor = SalePrimary,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        )
    )
}

// --- 【修正2】補回漏掉的 WishDataDTO ---
data class WishDataDTO(
    val title: String = "",
    val price: String = "",       // 預算 -> 變價格
    val description: String = "", // 描述 -> 變文案
    val qty: String = "",         // 數量 -> 變庫存
    val payment: String = "",     // 物流字串
    val imageUri: String = "",
    val condition: String = ""
)