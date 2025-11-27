package com.example.c2cfastpay_card.UIScreen.Screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.ShoppingCart
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
    val isLoading by viewModel.isLoading.collectAsState()

    // 計算已選數量
    val selectedCount = cartItems.count { it.isChecked }

    // 監聽結帳結果 Toast
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

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
                                    color = Color.Black
                                )
                            }

                            // 結帳按鈕
                            Button(
                                onClick = { viewModel.checkout() },
                                enabled = selectedCount > 0 && !isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryGreen,
                                    disabledContainerColor = Color.LightGray
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
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
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                if (cartItems.isEmpty()) {
                    // --- 空購物車畫面 ---
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp),
                            tint = Color(0xFFE0E0E0)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

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

                        Button(
                            onClick = {
                                navController.navigate(Screen.Sale.route) {
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
                    // --- 購物車列表 ---
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFFAFAFA)),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(cartItems, key = { it.id }) { item ->
                            CartItemRow(
                                item = item,
                                onToggleCheck = { viewModel.toggleItemChecked(item) },
                                onIncrease = { viewModel.increaseQuantity(item) },
                                onDecrease = { viewModel.decreaseQuantity(item) },
                                onDelete = { viewModel.removeItem(item.id) }
                            )
                        }
                    }
                }

                // --- 全螢幕 Loading 遮罩 ---
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                            .clickable(enabled = false) {},
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
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
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            // 【修正 1】改為頂部對齊，讓內容可以向下延伸
            verticalAlignment = Alignment.Top
        ) {
            // 1. 勾選框 (保持置中)
            Box(
                modifier = Modifier
                    .height(80.dp) // 與圖片同高，讓 Checkbox 垂直置中
                    .wrapContentWidth(),
                contentAlignment = Alignment.Center
            ) {
                Checkbox(
                    checked = item.isChecked,
                    onCheckedChange = { onToggleCheck() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PrimaryGreen,
                        uncheckedColor = Color.LightGray
                    )
                )
            }

            // 2. 商品圖片 (固定大小)
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
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No Img", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 3. 商品資訊與控制
            Column(
                modifier = Modifier
                    .weight(1f), // 佔滿剩餘寬度
                // .height(80.dp) // 【修正 2】移除固定高度限制
                verticalArrangement = Arrangement.spacedBy(8.dp) // 【修正 3】增加間距
            ) {
                // 標題與刪除鈕
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
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    )

                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "刪除",
                        tint = Color.LightGray,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onDelete() }
                    )
                }

                // 價格與數量控制
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NT$ ${item.productPrice}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    QuantitySelector(
                        quantity = item.quantity,
                        onIncrease = onIncrease,
                        onDecrease = onDecrease,
                        isMaxReached = item.quantity >= item.stock
                    )
                }
            }
        }
    }
}

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

        Box(
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight()
                .border(width = 1.dp, color = Color(0xFFF0F0F0)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = quantity.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

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