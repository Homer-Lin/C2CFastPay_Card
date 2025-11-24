package com.example.c2cfastpay_card.UIScreen.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.c2cfastpay_card.UIScreen.components.WishRepository
import com.example.c2cfastpay_card.model.WishItem // 假設你的 Model 在這
import kotlinx.coroutines.launch

// --- 許願牆主題色 (黃/橘系) ---
val WishYellowAccent = Color(0xFFFFF176) // 淺黃標題底
val WishOrangeDark = Color(0xFFFF9800)   // 橘色按鈕
val WishTextBlack = Color(0xFF191C1C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishDetailScreen(
    navController: NavController,
    wishUuid: String // 接收參數
) {
    val context = LocalContext.current
    val wishRepository = remember { WishRepository(context) }
    var wishItem by remember { mutableStateOf<WishItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // 載入資料
    LaunchedEffect(wishUuid) {
        wishItem = wishRepository.getWishByUuid(wishUuid)
        isLoading = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("願望詳情", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (wishItem != null) {
                BottomAppBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    // 快速上架按鈕
                    Button(
                        onClick = {
                            // 帶著 UUID 跳轉到 AddProductScreen
                            navController.navigate("add_product?wishUuid=${wishItem!!.uuid}")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WishOrangeDark),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(50.dp)
                    ) {
                        Text("我有這個商品！快速上架", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = WishOrangeDark)
            }
        } else {
            val item = wishItem
            if (item != null) {
                // --- 準備變數 (若無資料顯示預設值) ---
                val displayDesc = item.description.ifBlank { "沒有詳細描述。" }
                val displayLogistics = item.payment.ifBlank { "皆可" } // 物流
                val displayCondition = item.condition.ifBlank { "不拘" }
                val displayQty = item.qty.ifBlank { "1" }
                val displayNote = item.memo.ifBlank { "無備註" } // 備註

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color.White)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // 1. 圖片 (參考圖片)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.3f),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        if (item.imageUri.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(model = item.imageUri),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize().background(Color(0xFFFFF8E1)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("無參考圖片", color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // 2. 標題
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .width(200.dp)
                                .height(16.dp)
                                .offset(y = 10.dp)
                                .background(WishYellowAccent, RoundedCornerShape(50))
                        )
                        Text(
                            text = item.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = WishTextBlack,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // 3. 詳細資訊 (兩欄配置)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // 左欄：預算、物流
                        Column(modifier = Modifier.weight(1f)) {
                            WishSectionTitle("預算")
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = item.price,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WishTextBlack
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            WishSectionTitle("物流方式")
                            Text(text = displayLogistics, fontSize = 14.sp, color = Color.DarkGray)
                        }

                        Spacer(modifier = Modifier.width(24.dp))

                        // 右欄：需求規格 (數量、狀態)
                        Column(modifier = Modifier.weight(1f)) {
                            WishSectionTitle("需求規格")
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                WishSpecText(label = "欲購數量", value = displayQty)
                                WishSpecText(label = "接受狀態", value = displayCondition)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(32.dp))

                    // 4. 願望描述 (文案)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        WishSectionTitle("願望描述")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = displayDesc,
                            fontSize = 15.sp,
                            color = Color.DarkGray,
                            lineHeight = 22.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // 5. 備註
                    if (displayNote.isNotBlank() && displayNote != "無備註") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            WishSectionTitle("備註")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = displayNote,
                                fontSize = 14.sp,
                                color = Color.Gray,
                                lineHeight = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("找不到該願望")
                }
            }
        }
    }
}

// --- 許願專用樣式元件 ---
@Composable
fun WishSectionTitle(text: String) {
    Box(modifier = Modifier.padding(bottom = 8.dp), contentAlignment = Alignment.CenterStart) {
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(10.dp)
                .offset(y = 6.dp)
                .background(WishYellowAccent, RoundedCornerShape(4.dp))
        )
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = WishTextBlack)
    }
}

@Composable
fun WishSpecText(label: String, value: String) {
    Row {
        Text(text = "$label : ", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 13.sp, color = Color.DarkGray)
    }
}