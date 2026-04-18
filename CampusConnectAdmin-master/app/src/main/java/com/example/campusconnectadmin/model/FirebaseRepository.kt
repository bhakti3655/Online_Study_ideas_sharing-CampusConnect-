package com.example.campusconnectadmin.model

import com.google.firebase.database.*

/**
 * Model: Handles all data interactions with Firebase Realtime Database.
 * This class is the single source of truth for data.
 */
class FirebaseRepository {

    private val DATABASE_URL = "https://campusconnectdb-default-rtdb.firebaseio.com/"
    private val database = FirebaseDatabase.getInstance(DATABASE_URL).reference

    // Fetch dynamic counts for Dashboard
    fun getDashboardCounts(listener: ValueEventListener) {
        database.addValueEventListener(listener)
    }

    // Skills Node
    fun getSkillsRef(): DatabaseReference = database.child("Skills")
    
    // Startups Node
    fun getStartupsRef(): DatabaseReference = database.child("Startups")
    
    // Users Node
    fun getUsersRef(): DatabaseReference = database.child("Users")

    // Update Status (Generic for Skills/Ideas)
    fun updateStatus(node: String, id: String, status: String, onComplete: (Boolean) -> Unit) {
        database.child(node).child(id).child("status").setValue(status)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    // Delete Record
    fun deleteRecord(node: String, id: String, onComplete: (Boolean) -> Unit) {
        database.child(node).child(id).removeValue()
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }
}
