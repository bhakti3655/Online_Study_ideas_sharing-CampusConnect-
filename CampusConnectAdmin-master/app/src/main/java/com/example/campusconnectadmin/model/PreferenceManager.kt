package com.example.campusconnectadmin.model

import android.content.Context
import android.content.SharedPreferences

/**
 * Model: Handles all local data storage (SharedPreferences).
 */
class PreferenceManager(context: Context) {

    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE)

    fun setLoggedIn(isLoggedIn: Boolean, email: String? = null) {
        val editor = sharedPref.edit()
        editor.putBoolean("isLoggedIn", isLoggedIn)
        email?.let { editor.putString("adminEmail", it) }
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPref.getBoolean("isLoggedIn", false)
    }

    fun getAdminEmail(): String {
        return sharedPref.getString("adminEmail", "bhaktinarola77@gmail.com") ?: "bhaktinarola77@gmail.com"
    }

    fun clearSession() {
        sharedPref.edit().clear().apply()
    }
}
