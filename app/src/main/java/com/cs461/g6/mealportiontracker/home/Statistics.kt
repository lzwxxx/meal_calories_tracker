package com.cs461.g6.mealportiontracker.home

import android.content.Context
import android.content.res.Configuration
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.cs461.g6.mealportiontracker.core.FirebaseAuthUtil
import com.cs461.g6.mealportiontracker.core.SessionManager
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.cs461.g6.mealportiontracker.theme.MealTheme
import com.cs461.g6.mealportiontracker.theme.mealColors
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

val myRedColor = Color(0xFFE25546)
val myGreenColor = Color(0xFF5BA55B)
val myBlueColor = Color(0xFF61ABDD)

data class StatisticsData(
    val totalCalories: Float,
    val totalFats: Float,
    val totalProteins: Float,
    val totalCarbo: Float
)

data class FoodInfoWithDate(
    val name: String = "",
    val calories: Float = 0.0f,
    val proteins: Float = 0.0f,
    val carbo: Float = 0.0f,
    val fats: Float = 0.0f,
    val date: String = "",
    val imageUrl: String = "",
    val userId: String = ""
)

class Statistics : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up any necessary NavHostController and SessionManager here if needed
        setContent {
                // You can pass your NavHostController and SessionManager to ScreenStats
                val navController: NavHostController = remember { NavHostController(this) }
                val sessionManager: SessionManager = remember { SessionManager(this) }

            MealTheme {
                // Display the ScreenStats composable within the ComposeView
                ScreenStats(sessionManager, navController)
            }

        }
    }
}

@Composable
fun ScreenStats(sessionManager: SessionManager, navController: NavHostController) {
    var totalCalories by remember { mutableStateOf(0.0f) }
    var totalFats by remember { mutableStateOf(0.0f) }
    var totalProteins by remember { mutableStateOf(0.0f) }
    var totalCarbo by remember { mutableStateOf(0.0f) }

    var recommendedCalories by remember { mutableStateOf(0) }

    val currentUser = FirebaseAuthUtil.getCurrentUser()
    var selectedFilter by remember { mutableStateOf("Today") }
    val context = LocalContext.current

    var todaySelected by remember { mutableStateOf(true) }


    // Function to calculate the date range for the current week
    fun calculateDateRangeForWeek(): Pair<String, String> {
        val calendar = Calendar.getInstance()
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        calendar.time = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(currentDate)

        // Calculate the start of the week (Monday)
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysToSubtract = if (currentDayOfWeek == Calendar.SUNDAY) 6 else currentDayOfWeek - 2
        calendar.add(Calendar.DAY_OF_MONTH, -daysToSubtract)
        val startOfWeekDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)

        // Calculate the end of the week (Sunday)
        calendar.add(Calendar.DAY_OF_MONTH, 6)
        val endOfWeekDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)

        return Pair(startOfWeekDate, endOfWeekDate)
    }


    if (currentUser != null) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val currentDate = dateFormat.format(Date())
        val filterDate: String
        val endDate: String

        if (selectedFilter == "Today") {
            filterDate = currentDate
            endDate = currentDate
        } else {
            val (startOfWeekDate, endOfWeekDate) = calculateDateRangeForWeek()
            filterDate = startOfWeekDate
            endDate = endOfWeekDate
        }

        LaunchedEffect(currentUser to filterDate to selectedFilter) {
            fetchUserMealHistories(currentUser.uid, filterDate, endDate) { mealHistories ->
                val (calories, fats, proteins, carbo) = calculateTotalStatistics(mealHistories)
                totalCalories = calories
                totalFats = fats
                totalProteins = proteins
                totalCarbo = carbo
            }
            fetchUserRecommendedCalories(currentUser.uid) { calories ->
                recommendedCalories = calories
            }
        }
    }

    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val portraitHeight = 320.dp
    val landscapeHeight = 670.dp

    val height = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        landscapeHeight
    } else {
        portraitHeight
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = scrollState)
            .height(height)
            .padding(bottom = 20.dp)
    ) {
        // Put your DateFilterButtons inside the Column
        DateFilterTabs(
            todaySelected = todaySelected,
            onTodayClicked = {
                todaySelected = true
                selectedFilter = "Today"
            },
            onThisWeekClicked = {
                todaySelected = false
                selectedFilter = "This Week"
            }
        )


        Spacer(modifier = Modifier.height(5.dp))


        if (totalCalories > 0 || totalFats > 0 || totalProteins > 0 || totalCarbo > 0) {
            val total = totalFats + totalProteins + totalCarbo
            val fatPercentage = (totalFats / total) * 100
            val proteinPercentage = (totalProteins / total) * 100
            val carboPercentage = (totalCarbo / total) * 100

            // Put your MealStatsContent inside the Column
            MealStatsContent(
                totalCalories = totalCalories,
                totalFats = totalFats,
                totalProteins = totalProteins,
                totalCarbo = totalCarbo,
                recommendedCalories = recommendedCalories,
                fatPercentage = fatPercentage,
                proteinPercentage = proteinPercentage,
                carboPercentage = carboPercentage
            )

        } else {
            // Put your NoRecordsFoundContent inside the Column
            NoRecordsFoundContent(context)
        }

//        Spacer(modifier = Modifier.height(25.dp))
    }

}

