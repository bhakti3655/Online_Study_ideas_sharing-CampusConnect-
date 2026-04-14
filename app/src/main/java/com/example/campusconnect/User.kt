package com.example.campusconnect

data class User(
    val uid: String? = null,
    val fullName: String? = null,
    val email: String? = null,
    val mobile: String? = null,
    val branch: String? = null,
    val skills: String? = null,
    val profileImageUrl: String? = null,
    val fcmToken: String? = null
)