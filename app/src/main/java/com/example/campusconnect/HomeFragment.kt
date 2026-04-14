package com.example.campusconnect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.campusconnect.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Quick Actions - Redirects
        binding.cardFindSkills.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_skillsFragment)
        }

        binding.cardEvents.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_eventsFragment)
        }

        binding.cardStartup.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_startupFragment)
        }

        binding.cardProfile.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }

        // Removed Bottom Navigation listeners as they are now handled by MainActivity
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}