package com.example.campusconnectadmin

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SkillsFragment : Fragment() {

    private val list = mutableListOf<Skill>()
    private lateinit var adapter: SkillAdapter
    private val DATABASE_URL = "https://campusconnectdb-default-rtdb.firebaseio.com/"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_skills, container, false)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerSkills)
        val btnProfile = view.findViewById<ImageView>(R.id.btnProfile)

        btnProfile?.setOnClickListener {
            (activity as? MainActivity)?.openProfile()
        }

        recycler.layoutManager = LinearLayoutManager(requireContext())
        
        adapter = SkillAdapter(list)
        recycler.adapter = adapter

        val db = FirebaseDatabase.getInstance(DATABASE_URL).getReference("Skills")

        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                
                try {
                    list.clear()
                    for (data in snapshot.children) {
                        // Manually mapping with more fallback options to find the actual skill name
                        val skill = Skill(
                            skillId = data.key,
                            // Expanded check: looks for 'skill', 'skillName', 'name', or 'title'
                            name = data.child("skill").value?.toString() 
                                ?: data.child("skillName").value?.toString()
                                ?: data.child("name").value?.toString() 
                                ?: data.child("title").value?.toString() 
                                ?: "No Name",
                            
                            category = data.child("category").value?.toString() ?: "Offering",
                            
                            studentName = data.child("studentName").value?.toString() 
                                ?: data.child("userName").value?.toString() 
                                ?: data.child("name").value?.toString() // Fallback to name if it's user name
                                ?: "Anonymous",
                            
                            timestamp = data.child("timestamp").value?.toString() ?: "Recently",
                            status = data.child("status").value?.toString() ?: "pending"
                        )
                        list.add(skill)
                    }
                    list.reverse()
                    adapter.notifyDataSetChanged()
                } catch (e: Exception) {
                    Log.e("SkillsFragment", "Error mapping data: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        return view
    }
}
