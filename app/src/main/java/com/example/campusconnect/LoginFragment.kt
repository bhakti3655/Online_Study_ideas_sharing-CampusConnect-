package com.example.campusconnect

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.campusconnect.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Configure Google Sign In
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        } catch (e: Exception) {
            // Toast.makeText(requireContext(), "Client ID missing in strings.xml", Toast.LENGTH_LONG).show()
        }

        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        binding.tvForgotPassword.setOnClickListener {
            // Navigate to Forgot Password Selection Screen instead of sending email directly
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordSelectionFragment)
        }

        binding.tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
        }
    }

    private fun loginUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnLogin.isEnabled = false
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    } else {
                        Toast.makeText(requireContext(), "Please verify your email first.", Toast.LENGTH_LONG).show()
                        auth.signOut()
                    }
                } else {
                    Toast.makeText(requireContext(), "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
                binding.btnLogin.isEnabled = true
            }
    }

    private fun signInWithGoogle() {
        if (::googleSignInClient.isInitialized) {
            val signInIntent = googleSignInClient.signInIntent
            googleLauncher.launch(signInIntent)
        } else {
            Toast.makeText(requireContext(), "Google Sign In not configured", Toast.LENGTH_SHORT).show()
        }
    }

    private val googleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(requireContext(), "Google Error: ${e.statusCode}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(requireContext(), "Google Login Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        val userId = firebaseUser.uid
                        val userRef = database.reference.child("Users").child(userId)
                        userRef.get().addOnSuccessListener { snapshot ->
                            if (!snapshot.exists()) {
                                val newUser = User(uid = userId, fullName = firebaseUser.displayName, email = firebaseUser.email)
                                userRef.setValue(newUser)
                            }
                            EmailSender.sendWelcomeEmail(firebaseUser.email!!, firebaseUser.displayName ?: "User")
                            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Firebase Auth Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
