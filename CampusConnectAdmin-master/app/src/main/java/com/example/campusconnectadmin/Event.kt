package com.example.campusconnectadmin

data class Event(
    var eventId: String? = "",
    var title: String? = "",
    var date: String? = "",
    var time: String? = "",
    var venue: String? = "",
    var category: String? = "",
    var description: String? = "",
    var imageUrl: String? = ""
)