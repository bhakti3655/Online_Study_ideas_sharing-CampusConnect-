package com.example.campusconnectadmin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class SkillAdapter(private val list: List<Skill>) :
    RecyclerView.Adapter<SkillAdapter.ViewHolder>() {

    private val DATABASE_URL = "https://campusconnectdb-default-rtdb.firebaseio.com/"

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.txtSkillName)
        val category: TextView = view.findViewById(R.id.txtCategory)
        val student: TextView = view.findViewById(R.id.txtByStudent)
        val time: TextView = view.findViewById(R.id.txtTime)
        val approve: Button = view.findViewById(R.id.btnApprove)
        val reject: Button = view.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_skill, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val skill = list[position]
        
        holder.name.text = skill.name
        holder.category.text = skill.category ?: "Offering"
        
        val displayName = if (skill.studentName.isNullOrEmpty()) "Anonymous" else skill.studentName
        holder.student.text = "By: $displayName"
        
        holder.time.text = skill.timestamp ?: "Just now"

        // REQUIREMENT: Show Approve button for pending skills
        if (skill.status == "pending") {
            holder.approve.visibility = View.VISIBLE
            holder.approve.text = "Approve"
            holder.reject.text = "Reject"
        } else {
            // Already approved or rejected
            holder.approve.visibility = View.GONE
            holder.reject.text = "Remove"
        }

        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("Skills")

        holder.approve.setOnClickListener {
            skill.skillId?.let { id ->
                ref.child(id).child("status").setValue("approved").addOnSuccessListener {
                    Toast.makeText(holder.itemView.context, "Skill Approved! Visible to users.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        holder.reject.setOnClickListener {
            skill.skillId?.let { id ->
                ref.child(id).removeValue().addOnSuccessListener {
                    Toast.makeText(holder.itemView.context, "Skill Removed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount() = list.size
}
