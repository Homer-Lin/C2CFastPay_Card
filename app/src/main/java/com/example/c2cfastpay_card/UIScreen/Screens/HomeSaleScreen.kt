package com.example.c2cfastpay_card.UIScreen.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.c2cfastpay_card.R
import com.example.c2cfastpay_card.UIScreen.components.BottomNavigationBar
import com.example.c2cfastpay_card.UIScreen.components.ProductRepository
import com.example.c2cfastpay_card.navigation.Screen
import com.example.c2cfastpay_card.UIScreen.components.ProductItem // 確保有這個 import

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleProductPage(
    navController: NavController
) {
    val context = LocalContext.current
    val productRepository = remember { ProductRepository(context) }

    // --- 【修正 1】改用 collectAsState 監聽 Firestore 資料流 ---
    // 這樣當有人上架新商品時，畫面會自動更新
    val productList by productRepository.getAllProducts()
        .collectAsState(initial = emptyList())
    // -------------------------------------------------------

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            // 背景圖
            Image(
                painter = painterResource(R.drawable.backgroud_of_selling_page),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            )

            // 購物車按鈕
            IconButton(
                onClick = { navController.navigate(Screen.Cart.route) },
                modifier = Modifier
                    .size(35.dp)
                    .align(Alignment.TopEnd)
                    .padding(top = 56.dp, end = 16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.cart_button),
                    contentDescription = "Cart"
                )
            }

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

            // 搜尋列 + 切換圖示
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 150.dp)
                    .fillMaxWidth(0.9f)
                    .height(50.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.search_bar),
                    contentDescription = null,
                    contentScale = ContentScale.FillHeight,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                IconButton(
                    onClick = { /* TODO: 執行搜尋 */ },
                    modifier = Modifier
                        .size(32.dp)
                        .offset(x = (-40).dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.search_button),
                        contentDescription = "Search"
                    )
                }
                Spacer(Modifier.width(12.dp))
                IconButton(
                    onClick = { /* TODO: 切換視圖 */ },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.img_7),
                        contentDescription = "Toggle View"
                    )
                }
            }

            // 商品列表
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 200.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(productList) { product ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            // 【新增】點擊導航到詳情頁
                            .clickable {
                                navController.navigate("product_detail/${product.id}")
                            },
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (product.imageUri.isNotEmpty()) {
                                // --- 【修正 2】直接載入 Storage 網址 ---
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
        }
    }
}