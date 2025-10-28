package com.example.c2cfastpay_card.UIScreen.components // 確保 package 名稱正確

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset // 保留 Offset，因為 LazySwipeableCards 可能會用到
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
// import com.example.c2cfastpay_card.R // 移除 R 的 import，不再需要背景圖
// import com.example.c2cfastpay_card.data.CardData // 移除 CardData import
// import com.example.c2cfastpay_card.data.sampleData // 移除 sampleData import
// import com.example.c2cfastpay_card.ui.theme.C2CFastPay_CardTheme // 移除 Theme import
// import com.example.c2cfastpay_card.utils.rememberDominantColor // 移除 dominant color import (暫時不用)

@Composable
fun CardItem(
    modifier: Modifier = Modifier,
    // index: Int, // 不再需要 index
    // cardData: CardData, // 改為接收 ProductItem
    product: ProductItem, // <-- 修改：接收 ProductItem
    offset: Offset, // 保留 offset，LazySwipeableCards 會傳遞
) {
    Card(
        modifier = modifier
            .fillMaxSize(), // 讓 Card 填滿 LazySwipeableCards 提供的空間
        shape = RoundedCornerShape(16.dp), // 增加圓角
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        // val dominantColor = rememberDominantColor(imageRes = cardData.image) // 移除

        Box (modifier = Modifier.fillMaxSize()){
            // 移除背景圖 R.drawable.b_14_business_card_front_page

            // --- 使用 Coil 載入 ProductItem 的 imageUri ---
            Image(
                painter = rememberAsyncImagePainter(model = product.imageUri), // 使用 Coil
                contentDescription = product.title,
                contentScale = ContentScale.Crop, // 裁剪以填滿
                modifier = Modifier.fillMaxSize() // 填滿 Card
            )

            // --- 漸層遮罩 (讓文字更易讀) ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = Float.POSITIVE_INFINITY, // 從底部開始
                            endY = 0f // 到頂部
                        ),
                        // 從底部大概 1/3 處開始漸層
                        //startY = size.height * 0.6f // 需要 BoxWithConstraints 取得高度，暫時簡化
                    )
            )

            // --- 文字內容 ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp), // 在卡片內部加入邊距
                horizontalAlignment = Alignment.Start, // 文字靠左
                verticalArrangement = Arrangement.Bottom // 文字置底
            ) {

                // --- 顯示 ProductItem 資料 ---
                Text(
                    text = product.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White, // 文字改為白色
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp)) // 間距

                Text(
                    text = "NT$ ${product.price}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f) // 白色稍微透明
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = product.description.ifEmpty { "無商品描述" }, // 來自 ProductItem
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f), // 白色更透明
                    maxLines = 3, // 最多顯示 3 行
                    overflow = TextOverflow.Ellipsis // 超出部分顯示 ...
                )
            }
        }
    }
}


// @Preview 暫時移除或註解掉，因為需要 ProductItem 實例
// @Preview(showBackground = true)
// @Composable
// fun CardItemPreview() {
//    C2CFastPay_CardTheme {
//        CardItem(
//            product = ProductItem("範例商品", "這是描述", "", "100", "", "", "", ""),
//            offset = Offset.Zero
//        )
//    }
// }