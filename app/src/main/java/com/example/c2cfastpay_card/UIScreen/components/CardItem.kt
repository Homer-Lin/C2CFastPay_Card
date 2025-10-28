package com.example.c2cfastpay_card.UIScreen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.c2cfastpay_card.R
import com.example.c2cfastpay_card.data.CardData
import com.example.c2cfastpay_card.data.sampleData
import com.example.c2cfastpay_card.ui.theme.C2CFastPay_CardTheme
import com.example.c2cfastpay_card.utils.rememberDominantColor

@Composable
fun CardItem(
    modifier: Modifier = Modifier,
    index: Int,
    cardData: CardData,
    offset: Offset,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(500.dp),
        //border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp) // 加點陰影
    ) {
        val dominantColor = rememberDominantColor(imageRes = cardData.image)

        Box (modifier = Modifier.fillMaxSize()){
            Image(
                painter = painterResource(id = R.drawable.b_14_business_card_front_page),
                contentDescription = "Card Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Spacer(modifier = Modifier.height(20.dp))
            // --- 這是您的內容物 (商品圖和文字) ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp), // 在卡片內部加入邊距
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top // 讓內容物從頂部開始排列
            ) {

                // 3. 您的「物品照片」
                // (這對應到您 XML 中的 imageButton44)
                Image(
                    painter = painterResource(id = cardData.image), // 來自 CardData
                    contentDescription = cardData.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(300.dp) // 物品照片大小 (同 XML)
                        .clip(RoundedCornerShape(12.dp)) // 讓照片有點圓角
                )

                Spacer(modifier = Modifier.height(20.dp)) // 圖片和文字之間的間距

                // 4. 您的「物品名稱」
                // (這對應到您 XML 中的 textView44)
                Text(
                    text = cardData.title, // 來自 CardData
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF070707) // 使用 XML 中的深色
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 5. 您的「物品描述」 (例如 "BRUNO")
                // (這對應到您 XML 中的 textView45)
//                Text(
//                    text = cardData.description, // 來自 CardData
//                    fontSize = 16.sp,
//                    color = Color.DarkGray
//                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CardItemPreview() {
    C2CFastPay_CardTheme {
        CardItem(
            index = 0,
            cardData = sampleData.first(),
            offset = Offset.Zero
        )
    }
}