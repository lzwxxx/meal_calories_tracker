package com.cs461.g6.mealportiontracker.home

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cs461.g6.mealportiontracker.R
import com.cs461.g6.mealportiontracker.accounts.AccountNavigationActivity
import com.cs461.g6.mealportiontracker.core.FirebaseAuthUtil
import com.cs461.g6.mealportiontracker.core.FoodItem
import com.cs461.g6.mealportiontracker.foodimageprocessing.mToast
import com.cs461.g6.mealportiontracker.core.SessionManager
import com.cs461.g6.mealportiontracker.theme.mealColors
import com.cs461.g6.mealportiontracker.theme.mealColorsAlt
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.math.roundToInt

var RefreshButtonClickFlag = false

data class User(
    val userId: String,
    val email: String,
    val weight: Float?,
    val height: Float?,
    val recommendedCalories: Float,
    val age: Number?,
    val gender: String,
    val activity: Float?
)


fun readCsv(context: Context, fileName: String): List<FoodItem> {
    val result: MutableList<FoodItem> = mutableListOf()

    try {
        val inputStream = context.assets.open(fileName)
        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String?

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
    }

    return result
}


private fun fetchUserProfile(sessionManager: SessionManager, callback: (User?) -> Unit) {
    val databaseReference = FirebaseDatabase.getInstance().getReference("users")
    databaseReference
        .orderByKey()
        .equalTo(sessionManager.getUserId())
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var user_profile: User? = null
                for (datas in snapshot.children) {
                    user_profile = User(
                        userId = datas.child("userId").getValue(String::class.java) ?: "",
                        email = datas.child("email").getValue(String::class.java) ?: "",
                        weight = datas.child("weight").getValue(Float::class.java),
                        height = datas.child("height").getValue(Float::class.java),
                        recommendedCalories = datas.child("recommended_calories")
                            .getValue(Float::class.java) ?: 0.0f,
                        age = datas.child("age").getValue(Long::class.java)?.toInt() ?: 0,
                        gender = datas.child("gender").getValue(String::class.java) ?: "",
                        activity = datas.child("activity").getValue(Float::class.java)
                    )
                }
                callback(user_profile)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
}

@Composable
fun ActivityRadioButton(
    options: List<Pair<Double, String>>,
    selectedActivity: Double,
    onOptionSelected: (Double) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        options.forEach { (value, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (selectedActivity == value),
                        onClick = {
                            onOptionSelected(value)
                        }
                    )
            ) {
                RadioButton(
                    selected = (selectedActivity == value),
                    onClick = { onOptionSelected(value) }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = label,
                    fontSize = 15.sp
                )
            }
        }
    }
}


