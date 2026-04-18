package com.example.campusconnectadmin

import android.app.Application
import com.cloudinary.android.MediaManager

class CampusConnectApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Updated with your new Cloudinary credentials
        val config = hashMapOf(
            "cloud_name" to "dmnujg3vk",
            "api_key" to "541751494263181"
        )
        try {
            MediaManager.init(this, config)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}