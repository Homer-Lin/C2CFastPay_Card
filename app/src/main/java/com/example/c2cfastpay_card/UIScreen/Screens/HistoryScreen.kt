package com.example.c2cfastpay_card.UIScreen.Screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
// --- 修改：只導入 Material 3 ---
import androidx.compose.material3.*
import androidx.compose.material3.Icon // 確保 Icon 是 M3
import androidx.compose.material3.IconButton // 確保 IconButton 是 M3
import androidx.compose.material.icons.automirrored.filled.ArrowBack // M3 中 ArrowBack 的新路徑
// --- 修改結束 ---
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.c2cfastpay_card.UIScreen.components.MatchItem
import com.example.c2cfastpay_card.UIScreen.components.MatchRepository


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {

    // 1. 取得 Context 並建立 Repository
    val context = LocalContext.current
    val matchRepository = remember { MatchRepository(context) }

    // 2. 使用 Factory 建立 ViewModel
    val viewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModelFactory(matchRepository)
    )

    // 3. 從 ViewModel 觀察 (collect) 配對列表狀態
    val matches by viewModel.matches.collectAsState()

    // --- 修改：使用 M3 Scaffold ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text( // M3 Text
                        "配對紀錄",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // M3 IconButton
                        Icon( // M3 Icon
                            // --- 這是 M3 正確的語法 ---
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF759E9F)
                )
            )
        }
    ) { paddingValues ->

        if (matches.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text( // M3 Text
                    text = "目前沒有任何配對紀錄",
                    fontSize = 18.sp,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(matches, key = { it.id }) { matchItem ->
                    MatchHistoryItem(item = matchItem)
                }
            }
        }
    }
}

// 6. 用於顯示單個 MatchItem 的 Composable
@Composable
fun MatchHistoryItem(item: MatchItem) {
    // --- 修改：使用 M3 Card ---
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(8.dp),
        // --- 這是 M3 正確的 elevation 語法 ---
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = Uri.parse(item.productImageUrl), // 使用 productImageUrl
                contentDescription = item.productTitle,   // 使用 productTitle
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(100.dp)
                    .clip(RoundedCornerShape(8.dp)) // M3 .clip() 語法
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp) // M3 .padding() 語法
            ) {
                Text( // M3 Text
                    text = item.productTitle, // 使用 productTitle
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
//                Text( // M3 Text
//                    text = "NT$ ${item.price}",
//                    fontSize = 16.sp,
//                    color = Color.Gray
//                )
            }
        }
    }
}