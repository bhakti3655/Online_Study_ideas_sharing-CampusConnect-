package com.example.campusconnect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.campusconnect.databinding.FragmentEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupBranchSpinner()
        loadCurrentUserData()

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnUpdateProfile.setOnClickListener {
            updateProfile()
        }
    }

    private fun setupBranchSpinner() {
        val branches = arrayOf("BCA", "MCA", "B.Tech CE", "B.Tech IT")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, branches)
        binding.spinnerEditBranch.setAdapter(adapter)
    }

    private fun loadCurrentUserData() {
        val userId = auth.currentUser?.uid ?: return
        database.reference.child("Users").child(userId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    binding.etEditName.setText(it.fullName)
                    binding.etEditMobile.setText(it.mobile)
                    binding.spinnerEditBranch.setText(it.branch, false)
                    binding.etEditSkills.setText(it.skills)
                }
            }
        }
    }

    private fun updateProfile() {
        val name = binding.etEditName.text.toString().trim()
        val mobile = binding.etEditMobile.text.toString().trim()
        val branch = binding.spinnerEditBranch.text.toString().trim()
        val skills = binding.etEditSkills.text.toString().trim()

        if (name.isEmpty() || mobile.isEmpty() || branch.isEmpty() || skills.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return
        val updates = mapOf(
            "fullName" to name,
            "mobile" to mobile,
            "branch" to branch,
            "skills" to skills
        )

        binding.btnUpdateProfile.isEnabled = false
        database.reference.child("Users").child(userId).updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
                binding.btnUpdateProfile.isEnabled = true
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}