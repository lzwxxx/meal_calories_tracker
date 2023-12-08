package com.cs461.g6.mealportiontracker.core

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveUserData(userId: String, userEmail: String, password: String, isUserLoggedIn: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putString("user_id", userId)
        editor.putString("user_email", userEmail)
        editor.putString("user_password", password)
        editor.putBoolean("logged_in", isUserLoggedIn)
        editor.apply()
    }

    fun getUserId(): String? {
        return sharedPreferences.getString("user_id", null)
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString("user_email", null)
    }

    fun getUserPassword(): String? {
        return sharedPreferences.getString("user_password", null)
    }

    fun getIsUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("logged_in", false)
    }
    fun clearUserData() {
        val editor = sharedPreferences.edit()
        editor.remove("user_id")
        editor.remove("user_email")
        editor.remove("user_password")
        editor.remove("logged_in")
        editor.apply()
    }
}