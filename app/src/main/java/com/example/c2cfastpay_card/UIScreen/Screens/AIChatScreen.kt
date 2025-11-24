package com.example.c2cfastpay_card.UIScreen.Screens

import android.net.Uri
import android.util.Log
import java.net.URLEncoder

// Android & Compose 基礎
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ViewModel & Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

// 專案內的類別
// ★★★ 修正重點 1：不需要 import com.example.c2cfastpay_card.ProductFlowViewModel ★★★
// 因為現在這個檔案跟 ViewModel 都在同一個 package (UIScreen.Screens)，它會自動抓到正確的

import com.example.c2cfastpay_card.model.DraftProduct
import com.google.gson.Gson
import kotlinx.coroutines.launch

// --- ViewModel ---
class AIChatViewModel(
    private val api: C2CFastPayApi
) : ViewModel() {

    // ★ 新增：暫存圖片 Uri
    var currentImageUri by mutableStateOf<Uri?>(null)

    // --- 原本的邏輯 ---
    var chatLog by mutableStateOf(listOf("AI助手：請問您要販售什麼商品（例如：保溫瓶、瑜珈褲、手機殼）？"))
    var userInput by mutableStateOf("")
    var productName by mutableStateOf("")
    var aiDescription by mutableStateOf("")
    var finalStory by mutableStateOf("")

    // 1=輸入商品 2=選屬性 3=填屬性值 4=問新舊 5=填使用描述 6=生成文案 7=故事問答 8=故事生成
    var step by mutableStateOf(1)
    var showStoryNext by mutableStateOf(false)

    var attributeCandidates by mutableStateOf(listOf<String>())
    var selectedAttributes by mutableStateOf(setOf<String>())
    var attributeValues by mutableStateOf(mapOf<String, String>())

    var isNewItem by mutableStateOf<Boolean?>(null)
    var usageDescription by mutableStateOf("")

    private val storyTypes = listOf("購買動機", "使用經驗", "轉售原因")
    private val storyQuestions = listOf(
        { pname: String -> "請問當初購入這件「$pname」的動機是什麼？" },
        { pname: String -> "請問你平時怎麼使用這件「$pname」？有什麼印象深刻的經歷嗎？" },
        { pname: String -> "請問你為什麼決定轉讓這件「$pname」？" }
    )
    var storyStage by mutableStateOf(0)
    var subStage by mutableStateOf(0)
    var storyQA by mutableStateOf(mutableListOf<StoryQaItem>())
    var tmpMainAnswer by mutableStateOf("")
    var tmpFollowupQ by mutableStateOf("")

    fun onUserSendMessage(message: String) {
        if (message.isBlank()) return
        chatLog += "你：$message"
        userInput = ""

        when (step) {
            1 -> {
                productName = message
                chatLog += "AI助手：正在查詢屬性，請稍候..."
                viewModelScope.launch {
                    try {
                        val resp = api.ragQuery(QueryRequest(message))
                        attributeCandidates = resp.attributes
                        selectedAttributes = setOf()
                        attributeValues = mapOf()
                        chatLog = chatLog.dropLast(1) + "AI助手：請勾選你想填寫的商品屬性（至少選三個）"
                        step = 2
                    } catch (e: Exception) {
                        Log.e("AIChatViewModel", "RAG Query 失敗: ${e.message}")
                        chatLog = chatLog.dropLast(1) + "AI助手：查詢失敗，請重試。（錯誤：${e.message}）"
                    }
                }
            }
            3 -> {
                chatLog += "AI助手：請問您的「$productName」是全新商品嗎？請回答「是」或「否」。"
                step = 4
            }
            4 -> {
                val cleanMsg = message.lowercase().trim()
                val isYes = cleanMsg.contains("是") || cleanMsg.contains("全新") || cleanMsg.contains("yes")
                val isNo = cleanMsg.contains("否") || cleanMsg.contains("二手") || cleanMsg.contains("no")

                if (isYes) {
                    isNewItem = true
                    generateDescription()
                } else if (isNo) {
                    isNewItem = false
                    chatLog += "AI助手：好的，請問它的使用情況如何？請具體描述它的新舊程度和使用痕跡。"
                    step = 5
                } else {
                    chatLog += "AI助手：請明確回答「是」或「否」，以便繼續。"
                }
            }
            5 -> {
                usageDescription = message
                generateDescription()
            }
            7 -> {
                onStoryAnswer(message)
            }
        }
    }

    fun onSelectAttribute(attr: String) {
        selectedAttributes = if (selectedAttributes.contains(attr)) selectedAttributes - attr else selectedAttributes + attr
    }

    fun onConfirmAttributes() {
        attributeValues = selectedAttributes.associateWith { "" }
        chatLog += "AI助手：請針對下列屬性填寫內容："
        step = 3
    }

    fun onFillAttribute(attr: String, value: String) {
        attributeValues = attributeValues.toMutableMap().apply { put(attr, value) }
    }

    fun onSendAttributes() {
        if (attributeValues.values.count { it.isNotBlank() } >= 3) {
            onUserSendMessage("（填寫屬性完成）")
        }
    }

    private fun generateDescription() {
        chatLog += "AI助手：正在生成商品文案..."
        step = 6
        val finalAttributes = attributeValues.filterValues { it.isNotBlank() }.toMutableMap()
        finalAttributes["商品狀況"] = if (isNewItem == true) "全新未拆封" else "二手商品，狀況描述：$usageDescription"

        viewModelScope.launch {
            try {
                val req = GenDescRequest(attributes = finalAttributes)
                val resp = api.generateDescription(req)
                aiDescription = resp.description
                chatLog = chatLog.dropLast(1) + "AI助手： ${aiDescription}"
                showStoryNext = true
            } catch (e: Exception) {
                Log.e("AIChatViewModel", "文案生成失敗: ${e.message}")
                chatLog = chatLog.dropLast(1) + "AI助手：文案生成失敗，請重試。"
            }
        }
    }

    fun startStoryMode() {
        step = 7
        storyStage = 0
        subStage = 0
        storyQA.clear()
        tmpMainAnswer = ""
        tmpFollowupQ = ""
        chatLog += "AI助手：讓我們聊聊這個商品背後的故事～"
        askStoryMainQ()
    }

    private fun askStoryMainQ() {
        val q = storyQuestions[storyStage](productName)
        chatLog += "AI助手：$q"
    }

    fun onStoryAnswer(userAnswer: String) {
        if (subStage == 0) {
            tmpMainAnswer = userAnswer
            if (storyStage == 2) {
                tmpFollowupQ = "那您認為這件商品最適合哪一類型的買家呢？"
                chatLog += "AI助手：${tmpFollowupQ}"
                subStage = 1
            } else {
                chatLog += "AI助手：思考更深入的追問..."
                viewModelScope.launch {
                    try {
                        val resp = api.generateFollowupQuestion(
                            GenFollowupRequest(storyTypes[storyStage], productName, userAnswer)
                        )
                        tmpFollowupQ = resp.followup_question
                        chatLog = chatLog.dropLast(1) + "AI助手：${tmpFollowupQ}"
                        subStage = 1
                    } catch (e: Exception) {
                        tmpFollowupQ = "了解，那還有什麼細節嗎？"
                        chatLog = chatLog.dropLast(1) + "AI助手：$tmpFollowupQ"
                        subStage = 1
                    }
                }
            }
        } else {
            val storyBlock = StoryQaItem(storyTypes[storyStage], storyQuestions[storyStage](productName), tmpMainAnswer, tmpFollowupQ, userAnswer)
            storyQA.add(storyBlock)
            if (storyStage < 2) {
                storyStage++
                subStage = 0
                askStoryMainQ()
            } else {
                generateFinalStory()
            }
        }
    }

    fun generateFinalStory() {
        chatLog += "AI助手：正在生成你的商品故事..."
        step = 8
        viewModelScope.launch {
            try {
                val resp = api.generateStory(GenStoryRequest(productName, storyQA))
                finalStory = resp.story
                chatLog = chatLog.dropLast(1) + "AI助手：${finalStory}"
            } catch (e: Exception) {
                Log.e("AIChatViewModel", "故事生成失敗: ${e.message}")
                chatLog = chatLog.dropLast(1) + "AI助手：故事生成失敗。"
            }
        }
    }

    fun restart() {
        productName = ""
        attributeCandidates = listOf()
        selectedAttributes = setOf()
        attributeValues = mapOf()
        aiDescription = ""
        finalStory = ""
        storyQA.clear()
        isNewItem = null
        usageDescription = ""
        step = 1
        showStoryNext = false
        chatLog = listOf("AI助手：請問您要販售什麼商品（例如：保溫瓶、瑜珈褲、手機殼）？")
    }
}