@Composable
fun ScreenProfile(sessionManager: SessionManager, navController: NavHostController) {
    val mContext = LocalContext.current
    var user_profile by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val context: Context = mContext
    val fileName = "food_nutrition.csv"
    val foodItems = readCsv(context, fileName)
    val recommendedFoodList = mutableListOf<FoodItem>()

    var showRecommendedFoods by remember { mutableStateOf(true) }
    var refreshed by remember { mutableStateOf(false) }

    for (foodItem in foodItems) {
        recommendedFoodList.add(foodItem)
    }

    LaunchedEffect(isLoading) {
        fetchUserProfile(sessionManager) { fetchedUser ->
            if (fetchedUser != null) {
                user_profile = fetchedUser
                isLoading = false
            } else {
                isLoading = false
                mToast(mContext, "Error retrieving profile information from database.")
            }
        }
    }

    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.size(50.dp))
    } else if (user_profile != null) {
        var height by remember { mutableFloatStateOf(user_profile?.height ?: 0.0f) }
        var weight by remember { mutableFloatStateOf(user_profile?.weight ?: 0.0f) }
        var recommendedCalories by remember {
            mutableFloatStateOf(
                user_profile?.recommendedCalories ?: 0.0f
            )
        }
        var age by remember { mutableStateOf(user_profile?.age ?: 0) }
        val radioOptions = listOf("M", "F")
        val activityOptions = listOf(
            1.2 to "Sedentary (little or no exercise)",
            1.375 to "Lightly active (light exercise/sports 1-3 days/week)",
            1.55 to "Moderately active (moderate exercise/sports 3-5 days/week)",
            1.725 to "Very active (hard exercise/sports 6-7 days a week)",
            1.9 to "Extra active (very hard exercise/sports & physical job or 2x training)"
        )
        val (selectedOption, onOptionSelected) = remember {
            mutableStateOf(
                user_profile?.gender ?: "M"
            )
        }
        var selectedActivity by remember {
            mutableDoubleStateOf(
                (user_profile?.activity as? Double) ?: 1.2
            )
        }

        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        )
        {
            var isEditing by remember { mutableStateOf(false) }
            // User Details
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                backgroundColor = mealColors.onPrimary,
                elevation = 5.dp
            ) {

                Column(modifier = Modifier.padding(10.dp)) {
                    //User Icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        //User Icon
                        Image(
                            painter = painterResource(R.drawable.user_profile),
                            contentDescription = null,
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(60.dp)
                        )
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .weight(1f)
                        ) {
                            val currentEmail = sessionManager.getUserEmail().toString()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Email:", fontWeight = FontWeight.W700, fontSize = 16.sp)
                                Text(text = currentEmail, fontSize = 16.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 30.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(" ", fontSize = 15.sp)
                    }
                    if (!isEditing) {
                        //Gender
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 30.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Gender:", fontWeight = FontWeight.W700, fontSize = 15.sp)
                            Text(
                                text = selectedOption,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Start
                            )
                        }

                        //Age
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 30.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Age:", fontWeight = FontWeight.W700, fontSize = 15.sp)
                            Text(
                                text = age.toString(),
                                fontSize = 15.sp,
                                textAlign = TextAlign.Start
                            )
                        }

                        //Weight
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 30.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Weight (in kg):", fontWeight = FontWeight.W700, fontSize = 15.sp)
                            Text(
                                text = weight.toString(),
                                fontSize = 15.sp,
                                textAlign = TextAlign.Start
                            )
                        }
                        //Height
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 30.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Height (in cm):", fontWeight = FontWeight.W700, fontSize = 15.sp)
                            Text(
                                text = height.toString(),
                                fontSize = 15.sp,
                                textAlign = TextAlign.Start
                            )
                        }

                        //Active Status
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 30.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Active Status:", fontWeight = FontWeight.W700, fontSize = 15.sp)
                            Text(
                                text = when(selectedActivity) {
                                    1.2 -> "Sedentary"
                                    1.375 -> "Lightly Active"
                                    1.55 -> "Moderately Active"
                                    1.725 -> "Very Active"
                                    1.9 -> "Super Active"
                                    else -> " "
                                },
                                fontSize = 15.sp,
                                textAlign = TextAlign.Start
                            )

                        }

                    }

                    // Edit Information Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {

                        //Edit Info Button
                        if (!isEditing) {
                            Button(
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.padding(0.dp),
                                onClick = {
                                    isEditing = !isEditing
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = " Edit Info",
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    " Edit Info",
                                    fontSize = 15.sp
                                )
                            }
                        }

                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {

                        //Editable Fields for Info
                        if (isEditing) {
                            Column {

                                //Gender
                                Text(
                                    "Edit Gender: ",
                                    fontWeight = FontWeight.W700,
                                    fontSize = 15.sp,
                                    color = mealColors.secondary
                                )
                                radioOptions.forEach { text ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(0.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = (text == selectedOption),
                                            onClick = { onOptionSelected(text) }
                                        )
                                        Text(text = text)
                                    }
                                }

                                // Age
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = if (age.toInt() > 0) age.toString() else "",
                                    onValueChange = { newAgeText ->
                                        if (newAgeText.isEmpty()) {
                                            age = 0 // Set age to 0 when the field is empty
                                        } else if (newAgeText.matches(Regex("^[1-9]\\d*$"))) {
                                            val newAge = newAgeText.toInt()
                                            age = newAge
                                        }
                                    },
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedLabelColor = mealColors.secondary,
                                        unfocusedBorderColor = mealColors.secondary,
                                        errorBorderColor = MaterialTheme.colors.error,
                                    ),
                                    label = {
                                        Text(
                                            "Edit Age:",
                                            fontWeight = FontWeight.W700,
                                            fontSize = 15.sp,
                                        )
                                    },
                                    placeholder = { Text(if (age.toInt() > 0) age.toString() else "Enter Age") }
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = if (weight > 0) "$weight kg" else "",
                                    onValueChange = { newWeightText ->
                                        val newWeight =
                                            newWeightText.removeSuffix(" kg").toFloatOrNull()
                                        if (newWeight != null) {
                                            weight = newWeight
                                        }
                                    }, colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedLabelColor = mealColors.secondary,
                                        unfocusedBorderColor = mealColors.secondary,
                                        errorBorderColor = MaterialTheme.colors.error,
                                    ),
                                    label = {
                                        Text(
                                            "Edit Weight:",
                                            fontWeight = FontWeight.W700,
                                            fontSize = 15.sp,
                                        )
                                    },
