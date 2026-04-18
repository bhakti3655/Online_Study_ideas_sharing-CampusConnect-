package com.example.campusconnectadmin

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class IdeasFragment : Fragment() {

    private val list = mutableListOf<Idea>()
    private lateinit var adapter: IdeaAdapter
    private val DATABASE_URL = "https://campusconnectdb-default-rtdb.firebaseio.com/"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val view = inflater.inflate(R.layout.fragment_ideas, container, false)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerIdeas)
        val btnAddIdea = view.findViewById<FloatingActionButton>(R.id.btnAddIdea)
        val btnProfile = view.findViewById<ImageView>(R.id.btnProfile)

        btnProfile.setOnClickListener {
            (activity as? MainActivity)?.openProfile()
        }

        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = IdeaAdapter(list)
        recycler.adapter = adapter

        // Sync with 'Startups' node
        fetchIdeas()

        btnAddIdea.setOnClickListener {
            startActivity(Intent(requireContext(), AddStartupActivity::class.java))
        }

        return view
    }

    private fun fetchIdeas() {
        // Changed node to 'Startups' to match AddStartupActivity
        FirebaseDatabase.getInstance(DATABASE_URL).getReference("Startups")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    list.clear()
                    for (data in snapshot.children) {
                        val idea = data.getValue(Idea::class.java)
                        idea?.let {
                            it.ideaId = data.key
                            list.add(it)
                        }
                    }
                    list.reverse()
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    if (isAdded) Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
