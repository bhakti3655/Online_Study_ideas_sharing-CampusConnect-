package com.example.campusconnectadmin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardFragment : Fragment() {

    private val DATABASE_URL = "https://campusconnectdb-default-rtdb.firebaseio.com/"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        val txtUsers = view.findViewById<TextView>(R.id.txtUsers)
        val txtSkills = view.findViewById<TextView>(R.id.txtSkills)
        val txtEvents = view.findViewById<TextView>(R.id.txtEvents)
        val txtIdeas = view.findViewById<TextView>(R.id.txtIdeas)
        val btnProfile = view.findViewById<ImageView>(R.id.btnProfile)

        btnProfile.setOnClickListener {
            (activity as? MainActivity)?.openProfile()
        }

        val db = FirebaseDatabase.getInstance(DATABASE_URL).reference

        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return

                val usersCount = snapshot.child("Users").childrenCount
                val skillsCount = snapshot.child("Skills").childrenCount
                val eventsCount = snapshot.child("Events").childrenCount
                
                // IMPORTANT: Counting from 'Startups' node as requested in previous requirement
                val startupsCount = snapshot.child("Startups").childrenCount

                txtUsers.text = usersCount.toString()
                txtSkills.text = skillsCount.toString()
                txtEvents.text = eventsCount.toString()
                txtIdeas.text = startupsCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        return view
    }
}
