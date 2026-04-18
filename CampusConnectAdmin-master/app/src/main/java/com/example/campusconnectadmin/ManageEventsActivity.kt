package com.example.campusconnectadmin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class ManageEventsActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: EventAdapter
    private val list = mutableListOf<Event>()
    private val DATABASE_URL = "https://firestoredemo-8fba5-default-rtdb.firebaseio.com/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_manage_events)

        recycler = findViewById(R.id.recyclerManageEvents)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = EventAdapter(list)
        recycler.adapter = adapter

        FirebaseDatabase.getInstance(DATABASE_URL).getReference("Events")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    list.clear()
                    for (data in snapshot.children) {
                        val event = data.getValue(Event::class.java)
                        event?.let { list.add(it) }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}