//                                    label = { Text("Current Weight: " + (if (weight > 0) weight.toString() else "") + " kg") },
                                    placeholder = { Text(if (weight > 0) "$weight kg" else "Enter Weight in kg") }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = if (height > 0) "$height cm" else "",
                                    onValueChange = { newHeightText ->
                                        val newHeight =
                                            newHeightText.removeSuffix(" cm").toFloatOrNull()
                                        if (newHeight != null) {
                                            height = newHeight
                                        }
                                    }, colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedLabelColor = mealColors.secondary,
                                        unfocusedBorderColor = mealColors.secondary,
                                        errorBorderColor = MaterialTheme.colors.error,
                                    ),
                                    label = {
                                        Text(
                                            "Edit Height:",
                                            fontWeight = FontWeight.W700,
                                            fontSize = 15.sp,
                                        )
                                    },
//                                    label = { Text("Current Height: " + (if (height > 0) height.toString() else "") + " cm") },
                                    placeholder = { Text(if (height > 0) "$height cm" else "Enter Height in cm") }
                                )

                                //Active
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    "Change Activity Habits: ",
                                    fontWeight = FontWeight.W700,
                                    fontSize = 15.sp,
                                    color = mealColors.secondary
                                )
                                ActivityRadioButton(
                                    options = activityOptions,
                                    selectedActivity = selectedActivity
                                ) { selectedOption ->
                                    selectedActivity = selectedOption
                                }

                            }
                        }
                    }

                    //Update Info Button
                    if (isEditing){
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp)
                        ) {
                            Column {
                                Button(
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.padding(0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        contentColor = Color.White,
                                        backgroundColor = mealColors.secondary,
                                    ),
                                    onClick = {
                                        var haveError = false;
                                        if (height <= 0.0) {
                                            mToast(mContext, "Please enter valid height")
                                            haveError = true
                                        }
                                        if (weight <= 0.0) {
                                            mToast(mContext, "Please enter valid weight")
                                            haveError = true
                                        }
                                        if (age.toInt() <= 0) {
                                            mToast(mContext, "Please enter valid age")
                                            haveError = true
                                        }
                                        var recommendedCaloriesDb = 0.0f
                                        if (selectedOption == "M") {
                                            recommendedCaloriesDb =
                                                (((13.397 * weight) + (4.799 * height) - (5.677 * age.toFloat()) + 88.362) * selectedActivity.toFloat()).toFloat()
                                        } else {
                                            recommendedCaloriesDb =
                                                (((9.247 * weight) + (3.098 * height) - (4.330 * age.toFloat()) + 447.593) * selectedActivity.toFloat()).toFloat()
                                        }
                                        if (!haveError) {
                                            val updateData: MutableMap<String, Any> =
                                                HashMap()
                                            updateData["weight"] = weight
                                            updateData["height"] = height
                                            updateData["age"] = age
                                            updateData["gender"] = selectedOption
                                            updateData["activity"] = selectedActivity
                                            updateData["recommended_calories"] =
                                                recommendedCaloriesDb
                                            val databaseReference =
                                                FirebaseDatabase.getInstance()
                                                    .getReference("users")
                                            databaseReference.child(
                                                sessionManager.getUserId().toString()
                                            ).updateChildren(
                                                updateData
                                            ) { databaseError, databaseReference ->
                                                if (databaseError != null) {
                                                    mToast(
                                                        mContext,
                                                        "An error has occurred while updating information"
                                                    )
                                                } else {
                                                    recommendedCalories =
                                                        recommendedCaloriesDb
                                                    mToast(
                                                        mContext,
                                                        "Successfully Updated Information"
                                                    )
                                                    RefreshButtonClickFlag = true
                                                    isEditing = !isEditing
                                                }
                                            }
                                        }
                                    }) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = " Update Info",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        " Update",
                                        fontSize = 15.sp
                                    )
                                }
                            }

                            Column {
                                Button(
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.padding(start = 10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        contentColor = Color.White,
                                        backgroundColor = mealColors.primaryVariant,
                                    ),
                                    onClick = {
                                        isEditing = !isEditing
                                    }) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "Cancel",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        "Cancel",
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 30.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CensoredUID(sessionManager.getUserId().toString())
                    }
                }

            }

