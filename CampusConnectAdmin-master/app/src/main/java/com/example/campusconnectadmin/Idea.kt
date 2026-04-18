package com.example.campusconnectadmin

data class Idea(
    var ideaId: String? = "",
    var title: String? = "",
    var description: String? = "",
    var category: String? = "",
    var studentName: String? = "",
    var studentId: String? = "",
    var status: String? = "pending",
    var imageUrl: String? = ""
)