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
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.c2cfastpay_card.UIScreen.components.ProductRepository
import com.example.c2cfastpay_card.navigation.Screen
import com.google.gson.Gson

// 沿用主題色
val SalePrimary = Color(0xFF487F81)
val SaleLight = Color(0xFFE0F2F1)
val SaleText = Color(0xFF191C1C)
val SaleBackground = Color(0xFFF4F7F7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(navController: NavController, draftJson: String? = null) {
    val context = LocalContext.current

    val productRepository = remember { ProductRepository(context) }
    val viewModel: AddProductViewModel = viewModel(
        factory = AddProductViewModelFactory(productRepository)
    )

    val scrollState = rememberScrollState()

    var photoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var cameraUris by remember { mutableStateOf<Set<Uri>>(emptySet()) }

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") } // 商品文案
    var story by remember { mutableStateOf("") }   // 商品故事
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("1") }

    val statusOptions = listOf("全新", "二手")
    var selectedStatus by remember { mutableStateOf(statusOptions[0]) }
    var statusExpanded by remember { mutableStateOf(false) }

    val logisticOptions = listOf("7-11", "全家", "面交")
    var selectedLogistics by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(viewModel.uploadStatus) {
        viewModel.uploadStatus?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearStatus()
        }
    }

    // 自動帶入資料 (解析 JSON)
    LaunchedEffect(draftJson) {
        if (!draftJson.isNullOrEmpty() && draftJson != "null") {
            try {
                val wishData = Gson().fromJson(draftJson, WishDataDTO::class.java)
                if (wishData.title.isNotEmpty()) title = wishData.title
                if (wishData.description.isNotEmpty()) content = wishData.description

                // 帶入故事欄位
                if (wishData.story.isNotEmpty()) story = wishData.story

                if (wishData.price.isNotEmpty()) price = wishData.price
                if (wishData.qty.isNotEmpty()) stock = wishData.qty

                // 1. 復原照片列表
                val newUris = mutableListOf<Uri>()
                if (wishData.imageUri.isNotEmpty()) newUris.add(wishData.imageUri.toUri())
                if (wishData.images.isNotEmpty()) newUris.addAll(wishData.images.map { it.toUri() })
                if (newUris.isNotEmpty()) photoUris = (photoUris + newUris).distinct()

                // 2. 復原「相機實拍」標記
                if (wishData.cameraImages.isNotEmpty()) {
                    val camUris = wishData.cameraImages.map { it.toUri() }
                    cameraUris = (cameraUris + camUris).toSet()
                }

                selectedStatus = if (wishData.condition == "全新") "全新" else "二手"
                if (wishData.payment.isNotEmpty()) {
                    val newLogistics = mutableSetOf<String>()
                    if (wishData.payment.contains("7-11")) newLogistics.add("7-11")
                    if (wishData.payment.contains("全家")) newLogistics.add("全家")
                    if (wishData.payment.contains("面交")) newLogistics.add("面交")
                    if (newLogistics.isNotEmpty()) selectedLogistics = newLogistics
                }

                if (wishData.payment.isNotEmpty()) {
                    val newLogistics = mutableSetOf<String>()
                    if (wishData.payment.contains("7-11")) newLogistics.add("7-11")
                    if (wishData.payment.contains("全家")) newLogistics.add("全家")
                    if (wishData.payment.contains("面交")) newLogistics.add("面交")

                    if (newLogistics.isNotEmpty()) {
                        selectedLogistics = newLogistics
                    }
                }
                Log.d("AddProductScreen", "已自動帶入許願資料: ${wishData.title}")
            } catch (e: Exception) {
                Log.e("AddProductScreen", "解析失敗，可能是格式不符", e)
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        photoUris = photoUris + uris
    }

    Scaffold(
        containerColor = SaleBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("上架商品", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = SaleText)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SaleText)
                    }
                },
                actions = {
                    Button(
                        onClick = { navController.navigate(Screen.AddWish.route) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.padding(end = 12.dp).height(36.dp)
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
                modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(scrollState).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- 圖片上傳區 ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.width(4.dp).height(16.dp).background(SalePrimary, CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("商品圖片", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SaleText)
                            Text(" (第一張將成為封面)", fontSize = 12.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(photoUris) { uri ->
                                Box(modifier = Modifier.size(100.dp)) {
                                    Image(
                                        painter = rememberAsyncImagePainter(uri),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )

                                    val isCameraPhoto = cameraUris.contains(uri)

                                    if (isCameraPhoto) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Verified Photo",
                                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).background(SalePrimary.copy(alpha = 0.9f), CircleShape).padding(4.dp),
                                            tint = Color.White
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(20.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape).padding(2.dp).clickable { photoUris = photoUris - uri },
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                            item {
                                Box(
                                    modifier = Modifier.size(100.dp).clip(RoundedCornerShape(12.dp)).background(SaleLight.copy(alpha = 0.3f)).border(2.dp, SalePrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).clickable { galleryLauncher.launch("image/*") },
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

                // --- 商品資訊表單 ---
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
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        BeautifulTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = "商品標題",
                            icon = Icons.Default.Title,
                            placeholder = "例如：全新 switch 遊戲片"
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            BeautifulTextField(
                                value = price,
                                onValueChange = { if (it.all { c -> c.isDigit() }) price = it },
                                label = "價格",
                                keyboardType = KeyboardType.Number,
                                modifier = Modifier.weight(1f)
                            )
                            BeautifulTextField(
                                value = stock,
                                onValueChange = { if (it.all { c -> c.isDigit() }) stock = it },
                                label = "庫存",
                                keyboardType = KeyboardType.Number,
                                modifier = Modifier.weight(1f)
                            )
                        }

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

                        BeautifulTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = "商品文案",
                            singleLine = false,
                            minLines = 4,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = "詳細描述您的商品..."
                        )

                        BeautifulTextField(
                            value = story,
                            onValueChange = { story = it },
                            label = "商品故事 (選填)",
                            singleLine = false,
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = "分享這個商品背後的小故事..."
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

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
                            onSuccess = {
                                // ★★★ 修正：上架成功後，跳轉到 Sale 頁面並清空返回堆疊 ★★★
                                navController.navigate(Screen.Sale.route) {
                                    popUpTo(Screen.Sale.route) { inclusive = true }
                                }
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp).shadow(8.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SalePrimary, disabledContainerColor = Color.Gray),
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

            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)).clickable(enabled = false) {},
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

@Composable
fun BeautifulTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier.fillMaxWidth(),
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = Color.LightGray) },
        leadingIcon = if (icon != null) {
            { Icon(icon, contentDescription = null, tint = SalePrimary) }
        } else null,
        modifier = modifier,
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        // ★★★ 關鍵修改：強制設定文字顏色為黑色 ★★★
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = SalePrimary,
            unfocusedBorderColor = Color.LightGray,
            focusedLabelColor = SalePrimary,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        )
    )
}

data class WishDataDTO(
    val title: String = "",
    val price: String = "",
    val description: String = "",
    val story: String = "",
    val qty: String = "",
    val payment: String = "",
    val imageUri: String = "",
    val images: List<String> = emptyList(),
    val cameraImages: List<String> = emptyList(),
    val condition: String = ""
)