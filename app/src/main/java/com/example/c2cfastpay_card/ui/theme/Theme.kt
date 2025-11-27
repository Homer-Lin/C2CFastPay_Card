package com.example.c2cfastpay_card.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

// --- 為 Sale 定義的配色方案 ---
val SaleColorScheme = lightColorScheme(
    primary = Color(0xFFE7F2F2),         // Sale 主要色 (淺藍綠)
    onPrimary = Color(0xFF487F81),       // Sale 主要色上的文字/圖示 (深藍綠)
    secondary = Color(0xFF759E9F),       // Sale 次要色 (中藍綠)
    onSecondary = Color.White,        // 您可以定義次要色上的顏色，預設通常是 Black 或 White
    background = Color.White,            // Sale 背景色 (白色)
    onBackground = Color.Black,       // 背景上的文字/圖示
    surface = Color(0xFFE7F2F2),         // Sale 表面色 (淺藍綠，用於 Card 等)
    onSurface = Color(0xFF487F81),    // 表面上的文字/圖示 (可以使用 onPrimary)
)

// --- 為 Wish 定義的配色方案 ---
val WishColorScheme = lightColorScheme(
    primary = Color(0xFFFBE1BF),         // Wish 主要色 (淺橘黃)
    onPrimary = Color(0xFFF79329),       // Wish 主要色上的文字/圖示 (深橘)
    secondary = Color(0xFFFFC881),       // Wish 次要色 (中橘黃)
    onSecondary = Color.White,
    background = Color.White,            // Wish 背景色 (白色)
    onBackground = Color.Black,
    surface = Color(0xFFFBE1BF),         // Wish 表面色 (淺橘黃)
    onSurface = Color(0xFFF79329),    // 表面上的文字/圖示 (可以使用 onPrimary)
)

// (您可以自訂這些顏色，這裡只是範例)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF759E9F), // 使用 Sale 的次要色作為範例
    secondary = Color(0xFFFFC881), // 使用 Wish 的次要色作為範例
    background = Color(0xFF1C1B1F), // 深色背景
    surface = Color(0xFF2C2B2F), // 深色表面
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

// --- 2. 定義一個預設的 LightColorScheme ---
// (可以使用 Sale 或 Wish 作為基礎，或定義一套全新的)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF759E9F), // 預設使用 Sale 的次要色
    secondary = Color(0xFFFF9800), // 預設橘色
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)


@Composable
fun C2CFastPay_CardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}