package com.example.campusconnect

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.campusconnect.databinding.ItemEventBinding

class EventAdapter(private val eventList: List<Event>) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]
        holder.binding.tvEventTitle.text = event.title
        holder.binding.tvEventDate.text = "Date: ${event.date}"
        holder.binding.tvEventLocation.text = "Location: ${event.location}"

        holder.binding.btnRegister.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Registration for ${event.title} successful!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = eventList.size
}