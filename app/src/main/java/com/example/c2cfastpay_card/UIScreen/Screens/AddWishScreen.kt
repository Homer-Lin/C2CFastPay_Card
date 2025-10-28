package com.example.c2cfastpay_card.UIScreen.Screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.c2cfastpay_card.UIScreen.components.WishRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.graphics.Color


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWishScreen(navController: NavController) {

    val context = LocalContext.current
    val wishRepository = remember { WishRepository(context) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var wishTitle by remember { mutableStateOf("") }
    var wishDescription by remember { mutableStateOf("") }
    var wishSpecs by remember { mutableStateOf("") }
    var wishPrice by remember { mutableStateOf("") }
    var wishNotes by remember { mutableStateOf("") }
    var wishOtherInfo by remember { mutableStateOf("") }

    // 圖片選擇
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    // 下拉式交易方式
    val tradeMethods = listOf("面交", "宅配", "超商取貨")
    var expanded by remember { mutableStateOf(false) }
    var selectedTradeMethod by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
            }
            Text(text = "新增許願", fontSize = 20.sp, modifier = Modifier.weight(1f))
            Button(onClick = { navController.navigate("add_product") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))) {
                Text("上架商品")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // 圖片
        Box(
            modifier = Modifier
                .size(200.dp)
                .clickable { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "許願圖片",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "上傳圖片",
                    modifier = Modifier.size(50.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = wishTitle,
            onValueChange = { wishTitle = it },
            label = { Text("許願商品名稱*") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = wishDescription,
            onValueChange = { wishDescription = it },
            label = { Text("許願商品描述") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = wishSpecs,
            onValueChange = { wishSpecs = it },
            label = { Text("許願商品規格") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = wishPrice,
            onValueChange = { wishPrice = it },
            label = { Text("許願商品價格*") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(10.dp))

// 交易方式下拉選單
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedTradeMethod,
                onValueChange = {},
                readOnly = true,
                label = { Text("交易方式*") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                tradeMethods.forEach { method ->
                    DropdownMenuItem(
                        text = { Text(method) },
                        onClick = {
                            selectedTradeMethod = method
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = wishNotes,
            onValueChange = { wishNotes = it },
            label = { Text("注意事項") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = wishOtherInfo,
            onValueChange = { wishOtherInfo = it },
            label = { Text("其他") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    val isValid = wishTitle.isNotBlank() &&
                            wishPrice.toDoubleOrNull() != null &&
                            selectedTradeMethod.isNotBlank()

                    if (!isValid) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "請填寫必填欄位並確認價格格式正確", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }

                    try {
                        wishRepository.saveWishData(
                            wishTitle,
                            wishDescription,
                            wishSpecs,
                            wishPrice,
                            selectedTradeMethod,
                            wishNotes,
                            wishOtherInfo,
                            imageUri
                        )
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "願望已儲存", Toast.LENGTH_SHORT).show()
                            navController.navigate("home_wish")
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "儲存失敗：${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)), // 設定按鈕顏色為橘色
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("新增許願", fontSize = 18.sp)
        }
    }
}