@Composable
fun DateFilterTabs(todaySelected: Boolean, onTodayClicked: () -> Unit, onThisWeekClicked: () -> Unit) {
    TabRow(
        selectedTabIndex = if (todaySelected) 0 else 1,
        modifier = Modifier.fillMaxWidth(),
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                height = (4.dp),
                color = mealColors.secondary,
                modifier = Modifier
                    .tabIndicatorOffset(tabPositions[if (todaySelected) 0 else 1])
            )
        }
    ) {
        Tab(selected = todaySelected, onClick = onTodayClicked) {
            Text("Today",
                fontWeight = FontWeight.W500,
                color = Color.White,
                modifier = Modifier.padding(10.dp))
        }
        Tab(selected = !todaySelected, onClick = onThisWeekClicked) {
            Text("This Week",
                fontWeight = FontWeight.W500,
                color = Color.White,
                modifier = Modifier.padding(10.dp))
        }
    }
}


@Composable
fun MealStatsContent(
    totalCalories: Float,
    totalFats: Float,
    totalProteins: Float,
    totalCarbo: Float,
    recommendedCalories: Int,
    fatPercentage: Float,
    proteinPercentage: Float,
    carboPercentage: Float
) {

    val configuration = LocalConfiguration.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(3.dp)
    ) {

        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Recommended Calories (Per Day):")
                }
                append(" $recommendedCalories")
            }
        )

        Spacer(modifier = Modifier.height(25.dp))

        val pieChartSizePercentage = 0.5f  // Adjust this value as needed

        // Calculate the PieChart size based on the screen width
        val screenWidth = configuration.screenWidthDp
        val pieChartSize = (screenWidth * pieChartSizePercentage).dp

        // Display the pie chart
        PieChart(
            fatPercentage = fatPercentage,
            proteinPercentage = proteinPercentage,
            carboPercentage = carboPercentage,
            modifier = Modifier.size(pieChartSize)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            PieChartLegend()
        }

        Spacer(modifier = Modifier.height(25.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Calories", fontWeight = FontWeight.Bold)
                Text("$totalCalories kcals")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
            ) {
                Text("Total Macronutrients (per 100g)", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(15.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Fats:", fontWeight = FontWeight.Bold)
                Text("$totalFats")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Proteins: ", fontWeight = FontWeight.Bold)
                Text("$totalProteins")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Carbohydrates: ", fontWeight = FontWeight.Bold)
                Text("$totalCarbo")
            }
        }
    }
}


@Composable
fun NoRecordsFoundContent(context: Context) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(3.dp)
    ) {
        Text(
            text = "No records found! Log a meal now to view your stats :)",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}


private fun fetchUserMealHistories(userId: String, startDate: String, endDate: String, callback: (List<FoodInfoWithDate>) -> Unit) {
    val databaseReference = FirebaseDatabase.getInstance().getReference("meal_histories")
    val userQuery: Query = databaseReference.orderByChild("userId").equalTo(userId)

    userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val mealHistories = mutableListOf<FoodInfoWithDate>()
            for (dataSnapshot in snapshot.children) {
                val foodInfoWithDate = dataSnapshot.getValue(FoodInfoWithDate::class.java)
                foodInfoWithDate?.let {
                    if (isDateInRange(it.date, startDate, endDate)) {
                        mealHistories.add(it)
                    }
                }
            }
            callback(mealHistories)
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle database query error
            callback(emptyList())
        }
    })
}


