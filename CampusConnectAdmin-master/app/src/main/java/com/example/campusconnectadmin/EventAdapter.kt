package com.example.campusconnectadmin

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase

class EventAdapter(private val list: List<Event>) :
    RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.txtTitle)
        val category: TextView = view.findViewById(R.id.txtCategory)
        val date: TextView = view.findViewById(R.id.txtDate)
        val time: TextView = view.findViewById(R.id.txtTime)
        val location: TextView = view.findViewById(R.id.txtLocation)
        val imgEvent: ImageView = view.findViewById(R.id.imgEvent)
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
        val btnEdit: ImageView = view.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = list[position]
        
        holder.title.text = event.title
        holder.category.text = event.category?.uppercase()
        holder.date.text = event.date
        holder.time.text = event.time
        holder.location.text = event.venue

        if (event.imageUrl.isNullOrEmpty()) {
            holder.imgEvent.setImageResource(android.R.drawable.ic_menu_gallery)
        } else {
            Glide.with(holder.itemView.context)
                .load(event.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.imgEvent)
        }

        holder.btnEdit.setOnClickListener {
            val intent = Intent(holder.itemView.context, AddEventActivity::class.java)
            intent.putExtra("EVENT_ID", event.eventId)
            intent.putExtra("TITLE", event.title)
            intent.putExtra("DATE", event.date)
            intent.putExtra("TIME", event.time)
            intent.putExtra("LOCATION", event.venue)
            intent.putExtra("CATEGORY", event.category)
            intent.putExtra("DESC", event.description)
            intent.putExtra("IMAGE", event.imageUrl)
            holder.itemView.context.startActivity(intent)
        }

        holder.btnDelete.setOnClickListener {
            event.eventId?.let { id ->
                FirebaseDatabase.getInstance()
                    .getReference("Events")
                    .child(id)
                    .removeValue()
            }
        }
    }

    override fun getItemCount() = list.size
}