// --- Composable UI ---

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AIChatScreen(
    navController: NavController,
    flowViewModel: ProductFlowViewModel,
    imageUri: String? = null // ★★★ 修正重點 2：補上這個參數，解決 "Cannot find a parameter" ★★★
) {
    // 建立 ViewModel (使用 Factory 注入 API)
    val viewModel: AIChatViewModel = viewModel(factory = AIChatViewModelFactory(RetrofitClient.apiService))

    // 處理傳入的圖片
    LaunchedEffect(imageUri) {
        if (imageUri != null) {
            val uri = Uri.parse(imageUri)
            viewModel.currentImageUri = uri
            Log.d("AIChat", "已載入圖片: $uri")
        } else {
            // 如果沒傳入，試試看從 SharedViewModel 拿 (備用)
            val sharedPhotos = flowViewModel.photoUris.value
            if (sharedPhotos.isNotEmpty()) {
                viewModel.currentImageUri = sharedPhotos.last()
            }
        }
    }

    // 狀態解構
    val step = viewModel.step
    val showStoryNext = viewModel.showStoryNext
    val chatLog = viewModel.chatLog
    val listState = rememberLazyListState()

    // 聊天室自動滾動
    LaunchedEffect(chatLog.size) {
        if (chatLog.isNotEmpty()) listState.animateScrollToItem(chatLog.size - 1)
    }

    // 同步狀態回 SharedViewModel (可選)
    LaunchedEffect(viewModel.aiDescription) {
        if (viewModel.step == 6 && viewModel.aiDescription.isNotBlank()) {
            flowViewModel.aiDescription.value = viewModel.aiDescription
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        // --- 頂部標題與返回按鈕 ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "返回"
                )
            }

            Text(
                text = "AI 上架助手",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // --- 聊天顯示區 ---
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chatLog) { message ->
                val isUser = message.startsWith("你：")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        color = if (isUser) Color(0xFFD1F5D3) else Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.widthIn(max = 300.dp)
                    ) {
                        Text(
                            text = message.removePrefix("你：").removePrefix("AI助手："),
                            modifier = Modifier.padding(12.dp),
                            color = Color.Black
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 底部互動區 ---
        when (step) {
            1 -> { // 輸入商品名稱
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = viewModel.userInput,
                        onValueChange = { viewModel.userInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("請輸入商品名稱...") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.onUserSendMessage(viewModel.userInput) },
                        enabled = viewModel.userInput.isNotBlank()
                    ) { Text("送出") }
                }
            }

            2 -> { // 選擇屬性
                Text("請選擇屬性（至少三個）", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    viewModel.attributeCandidates.forEach { attr ->
                        FilterChip(
                            selected = viewModel.selectedAttributes.contains(attr),
                            onClick = { viewModel.onSelectAttribute(attr) },
                            label = { Text(attr) }
                        )
                    }
                }

                Button(
                    onClick = { viewModel.onConfirmAttributes() },
                    enabled = viewModel.selectedAttributes.size >= 3,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) { Text("下一步：填寫屬性") }
            }

            3 -> { // 填寫屬性值
                Column {
                    viewModel.selectedAttributes.forEach { attr ->
                        OutlinedTextField(
                            value = viewModel.attributeValues[attr] ?: "",
                            onValueChange = { viewModel.onFillAttribute(attr, it) },
                            label = { Text(attr) },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                    }
                    Button(
                        onClick = { viewModel.onSendAttributes() },
                        enabled = viewModel.attributeValues.values.count { it.isNotBlank() } >= 3,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("送出，詢問商品狀況") }
                }
            }

            4, 5, 7 -> { // 問答通用輸入
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = viewModel.userInput,
                        onValueChange = { viewModel.userInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(if (step == 4) "是/否" else "請回答...")
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.onUserSendMessage(viewModel.userInput) },
                        enabled = viewModel.userInput.isNotBlank()
                    ) { Text("送出") }
                }
            }

            6 -> { // 文案確認
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = { viewModel.restart() }) {
                        Text("重來")
                    }
                    Button(
                        onClick = { viewModel.startStoryMode() },
                        enabled = showStoryNext
                    ) { Text("下一步：生成故事") }
                }
            }

            8 -> { // 最終確認與跳轉
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { viewModel.generateFinalStory() }
                    ) {
                        Text("重新生成故事")
                    }

                    Button(
                        onClick = {
                            // 打包資料
                            val draft = DraftProduct(
                                imageUri = viewModel.currentImageUri?.toString() ?: "",
                                title = viewModel.productName,
                                description = viewModel.aiDescription,
                                story = viewModel.finalStory,
                                condition = if (viewModel.isNewItem == true) "全新" else "二手",
                                fromAI = true
                            )

                            val json = Gson().toJson(draft)
                            val encodedJson = URLEncoder.encode(json, "UTF-8")

                            navController.navigate("add_product?draftJson=$encodedJson")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF759E9F))
                    ) {
                        Text("完成上架")
                    }
                }
            }
        }
    }
}

// --- ViewModel Factory ---
class AIChatViewModelFactory(private val api: C2CFastPayApi) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AIChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AIChatViewModel(api) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}