private fun isDateInRange(date: String, startDate: String, endDate: String): Boolean {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val parsedDate = dateFormat.parse(date)
    val parsedStartDate = dateFormat.parse(startDate)
    val parsedEndDate = dateFormat.parse(endDate)

    Log.d("DateDebug", "Date: $date")
    Log.d("DateDebug", "StartDate: $startDate")
    Log.d("DateDebug", "EndDate: $endDate")

    // Check if the date is within the range
    val isInRange = parsedDate in parsedStartDate..parsedEndDate
    Log.d("DateDebug", "Is in Range: $isInRange")

    return isInRange
}


private fun calculateTotalStatistics(mealHistories: List<FoodInfoWithDate>): StatisticsData {
    var totalCalories = 0.0f
    var totalFats = 0.0f
    var totalProteins = 0.0f
    var totalCarbo = 0.0f

    for (meal in mealHistories) {
        totalCalories += meal.calories
        totalFats += meal.fats
        totalProteins += meal.proteins
        totalCarbo += meal.carbo
    }

    return StatisticsData(totalCalories, totalFats, totalProteins, totalCarbo)
}

@Composable
fun PieChart(fatPercentage: Float, proteinPercentage: Float, carboPercentage: Float, modifier: Modifier) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.minDimension / 2
        val startAngle = 0f

        // Draw the fat segment
        val fatSweepAngle = (fatPercentage / 100) * 360
        drawArc(
            color = myRedColor,
            startAngle = startAngle,
            sweepAngle = fatSweepAngle,
            useCenter = true,
            style = Fill
        )

        // Draw the protein segment
        val proteinSweepAngle = (proteinPercentage / 100) * 360
        drawArc(
            color = myGreenColor,
            startAngle = startAngle + fatSweepAngle,
            sweepAngle = proteinSweepAngle,
            useCenter = true,
            style = Fill
        )

        // Draw the carbohydrate segment
        val carboSweepAngle = (carboPercentage / 100) * 360
        drawArc(
            color = myBlueColor,
            startAngle = startAngle + fatSweepAngle + proteinSweepAngle,
            sweepAngle = carboSweepAngle,
            useCenter = true,
            style = Fill
        )

        // Update the protein percentage label
        drawPercentageLabel(centerX, centerY, radius, startAngle, fatSweepAngle, "${fatPercentage.toInt()}%")
        drawPercentageLabel(centerX, centerY, radius, startAngle + fatSweepAngle, proteinSweepAngle, "${proteinPercentage.toInt()}%")
        drawPercentageLabel(centerX, centerY, radius, startAngle + fatSweepAngle + proteinSweepAngle, carboSweepAngle, "${carboPercentage.toInt()}%")

    }
}

private fun DrawScope.drawPercentageLabel(centerX: Float, centerY: Float, radius: Float, startAngle: Float, sweepAngle: Float, label: String) {
    val angle = startAngle + sweepAngle / 2
    val textX = centerX + (radius / 1.5f) * cos(Math.toRadians(angle.toDouble()).toFloat())
    val textY = centerY + (radius / 1.5f) * sin(Math.toRadians(angle.toDouble()).toFloat())
    val fontSize = 35f
    val paint = Paint().apply {
        color = Color.Black.toArgb()
        textAlign = Paint.Align.CENTER
        textSize = fontSize
    }
    drawIntoCanvas {
        it.nativeCanvas.drawText(label, textX, textY, paint)
    }
}


fun fetchUserRecommendedCalories(userId: String, callback: (Int) -> Unit) {
    val databaseReference = FirebaseDatabase.getInstance().getReference("users")

    databaseReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val recommendedCalories = snapshot.child("recommended_calories").getValue(Int::class.java)
            recommendedCalories?.let {
                callback(it)
            } ?: run {

                callback(0)
            }
        }

        override fun onCancelled(error: DatabaseError) {

            callback(0)
        }
    })
}

@Composable
fun PieChartLegend() {
    Row(
        modifier = Modifier.padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        PieChartLegendItem(myRedColor, "Fats")
        Spacer(modifier = Modifier.width(16.dp))
        PieChartLegendItem(myGreenColor, "Proteins")
        Spacer(modifier = Modifier.width(16.dp))
        PieChartLegendItem(myBlueColor, "Carbohydrates")
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
fun PieChartLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp, 16.dp)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = TextStyle(fontWeight = FontWeight.Bold))
    }
}





