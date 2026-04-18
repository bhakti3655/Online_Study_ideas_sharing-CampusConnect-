package com.example.campusconnect

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusconnect.databinding.FragmentStartupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class StartUpFragment : Fragment() {

    private var _binding: FragmentStartupBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var startupAdapter: StartupAdapter
    private val startupList = mutableListOf<Startup>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        
        setupRecyclerView()
        fetchApprovedIdeas()

        binding.fabAddIdea.setOnClickListener {
            showAddStartupDialog()
        }
    }

    private fun setupRecyclerView() {
        startupAdapter = StartupAdapter(startupList)
        binding.rvStartups.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStartups.adapter = startupAdapter
    }

    private fun fetchApprovedIdeas() {
        val ideasRef = database.reference.child("Ideas")
        
        // Filter: ONLY show approved ideas on the main Startup Hub screen
        ideasRef.orderByChild("status").equalTo("approved")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (_binding == null) return
                    startupList.clear()
                    for (ideaSnapshot in snapshot.children) {
                        val startup = ideaSnapshot.getValue(Startup::class.java)
                        if (startup != null) {
                            startupList.add(startup)
                        }
                    }
                    startupList.reverse() // Most recent first
                    startupAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    if (_binding != null) {
                        Toast.makeText(requireContext(), "Failed to load ideas", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    private fun showAddStartupDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Submit Startup Idea")
        builder.setMessage("Your idea will be visible after admin approval.")
        
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }
        
        val etTitle = EditText(requireContext()).apply { hint = "Startup Title" }
        val etCategory = EditText(requireContext()).apply { hint = "Category (e.g. Tech, Food)" }
        val etDesc = EditText(requireContext()).apply { hint = "Short Description" }
        
        layout.addView(etTitle)
        layout.addView(etCategory)
        layout.addView(etDesc)
        builder.setView(layout)
        
        builder.setPositiveButton("Submit") { _, _ ->
            val title = etTitle.text.toString().trim()
            val category = etCategory.text.toString().trim()
            val desc = etDesc.text.toString().trim()
            
            if (title.isNotEmpty()) {
                val userId = auth.currentUser?.uid ?: return@setPositiveButton
                
                database.reference.child("Users").child(userId).get().addOnSuccessListener { snapshot ->
                    val userName = snapshot.child("fullName").value?.toString() ?: "Student"
                    val ideaId = database.reference.child("Ideas").push().key ?: return@addOnSuccessListener
                    
                    val startup = Startup(
                        id = ideaId,
                        title = title,
                        category = category,
                        studentName = userName,
                        studentId = userId,
                        description = desc,
                        status = "pending" // Initial status is ALWAYS pending
                    )
                    
                    database.reference.child("Ideas").child(ideaId).setValue(startup)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Idea submitted for approval!", Toast.LENGTH_LONG).show()
                        }
                }
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}