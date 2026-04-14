package com.example.campusconnect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.campusconnect.databinding.ItemStartupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class StartupAdapter(private val startupList: List<Startup>) : RecyclerView.Adapter<StartupAdapter.StartupViewHolder>() {

    class StartupViewHolder(val binding: ItemStartupBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StartupViewHolder {
        val binding = ItemStartupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StartupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StartupViewHolder, position: Int) {
        val startup = startupList[position]
        holder.binding.tvTitle.text = startup.title
        holder.binding.tvCategory.text = startup.category
        holder.binding.tvStudentName.text = "By: ${startup.studentName}"
        holder.binding.tvDescription.text = startup.description
        
        holder.binding.btnView.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable("startup", startup)
            it.findNavController().navigate(R.id.action_startupFragment_to_startupDetailsFragment, bundle)
        }

        holder.binding.btnJoinTeam.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            val userId = auth.currentUser?.uid
            
            if (userId == null) {
                Toast.makeText(holder.itemView.context, "Please login to join", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Real team join logic: add current user to the idea's team in database
            val database = FirebaseDatabase.getInstance()
            val startupId = startup.id ?: return@setOnClickListener
            
            // Get current user details from 'Users' node first to save in 'Ideas'
            database.reference.child("Users").child(userId).get().addOnSuccessListener { snapshot ->
                val userName = snapshot.child("fullName").value.toString()
                val userRole = "Member" // Default role
                
                val memberData = mapOf(
                    "name" to userName,
                    "role" to userRole,
                    "uid" to userId
                )

                database.reference.child("Ideas")
                    .child(startupId)
                    .child("teamMembers")
                    .child(userId)
                    .setValue(memberData)
                    .addOnSuccessListener {
                        Toast.makeText(holder.itemView.context, "Successfully joined the team!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(holder.itemView.context, "Failed to join team", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    override fun getItemCount(): Int = startupList.size
}