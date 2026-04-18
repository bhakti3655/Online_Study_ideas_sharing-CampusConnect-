package com.example.campusconnectadmin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class IdeaAdapter(private val list: List<Idea>) :
    RecyclerView.Adapter<IdeaAdapter.ViewHolder>() {

    private val DATABASE_URL = "https://campusconnectdb-default-rtdb.firebaseio.com/"

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.txtTitle)
        val desc: TextView = view.findViewById(R.id.txtDesc)
        val student: TextView = view.findViewById(R.id.txtStudent)
        val category: TextView = view.findViewById(R.id.txtCategory)
        val status: TextView = view.findViewById(R.id.txtStatus)
        val approve: Button = view.findViewById(R.id.btnApprove)
        val reject: Button = view.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_idea, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val idea = list[position]

        holder.title.text = idea.title
        holder.desc.text = idea.description
        
        val displayName = if (idea.studentName.isNullOrEmpty()) "Anonymous" else idea.studentName
        holder.student.text = "By: $displayName"
        
        holder.category.text = idea.category ?: "Startup"
        holder.status.text = idea.status?.uppercase()

        // Step 1: Differentiate between Admin-added and User-submitted ideas
        val isAdminAdded = idea.studentId == "admin_added"

        if (isAdminAdded) {
            // If admin added it, show only DELETE button
            holder.approve.visibility = View.GONE
            holder.reject.text = "Delete"
            holder.reject.visibility = View.VISIBLE
        } else {
            // User submitted: Show Approve/Reject or Delete if already approved
            if (idea.status == "approved") {
                holder.approve.visibility = View.GONE
                holder.reject.text = "Delete"
                holder.reject.visibility = View.VISIBLE
            } else if (idea.status == "rejected") {
                holder.approve.visibility = View.VISIBLE
                holder.approve.text = "Approve Now"
                holder.reject.text = "Delete Permanent"
                holder.reject.visibility = View.VISIBLE
            } else {
                // Pending status
                holder.approve.visibility = View.VISIBLE
                holder.approve.text = "Approve"
                holder.reject.visibility = View.VISIBLE
                holder.reject.text = "Reject"
            }
        }

        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("Startups")

        holder.approve.setOnClickListener {
            val id = idea.ideaId ?: return@setOnClickListener
            ref.child(id).child("status").setValue("approved").addOnSuccessListener {
                Toast.makeText(holder.itemView.context, "Idea Approved! Visible to students.", Toast.LENGTH_SHORT).show()
            }
        }

        holder.reject.setOnClickListener {
            val id = idea.ideaId ?: return@setOnClickListener
            
            if (holder.reject.text == "Reject") {
                // Just mark as rejected, don't delete yet
                ref.child(id).child("status").setValue("rejected").addOnSuccessListener {
                    Toast.makeText(holder.itemView.context, "Idea Rejected", Toast.LENGTH_SHORT).show()
                }
            } else {
                // "Delete" or "Delete Permanent" - Remove from DB entirely
                ref.child(id).removeValue().addOnSuccessListener {
                    Toast.makeText(holder.itemView.context, "Idea Deleted Permanently", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount() = list.size
}
