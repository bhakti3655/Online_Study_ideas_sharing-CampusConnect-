package com.example.campusconnect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.campusconnect.databinding.FragmentBottomNavBinding

class BottomNavFragment : Fragment() {

    private var _binding: FragmentBottomNavBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBottomNavBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Reset all icons to default color
        resetColors()

        // Highlight the current destination
        val currentDest = findNavController().currentDestination?.id
        highlightDestination(currentDest)

        binding.llNavHome.setOnClickListener {
            if (currentDest != R.id.homeFragment) {
                findNavController().navigate(R.id.homeFragment)
            }
        }

        binding.llNavSkills.setOnClickListener {
            if (currentDest != R.id.skillsFragment) {
                findNavController().navigate(R.id.skillsFragment)
            }
        }

        binding.llNavStartup.setOnClickListener {
            if (currentDest != R.id.startupFragment) {
                findNavController().navigate(R.id.startupFragment)
            }
        }

        binding.llNavEvents.setOnClickListener {
            if (currentDest != R.id.eventsFragment) {
                findNavController().navigate(R.id.eventsFragment)
            }
        }

        binding.llNavProfile.setOnClickListener {
            if (currentDest != R.id.profileFragment) {
                findNavController().navigate(R.id.profileFragment)
            }
        }
    }

    private fun resetColors() {
        val gray = resources.getColor(R.color.gray_text)
        binding.ivNavHome.setColorFilter(gray)
        binding.tvNavHome.setTextColor(gray)
        binding.ivNavSkills.setColorFilter(gray)
        binding.tvNavSkills.setTextColor(gray)
        binding.ivNavStartup.setColorFilter(gray)
        binding.tvNavStartup.setTextColor(gray)
        binding.ivNavEvents.setColorFilter(gray)
        binding.tvNavEvents.setTextColor(gray)
        binding.ivNavProfile.setColorFilter(gray)
        binding.tvNavProfile.setTextColor(gray)
    }

    private fun highlightDestination(destId: Int?) {
        val red = resources.getColor(R.color.primaryRed)
        when (destId) {
            R.id.homeFragment -> {
                binding.ivNavHome.setColorFilter(red)
                binding.tvNavHome.setTextColor(red)
            }
            R.id.skillsFragment -> {
                binding.ivNavSkills.setColorFilter(red)
                binding.tvNavSkills.setTextColor(red)
            }
            R.id.startupFragment -> {
                binding.ivNavStartup.setColorFilter(red)
                binding.tvNavStartup.setTextColor(red)
            }
            R.id.eventsFragment -> {
                binding.ivNavEvents.setColorFilter(red)
                binding.tvNavEvents.setTextColor(red)
            }
            R.id.profileFragment -> {
                binding.ivNavProfile.setColorFilter(red)
                binding.tvNavProfile.setTextColor(red)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}