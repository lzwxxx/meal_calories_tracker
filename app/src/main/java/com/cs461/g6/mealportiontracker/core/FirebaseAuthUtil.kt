package com.cs461.g6.mealportiontracker.core

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseAuthUtil {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    data class MealHistory(
        val userId: String,
        val name: String,
        val calories: Double,
        val proteins: Double,
        val carbo: Double,
        val fats: Double,
        val date: String,
        val imageUrl: String
    )

    fun registerUserWithEmailAndPassword(email: String, password: String): Task<AuthResult> {
        return auth.createUserWithEmailAndPassword(email, password)
    }

    fun loginUserWithEmailAndPassword(email: String, password: String): Task<AuthResult> {
        return auth.signInWithEmailAndPassword(email, password)
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }



    @RequiresApi(Build.VERSION_CODES.N)
    fun addMealHistory(
        userId: String,
        name: String,
        calories: Double,
        proteins: Double,
        carbo: Double,
        fats: Double,
        date: String,
        imageUrl: String
    ): Task<Boolean> {
        val db: FirebaseDatabase = FirebaseDatabase.getInstance()

        val dbReference: DatabaseReference = db.getReference("meal_histories")

        val future = TaskCompletionSource<Boolean>()

        // Create a Map to store your meal history data
        val mealHistoryData = mapOf(
            "userId" to userId,
            "name" to name,
            "calories" to calories,
            "proteins" to proteins,
            "carbo" to carbo,
            "fats" to fats,
            "date" to date,
            "imageUrl" to imageUrl
        )

        dbReference.push().setValue(mealHistoryData)
            .addOnSuccessListener {
                future.setResult(true) // Operation succeeded, set Task to true
            }
            .addOnFailureListener { e ->
                Log.e("RealtimeDatabase", "Error writing document: $e")
                future.setResult(false) // Operation failed, set Task to false in case of an error
            }

        return future.task
    }



    fun signOut() {
        auth.signOut()
    }
}