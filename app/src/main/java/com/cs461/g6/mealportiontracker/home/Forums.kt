package com.cs461.g6.mealportiontracker.home

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cs461.g6.mealportiontracker.R
import com.cs461.g6.mealportiontracker.core.FirebaseAuthUtil
import com.cs461.g6.mealportiontracker.core.SessionManager
import com.cs461.g6.mealportiontracker.theme.MealTheme
import com.cs461.g6.mealportiontracker.theme.mealColors
import com.cs461.g6.mealportiontracker.theme.mealColorsAlt
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

data class Post(
    val userId: String = "",
    val postDate: String = "",
    val title: String = "",
    val body: String = ""
)

var postList = mutableListOf<Post>()

class Forums : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up any necessary NavHostController and SessionManager here if needed
        setContent {
            // You can pass your NavHostController and SessionManager to ScreenStats
            val navController: NavHostController = remember { NavHostController(this) }
            val sessionManager: SessionManager = remember { SessionManager(this) }


            MealTheme {
                // Display the ScreenStats composable within the ComposeView
                ScreenForums(navController, viewModel = viewModel, sessionManager)
            }
        }
    }
}
@Composable
fun ScreenForums(navController: NavHostController,
                 viewModel: MainViewModel,
                 sessionManager: SessionManager) {

    val currentUser = FirebaseAuthUtil.getCurrentUser()
    val searchText by viewModel.searchText.collectAsState()
    var query by remember { mutableStateOf(searchText) }
    val loading = remember { mutableStateOf(true) }

    LaunchedEffect(key1 = currentUser) {
        if (currentUser != null) {
            val databaseReference = FirebaseDatabase.getInstance().getReference("forum_posts")
            val postQuery: Query = databaseReference.orderByChild("postDate")

            postQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val temp = mutableListOf<Post>()
                    for (dataSnapshot in snapshot.children) {
                        val post = dataSnapshot.getValue(Post::class.java)
                        Log.d("Test", post.toString())
                        post?.let {
                            temp.add(it)
                        }
                    }
                    postList = temp
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
        Scaffold { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(start = 16.dp, end = 16.dp)
            ) {
                CreatePost(
                    onAddClick = {
                        //val intent = Intent(context, ManualInputActivity::class.java)
                        //context.startActivity(intent)
                        navController.navigate(AppScreen.ScreenAddPost.name)
                    }
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(postList.reversed()) { post ->
                        PostEntryCard(post)
                    }
                }
            }
        }
    }
}

@Composable
fun CreatePost(
    onAddClick: () -> Unit
) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { onAddClick()
                          }, // Call the provided onAddClick lambda when the button is clicked
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Back"
                )
                Text(" Create a Post")
            }
        }
}

@Composable
fun PostEntryCard(post: Post) {
    val isExpanded = remember { mutableStateOf(false) }
    val shortTextLimit = 70
    val currentUser = FirebaseAuthUtil.getCurrentUser()
    val currentUserId = currentUser?.uid
    val bodyText = if (isExpanded.value || post.body.length <= shortTextLimit) post.body else post.body.take(shortTextLimit)
    Card(
        modifier = Modifier
            .padding(vertical = 10.dp)
            .fillMaxWidth(),
        backgroundColor = if (post.userId == currentUserId) mealColorsAlt.surface else mealColors.background,
        shape = RoundedCornerShape(10.dp),
        elevation = 10.dp
    ) {
        Log.i("USER ID CHECK: ", "postid: $post.userId, userid: $currentUserId" )
        Column {
            Spacer(Modifier.height(15.dp))

            Row(Modifier.padding(horizontal = 15.dp)) {
                Column {

                    if (post.userId == currentUserId){
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Your Post",
                            tint = mealColors.secondaryVariant
                        )
                    }

                    Text(
                        text = post.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Text(
                        text = bodyText,
                        fontSize = 14.sp
                    )

                    if(post.body.length > shortTextLimit) {
                        TextButton(
                            onClick = { isExpanded.value = !isExpanded.value },
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .defaultMinSize(minHeight = 16.dp),
                        ) {
                            Text(
                                text = if (isExpanded.value) "Read Less" else "Read More",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.W800,
                                color = mealColors.secondary
                            )
                            Icon(
                                imageVector =
                                if (isExpanded.value) Icons.Filled.KeyboardArrowUp
                                else Icons.Filled.KeyboardArrowRight,
                                contentDescription = "Read More / Less",
                                tint = mealColors.secondary
                            )

                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ){
                Text(
                    textAlign = TextAlign.Right,
                    text = "Posted on: ${post.postDate}",
                    color = Color.Gray,
                    fontSize = 12.sp)
            }
        }
    }
}






