package com.example.campusconnect

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.campusconnect.databinding.FragmentRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegistrationFragment : Fragment() {

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    // Password Validation Regex
    // ^                 # start-of-string
    // (?=.*[0-9])       # a digit must occur at least once
    // (?=.*[a-z])       # a lower case letter must occur at least once
    // (?=.*[A-Z])       # an upper case letter must occur at least once
    // (?=.*[@#$%^&+=])  # a special character must occur at least once
    // (?=\S+$)          # no whitespace allowed in the entire string
    // .{8,}             # anything, at least eight places though
    // $                 # end-of-string
    private val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$".toRegex()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupBranchSpinner()
        setupPasswordRealTimeValidation()

        binding.btnSignUp.setOnClickListener {
            registerUser()
        }

        binding.tvSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_registrationFragment_to_loginFragment)
        }
    }

    private fun setupBranchSpinner() {
        val branches = arrayOf("BCA", "MCA", "B.Tech CE", "B.Tech IT")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, branches)
        binding.spinnerBranch.setAdapter(adapter)
    }

    private fun setupPasswordRealTimeValidation() {
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = s.toString()
                if (password.isNotEmpty() && !password.matches(passwordRegex)) {
                    binding.etPassword.error = "Min 8 chars, 1 Uppercase, 1 Lowercase, 1 Number & 1 Special Char (@#$%^&+=)"
                } else {
                    binding.etPassword.error = null
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.matches(passwordRegex)
    }

    private fun registerUser() {
        val name = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val mobile = binding.etMobile.text.toString().trim()
        val branch = binding.spinnerBranch.text.toString().trim()
        val skills = binding.etSkills.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || mobile.isEmpty() || branch.isEmpty() || 
            skills.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isPasswordValid(password)) {
            binding.etPassword.error = "Password does not meet security requirements"
            Toast.makeText(requireContext(), "Weak Password", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Passwords do not match"
            return
        }

        if (!binding.cbTerms.isChecked) {
            Toast.makeText(requireContext(), "Please accept terms and conditions", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSignUp.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val userId = firebaseUser?.uid
                    
                    // Send Email Verification
                    firebaseUser?.sendEmailVerification()
                        ?.addOnCompleteListener { verificationTask ->
                            if (verificationTask.isSuccessful) {
                                Toast.makeText(requireContext(), "Verification email sent to $email. Please verify before login.", Toast.LENGTH_LONG).show()
                            }
                        }

                    val user = User(userId, name, email, mobile, branch, skills)

                    if (userId != null) {
                        database.reference.child("Users").child(userId).setValue(user)
                            .addOnSuccessListener {
                                // Mark that first-time registration is done
                                val sharedPref = requireActivity().getSharedPreferences("CampusConnectPrefs", Context.MODE_PRIVATE)
                                sharedPref.edit().putBoolean("isFirstRun", false).apply()

                                // Send welcome email
                                EmailSender.sendWelcomeEmail(email, name)

                                // After registration success, go to Login
                                findNavController().navigate(R.id.action_registrationFragment_to_loginFragment)
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Failed to save user data", Toast.LENGTH_SHORT).show()
                                binding.btnSignUp.isEnabled = true
                            }
                    }
                } else {
                    Toast.makeText(requireContext(), "Auth Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    binding.btnSignUp.isEnabled = true
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
