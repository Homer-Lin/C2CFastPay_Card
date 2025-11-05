package com.example.c2cfastpay_card.UIScreen.Screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio // 1. 確保 import
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.c2cfastpay_card.UIScreen.components.MatchRepository // 2. 確保 import
import com.example.c2cfastpay_card.navigation.Screen
import com.spartapps.swipeablecards.state.rememberSwipeableCardsState
import com.spartapps.swipeablecards.ui.lazy.LazySwipeableCards
import com.spartapps.swipeablecards.ui.lazy.items
import com.example.c2cfastpay_card.UIScreen.components.CardItem
// 3. 修正 Import：使用您提供的正確路徑
import com.spartapps.swipeablecards.ui.SwipeableCardDirection


@Composable
fun CardStackScreen( // 外層 Composable
    navController: NavController
) {
    val context = LocalContext.current
    // 4. 建立兩個 Repositories
    val productRepository = remember { ProductRepository(context) }
    val matchRepository = remember { MatchRepository(context) }

    // 5. 建立 ViewModel，傳入兩個 Repositories
    val viewModel: CardStackViewModel = viewModel(
        factory = CardStackViewModelFactory(productRepository, matchRepository)
    )

    val cardsToShow by viewModel.cards.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Log.d("CardStackDebug", "CardStackScreen recomposing. isLoading: $isLoading, cardsToShow: ${cardsToShow.size}")

    LaunchedEffect(Unit) {
        Log.d("CardStackDebug", "LaunchedEffect triggered. Calling loadPotentialMatches()")
        viewModel.loadPotentialMatches()
    }

    // 6. 呼叫 CardStackLayout，傳入 viewModel
    CardStackLayout(
        navController = navController,
        items = cardsToShow,
        isLoading = isLoading,
        viewModel = viewModel // <-- 傳入 viewModel
    )
}

// 實際的佈局 Composable
@Composable
fun CardStackLayout(
    navController: NavController,
    items: List<ProductItem>, // <-- 'items' 在這裡被定義
    isLoading: Boolean,
    viewModel: CardStackViewModel, // <-- 接收 viewModel
    modifier: Modifier = Modifier,
) {
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
            }
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
            }
        ) {
            Image(
                painter = painterResource(id = R.drawable.b_13_help), //
                modifier = Modifier.size(30.dp),
                contentDescription = "Help",
            )
        }
        // --- 頂部 UI 結束 ---

        Box(
            modifier = Modifier
                .constrainAs(cardDeck) {
                    top.linkTo(imageView29.bottom, margin = 20.dp)
                    bottom.linkTo(textView33.top, margin = 20.dp)
                    start.linkTo(parent.start, margin = 32.dp)
                    end.linkTo(parent.end, margin = 32.dp)
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                }
                .aspectRatio(0.75f), // 保持卡片長寬比
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                if (items.isNotEmpty()) {

                    // --- 修改開始 ---
                    // 1. 使用 key(items.size) 包住所有東西
                    //    當 items.size 改變時 (例如 3 -> 2)，
                    //    key() { ... } 區塊內的一切都將被強制「重置」。
                    key(items.size) {

                        // 2. 將 items.size 作為「值」傳入
                        //    而不是作為 lambda ({ items.size }) 傳入。
                        //    這能確保新的 state 物件在建立時
                        //    就 100% 知道正確的、最新的卡片數量。
                        val state = rememberSwipeableCardsState(
                            itemCount = { items.size } // <-- 這樣就 100% 正確了
                        )
                        // --- 修改結束 ---

                        LazySwipeableCards(
                            state = state,

                        // --- 8. 這是 *真正* 正確的 onSwipe 邏輯 ---
                        onSwipe = { swipedProduct, direction ->
                            // 'swipedProduct' *就是* ProductItem 物件
                            // 'direction' *就是* SwipeableCardDirection

                            // 直接呼叫 viewModel
                            when (direction) {
                                SwipeableCardDirection.Left -> viewModel.swipeLeft(swipedProduct as ProductItem) // <-- 類型轉換
                                SwipeableCardDirection.Right -> viewModel.swipeRight(swipedProduct as ProductItem) // <-- 類型轉換
                                else -> viewModel.swipeLeft(swipedProduct as ProductItem) // 預設當作不喜歡
                            }
                        },
                        // --- onSwipe 結束 ---
                    ) {
                        // 9. 這裡的 items lambda 是用於 *顯示* 卡片
                        items(items) { product, index, offset ->
                            CardItem(
                                product = product,
                                offset = offset
                            )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "目前沒有任何上架的商品",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }


            }
        }

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