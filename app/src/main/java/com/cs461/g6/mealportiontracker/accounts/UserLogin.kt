package com.cs461.g6.mealportiontracker.accounts

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.material.*
import androidx.compose.runtime.*
import com.google.firebase.auth.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.cs461.g6.mealportiontracker.R
import com.google.firebase.database.FirebaseDatabase
import com.cs461.g6.mealportiontracker.core.FirebaseAuthUtil
import com.cs461.g6.mealportiontracker.home.AppScreen
import com.cs461.g6.mealportiontracker.home.HomeNavigationActivity
import com.cs461.g6.mealportiontracker.core.SessionManager


data class UserAuth(
    val userId: String,
    val email: String,
    val password: String
)

@Composable
fun LoginScreen(navController: NavHostController, sessionManager: SessionManager) {
    var isLoading by remember { mutableStateOf(false) } // To control the visibility of the progress dialog
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val mContext = LocalContext.current

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )
    {
        Image(
            painter = painterResource(id = R.drawable.logo_title_shadow),
            contentDescription = "App Logo",
            modifier = Modifier.width(300.dp)
        )
        OutlinedTextField(
            value = email,
            onValueChange = { newEmail -> email = newEmail },
            label = { Text(text = "Email") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { newPassword -> password = newPassword },
            label = { Text(text = "Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation()

        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            isLoading = true // Show the progress dialog
            FirebaseAuthUtil.loginUserWithEmailAndPassword(email.lowercase().trim(), password.trim())
                .addOnCompleteListener { task ->
                    isLoading = false // Hide the progress dialog

                    if (task.isSuccessful) {
                        val user = FirebaseAuthUtil.getCurrentUser()
//                        val userId = FirebaseAuthUtil.getCurrentUser()!!.uid
                        mToast(mContext, "Login Successful!")
                        sessionManager.saveUserData(user!!.uid, user.email?: "", password, true)

                        // Go to HomeNavigation
                        val intent = Intent(mContext, HomeNavigationActivity::class.java)
                        mContext.startActivity(intent)
                        //navController.navigate(AppScreen.ScreenProfile.name)

                    } else {
                        /*val exception = task.exception
                        mToast(mContext, "Login Failed: ${exception?.message}")*/
                        // Login failed
                        val exception = task.exception
                        var errorMessage = "Login failed. Please try again later."
                        // Check the type of exception and customize the error message accordingly
                        when (exception) {
                            is FirebaseAuthInvalidUserException -> {
                                errorMessage = "Invalid email address. Please enter a valid email."
                            }
                            is FirebaseAuthInvalidCredentialsException -> {
                                errorMessage = "Invalid password. Please enter the correct password."
                            }
                            is FirebaseAuthEmailException -> {
                                errorMessage = "Email address is not registered. Please sign up first."
                            }
                        }

                        mToast(mContext, errorMessage)

                    }
                }
            /*FirebaseAuthUtil.loginUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    isLoading = false // Hide the progress dialog

                    if (task.isSuccessful) {
                        val user = FirebaseAuthUtil.getCurrentUser()

                    } else {
                        val exception = task.exception
                        // Handle login failure, e.g., show an error message
                    }
                }*/
        }) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = {
            isLoading = true // Show the progress dialog

            FirebaseAuthUtil.registerUserWithEmailAndPassword(email.trim(), password)
                .addOnCompleteListener { task ->
                    isLoading = false // Hide the progress dialog

                    if (task.isSuccessful) {
                        val firebaseUser = FirebaseAuthUtil.getCurrentUser()
                        val userId = firebaseUser?.uid
                        val userEmail = firebaseUser?.email

                        // Save user data to Realtime Firebase database
                        if (userId != null && userEmail != null) {
                            val databaseReference = FirebaseDatabase.getInstance().getReference("users")
                            val user = UserAuth(userId, userEmail.lowercase().trim(), password) // Assuming you have a User data class

                            databaseReference.child(userId).setValue(user)
                                .addOnCompleteListener { saveTask ->
                                    if (saveTask.isSuccessful) {
                                        sessionManager.saveUserData(user.userId,
                                            user.email, password, true)
                                        // Registration and data save were successful
                                        // Save user data to session
                                        mToast(mContext, "Registration Successful!")
                                        navController.navigate(AppScreen.ScreenProfile.name)
                                    } else {
                                        // Handle database save failure
                                        val saveException = saveTask.exception
                                        mToast(mContext, "Failed to save user data: ${saveException?.message}")
                                    }
                                }
                        }

                    } else {
                        /*val exception = task.exception
                        mToast(mContext, "Registration failed: ${exception?.message}")*/

                        // Handle registration failure
                        val exception = task.exception
                        var errorMessage = "Registration failed. Please try again later."
                        // Check the type of exception and customize the error message accordingly
                        when (exception) {
                            is FirebaseAuthWeakPasswordException -> {
                                errorMessage = "Weak password. Please choose a stronger password."
                            }
                            is FirebaseAuthInvalidCredentialsException -> {
                                errorMessage = "Invalid email format. Please enter a valid email address."
                            }
                            is FirebaseAuthUserCollisionException -> {
                                errorMessage = "Email address is already in use. Please use a different email."
                            }
                        }

                        mToast(mContext, errorMessage)

                    }
                }
            /*
                        FirebaseAuthUtil.registerUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false // Hide the progress dialog

                                if (task.isSuccessful) {
                                    val user = FirebaseAuthUtil.getCurrentUser()
                                } else {
                                    val exception = task.exception
                                    // Handle registration failure, e.g., show an error message
                                }
                            }*/
        }) {
            Text("Register")
        }

        // Progress Dialog
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(50.dp))
        }
    }
}

private fun mToast(context: Context, message: String){
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}

