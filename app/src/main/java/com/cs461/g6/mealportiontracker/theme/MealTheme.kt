package com.cs461.g6.mealportiontracker.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

val mealColors = lightColors(
    primary = Color(0xFFFF9C29),
    primaryVariant = Color(0xFFF8694D),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFFA1C44D),
    secondaryVariant = Color(0xFFFFD966),
    onSecondary = Color(0xFF000000),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFDF92),
    onSurface = Color(0xFF000000),
    error = Color(0xFFB00020),
)

val mealColorsAlt = lightColors(
    primary = Color(0xFFFF9C29),
    primaryVariant = Color(0xFFF8694D),
    onPrimary = Color(0xFFF4F0EC), //for searchbar bg
    secondary = Color(0xFFA1C44D),
    secondaryVariant = Color(0xFFFFD966),
    onSecondary = Color(0xFF000000),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFF3D7),
    onSurface = Color(0xFF000000),
    error = Color(0xFFB00020),
)


//@Composable
//fun MealTheme(children: @Composable () -> Unit) {
//    MaterialTheme(colors = mealColors, content = children)
//}

@Composable
fun MealTheme(content: @Composable () -> Unit) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = MaterialTheme.colors.isLight

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = mealColors.primary,
            darkIcons = useDarkIcons
        )
    }

    MaterialTheme(colors = mealColors) {
        content()
    }
}
