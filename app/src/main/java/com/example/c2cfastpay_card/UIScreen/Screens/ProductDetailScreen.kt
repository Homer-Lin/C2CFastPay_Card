package com.example.c2cfastpay_card.UIScreen.Screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.c2cfastpay_card.UIScreen.components.CartRepository
import com.example.c2cfastpay_card.UIScreen.components.ProductItem
import com.example.c2cfastpay_card.UIScreen.components.ProductRepository
import com.example.c2cfastpay_card.navigation.Screen
import com.example.c2cfastpay_card.ui.theme.SaleColorScheme
import kotlinx.coroutines.launch

// --- 定義顏色 ---
val MintGreenAccent = Color(0xFFE0F2F1)
val MintGreenDark = Color(0xFF487F81)
val TextBlack = Color(0xFF191C1C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: String
) {
    val context = LocalContext.current
    val productRepository = remember { ProductRepository(context) }
    val cartRepository = remember { CartRepository(context) }
    val scope = rememberCoroutineScope()

    // --- 使用本地狀態管理 (不需 ViewModel) ---
    var product by remember { mutableStateOf<ProductItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // 載入資料
    LaunchedEffect(productId) {
        product = productRepository.getProductById(productId)
        isLoading = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("商品詳情", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Cart.route) }) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "購物車", tint = MintGreenDark)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (product != null) {
                BottomAppBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            val item = product
                            if (item != null) {
                                scope.launch {
                                    val cartItem = com.example.c2cfastpay_card.data.CartItem(
                                        productId = item.id,
                                        productTitle = item.title,
                                        productPrice = item.price,
                                        productImage = item.imageUri,
                                        sellerId = item.ownerId,
                                        stock = item.stock.toIntOrNull() ?: 1
                                    )
                                    cartRepository.addToCart(cartItem)
                                    Toast.makeText(context, "已加入購物車", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MintGreenDark),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("加入購物車", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MintGreenDark)
            }
        } else {
            val item = product
            if (item != null) {
                // --- 變數準備 ---
                val rawStory = if (item.story.isNullOrBlank()) "" else item.story
                val displayStock = if (item.stock.isNullOrBlank()) "1" else item.stock
                val displayCondition = if (item.condition.isNullOrBlank()) "全新" else item.condition
                val displayLogistics = item.payment.ifBlank { "7-11、全家、面交" }
                val displayDescription = if (item.description.isNullOrBlank()) "賣家沒有留下文案。" else item.description

                // ★★★ 圖片處理核心邏輯 ★★★
                // 1. 合併主圖與副圖，並去除重複與空值
                val allImages = remember(item) {
                    (listOf(item.imageUri) + item.images)
                        .filter { it.isNotEmpty() }
                        .distinct()
                }
                // 2. 記錄當前選中的圖片索引
                var selectedImageIndex by remember { mutableStateOf(0) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color.White)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // --- 1. 主要圖片顯示區 ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.3f),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        if (allImages.isNotEmpty()) {
                            // 顯示選中的那張圖
                            val currentImage = allImages.getOrElse(selectedImageIndex) { allImages[0] }
                            Image(
                                painter = rememberAsyncImagePainter(model = currentImage),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize().background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("無圖片", color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- 2. 縮圖列表區 (只有當圖片大於 1 張時才顯示) ---
                    if (allImages.size > 1) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(allImages) { index, imgUri ->
                                val isSelected = index == selectedImageIndex
                                Card(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clickable { selectedImageIndex = index }, // 點擊切換
                                    shape = RoundedCornerShape(12.dp),
                                    border = if (isSelected) BorderStroke(2.dp, MintGreenDark) else null, // 選中框
                                    elevation = CardDefaults.cardElevation(if(isSelected) 4.dp else 1.dp)
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = imgUri),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // 3. 商品標題
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .width(220.dp)
                                .height(16.dp)
                                .offset(y = 10.dp)
                                .background(MintGreenAccent, RoundedCornerShape(50))
                        )
                        Text(
                            text = item.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextBlack,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // 4. 詳細資訊區
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            SectionTitle("物流方式")
                            Text(text = displayLogistics, fontSize = 14.sp, color = Color.DarkGray)

                            Spacer(modifier = Modifier.height(24.dp))

                            SectionTitle("商品價格")
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = item.price,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextBlack
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(24.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            SectionTitle("商品規格")
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                SpecText(label = "庫存", value = displayStock)
                                SpecText(label = "狀態", value = displayCondition)
                                // 賣家資訊
                                SpecText(label = "賣家", value = item.ownerName.ifBlank { "匿名" })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(32.dp))

                    // 5. 商品文案
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SectionTitle("商品文案")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = displayDescription,
                            fontSize = 15.sp,
                            color = Color.DarkGray,
                            lineHeight = 22.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 6. 商品故事
                    if (rawStory.isNotBlank()) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            SectionTitle("商品故事")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = rawStory,
                                fontSize = 13.sp,
                                color = Color.DarkGray,
                                lineHeight = 18.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("找不到該商品")
                }
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Box(
        modifier = Modifier.padding(bottom = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(10.dp)
                .offset(y = 6.dp)
                .background(MintGreenAccent, RoundedCornerShape(4.dp))
        )
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextBlack
        )
    }
}

@Composable
fun SpecText(label: String, value: String) {
    Row {
        Text(text = "$label : ", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 13.sp, color = Color.DarkGray)
    }
}