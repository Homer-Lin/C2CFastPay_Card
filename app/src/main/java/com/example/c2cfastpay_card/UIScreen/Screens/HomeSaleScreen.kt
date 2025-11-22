package com.example.c2cfastpay_card.UIScreen.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.c2cfastpay_card.R
import com.example.c2cfastpay_card.UIScreen.components.BottomNavigationBar
import com.example.c2cfastpay_card.UIScreen.components.ProductRepository
import com.example.c2cfastpay_card.navigation.Screen
import com.example.c2cfastpay_card.UIScreen.components.ProductItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleProductPage(
    navController: NavController
) {
    val context = LocalContext.current
    val productRepository = remember { ProductRepository(context) }

    var searchQuery by remember { mutableStateOf("") }

    val productList by productRepository.getAllProducts(searchQuery = searchQuery)
        .collectAsState(initial = emptyList())

    val primaryColor = Color(0xFF487F81)

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .background(Color.White)
        ) {
            // --- 底層：背景圖 ---
            Image(
                painter = painterResource(R.drawable.backgroud_of_selling_page),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .align(Alignment.TopCenter)
            )

            // --- 中層：頁面內容 ---

            // SALE 按鈕
            IconButton(
                onClick = { /* 當前頁面 */ },
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = 88.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.sale_button),
                    contentDescription = "SALE",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            // WISH 按鈕
            IconButton(
                onClick = { navController.navigate(Screen.WishList.route) },
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.TopCenter)
                    .offset(x = 62.dp, y = 100.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.wish_button),
                    contentDescription = "WISH",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            // 搜尋列
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 150.dp)
                    .fillMaxWidth(0.9f)
                    .height(50.dp) // 稍微加高一點點以免文字被切，您可以根據需要微調回 45.dp
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    // 【修正 2】明確命名參數，解決 Unresolved reference: it
                    onValueChange = { newText -> searchQuery = newText },
                    label = {
                        Text(
                            "搜尋商品...",
                            style = TextStyle(fontSize = 14.sp)
                        )
                    },
                    textStyle = TextStyle(fontSize = 14.sp),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "搜尋",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.9f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.LightGray,
                    ),
                    shape = RoundedCornerShape(24.dp)
                    // 【修正 1】已移除 contentPadding 參數
                )

                Spacer(Modifier.width(12.dp))

                IconButton(
                    onClick = { /* 切換檢視 */ },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.img_7),
                        contentDescription = "Toggle View",
                        tint = primaryColor
                    )
                }
            }

            // 商品列表
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 220.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(productList) { product ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                navController.navigate("product_detail/${product.id}")
                            },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0EBE8)),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (product.imageUri.isNotEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = product.imageUri),
                                    contentDescription = "商品圖片",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .padding(end = 12.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.title,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "交易方式：${product.payment}",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "價格：${product.price}",
                                    fontSize = 16.sp,
                                    color = Color.Red
                                )
                            }
                        }
                    }
                }
            }

            // --- 頂層：透明標題列與購物車 (Layer On Top) ---
            TopAppBar(
                title = { }, // 留空，不顯示文字
                navigationIcon = {},
                actions = {
                    // 確保 Screen.Cart 已經在您的 Screen.kt 中定義
                    IconButton(onClick = { navController.navigate(Screen.Cart.route) }) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "購物車",
                            tint = primaryColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
            )
        }
    }
}