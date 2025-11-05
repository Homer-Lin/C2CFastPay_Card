package com.example.c2cfastpay_card.UIScreen.Screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.c2cfastpay_card.R
import com.example.c2cfastpay_card.UIScreen.components.ProductItem
import com.example.c2cfastpay_card.UIScreen.components.ProductRepository
import com.example.c2cfastpay_card.UIScreen.components.WishRepository
import com.example.c2cfastpay_card.navigation.Screen

// --- 修改：重新加入 LazySwipeableCards 相關的 import ---
import com.spartapps.swipeablecards.state.rememberSwipeableCardsState
import com.spartapps.swipeablecards.ui.lazy.LazySwipeableCards
import com.spartapps.swipeablecards.ui.lazy.items
import com.example.c2cfastpay_card.UIScreen.components.CardItem // <-- 確保 import 我們修改過的 CardItem
// --- 修改結束 ---


@Composable
fun CardStackScreen( // 外層 Composable，管理 ViewModel
    navController: NavController
) {
    val context = LocalContext.current
    val productRepository = remember { ProductRepository(context) }
    val viewModel: CardStackViewModel = viewModel(
        factory = CardStackViewModelFactory(productRepository) // <-- 只傳入 productRepository
    )
    val cardsToShow by viewModel.cards.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Log.d("CardStackDebug", "CardStackScreen recomposing. isLoading: $isLoading, cardsToShow: ${cardsToShow.size}")

    LaunchedEffect(Unit) {
        Log.d("CardStackDebug", "LaunchedEffect triggered. Calling loadPotentialMatches()")
        viewModel.loadPotentialMatches()
    }

    CardStackLayout(
        navController = navController,
        items = cardsToShow,
        isLoading = isLoading
    )
}

// 實際的佈局 Composable
@Composable
fun CardStackLayout(
    navController: NavController,
    items: List<ProductItem>,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    // --- 修改：將 state 重新加回來 ---
//    val state = rememberSwipeableCardsState(
//        itemCount = { items.size }
//    )
    // --- 修改結束 ---

    ConstraintLayout(modifier = modifier.fillMaxSize()) {
        val (
            imageView29, imageButton37, textView32, imageButton38,
            cardDeck,
            textView33
        ) = createRefs()

        // --- 頂部 UI (Banner + 按鈕 + 標題) (保持不變) ---
        Image(
            painter = painterResource(id = R.drawable.banner_in_choose_photo), //
            contentDescription = "Banner",
            modifier = Modifier.constrainAs(imageView29) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
                height = Dimension.wrapContent
            },
            contentScale = ContentScale.FillWidth
        )
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.size(50.dp).constrainAs(imageButton37) {
                start.linkTo(parent.start, margin = 8.dp)
                top.linkTo(parent.top, margin = 34.dp)
            },
            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent)
        ) {
            Image(
                painter = painterResource(id = R.drawable.a_1_back_buttom), //
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
            modifier = Modifier.size(50.dp).constrainAs(imageButton38) {
                end.linkTo(parent.end, margin = 8.dp)
                top.linkTo(parent.top, margin = 36.dp)
            },
            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent)
        ) {
            Image(
                painter = painterResource(id = R.drawable.b_13_help), //
                modifier = Modifier.size(30.dp),
                contentDescription = "Help",
            )
        }
        // --- 頂部 UI 結束 ---


        // --- 中央內容區 (還原為 LazySwipeableCards) ---
        Box(
            modifier = Modifier
                .constrainAs(cardDeck) {
                    top.linkTo(imageView29.bottom, margin = 20.dp)
                    bottom.linkTo(textView33.top, margin = 20.dp)
                    start.linkTo(parent.start, margin = 32.dp)
                    end.linkTo(parent.end, margin = 32.dp)
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                }.
                aspectRatio(0.75f) // (寬高比 3:4，您可以調整 0.7f, 0.8f 等)
                ,
            contentAlignment = Alignment.Center
        ) {
            Log.d("CardStackDebug", "CardStackLayout recomposing inside Box. isLoading: $isLoading")
            if (isLoading) {
                Log.d("CardStackDebug", "Displaying CircularProgressIndicator")
                CircularProgressIndicator()
            } else {
                Log.d("CardStackDebug", "Displaying Cards or Empty Text. Item count: ${items.size}")

                // --- 修改：還原 LazySwipeableCards ---
                if (items.isNotEmpty()) {
                    val state = rememberSwipeableCardsState(
                        itemCount = { items.size }
                    )
                    LazySwipeableCards(
                        state = state,
                        onSwipe = { index, direction ->
                            // index 是被滑掉的項目在 items 列表中的索引
                            Log.d("CardsScreen", "onSwipe: index $index, direction $direction")
                            // TODO: 階段三處理 swipeRight / swipeLeft
                        },
                    ) {
                        items(items) { product, index, offset -> // <-- 直接傳入 items 列表
                            CardItem(
                                product = product,
                                offset = offset
                            )
                        }
                    }
                } else {
                    Text(
                        text = "目前沒有任何上架的商品",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
                // --- 修改結束 ---
            }
        }
        // --- 中央內容區結束 ---


        // --- 底部按鈕 (保持不變) ---
        TextButton(
            onClick = { navController.navigate(Screen.History.route) }, //
            modifier = Modifier.constrainAs(textView33) {
                start.linkTo(parent.start, margin = 16.dp)
                bottom.linkTo(parent.bottom, margin = 36.dp)
            }
        ) {
            Text(
                text = "歷史紀錄",
                color = Color(0xFF759E9F)
            )
        }
        // --- 底部按鈕結束 ---
    }
}