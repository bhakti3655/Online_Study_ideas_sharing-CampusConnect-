package com.example.campusconnectadmin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate

class SettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val btnLogout = view.findViewById<View>(R.id.btnLogout)
        val btnEditProfile = view.findViewById<View>(R.id.btnEditProfile)
        val txtAdminName = view.findViewById<TextView>(R.id.txtAdminName)
        val txtAdminEmail = view.findViewById<TextView>(R.id.txtAdminRole)
        val txtAvatar = view.findViewById<TextView>(R.id.txtAvatar)
        val switchDarkMode = view.findViewById<Switch>(R.id.switchDarkMode)

        // Dynamic Login Detection
        val sharedPref = requireContext().getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE)
        val loggedInEmail = sharedPref.getString("adminEmail", "bhaktinarola77@gmail.com")

        // Set Dynamic Identity
        if (loggedInEmail?.contains("bhakti") == true) {
            txtAdminName.text = "BHAKTI NAROLA"
            txtAvatar.text = "B"
        } else {
            txtAdminName.text = "DILJAAN UMRETIYA"
            txtAvatar.text = "D"
        }
        txtAdminEmail.text = loggedInEmail

        // Stable Dark Mode Logic
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        switchDarkMode.isChecked = currentMode == AppCompatDelegate.MODE_NIGHT_YES

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            // Use a slight delay to prevent immediate recreation crash
            Handler(Looper.getMainLooper()).postDelayed({
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }, 100)
        }

        btnEditProfile.setOnClickListener {
            Toast.makeText(context, "Opening Edit Profile...", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            sharedPref.edit().putBoolean("isLoggedIn", false).apply()
            Toast.makeText(context, "Logged Out", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        return view
    }
}
