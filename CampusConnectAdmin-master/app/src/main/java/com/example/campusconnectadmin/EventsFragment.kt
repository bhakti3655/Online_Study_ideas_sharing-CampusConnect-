package com.example.campusconnectadmin

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EventsFragment : Fragment() {

    private val list = mutableListOf<Event>()
    private lateinit var adapter: EventAdapter
    private val DATABASE_URL = "https://campusconnectdb-default-rtdb.firebaseio.com/"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val view = inflater.inflate(R.layout.fragment_events, container, false)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerEvents)
        val btnAddEvent = view.findViewById<FloatingActionButton>(R.id.btnAddEvent)

        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = EventAdapter(list)
        recycler.adapter = adapter

        // Fetch events in real-time
        fetchEvents()

        btnAddEvent.setOnClickListener {
            // Open Add Event Screen
            startActivity(Intent(requireContext(), AddEventActivity::class.java))
        }

        return view
    }

    private fun fetchEvents() {
        FirebaseDatabase.getInstance(DATABASE_URL).getReference("Events")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    list.clear()
                    for (data in snapshot.children) {
                        val event = data.getValue(Event::class.java)
                        event?.let {
                            it.eventId = data.key
                            list.add(it)
                        }
                    }
                    list.reverse() // Newest events first
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    if (isAdded) Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
