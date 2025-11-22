package com.example.c2cfastpay_card.UIScreen.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.c2cfastpay_card.data.CartItem
import com.example.c2cfastpay_card.UIScreen.components.CartRepository
import com.example.c2cfastpay_card.ui.theme.SaleColorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavController) {
    val context = LocalContext.current
    val cartRepository = remember { CartRepository(context) }
    val viewModel: CartViewModel = viewModel(
        factory = CartViewModelFactory(cartRepository)
    )

    val cartItems by viewModel.cartItems.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()

    MaterialTheme(colorScheme = SaleColorScheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("購物車") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                )
            },
            bottomBar = {
                // --- 【UI 優化】底部結帳區 ---
                if (cartItems.isNotEmpty()) {
                    // 改用 Surface，它會根據內容自動調整高度，不會切到文字
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 16.dp, // 加深陰影，讓它更有層次感
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp) // 外部邊距
                                .navigationBarsPadding(), // 【關鍵】避開手機底部導航條
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. 總金額顯示區 (佔用剩餘空間)
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "總金額",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "NT$ $totalPrice",
                                    fontSize = 24.sp, // 加大字體
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD32F2F) // 深紅色
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // 2. 結帳按鈕 (加大、加強視覺)
                            Button(
                                onClick = { viewModel.checkout() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE91E63), // 醒目的粉紅色
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp), // 稍微方一點，看起來更穩重
                                contentPadding = PaddingValues(
                                    horizontal = 24.dp,
                                    vertical = 12.dp
                                ) // 【關鍵】增加內距，讓按鈕變大
                            ) {
                                Text(
                                    text = "前往結帳 (${cartItems.size})",
                                    fontSize = 18.sp, // 加大按鈕文字
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            if (cartItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("購物車是空的", color = Color.Gray, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { navController.popBackStack() }) {
                            Text("去逛逛")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color(0xFFF5F5F5)),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cartItems, key = { it.id }) { item ->
                        CartItemRow(item = item, onDelete = { viewModel.removeItem(item.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(item: CartItem, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 商品圖片
            if (item.productImage.isNotEmpty()) {
                // 處理 Base64 或網址圖片
                val painter = rememberAsyncImagePainter(model = item.productImage)
                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.LightGray)
                )
            } else {
                // 無圖片時的佔位符
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("無圖片", fontSize = 10.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 商品資訊
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "NT$ ${item.productPrice}",
                    fontSize = 16.sp,
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "數量: ${item.quantity}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // 刪除按鈕
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "刪除",
                    tint = Color.Gray
                )
            }
        }
    }
}