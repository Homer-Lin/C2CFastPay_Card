// 請將此內容更新到 CardStack.kt
package com.example.c2cfastpay_card.UIScreen.Screens

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.example.c2cfastpay_card.UIScreen.components.CardItem // 如果您有獨立檔案，保留這個，否則使用下方定義
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import coil.compose.AsyncImage

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

        // --- 1. 頂部 Bar (簡化版) ---
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

        // --- 2. 卡片區域 ---
        Box(
            modifier = Modifier
                .constrainAs(cardDeck) {
                    top.linkTo(topBar.bottom, margin = 20.dp)
                    bottom.linkTo(controlButtons.top, margin = 20.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints // 讓卡片區盡量佔滿中間
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

                    // 將 state 傳遞給按鈕使用 (透過 Callback 或直接在同個 Scope)

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.75f) // 卡片比例
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
                                    // 這裡呼叫增強版的 CardItem
                                    EnhancedCardItem(
                                        product = product,
                                        offsetX = offset.x // 傳入 X 軸偏移量
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // --- 3. 底部控制按鈕 (放在卡片下方) ---
                        // 讓使用者明確知道左邊是不喜歡，右邊是喜歡
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp)
                        ) {
                            // 叉叉按鈕 (Pass)
                            FloatingActionButton(
                                onClick = {
                                    scope.launch { state.swipe(SwipeableCardDirection.Left) }
                                },
                                containerColor = Color.White,
                                contentColor = Color(0xFFFF5252),
                                shape = CircleShape,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Pass", modifier = Modifier.size(32.dp))
                            }

                            // 愛心按鈕 (Like)
                            FloatingActionButton(
                                onClick = {
                                    scope.launch { state.swipe(SwipeableCardDirection.Right) }
                                },
                                containerColor = Color.White,
                                contentColor = Color(0xFF4CAF50), // 綠色愛心
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

        // --- 4. 歷史紀錄連結 ---
        TextButton(
            onClick = { navController.navigate(Screen.History.route) },
            modifier = Modifier.constrainAs(controlButtons) { // 借用 id 位置
                bottom.linkTo(parent.bottom, margin = 20.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        ) {
            Text("查看配對紀錄", color = Color(0xFF487F81))
        }
    }
}

// --- 增強版卡片 Item (包含 Overlay 動畫) ---
@Composable
fun EnhancedCardItem(
    product: ProductItem,
    offsetX: Float // 接收滑動偏移量
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 背景圖片
            AsyncImage( // 假設您有導入 Coil，如果沒有請用您原本的圖片載入方式
                model = product.imageUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // 漸層遮罩 (讓文字清楚)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(0.8f)),
                        startY = 300f
                    ))
            )

            // 商品資訊
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Text(
                    text = product.title,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$${product.price}",
                    color = Color.Green,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = product.description,
                    color = Color.White.copy(0.8f),
                    fontSize = 14.sp,
                    maxLines = 2
                )
            }

            // --- 視覺回饋 Overlay ---
            // 根據 offsetX 計算透明度
            // 正數 (向右滑) -> 顯示 LIKE
            // 負數 (向左滑) -> 顯示 NOPE

            val likeAlpha = (offsetX / 300f).coerceIn(0f, 1f)
            val nopeAlpha = (-offsetX / 300f).coerceIn(0f, 1f)

            // LIKE (右滑顯示)
            if (likeAlpha > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Green.copy(alpha = likeAlpha * 0.3f)), // 整個變綠
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(40.dp)
                            .rotate(-20f)
                            .border(4.dp, Color.Green, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "LIKE",
                            color = Color.Green,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.alpha(likeAlpha)
                        )
                    }
                    // 中心大愛心
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(100.dp).alpha(likeAlpha)
                    )
                }
            }

            // NOPE (左滑顯示)
            if (nopeAlpha > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Red.copy(alpha = nopeAlpha * 0.3f)), // 整個變紅
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(40.dp)
                            .rotate(20f)
                            .border(4.dp, Color.Red, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "NOPE",
                            color = Color.Red,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.alpha(nopeAlpha)
                        )
                    }
                    // 中心大叉叉
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(100.dp).alpha(nopeAlpha)
                    )
                }
            }
        }
    }
}
// 需要 Coil，請確保您的 imports 包含：
// import coil.compose.AsyncImage