//            UserDetails(sessionManager)

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                backgroundColor = mealColors.onPrimary,
                elevation = 5.dp
            ) {

                Column(modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = " Recommended Daily Calories: ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W900
                        )

                        val calorieRec = recommendedCalories.roundToInt().toString()
                        Text(
                            text = "$calorieRec kcals",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Italic
                        )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))


            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                backgroundColor = mealColors.onPrimary,
                elevation = 5.dp
            ) {

                Column(modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = " Recommended Foods: ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W900
                    )

                    Text(
                        text = "3 Foods within your daily caloric limit: ",
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic
                    )

                    Spacer(modifier = Modifier.height(10.dp))


                    if (showRecommendedFoods) {
                        RecommendedFoodListRow(recommendedFoodList, recommendedCalories)
                    }

                    // Refresh Button
                    Button(shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.White,
                            backgroundColor = mealColors.secondary,
                        ),
                        onClick = {
                            showRecommendedFoods = false
                            RefreshButtonClickFlag = true
                            refreshed = true
                            showRecommendedFoods = true
                        }) {
                        Icon(
                            painterResource(id = R.drawable.ic_refresh),
                            contentDescription = "Refresh",
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            " Refresh Suggestions",
                            fontSize = 14.sp
                        )
                    }

                }
            }


            Spacer(modifier = Modifier.height(20.dp))

            // LogOut Button
            Button(shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White,
                    backgroundColor = mealColors.primaryVariant,
                ),
                onClick = {
                    // Sign out user from Firebase and clear session
                    FirebaseAuthUtil.signOut()
                    sessionManager.clearUserData()
                    // Go to HomeNavigation
                    val intent = Intent(mContext, AccountNavigationActivity::class.java)
                    mContext.startActivity(intent)
                }) {
                Icon(
                    painterResource(id = R.drawable.ic_logout),
                    contentDescription = "Log Out",
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    " Log Out",
                    fontSize = 14.sp
                )
            }
        }
    }
    if (RefreshButtonClickFlag) {
        RefreshButtonClickFlag = false
        mToast(mContext, "Please wait...")
        refreshed = false
        showRecommendedFoods = true
    }
}


@Composable
fun RecommendedFoodListRow(items: List<FoodItem>, calorieLimit: Float) {
    val randomFoods = mutableListOf<FoodItem>()
    var remainingCalories = calorieLimit
    val shuffledList = items.shuffled()
    var beverageCount = 0
    var itemCount = 0  // track the number of items added to the list

    Log.i("recommended list", "pressed")

    for (food in shuffledList) {
        if (itemCount >= 3) {  // stops adding items once 6 are added
            break
        }
        if (food.calories > 0 && food.calories <= remainingCalories) {
            if (food.name.contains("beverage", ignoreCase = true)) {
                if (beverageCount < 1) {
                    randomFoods.add(food)
                    beverageCount++
                    itemCount++  // increment itemCount each time an item is added to the list
                    remainingCalories -= food.calories.toFloat()
                }
            } else {
                randomFoods.add(food)
                itemCount++  // increment itemCount each time an item is added to the list
                remainingCalories -= food.calories.toFloat()
            }
        }
        if (remainingCalories <= 0) {
            break
        }
    }

    val caloriesUsed = calorieLimit - remainingCalories

    Column {
        randomFoods.forEach { item ->
            FoodItemRow(item)
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Calories: $caloriesUsed",
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
private fun FoodItemRow(item: FoodItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(5.dp),
        backgroundColor = mealColorsAlt.surface,
    ){
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)) {
            Text(
                text = item.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.W700
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
                text = item.calories.toString() + " kcals",
            )
        }

    }
    Spacer(modifier = Modifier.height(8.dp))
}


@Composable
fun CensoredUID(input: String) {
    var isCensored by remember { mutableStateOf(true) }
    val displayText = if (isCensored) censorString(input) else input

    Spacer(modifier = Modifier.height(5.dp))

    Text(
        text = "UID: $displayText",
        textAlign = TextAlign.Center,
        fontSize = 10.sp,
        fontStyle = FontStyle.Italic,
        modifier = Modifier.clickable { isCensored = !isCensored }
    )
}


fun censorString(text: String): String {
    return text.mapIndexed { index, char ->
        when (index) {
            0, text.length - 1 -> char
            else -> '*'
        }
    }.joinToString(separator = "")
}



