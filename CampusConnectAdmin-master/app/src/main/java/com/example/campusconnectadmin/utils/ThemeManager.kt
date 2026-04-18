package com.example.campusconnectadmin.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/**
 * ThemeManager: Centralizes Dark Mode logic and persistence.
 * Prevents crashes during Activity recreation by handling states properly.
 */
object ThemeManager {
    private const val PREF_NAME = "ThemePrefs"
    private const val KEY_IS_DARK_MODE = "isDarkMode"

    fun applyTheme(context: Context) {
        val isDark = isDarkMode(context)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES 
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun setDarkMode(context: Context, isDark: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_DARK_MODE, isDark).apply()
        
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES 
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun isDarkMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_DARK_MODE, false)
    }
}
