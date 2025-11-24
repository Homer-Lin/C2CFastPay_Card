package com.example.c2cfastpay_card.UIScreen.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.c2cfastpay_card.data.CartItem
import com.example.c2cfastpay_card.UIScreen.components.CartRepository
import com.example.c2cfastpay_card.ui.theme.SaleColorScheme
import androidx.compose.material.icons.filled.ShoppingCart
import com.example.c2cfastpay_card.navigation.Screen

// 定義主題色 (深綠色)
val PrimaryGreen = Color(0xFF487F81)

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

    // 計算已選數量
    val selectedCount = cartItems.count { it.isChecked }

    MaterialTheme(colorScheme = SaleColorScheme) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("購物車 (${cartItems.size})", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.White
                    )
                )
            },
            bottomBar = {
                if (cartItems.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 16.dp,
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .navigationBarsPadding(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "總金額",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "NT$ $totalPrice",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black // 參考圖是黑色或深色
                                )
                            }

                            // 結帳按鈕 (只有選取商品時才啟用)
                            Button(
                                onClick = { viewModel.checkout() },
                                enabled = selectedCount > 0,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryGreen,
                                    disabledContainerColor = Color.LightGray
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = "去結帳 ($selectedCount)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            if (cartItems.isEmpty()) {
                // --- 空購物車畫面 (修正版) ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color.White), // 背景全白
                    verticalArrangement = Arrangement.Center, // 垂直置中
                    horizontalAlignment = Alignment.CenterHorizontally // 水平置中
                ) {
                    // 加一個大圖示，讓畫面不會太空
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = Color(0xFFE0E0E0) // 淺灰色圖示
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 文字 (強制設定為黑色，保證看得到)
                    Text(
                        text = "購物車是空的",
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "趕快去挑選喜歡的商品吧！",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 按鈕 (強制設定文字顏色為白色)
                    Button(
                        onClick = {
                            // ★★★ 修改這裡 ★★★
                            // 原本是 navController.popBackStack()
                            // 改成導航去販售首頁 (Screen.Sale.route)
                            navController.navigate(Screen.Sale.route) {
                                // 這行設定的意思是：跳轉後，把堆疊清空直到 Sale 頁面
                                // 這樣使用者按手機「返回鍵」時，不會又回到這個空的購物車頁面
                                popUpTo(Screen.Sale.route) { inclusive = true }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "去逛逛",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color(0xFFFAFAFA)), // 極淡的灰色背景
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(cartItems, key = { it.id }) { item ->
                        CartItemRow(
                            item = item,
                            // ★★★ 修正 1：這裡要傳 item (物件)，不要傳 item.id ★★★
                            onToggleCheck = { viewModel.toggleItemChecked(item) },

                            // ★★★ 修正 2：這兩個通常也需要 item (取決於你的 ViewModel 定義) ★★★
                            onIncrease = { viewModel.increaseQuantity(item) },
                            onDecrease = { viewModel.decreaseQuantity(item) },

                            // ★★★ 修正 3：刪除通常只需要 ID，所以這裡維持 item.id 沒錯 ★★★
                            onDelete = { viewModel.removeItem(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onToggleCheck: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp), // 平面化設計
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)) // 淡邊框
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. 勾選框
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onToggleCheck() },
                colors = CheckboxDefaults.colors(
                    checkedColor = PrimaryGreen,
                    uncheckedColor = Color.LightGray
                )
            )

            // 2. 商品圖片
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF0F0F0))
            ) {
                if (item.productImage.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(model = item.productImage),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // 無圖片
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No Img", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 3. 商品資訊與控制
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(80.dp), // 固定高度讓排版整齊
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 標題與刪除鈕 (Row)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = item.productTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = Color(0xFF333333),
                        modifier = Modifier.weight(1f)
                    )

                    // 垃圾桶放在右上角
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "刪除",
                        tint = Color.LightGray,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onDelete() }
                    )
                }

                // 價格與數量控制 (Row)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // 價格
                    Text(
                        text = "NT$ ${item.productPrice}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    // 數量控制器 [- 1 +]
                    QuantitySelector(
                        quantity = item.quantity,
                        onIncrease = onIncrease,
                        onDecrease = onDecrease,
                        // 如果數量達到庫存上限，加號變灰
                        isMaxReached = item.quantity >= item.stock
                    )
                }
            }
        }
    }
}

// 獨立出來的數量控制器元件 (美化版)
@Composable
fun QuantitySelector(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    isMaxReached: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
            .height(28.dp)
    ) {
        // 減號
        IconButton(
            onClick = onDecrease,
            modifier = Modifier.size(28.dp),
            enabled = quantity > 1
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "減少",
                modifier = Modifier.size(14.dp),
                tint = if (quantity > 1) Color.Gray else Color.LightGray
            )
        }

        // 數字
        Box(
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight()
                .border(width = 1.dp, color = Color(0xFFF0F0F0)), // 中間分隔線效果(模擬)
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = quantity.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // 加號
        IconButton(
            onClick = onIncrease,
            modifier = Modifier.size(28.dp),
            enabled = !isMaxReached
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "增加",
                modifier = Modifier.size(14.dp),
                tint = if (!isMaxReached) PrimaryGreen else Color.LightGray
            )
        }
    }
}