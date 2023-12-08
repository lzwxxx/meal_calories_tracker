package com.cs461.g6.mealportiontracker.home

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.PrintStream

class ManualInputActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val viewModel: InputViewModel = viewModel()
            ScreenManualInput(navController,viewModel, context = this)
        }
    }
}

fun getValidatedNumber(text: String): String {
    // Start by filtering out unwanted characters like commas and multiple decimals
    val filteredChars = text.filterIndexed { index, c ->
        c in "0123456789" ||                      // Take all digits
                (c == '.' && text.indexOf('.') == index)  // Take only the first decimal
    }
    // Now we need to remove extra digits from the input
    return if(filteredChars.contains('.')) {
        val beforeDecimal = filteredChars.substringBefore('.')
        val afterDecimal = filteredChars.substringAfter('.')
        beforeDecimal.take(3) + "." + afterDecimal.take(2)    // If decimal is present, take first 3 digits before decimal and first 2 digits after decimal
    } else {
        filteredChars.take(3)                     // If there is no decimal, just take the first 3 digits
    }
}

@Composable
fun ScreenManualInput(navController: NavHostController, viewModel: InputViewModel, context: Context) {
    val scrollState: ScrollState = rememberScrollState()

    var food by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbohydrates by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize() // Takes the full available space
            .padding(16.dp) // Adds padding around the content
            .verticalScroll(scrollState) // Enables vertical scrolling

    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally, // Center the content horizontally
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Add new food",
                modifier = Modifier
                    .padding(start = 18.dp)
                    .fillMaxWidth(),
                style = LocalTextStyle.current.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold // Make the text bold
                ),
                color = LocalContentColor.current.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(30.dp))

            //Food
            Text(
                text = "Food",
                modifier = Modifier
                    .padding(start = 18.dp)
                    .fillMaxWidth(),
                style = LocalTextStyle.current.copy(fontSize = 16.sp),
                color = LocalContentColor.current.copy(alpha = 0.7f)
            )
            TextField(
                value = food,
                onValueChange = {
                    food = it
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent
                ),
                placeholder = { Text("Enter Food Name") },
                modifier = Modifier
                    .width(320.dp) // Set the width of the TextField
            )

            Spacer(modifier = Modifier.height(30.dp))

            //Calories
            Text(
                text = "Calories",
                modifier = Modifier
                    .padding(start = 18.dp)
                    .fillMaxWidth(),
                style = LocalTextStyle.current.copy(fontSize = 16.sp),
                color = LocalContentColor.current.copy(alpha = 0.7f)
            )
            TextField(
                value = calories,
                onValueChange = { calories = getValidatedNumber(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("Enter Calories Amount")},
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent
                ),
                modifier = Modifier
                    .width(320.dp) // Set the width of the TextField
            )

            Spacer(modifier = Modifier.height(30.dp))

            //Protein
            Text(
                text = "Protein (gram) ",
                modifier = Modifier
                    .padding(start = 18.dp)
                    .fillMaxWidth(),
                style = LocalTextStyle.current.copy(fontSize = 16.sp),
                color = LocalContentColor.current.copy(alpha = 0.7f)
            )
            TextField(
                value = protein,
                onValueChange = { protein = getValidatedNumber(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("Enter Protein Amount in gram")},
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent
                ),
                modifier = Modifier
                    .width(320.dp) // Set the width of the TextField
            )

            Spacer(modifier = Modifier.height(30.dp))

            //Carbohydrates
            Text(
                text = "Carbohydrates (gram)",
                modifier = Modifier
                    .padding(start = 18.dp)
                    .fillMaxWidth(),
                style = LocalTextStyle.current.copy(fontSize = 16.sp),
                color = LocalContentColor.current.copy(alpha = 0.7f)
            )
            TextField(
                value = carbohydrates,
                onValueChange = { carbohydrates = getValidatedNumber(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("Enter Carbohydrates Amount in gram")},
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent
                ),
                modifier = Modifier
                    .width(320.dp) // Set the width of the TextField
            )

            Spacer(modifier = Modifier.height(30.dp))

            //Fat
            Text(
                text = "Fat (gram)",
                modifier = Modifier
                    .padding(start = 18.dp)
                    .fillMaxWidth(),
                style = LocalTextStyle.current.copy(fontSize = 16.sp),
                color = LocalContentColor.current.copy(alpha = 0.7f)
            )
            TextField(
                value = fat,
                onValueChange = { fat = getValidatedNumber(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("Enter Fat Amount in gram")},
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent
                ),
                modifier = Modifier
                    .width(320.dp) // Set the width of the TextField
            )

            Spacer(modifier = Modifier.height(60.dp))

            Button(onClick = {
                val foodExists = viewModel.checkFoodExistence(food)

                if (foodExists) {
                    Toast.makeText(
                        context,
                        "The food already exists in the database.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // The food does not exist, so add it to the CSV file
                    val newFood = FoodItem(food, calories.toDouble(), protein.toString(), carbohydrates.toString(), fat.toString())
                    viewModel.writeToFile(newFood, context, "food_nutrition.csv")

                    Toast.makeText(
                        context,
                        "The new food has been successfully added.",
                        Toast.LENGTH_SHORT
                    ).show()

                    navController.navigate(AppScreen.ScreenSearch.name)
                }
            }) {
                Text("Submit")
            }

        }
    }
}

