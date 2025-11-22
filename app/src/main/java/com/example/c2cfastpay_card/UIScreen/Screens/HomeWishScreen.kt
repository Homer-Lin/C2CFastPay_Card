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
import com.example.c2cfastpay_card.UIScreen.components.WishRepository
import com.example.c2cfastpay_card.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishPreviewPage(
    navController: NavController
) {
    val context = LocalContext.current
    val wishRepository = remember { WishRepository(context) }

    var searchQuery by remember { mutableStateOf("") }

    // 使用 Flow 監聽資料
    val wishList by wishRepository.getWishListFlow(searchQuery = searchQuery)
        .collectAsState(initial = emptyList())

    val primaryColor = Color(0xFFFBC02D) // 許願牆主題色 (黃色系)

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
                painter = painterResource(R.drawable.background_of_wishing_page),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .align(Alignment.TopCenter)
            )

            // --- 中層：頁面內容 ---

            // SALE 按鈕 (位置與 HomeSaleScreen 保持對稱)
            IconButton(
                onClick = { navController.navigate(Screen.Sale.route) },
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.TopCenter)
                    .offset(x = (-62).dp, y = 100.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.sale_button02),
                    contentDescription = "SALE",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            // WISH 按鈕
            IconButton(
                onClick = { /* 當前頁面 */ },
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = 88.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.wish_button02),
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
                    .height(45.dp) // 高度設定與 HomeSaleScreen 一致
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    // 【修正】明確指定參數名稱，解決 "Cannot infer type" 錯誤
                    onValueChange = { newText: String -> searchQuery = newText },
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
                    // 【修正】移除了不支援的 contentPadding 參數
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

            // 許願列表
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 220.dp), // 避開上方搜尋列
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(
                    items = wishList,
                    key = { wish -> wish.uuid }
                ) { wish ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                // 未來可導航到許願詳情
                            },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF0DF)),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            if (wish.imageUri.isNotEmpty()) {
                                // 直接使用 Coil 載入網址
                                Image(
                                    painter = rememberAsyncImagePainter(model = wish.imageUri),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .padding(end = 12.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(wish.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "交易方式：${wish.payment}",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Text("價格：${wish.price}", fontSize = 16.sp, color = Color.Red)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val wishUuid = wish.uuid
                                        navController.navigate("add_product?wishUuid=$wishUuid")
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFF9800)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("快速上架")
                                }
                            }
                        }
                    }
                }
            }

            // --- 頂層：透明標題列與購物車 (Layer On Top) ---
            TopAppBar(
                title = { }, // 留空
                navigationIcon = {},
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Cart.route) }) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "購物車",
                            tint = Color.White // 白色圖示，確保在背景圖上清晰可見
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