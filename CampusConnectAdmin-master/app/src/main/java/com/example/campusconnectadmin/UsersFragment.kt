package com.example.campusconnectadmin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UsersFragment : Fragment() {

    private val list = mutableListOf<User>()
    private lateinit var adapter: UserAdapter
    // UPDATED to correct database URL
    private val DATABASE_URL = "https://campusconnectdb-default-rtdb.firebaseio.com/"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val view = inflater.inflate(R.layout.fragment_users, container, false)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerUsers)
        val btnProfile = view.findViewById<ImageView>(R.id.btnProfile)

        btnProfile.setOnClickListener {
            (activity as? MainActivity)?.openProfile()
        }

        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = UserAdapter(list)
        recycler.adapter = adapter

        // Fetch users from the correct 'Users' node
        val db = FirebaseDatabase.getInstance(DATABASE_URL).getReference("Users")

        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                list.clear()
                for (data in snapshot.children) {
                    val user = data.getValue(User::class.java)
                    user?.let {
                        it.uid = data.key
                        list.add(it)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        return view
    }
}
