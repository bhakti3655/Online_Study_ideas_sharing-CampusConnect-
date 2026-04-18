package com.example.campusconnect.repository

import com.example.campusconnect.Skill
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Model (MVC): Handles all Skill-related data operations.
 */
class SkillRepository {
    private val db = FirebaseDatabase.getInstance().reference.child("Skills")

    fun getAllApprovedSkills(callback: (List<Skill>) -> Unit) {
        db.orderByChild("status").equalTo("approved")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { it.getValue(Skill::class.java) }
                    callback(list)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun getMySkills(userId: String, callback: (List<Skill>) -> Unit) {
        db.orderByChild("studentId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { it.getValue(Skill::class.java) }
                    callback(list)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun addSkill(skill: Skill, onComplete: (Boolean) -> Unit) {
        val id = db.push().key ?: return
        val newSkill = skill.copy(id = id)
        db.child(id).setValue(newSkill).addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun deleteSkill(skillId: String, onComplete: (Boolean) -> Unit) {
        db.child(skillId).removeValue().addOnCompleteListener { onComplete(it.isSuccessful) }
    }
}