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
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.c2cfastpay_card.UIScreen.components.ProductItem
import com.example.c2cfastpay_card.UIScreen.components.ProductRepository
import com.example.c2cfastpay_card.ui.theme.SaleColorScheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProductsScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { ProductRepository(context) }
    val scope = rememberCoroutineScope()

    var myProducts by remember { mutableStateOf<List<ProductItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 重新載入資料的函式
    fun loadData() {
        scope.launch {
            isLoading = true
            try {
                // 嘗試讀取
                myProducts = repository.getMyProducts()
            } catch (e: Exception) {
                // 【新增】捕捉錯誤，防止崩潰
                android.util.Log.e("MyProductsScreen", "讀取失敗", e)
                // 這裡也可以用 Toast 提示使用者
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    MaterialTheme(colorScheme = SaleColorScheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("我的商品管理") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        ) { innerPadding ->
            if (isLoading) {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (myProducts.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text("您還沒有上架任何商品", color = Color.Gray)
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
                    items(myProducts, key = { it.id }) { product ->
                        MyProductItemRow(
                            product = product,
                            onDelete = {
                                scope.launch {
                                    repository.deleteProduct(product.id)
                                    loadData() // 刪除後重新整理
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MyProductItemRow(product: ProductItem, onDelete: () -> Unit) {
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
            if (product.imageUri.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(model = product.imageUri),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.LightGray)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "NT$ ${product.price}",
                    fontSize = 15.sp,
                    color = Color(0xFFD32F2F)
                )
                Text(
                    text = "上架時間: ${java.text.SimpleDateFormat("MM/dd", java.util.Locale.getDefault()).format(java.util.Date(product.timestamp))}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
            }
        }
    }
}