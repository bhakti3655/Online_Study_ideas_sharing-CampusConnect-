package com.example.campusconnect

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusconnect.databinding.FragmentSkillsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SkillsFragment : Fragment() {

    private var _binding: FragmentSkillsBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var skillAdapter: SkillAdapter
    private val skillList = mutableListOf<Skill>()
    private val fullSkillList = mutableListOf<Skill>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSkillsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        
        // Redirect to login if not authenticated
        if (auth.currentUser == null) {
            findNavController().navigate(R.id.action_skillsFragment_to_loginFragment)
            return
        }
        
        setupRecyclerView()
        fetchSkills()
        setupSearch()

        // Navigation handled globally by BottomNavigationView in MainActivity
    }

    private fun setupRecyclerView() {
        skillAdapter = SkillAdapter(skillList, isProfilePage = false)
        binding.rvSkills.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSkills.adapter = skillAdapter
    }

    private fun fetchSkills() {
        val skillsRef = database.reference.child("Skills")
        skillsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return
                skillList.clear()
                fullSkillList.clear()
                for (skillSnapshot in snapshot.children) {
                    val skill = skillSnapshot.getValue(Skill::class.java)
                    if (skill != null && skill.status == "approved") {
                        skillList.add(skill)
                        fullSkillList.add(skill)
                    }
                }
                skillAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                if (_binding != null) {
                    Toast.makeText(requireContext(), "Failed to load skills", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setupSearch() {
        binding.etSearchSkills.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterSkills(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterSkills(query: String) {
        val filteredList = fullSkillList.filter { 
            it.skillName?.contains(query, ignoreCase = true) == true ||
            it.studentName?.contains(query, ignoreCase = true) == true
        }
        skillList.clear()
        skillList.addAll(filteredList)
        skillAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}