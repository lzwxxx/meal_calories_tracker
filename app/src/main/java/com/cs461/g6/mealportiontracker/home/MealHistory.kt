package com.cs461.g6.mealportiontracker.home

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cs461.g6.mealportiontracker.R
import com.cs461.g6.mealportiontracker.core.FirebaseAuthUtil
import com.cs461.g6.mealportiontracker.core.SessionManager
import com.cs461.g6.mealportiontracker.theme.MealTheme
import com.cs461.g6.mealportiontracker.theme.mealColors
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


data class MealEntry(
    val name: String = "",
    val calories: Float = 0.0f,
    val proteins: Float = 0.0f,
    val carbo: Float = 0.0f,
    val fats: Float = 0.0f,
    val date: String = "",
    val imageUrl: String = "",
    val userId: String = ""
)

enum class DateFilter { TODAY, LAST_WEEK, ALL_TIME }
var dateFilter = mutableStateOf(DateFilter.TODAY)
var mealHistories = mutableListOf<MealEntry>()

class MealHistory : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up any necessary NavHostController and SessionManager here if needed
        setContent {
            // You can pass your NavHostController and SessionManager to ScreenStats
//            val navController = rememberNavController()
//            val sessionManager: SessionManager = remember { SessionManager(this) }

            MealTheme {
                // Display the ScreenHistory composable within the ComposeView
                ScreenHistory()
            }
        }
        }
    }



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScreenHistory() {

    val loading = remember { mutableStateOf(true) }
    val currentUser = FirebaseAuthUtil.getCurrentUser()

    LaunchedEffect(key1 = currentUser) {
        if (currentUser != null ) {
            val databaseReference = FirebaseDatabase.getInstance().getReference("meal_histories")
            val userQuery: Query = databaseReference.orderByChild("userId").equalTo(currentUser.uid)
            userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val temp = mutableListOf<MealEntry>()
                    for (dataSnapshot in snapshot.children) {
                        val meal = dataSnapshot.getValue(MealEntry::class.java)
                        meal?.let {
                            temp.add(it)
                        }
                    }
                    mealHistories = temp
                    loading.value = false
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database query error
//                    callback(emptyList())
                }
            })
        }
    }
    if (!loading.value) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
        ) {

            DateFilterTabs(
                selectedFilter = dateFilter.value,
                onTodayClicked = { dateFilter.value = DateFilter.TODAY },
                onLastWeekClicked = { dateFilter.value = DateFilter.LAST_WEEK },
                onAllClicked = { dateFilter.value = DateFilter.ALL_TIME }
            )

            // List here
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp)
            ) {
                val meals = when (dateFilter.value) {
                    DateFilter.TODAY -> mealHistories.filterToday()
                    DateFilter.LAST_WEEK -> mealHistories.filterLastWeek()
                    DateFilter.ALL_TIME -> mealHistories.filterAllTime()
                }
                items(meals.reversed()) { meal ->
                    MealEntryCard(meal)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MealEntryCard(meal: MealEntry) {

    val openDialog = remember { mutableStateOf(false) }

    Card(
        onClick = { openDialog.value = true }, // Set dialog open state to true when card is clicked
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        elevation = 5.dp,
        backgroundColor = mealColors.background
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                    .padding(10.dp)
        ){
            Text(text = meal.date,
                color = Color.Gray,
                fontSize = 12.sp)
            Icon(
                painter = painterResource(id = R.drawable.ic_right),
                tint = mealColors.secondary,
                contentDescription = "More Info",
                )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(20.dp,30.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_restaurant),
                        contentDescription = "Meal Icon",
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = " " + meal.name.toTitleCase(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                    )
                }

                Text(text = "${meal.calories} kcals")
//                Text(text = "Protein: ${meal.proteins}")
//                Text(text = "Fat: ${meal.fats}")
//                Text(text = "Carbohydrates: ${meal.carbo}")
            }



        }

    }
    if (openDialog.value) {
        AlertDialog(
            backgroundColor = mealColors.background,
            onDismissRequest = { openDialog.value = false },
            title = {
                Text(text = " " + meal.name.toTitleCase(),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp)
            },
            text = {
                /* Display meal details */
//                Row(horizontalArrangement = Arrangement.Center){
//
//                    if (meal.imageUrl != "") {
//                        AsyncImage(
//                            model = ImageRequest.Builder(LocalContext.current)
//                                .data(meal.imageUrl)
//                                .crossfade(true)
//                                .build(),
//                            contentDescription = "",
//                            contentScale = ContentScale.Crop,
//                            modifier = Modifier
//                                .size(60.dp)
//                                .clip(RoundedCornerShape(10.dp)),
//                        )
//                    }
//                }
                Column {
                    Text(text = "${meal.calories} kcals")

                    Spacer(modifier = Modifier.size(15.dp))

                    Text(text = "Macronutrients (in grams): ", fontWeight = FontWeight.W500)
                    Spacer(modifier = Modifier.size(2.dp))
                    Text(text = "Protein: ${meal.proteins}")
                    Text(text = "Fat: ${meal.fats}")
                    Text(text = "Carbohydrates: ${meal.carbo}")
                }
            },
            confirmButton = {
                TextButton(
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = mealColors.secondary,
                        contentColor = Color.White),
                    onClick = { openDialog.value = false }) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.ic_exit),
//                        contentDescription = "Exit",
//                    )
                    Text(text = "Close")
                }
            },
            modifier = Modifier.padding(5.dp)
        )

    }
}

fun String.toTitleCase(): String {
    return split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}

@Composable
fun DateFilterTabs(
    selectedFilter: DateFilter,
    onTodayClicked: () -> Unit,
    onLastWeekClicked: () -> Unit,
    onAllClicked: () -> Unit
) {
    TabRow(
        selectedTabIndex = selectedFilter.ordinal,
        modifier = Modifier.fillMaxWidth(),
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                height = (4.dp),
                color = mealColors.secondary,
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedFilter.ordinal])
            )
        }
    ) {
        Tab(selected = selectedFilter == DateFilter.TODAY, onClick = onTodayClicked) {
            Text("Today",
                fontWeight = FontWeight.W500,
                color = Color.White,
                modifier = Modifier.padding(10.dp))
        }
        Tab(selected = selectedFilter == DateFilter.LAST_WEEK, onClick = onLastWeekClicked) {
            Text("Last Week",
                fontWeight = FontWeight.W500,
                color = Color.White,
                modifier = Modifier.padding(10.dp))
        }
        Tab(selected = selectedFilter == DateFilter.ALL_TIME, onClick = onAllClicked) {
            Text("All",
                fontWeight = FontWeight.W500,
                color = Color.White,
                modifier = Modifier.padding(10.dp))
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
fun List<MealEntry>.filterLastWeek(): List<MealEntry> {
    val oneWeekAgo = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    return this.filter {
        LocalDate.parse(it.date, formatter) >= oneWeekAgo.minusWeeks(1)
    }
}
@RequiresApi(Build.VERSION_CODES.O)
fun List<MealEntry>.filterToday(): List<MealEntry> {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    return this.filter {
        LocalDate.parse(it.date, formatter).isEqual(today)
    }
}

fun List<MealEntry>.filterAllTime(): List<MealEntry> {
    return this
}

