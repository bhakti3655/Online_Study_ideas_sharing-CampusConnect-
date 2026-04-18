package com.example.campusconnectadmin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase

class UserAdapter(private val list: List<User>) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private val DATABASE_URL = "https://campusconnectdb-default-rtdb.firebaseio.com/"

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.txtName)
        val email: TextView = view.findViewById(R.id.txtEmail)
        val phone: TextView = view.findViewById(R.id.txtPhone)
        val skills: TextView = view.findViewById(R.id.txtSkills)
        val image: ImageView = view.findViewById(R.id.imgUser)
        val deleteBtn: ImageView = view.findViewById(R.id.btnDeleteUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = list[position]

        holder.name.text = user.name ?: "N/A"
        holder.email.text = user.email ?: "N/A"
        holder.phone.text = user.phone ?: "No Phone"
        holder.skills.text = "Skills: ${user.skills ?: "None listed"}"

        if (!user.profileImage.isNullOrEmpty()) {
            Glide.with(holder.itemView.context).load(user.profileImage).into(holder.image)
        }

        holder.deleteBtn.setOnClickListener {
            val uid = user.uid
            if (!uid.isNullOrEmpty()) {
                FirebaseDatabase.getInstance(DATABASE_URL)
                    .getReference("Users")
                    .child(uid)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(holder.itemView.context, "User deleted successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(holder.itemView.context, "Failed to delete user", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    override fun getItemCount() = list.size
}
