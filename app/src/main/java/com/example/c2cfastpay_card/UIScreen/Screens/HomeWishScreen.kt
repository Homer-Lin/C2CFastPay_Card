package com.example.c2cfastpay_card.UIScreen.Screens

import android.net.Uri
import android.util.Log
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
import coil.compose.rememberAsyncImagePainter
import com.example.c2cfastpay_card.R
import com.example.c2cfastpay_card.UIScreen.components.BottomNavigationBar
import com.example.c2cfastpay_card.UIScreen.components.WishItem
import com.example.c2cfastpay_card.UIScreen.components.WishRepository
import com.example.c2cfastpay_card.navigation.Screen
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import com.google.gson.Gson
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.platform.LocalLifecycleOwner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishPreviewPage(
    navController: NavController
) {
    val context = LocalContext.current
    val wishRepository = remember { WishRepository(context) }
    var wishList by remember { mutableStateOf<List<WishItem>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        scope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                wishList = wishRepository.getWishList()
            }
        }
    }
    Scaffold(
        bottomBar = {
            // --- 8. 在 bottomBar 插槽中呼叫共用的導覽列 ---
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding -> // --- 9. Scaffold 提供 innerPadding ---

        // --- 10. 將您原本的 Box 放在 Scaffold 內容區塊 ---
        //      並套用 innerPadding
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            // 背景 and tabs (reuse from sale)
            Image(
                painter = painterResource(R.drawable.background_of_wishing_page),
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

            // TabRow: SALE / WISH
            // SALE 按鈕
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
                onClick = { /* 已經在此頁面，不需動作 */ },
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

            // 搜尋列 + 切換
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 150.dp)
                    .fillMaxWidth(0.9f)
                    .height(50.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.search_bar02),
                    contentDescription = null,
                    contentScale = ContentScale.FillHeight,
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight()
                )
                IconButton(
                    onClick = { /* TODO: 執行搜尋 */ },
                    modifier = Modifier
                        .size(32.dp)
                        .offset(x = (-40).dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.search_button02),
                        contentDescription = "Search"
                    )
                }
                Spacer(Modifier.width(12.dp))
                IconButton(
                    onClick = { /* TODO: 切換網格 */ },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.img_7),
                        contentDescription = "Toggle View"
                    )
                }
            }


            // 許願列表預覽
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 200.dp, bottom = 56.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(
                    count = wishList.size,
                    key = { index ->
                        // 2. 根據索引(index)找到 wish，並使用其 id 作為 key
                        wishList[index].uuid //
                    }
                ) { index ->
                    // 3. 取得該索引的 wish 物件
                    val wish = wishList[index]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                // 顯示詳細視窗
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF0DF)),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                            if (wish.imageUri.isNotEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(wish.imageUri.toUri()),
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
                                        val wishUuid = wish.uuid //
                                        Log.d("DataFlowDebug", "步驟 1: 正在導航... Uuid = $wishUuid")
                                        navController.navigate("add_product?wishUuid=$wishUuid")
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(
                                            0xFFFF9800
                                        )
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
        }
    }
}
