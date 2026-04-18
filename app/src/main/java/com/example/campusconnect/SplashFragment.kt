package com.example.campusconnect

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.campusconnect.databinding.FragmentSplashBinding
import com.google.firebase.auth.FirebaseAuth

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load and start zoom-in animation on logo
        val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.zoom_in)
        binding.logo.startAnimation(anim)

        Handler(Looper.getMainLooper()).postDelayed({
            if (_binding == null) return@postDelayed
            
            val sharedPref = requireActivity().getSharedPreferences("CampusConnectPrefs", Context.MODE_PRIVATE)
            val isFirstRun = sharedPref.getBoolean("isFirstRun", true)

            val currentUser = FirebaseAuth.getInstance().currentUser

            if (isFirstRun) {
                // First time user: Show Registration
                findNavController().navigate(R.id.action_splashFragment_to_registrationFragment)
            } else {
                if (currentUser != null) {
                    // Already logged in: Show Home
                    findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                } else {
                    // Not logged in but not first time: Show Login
                    findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
                }
            }
        }, 3000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}