class InputViewModel(application: Application) : AndroidViewModel(application) {

    private val _originalFoodItemList = MutableStateFlow<List<FoodItem>>(emptyList())

    init {
        val context: Context = application.applicationContext
        val fileName = "food_nutrition.csv"

        val foodItems: List<FoodItem>

        if(isFilePresent(context, fileName)){
            // File exists in internal storage, read data from it
            foodItems = readInternalCsv(context, fileName)
        } else {
            // Read CSV file from assets folder and populate food list during initialization
            val inputStream: InputStream = context.assets.open("food_nutrition.csv")
            foodItems = readCsv(inputStream)

            // Write CSV data to the file in the app's data folder
            try {
                val outStream = PrintStream(context.openFileOutput(fileName, Context.MODE_APPEND))

                // Iterate through foodItems and write them to the file
                for (foodItem in foodItems) {
                    val line = "\"${foodItem.name}\",${foodItem.calories},${foodItem.proteins},${foodItem.carbs},${foodItem.fats}"
                    outStream.println(line)
                }

                // Close the file
                outStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
                // Handle the exception (e.g., show an error message to the user)
            }
        }
        _originalFoodItemList.value = foodItems
    }

    fun isFilePresent(context: Context, fileName: String): Boolean {
        val file = File(context.filesDir, fileName)
        return file.exists()
    }

    private fun parseCsvLine(line: String): FoodItem? {
        val foodItemProperties = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*\$)".toRegex())
            .map { it.trim('"') } // Remove quotes from components

        return if (foodItemProperties.size >= 5) {
            val name = foodItemProperties[0]
            val calories = foodItemProperties[1].toDoubleOrNull() ?: 0.0
            val proteins = foodItemProperties[2].trim()
            val carbs = foodItemProperties[3].trim()
            val fats = foodItemProperties[4].trim()
            FoodItem(name, calories, proteins, carbs, fats)
        } else {
            null
        }
    }

    fun readInternalCsv(context: Context, fileName: String): List<FoodItem> {
        val result: MutableList<FoodItem> = mutableListOf()

        try {
            val file = File(context.filesDir, fileName)
            val fileContents = file.readText()

            val lines = fileContents.split("\n") // Split fileContents into lines
            for (line in lines) {
                val foodItem = parseCsvLine(line)
                foodItem?.let { result.add(it) }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // Handle IOException (e.g., show an error message to the user)
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle other exceptions (e.g., show an error message to the user)
        }

        return result
    }

    fun readCsv(inputStream: InputStream): List<FoodItem> {
        val result: MutableList<FoodItem> = mutableListOf()
        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String?
        try {
            while (reader.readLine().also { line = it } != null) {
                val foodItem = parseCsvLine(line!!)
                foodItem?.let { result.add(it) }
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

    fun checkFoodExistence(food: String): Boolean {
        val trimmedFood = food.trim() // Trim leading and trailing whitespaces
        val exists =
            _originalFoodItemList.value.any { it.name.equals(trimmedFood, ignoreCase = true) }
        return exists
    }

    fun writeToFile(food: FoodItem, context: Context, fileName: String) {
        try {
            val outStream = PrintStream(context.openFileOutput(fileName, Context.MODE_APPEND))

            // Format the new food item as a CSV line and write it to the file
            val line = "\"${food.name}\",${food.calories},${food.proteins}g,${food.carbs}g,${food.fats}g"
            outStream.println(line)

            // Update the internal data list with the new food item
            _originalFoodItemList.value = _originalFoodItemList.value + listOf(food)

            // Close the file
            outStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            // Handle the exception (e.g., show an error message to the user)
        }
    }
}

