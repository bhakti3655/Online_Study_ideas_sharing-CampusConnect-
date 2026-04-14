package com.example.campusconnect

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.campusconnect.databinding.ItemSkillBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SkillAdapter(
    private val skillList: List<Skill>,
    private val isProfilePage: Boolean = false,
    private val onEditClick: ((Skill) -> Unit)? = null,
    private val onDeleteClick: ((Skill) -> Unit)? = null,
    private val onViewProfileClick: ((String) -> Unit)? = null
) : RecyclerView.Adapter<SkillAdapter.SkillViewHolder>() {

    class SkillViewHolder(val binding: ItemSkillBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {
        val binding = ItemSkillBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SkillViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SkillViewHolder, position: Int) {
        val skill = skillList[position]
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        
        holder.binding.tvSkillName.text = skill.skillName
        holder.binding.ratingBar.rating = skill.rating
        holder.binding.tvRating.text = "${skill.rating} (${skill.reviewsCount} Reviews)"
        holder.binding.tvSkillInitial.text = skill.skillName?.take(1)?.uppercase() ?: "S"

        // Disable rating for own skills
        if (skill.studentId == currentUserId || isProfilePage) {
            holder.binding.ratingBar.setIsIndicator(true)
        } else {
            holder.binding.ratingBar.setIsIndicator(false)
            holder.binding.ratingBar.setOnRatingBarChangeListener { _, rating, fromUser ->
                if (fromUser) {
                    saveRatingToFirebase(skill, rating, holder.itemView)
                }
            }
        }

        if (isProfilePage) {
            holder.binding.tvStudentName.visibility = View.GONE
            holder.binding.llSkillActions.visibility = View.VISIBLE
            holder.binding.llPublicActions.visibility = View.GONE
            
            holder.binding.btnEditSkill.setOnClickListener { onEditClick?.invoke(skill) }
            holder.binding.btnDeleteSkill.setOnClickListener { onDeleteClick?.invoke(skill) }
        } else {
            holder.binding.tvStudentName.visibility = View.VISIBLE
            holder.binding.tvStudentName.text = skill.studentName ?: "Unknown"
            holder.binding.llSkillActions.visibility = View.GONE
            holder.binding.llPublicActions.visibility = View.VISIBLE
            
            // Email Logic
            holder.binding.btnEmail.setOnClickListener {
                skill.studentEmail?.let { email ->
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                        putExtra(Intent.EXTRA_SUBJECT, "Query regarding your skill: ${skill.skillName}")
                    }
                    try {
                        holder.itemView.context.startActivity(Intent.createChooser(intent, "Send Email..."))
                    } catch (e: Exception) {
                        Toast.makeText(holder.itemView.context, "No email app found", Toast.LENGTH_SHORT).show()
                    }
                } ?: run {
                    Toast.makeText(holder.itemView.context, "Email address not available", Toast.LENGTH_SHORT).show()
                }
            }

            holder.binding.btnViewProfile.setOnClickListener {
                skill.studentId?.let { uid -> onViewProfileClick?.invoke(uid) }
            }
        }
    }

    private fun saveRatingToFirebase(skill: Skill, newRating: Float, view: View) {
        val skillId = skill.id ?: return
        val database = FirebaseDatabase.getInstance().reference.child("Skills").child(skillId)

        val totalReviews = skill.reviewsCount + 1
        val updatedRating = ((skill.rating * skill.reviewsCount) + newRating) / totalReviews

        val updates = mapOf(
            "rating" to updatedRating,
            "reviewsCount" to totalReviews
        )

        database.updateChildren(updates).addOnSuccessListener {
            Toast.makeText(view.context, "Rating Saved: $newRating Stars", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = skillList.size
}
