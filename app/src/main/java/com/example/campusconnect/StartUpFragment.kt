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
        fetchIdeas()

        binding.fabAddIdea.setOnClickListener {
            showAddStartupDialog()
        }

        // NOTE: Bottom Navigation listeners are removed because navigation 
        // is now handled globally by BottomNavigationView in MainActivity.
    }

    private fun setupRecyclerView() {
        startupAdapter = StartupAdapter(startupList)
        binding.rvStartups.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStartups.adapter = startupAdapter
    }

    private fun fetchIdeas() {
        val userId = auth.currentUser?.uid ?: ""
        val ideasRef = database.reference.child("Ideas")
        
        ideasRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return
                startupList.clear()
                for (ideaSnapshot in snapshot.children) {
                    val startup = ideaSnapshot.getValue(Startup::class.java)
                    if (startup != null) {
                        // User sees their own ideas always, others see only approved
                        if (startup.studentId == userId || startup.status == "approved") {
                            startupList.add(startup)
                        }
                    }
                }
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
        builder.setTitle("Post New Idea")
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }
        val etTitle = EditText(requireContext()).apply { hint = "Startup Title" }
        val etDesc = EditText(requireContext()).apply { hint = "Description" }
        layout.addView(etTitle)
        layout.addView(etDesc)
        builder.setView(layout)
        builder.setPositiveButton("Submit") { _, _ ->
            val title = etTitle.text.toString().trim()
            val desc = etDesc.text.toString().trim()
            if (title.isNotEmpty()) {
                val userId = auth.currentUser?.uid ?: return@setPositiveButton
                
                // Fetch user name for the startup entry
                database.reference.child("Users").child(userId).child("fullName").get().addOnSuccessListener { snapshot ->
                    val userName = snapshot.value?.toString() ?: "Unknown"
                    val ideaId = database.reference.child("Ideas").push().key ?: return@addOnSuccessListener
                    val startup = mapOf(
                        "id" to ideaId,
                        "title" to title,
                        "description" to desc,
                        "studentName" to userName,
                        "studentId" to userId,
                        "status" to "pending"
                    )
                    database.reference.child("Ideas").child(ideaId).setValue(startup)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Idea submitted!", Toast.LENGTH_SHORT).show()
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