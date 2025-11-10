package com.example.c2cfastpay_card.UIScreen.components

// --- 【1. 新增必要的 import】 ---
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // 【確保 import】
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll // 【確保 import】
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip // 【新增】
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer // 【確保 import】
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource // 【新增】
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.c2cfastpay_card.R // 【新增】 為了 R.drawable...

@OptIn(ExperimentalMaterial3Api::class) // 為了 ExposedDropdownMenuBox
@Composable
fun CardItem(
    modifier: Modifier = Modifier,
    product: ProductItem,
    offset: Offset,
) {
    // --- (翻轉動畫狀態，保持不變) ---
    var isFlipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        label = "rotation"
    )

    Card(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 8 * density
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {

        if (rotation <= 90f) {
            // --- 這是卡片正面 ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { isFlipped = true } // 點擊正面
            ) {
                // (您原本的正面程式碼，保持不變)
                Image(
                    painter = rememberAsyncImagePainter(model = product.imageUri),
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                startY = Float.POSITIVE_INFINITY,
                                endY = 0f
                            ),
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = product.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "NT$ ${product.price}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.description.ifEmpty { "無商品描述" },
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            // --- 卡片正面結束 ---

        } else {

            // --- 【6. 這是卡片背面 (美編 + 內縮版)】 ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f } // 鏡像翻轉
                    .clickable { isFlipped = false } // 點擊背面
            ) {
                // 【美編】背景圖
                Image(
                    painter = painterResource(R.drawable.b_14_business_card_front_page), //
                    contentDescription = "卡片背面",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // 【美編】使用 Box 將內容推到底部
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // 【您的要求】在四周加入 padding，讓白底內縮
                        .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    contentAlignment = Alignment.BottomCenter // 將「白底」對齊底部
                ) {
                    // --- 【固定高度的白底】 ---
                    Column(
                        modifier = Modifier
                            // 【修改】現在它會填滿 'padding' 後的寬度
                            .fillMaxWidth()
                            .height(370.dp)
                            // 【美編】讓四個角都有圓角
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.95f))
                    ) {
                        // --- 【內部可滾動的內容】 ---
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(20.dp)
                        ) {
                            Text(
                                text = product.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "NT$ ${product.price}",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF6A0000) // 深紅色
                            )

                            Divider(modifier = Modifier.padding(vertical = 16.dp))

                            // 顯示商品描述
                            DetailRow(title = "商品描述", content = product.description.ifEmpty { "(無描述)" })

                            // 顯示商品規格
                            DetailRow(title = "商品規格", content = product.specs.ifEmpty { "(無規格)" })

                            // 顯示注意事項
                            DetailRow(title = "注意事項", content = product.notes.ifEmpty { "(無)" })

                            // 顯示其他資訊
                            DetailRow(title = "其他資訊", content = product.other.ifEmpty { "(無)" })

                            Spacer(modifier = Modifier.height(20.dp)) // 滾到底部的緩衝
                        }
                    }
                }
            }
        }
    }
}

// 【美編】共用的 Composable
@Composable
private fun DetailRow(title: String, content: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color.Black
    )
    Text(
        text = content,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        color = Color.DarkGray // 內容使用深灰色
    )
}