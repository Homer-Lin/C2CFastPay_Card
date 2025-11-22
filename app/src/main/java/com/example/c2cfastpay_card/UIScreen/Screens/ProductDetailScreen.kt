package com.example.c2cfastpay_card.UIScreen.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.c2cfastpay_card.UIScreen.components.CartRepository
import com.example.c2cfastpay_card.UIScreen.components.ProductItem
import com.example.c2cfastpay_card.UIScreen.components.ProductRepository
import com.example.c2cfastpay_card.ui.theme.SaleColorScheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: String
) {
    val context = LocalContext.current
    val productRepository = remember { ProductRepository(context) }
    var product by remember { mutableStateOf<ProductItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val cartRepository = remember { CartRepository(context) } // 記得宣告 Repository
    val scope = rememberCoroutineScope() // 記得宣告 Scope
    // 載入資料
    LaunchedEffect(productId) {
        product = productRepository.getProductById(productId)
        isLoading = false
    }

    MaterialTheme(colorScheme = SaleColorScheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("商品詳情") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                )
            },
            bottomBar = {
                // 這裡未來會放「加入購物車」按鈕
                if (product != null) {
                    BottomAppBar(
                        containerColor = Color.White,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Button(
                            onClick = {
                                // 【實作】加入購物車
                                val item = product // 確保 product 不為 null
                                if (item != null) {
                                    scope.launch {
                                        val cartItem = com.example.c2cfastpay_card.data.CartItem(
                                            productId = item.id,
                                            productTitle = item.title,
                                            productPrice = item.price,
                                            productImage = item.imageUri,
                                            sellerId = item.ownerId
                                        )
                                        cartRepository.addToCart(cartItem)
                                        // 可以加個 Toast 提示 "已加入購物車"
                                        android.widget.Toast.makeText(context, "已加入購物車", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                        ) {
                            Text("加入購物車", fontSize = 18.sp, color = Color.White)
                        }
                    }
                }
            }
        ) { innerPadding ->
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val item = product
                if (item != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState())
                            .background(Color.White)
                    ) {
                        // 1. 商品圖片
                        if (item.imageUri.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(model = item.imageUri),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Column(modifier = Modifier.padding(16.dp)) {
                            // 2. 價格與標題
                            Text(
                                text = "NT$ ${item.price}",
                                color = Color(0xFFD32F2F),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.title,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Divider(modifier = Modifier.padding(vertical = 16.dp))

                            // 3. 詳細資訊區塊
                            DetailSection(title = "商品描述", content = item.description)
                            DetailSection(title = "商品規格", content = item.specs)
                            DetailSection(title = "交易方式", content = item.payment)
                            DetailSection(title = "注意事項", content = item.notes)
                            DetailSection(title = "其他資訊", content = item.other)

                            Spacer(modifier = Modifier.height(24.dp))

                            // 4. 賣家資訊
                            Text(text = "賣家：${item.ownerName}", color = Color.Gray)
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("找不到該商品")
                    }
                }
            }
        }
    }
}

@Composable
fun DetailSection(title: String, content: String) {
    if (content.isNotBlank()) {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                fontSize = 15.sp,
                color = Color.DarkGray
            )
        }
    }
}