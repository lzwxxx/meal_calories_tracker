package com.cs461.g6.mealportiontracker.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cs461.g6.mealportiontracker.core.FirebaseAuthUtil
import com.cs461.g6.mealportiontracker.core.SessionManager
import com.cs461.g6.mealportiontracker.theme.MealTheme
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Locale

class AddPost : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up any necessary NavHostController and SessionManager here if needed
        setContent {
            // You can pass your NavHostController and SessionManager to ScreenStats
            val navController: NavHostController = remember { NavHostController(this) }
            val sessionManager: SessionManager = remember { SessionManager(this) }

            MealTheme {
                // Display the ScreenStats composable within the ComposeView
                ScreenAddPost(sessionManager, navController, context = this)
            }
        }
    }
}

@Composable
fun ScreenAddPost(sessionManager: SessionManager, navController: NavHostController, context: Context) {

    val scrollState: ScrollState = rememberScrollState()

    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

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
                text = "Create new forum post",
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
                text = "Title",
                modifier = Modifier
                    .padding(start = 18.dp)
                    .fillMaxWidth(),
                style = LocalTextStyle.current.copy(fontSize = 16.sp),
                color = LocalContentColor.current.copy(alpha = 0.7f)
            )
            TextField(
                value = title,
                onValueChange = {
                    title = it
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent
                ),
                placeholder = { Text("Enter post title") },
                modifier = Modifier
                    .width(320.dp) // Set the width of the TextField
            )

            Spacer(modifier = Modifier.height(30.dp))

            //Calories
            Text(
                text = "Body",
                modifier = Modifier
                    .padding(start = 18.dp)
                    .fillMaxWidth(),
                style = LocalTextStyle.current.copy(fontSize = 16.sp),
                color = LocalContentColor.current.copy(alpha = 0.7f)
            )
            TextField(
                value = body,
                onValueChange = {
                    body = it
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent
                ),
                placeholder = { Text("Enter post body") },
                modifier = Modifier
                    .width(320.dp) // Set the width of the TextField
            )

            Spacer(modifier = Modifier.height(60.dp))

            Button(onClick = {
                val db: FirebaseDatabase = FirebaseDatabase.getInstance()

                val dbReference: DatabaseReference = db.getReference("forum_posts")
                val currentUser = FirebaseAuthUtil.getCurrentUser()
                var success = false

                val currentTimeMillis = System.currentTimeMillis()
                val sdf = SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault()
                ) // Change date format here
                val formattedDate = sdf.format(currentTimeMillis)
                var newPost: Map<String, String>? = null
                if (currentUser != null) {
                    newPost = mapOf(
                        "userId" to currentUser.uid,
                        "postDate" to formattedDate,
                        "title" to title,
                        "body" to body
                    )
                }

                dbReference.push().setValue(newPost)
                    .addOnSuccessListener {
                        success = true
                    }
                    .addOnFailureListener { e ->
                        Log.e("RealtimeDatabase", "Error writing document: $e")
                    }
                if (success){
                    Toast.makeText(
                        context,
                        "The new post has been successfully created.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                navController.navigate(AppScreen.ScreenForums.name)
            }) {
                Text("Submit")
            }

        }
    }
}