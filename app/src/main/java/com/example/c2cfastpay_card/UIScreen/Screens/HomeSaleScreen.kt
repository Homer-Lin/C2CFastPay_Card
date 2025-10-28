package com.example.c2cfastpay_card.UIScreen.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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

import androidx.core.net.toUri
import com.example.c2cfastpay_card.UIScreen.components.ProductItem
import kotlinx.coroutines.launch
import androidx.compose.material3.Scaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleProductPage(
    navController: NavController
) {
    val context = LocalContext.current
    val productRepository = remember { ProductRepository(context) }
    var productList by remember { mutableStateOf<List<ProductItem>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            productList = productRepository.getProductList()
        }
    }
    Scaffold(
        bottomBar = {
            // 4. 在 bottomBar 插槽中呼叫您的導覽列
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding -> // 5. Scaffold 會提供 innerPadding
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
                onClick = { /* TODO: 導航到購物車 */ },
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
                onClick = { /* TODO: 導航到特價 */ },
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
                    onClick = { /* TODO: 執行搜尋 */ },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.img_7),
                        contentDescription = "Toggle View"
                    )
                }
            }

            // 商品列表，依照 HomeSaleScreen.kt 呈現
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
                            .padding(vertical = 4.dp),
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
                                    painter = rememberAsyncImagePainter(product.imageUri.toUri()),
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
