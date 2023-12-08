package com.cs461.g6.mealportiontracker.core

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

data class FoodItem(
    val name: String,
    val calories: Double,
    val proteins: String,
    val carbs: String,
    val fats: String
)


class MyActivity : AppCompatActivity() {
    val context: Context = application.applicationContext
    val inputStream: InputStream = context.assets.open("food_nutrition.csv")
    val foodItems = readCsv(inputStream)

    fun readCsv(inputStream: InputStream): List<FoodItem> {
        val result: MutableList<FoodItem> = mutableListOf()
        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String?
        try {
            while (reader.readLine().also { line = it } != null) {
                // Process each line of the CSV file and add it to the result list
                val foodItemProperties = line!!.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*\$)".toRegex())
                    .map { it.trim('"') } // Remove quotes from components
                if (foodItemProperties.size >= 5) {
                    val name = foodItemProperties[0]
                    val calories = foodItemProperties[1].toDoubleOrNull() ?: 0.0
                    val proteins = foodItemProperties[2].trim()
                    val carbs = foodItemProperties[3].trim()
                    val fats = foodItemProperties[4].trim()
                    val foodItem = FoodItem(name, calories, proteins, carbs, fats)
                    result.add(foodItem)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return result
    }
}
