package com.example.c2cfastpay_card.UIScreen.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.example.c2cfastpay_card.data.CardData
import com.spartapps.swipeablecards.state.rememberSwipeableCardsState
import com.spartapps.swipeablecards.ui.lazy.LazySwipeableCards
import com.spartapps.swipeablecards.ui.lazy.items


// 假設您的 drawable 資源都存在於 R.drawable 中
import com.example.c2cfastpay_card.R
import com.example.c2cfastpay_card.UIScreen.components.CardItem
import com.example.c2cfastpay_card.navigation.Screen

@Composable
fun CardStack(
    data: List<CardData>,
    navController: NavController, // <-- 3. 加入 NavController 參數
    modifier: Modifier = Modifier,  // <-- 4. 讓 modifier 成為可選的 (修正錯誤)
) {
    var indexInput by remember { mutableStateOf(0.toString()) }
    val state = rememberSwipeableCardsState(
        itemCount = { data.size }
    )

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        // 建立對應 XML id 的 references
        val (
            imageView29, imageButton37, textView32, imageButton38,
            imageView37, button13, button14, imageButton39,
            textView33, imageButton44, textView44, textView45,
            textView46, textView47 ,cardDeck
        ) = createRefs()
        Image(
            painter = painterResource(id = R.drawable.banner_in_choose_photo),
            contentDescription = "Banner",
            modifier = Modifier.constrainAs(imageView29) {
                bottom.linkTo(parent.bottom, margin = 556.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.wrapContent
                height = Dimension.wrapContent
            }
        )
        IconButton(
            onClick = {navController.popBackStack() },
            modifier = Modifier
                .size(50.dp)
                .constrainAs(imageButton37) {
                    start.linkTo(parent.start, margin = 8.dp)
                    top.linkTo(parent.top, margin = 34.dp)
                },
            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent)
        ) {
            Image(
                painter = painterResource(id = R.drawable.a_1_back_buttom), // 替換為您的 R.drawable
                contentDescription = "Back"
            )
        }
        Text(
            text = "商品名片配對",
            color = Color(0xFF759E9F),
            fontSize = 20.sp,
            modifier = Modifier.constrainAs(textView32) {
                start.linkTo(imageButton37.end, margin = 8.dp)
                top.linkTo(parent.top, margin = 44.dp)
            }
        )
        IconButton(
            onClick = { /* TODO: 幫助操作 */ },
            modifier = Modifier
                .size(50.dp)
                .constrainAs(imageButton38) {
                    end.linkTo(parent.end, margin = 8.dp)
                    top.linkTo(parent.top, margin = 36.dp)
                },
            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent)
        ) {
            Image(
                painter = painterResource(id = R.drawable.b_13_help), // 替換為您的 R.drawable
                modifier = Modifier.size(30.dp),
                contentDescription = "Help",
            )
        }

        Column(
            modifier = Modifier.constrainAs(cardDeck) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
                height = Dimension.fillToConstraints
            },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            //滑動時做的事情
            LazySwipeableCards(
                modifier = Modifier.padding(20.dp),
                state = state,
                onSwipe = { item, direction ->
                    Log.d("CardsScreen", "onSwipe: $item, $direction")
                },
            ) {
                items(data) { item, index, offset ->
                    CardItem(
                        index = index,
                        cardData = item,
                        offset = offset
                    )
                }
            }
        }
        TextButton(
            onClick = {navController.navigate(Screen.History.route)}, // <-- 3. 加入 onClick 動作
            modifier = Modifier.constrainAs(textView33) {
                // 4. 約束和之前一樣
                start.linkTo(parent.start, margin = 16.dp)
                bottom.linkTo(parent.bottom, margin = 36.dp)
            }
        ) {
            Text(
                text = "歷史紀錄",
                color = Color(0xFF759E9F)
            )
        }
    }
}


