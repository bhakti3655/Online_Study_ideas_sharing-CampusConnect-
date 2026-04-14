package com.example.campusconnect

data class Message(
    val messageId: String? = null,
    val senderId: String? = null,
    val receiverId: String? = null,
    val message: String? = null,
    val timestamp: Long? = null,
    val status: Int = 1, // 1: Sent, 2: Delivered, 3: Read
    val seenAt: Long? = null, // Timestamp when the message was read
    val type: String = "text",
    val isDeleted: Boolean = false,
    val deletedFor: String? = null // "sender", "receiver", or null (for everyone)
)