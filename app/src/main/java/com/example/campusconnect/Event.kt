package com.example.campusconnect

import java.io.Serializable

data class Event(
    val id: String? = null,
    val title: String? = null,
    val description: String? = null,
    val date: String? = null,
    val location: String? = null,
    val category: String? = null,
    val status: String? = "approved"
) : Serializable