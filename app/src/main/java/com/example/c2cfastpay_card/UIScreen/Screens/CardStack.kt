package com.example.c2cfastpay_card.UIScreen.Screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.c2cfastpay_card.R
import com.example.c2cfastpay_card.UIScreen.components.ProductItem
import com.example.c2cfastpay_card.UIScreen.components.ProductRepository
import com.example.c2cfastpay_card.UIScreen.components.MatchRepository
import com.example.c2cfastpay_card.navigation.Screen
import com.spartapps.swipeablecards.state.rememberSwipeableCardsState
import com.spartapps.swipeablecards.ui.lazy.LazySwipeableCards
import com.spartapps.swipeablecards.ui.lazy.items
import com.spartapps.swipeablecards.ui.SwipeableCardDirection
// ★★★ 關鍵：確保引用的是 components 裡的 CardItem ★★★
import com.example.c2cfastpay_card.UIScreen.components.CardItem
import kotlinx.coroutines.launch

@Composable
fun CardStackScreen(navController: NavController) {
    val context = LocalContext.current
    val productRepository = remember { ProductRepository(context) }
    val matchRepository = remember { MatchRepository(context) }

    val viewModel: CardStackViewModel = viewModel(
        factory = CardStackViewModelFactory(productRepository, matchRepository)
    )

    val cardsToShow by viewModel.cards.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPotentialMatches()
    }

    CardStackLayout(
        navController = navController,
        items = cardsToShow,
        isLoading = isLoading,
        viewModel = viewModel
    )
}

@Composable
fun CardStackLayout(
    navController: NavController,
    items: List<ProductItem>,
    isLoading: Boolean,
    viewModel: CardStackViewModel,
    modifier: Modifier = Modifier,
) {
    ConstraintLayout(modifier = modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        val (topBar, cardDeck, controlButtons, bottomNav) = createRefs()

        // 1. 頂部 Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
                .constrainAs(topBar) { top.linkTo(parent.top) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(painterResource(R.drawable.a_1_back_buttom), contentDescription = "Back", modifier = Modifier.size(24.dp))
            }
            Text("商品名片配對", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF487F81))
            IconButton(onClick = { /* Help */ }) {
                Icon(painterResource(R.drawable.b_13_help), contentDescription = "Help", modifier = Modifier.size(24.dp))
            }
        }

        // 2. 卡片區域
        Box(
            modifier = Modifier
                .constrainAs(cardDeck) {
                    top.linkTo(topBar.bottom, margin = 20.dp)
                    bottom.linkTo(controlButtons.top, margin = 20.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                },
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color(0xFF487F81))
            } else if (items.isEmpty()) {
                Text("沒有更多商品了", color = Color.Gray, fontSize = 18.sp)
            } else {
                key(items.size) {
                    val state = rememberSwipeableCardsState(itemCount = { items.size })
                    val scope = rememberCoroutineScope()

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.75f)
                        ) {
                            LazySwipeableCards(
                                state = state,
                                onSwipe = { swipedProduct, direction ->
                                    val product = swipedProduct as ProductItem
                                    if (direction == SwipeableCardDirection.Right) {
                                        viewModel.swipeRight(product)
                                    } else {
                                        viewModel.swipeLeft(product)
                                    }
                                }
                            ) {
                                items(items) { product, index, offset ->
                                    // ★★★ 修改處：改用 CardItem (這才是您有寫翻轉動畫的那個元件) ★★★
                                    // 這裡傳入 offset，讓 CardItem 可以自己決定要不要做滑動特效 (如果 CardItem 支援的話)
                                    CardItem(
                                        product = product,
                                        offset = offset // CardItem 需要這個參數
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 3. 底部按鈕
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp)
                        ) {
                            FloatingActionButton(
                                onClick = { scope.launch { state.swipe(SwipeableCardDirection.Left) } },
                                containerColor = Color.White,
                                contentColor = Color(0xFFFF5252),
                                shape = CircleShape,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Pass", modifier = Modifier.size(32.dp))
                            }

                            FloatingActionButton(
                                onClick = { scope.launch { state.swipe(SwipeableCardDirection.Right) } },
                                containerColor = Color.White,
                                contentColor = Color(0xFF4CAF50),
                                shape = CircleShape,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(Icons.Default.Favorite, contentDescription = "Like", modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }
        }

        // 4. 歷史紀錄
        TextButton(
            onClick = { navController.navigate(Screen.History.route) },
            modifier = Modifier.constrainAs(controlButtons) {
                bottom.linkTo(parent.bottom, margin = 20.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        ) {
            Text("查看配對紀錄", color = Color(0xFF487F81))
        }
    }
}