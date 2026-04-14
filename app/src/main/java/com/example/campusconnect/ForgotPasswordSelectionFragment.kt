package com.example.campusconnect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.campusconnect.databinding.FragmentForgotPasswordSelectionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ForgotPasswordSelectionFragment : Fragment() {

    private var _binding: FragmentForgotPasswordSelectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var selection = "email" // Default selection

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.cardEmail.setOnClickListener {
            selection = "email"
            updateUI()
        }

        binding.cardSms.setOnClickListener {
            selection = "sms"
            updateUI()
        }

        binding.btnContinue.setOnClickListener {
            val email = binding.etIdentifierEmail.text.toString().trim()
            if (email.isEmpty()) {
                binding.etIdentifierEmail.error = "Enter registered email"
                return@setOnClickListener
            }

            if (selection == "email") {
                sendEmailReset(email)
            } else {
                fetchPhoneAndStartSmsFlow(email)
            }
        }
    }

    private fun updateUI() {
        if (selection == "email") {
            binding.cardEmail.setStrokeColor(resources.getColor(R.color.primaryRed))
            binding.cardEmail.strokeWidth = 6
            binding.cardSms.setStrokeColor(resources.getColor(R.color.light_gray_bg))
            binding.cardSms.strokeWidth = 2
        } else {
            binding.cardSms.setStrokeColor(resources.getColor(R.color.primaryRed))
            binding.cardSms.strokeWidth = 6
            binding.cardEmail.setStrokeColor(resources.getColor(R.color.light_gray_bg))
            binding.cardEmail.strokeWidth = 2
        }
    }

    private fun sendEmailReset(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Reset link sent to your email", Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun fetchPhoneAndStartSmsFlow(email: String) {
        // Query database to find user with this email and get their phone number
        database.reference.child("Users").orderByChild("email").equalTo(email)
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    var phoneNumber: String? = null
                    for (userSnapshot in snapshot.children) {
                        phoneNumber = userSnapshot.child("mobile").value.toString()
                    }

                    if (phoneNumber != null) {
                        val bundle = Bundle().apply {
                            putString("phoneNumber", phoneNumber)
                        }
                        findNavController().navigate(R.id.action_forgotPasswordSelectionFragment_to_otpVerificationFragment, bundle)
                    } else {
                        Toast.makeText(requireContext(), "Phone number not found for this account", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "No account found with this email", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}