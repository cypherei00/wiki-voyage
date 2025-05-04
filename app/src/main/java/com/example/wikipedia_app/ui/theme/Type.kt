package com.example.wikipedia_app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.wikipedia_app.R

// Set of Material typography styles to start with
val RobotoFont = FontFamily(
    Font(R.font.roboto_regular),
    Font(R.font.roboto_bold, FontWeight.Bold),
    Font(R.font.roboto_italic, FontWeight.Normal, FontStyle.Italic)
)
val Typography = Typography(
    displayLarge = TextStyle(fontFamily = RobotoFont),
    titleLarge = TextStyle(fontFamily = RobotoFont),
    bodyLarge = TextStyle(fontFamily = RobotoFont),
    bodyMedium = TextStyle(fontFamily = RobotoFont),
    bodySmall = TextStyle(fontFamily = RobotoFont),
    labelLarge = TextStyle(fontFamily = RobotoFont)
)