package com.example.campusconnect

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.campusconnect.databinding.ItemDateHeaderBinding
import com.example.campusconnect.databinding.ItemMessageReceivedBinding
import com.example.campusconnect.databinding.ItemMessageSentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private val itemList: List<ChatListItem>,
    private val chatRoomId: String,
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2
    private val VIEW_TYPE_DATE = 3

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val binding = ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SentViewHolder(binding)
            }
            VIEW_TYPE_RECEIVED -> {
                val binding = ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ReceivedViewHolder(binding)
            }
            else -> {
                val binding = ItemDateHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                DateViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = itemList[position]

        if (item is ChatListItem.DateHeader) {
            (holder as DateViewHolder).binding.tvDateHeader.text = item.date
            return
        }

        val message = (item as ChatListItem.MessageItem).message
        val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp ?: 0))

        if (holder is SentViewHolder) {
            if (message.isDeleted) {
                holder.binding.tvSentMessage.text = "This message was deleted"
                holder.binding.tvSentMessage.alpha = 0.5f
                holder.itemView.setOnLongClickListener(null)
            } else {
                holder.binding.tvSentMessage.text = message.message
                holder.binding.tvSentMessage.alpha = 1.0f
                
                holder.itemView.setOnLongClickListener {
                    showDeleteDialog(message)
                    true
                }
            }
            
            holder.binding.tvSentTime.text = time

            when (message.status) {
                3 -> {
                    holder.binding.ivMessageTicks.setImageResource(android.R.drawable.ic_menu_send) 
                    holder.binding.ivMessageTicks.setColorFilter(0xFF2196F3.toInt()) // Blue
                    holder.binding.tvSeenStatus.visibility = View.VISIBLE
                    holder.binding.tvSeenStatus.text = "seen just now"
                }
                2 -> {
                    holder.binding.ivMessageTicks.setImageResource(android.R.drawable.ic_menu_send)
                    holder.binding.ivMessageTicks.setColorFilter(0xFF757575.toInt()) // Gray
                    holder.binding.tvSeenStatus.visibility = View.GONE
                }
                else -> {
                    holder.binding.ivMessageTicks.setImageResource(android.R.drawable.ic_menu_send)
                    holder.binding.ivMessageTicks.setColorFilter(0xFFBDBDBD.toInt()) // Light Gray
                    holder.binding.tvSeenStatus.visibility = View.GONE
                }
            }

        } else if (holder is ReceivedViewHolder) {
            if (message.isDeleted) {
                holder.binding.tvReceivedMessage.text = "This message was deleted"
                holder.binding.tvReceivedMessage.alpha = 0.5f
            } else {
                holder.binding.tvReceivedMessage.text = message.message
                holder.binding.tvReceivedMessage.alpha = 1.0f
            }
            holder.binding.tvReceivedTime.text = time
        }
    }

    private fun showDeleteDialog(message: Message) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Message")
        builder.setMessage("Do you want to delete this message?")
        builder.setPositiveButton("Delete for Everyone") { _, _ ->
            deleteMessage(message)
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun deleteMessage(message: Message) {
        message.messageId?.let { id ->
            FirebaseDatabase.getInstance().reference.child("Chats").child(chatRoomId).child(id)
                .child("isDeleted").setValue(true)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = itemList[position]) {
            is ChatListItem.DateHeader -> VIEW_TYPE_DATE
            is ChatListItem.MessageItem -> {
                if (FirebaseAuth.getInstance().currentUser?.uid == item.message.senderId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
            }
        }
    }

    override fun getItemCount(): Int = itemList.size

    class SentViewHolder(val binding: ItemMessageSentBinding) : RecyclerView.ViewHolder(binding.root)
    class ReceivedViewHolder(val binding: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(binding.root)
    class DateViewHolder(val binding: ItemDateHeaderBinding) : RecyclerView.ViewHolder(binding.root)
}
