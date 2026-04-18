package com.example.campusconnect

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
            bindSentMessage(holder, message, time, position == itemList.size - 1)
        } else if (holder is ReceivedViewHolder) {
            bindReceivedMessage(holder, message, time)
        }
    }

    private fun bindSentMessage(holder: SentViewHolder, message: Message, time: String, isLast: Boolean) {
        if (message.isDeleted) {
            holder.binding.tvSentMessage.visibility = View.VISIBLE
            holder.binding.cardSentImage.visibility = View.GONE
            holder.binding.tvSentMessage.text = "This message was deleted"
            holder.binding.tvSentMessage.alpha = 0.5f
            holder.itemView.setOnLongClickListener(null)
        } else {
            holder.binding.tvSentMessage.alpha = 1.0f
            if (message.type == "image") {
                holder.binding.tvSentMessage.visibility = View.GONE
                holder.binding.cardSentImage.visibility = View.VISIBLE
                val decodedByte = Base64.decode(message.message, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
                holder.binding.ivSentImage.setImageBitmap(bitmap)
            } else {
                holder.binding.tvSentMessage.visibility = View.VISIBLE
                holder.binding.cardSentImage.visibility = View.GONE
                holder.binding.tvSentMessage.text = message.message
            }

            holder.itemView.setOnLongClickListener {
                showOptionsDialog(message)
                true
            }
        }

        holder.binding.tvSentTime.text = time

        // Seen Status Logic - Last message only
        if (isLast && message.status == 3) {
            holder.binding.tvSeenStatus.visibility = View.VISIBLE
            holder.binding.tvSeenStatus.text = formatSeenStatus(message.seenAt)
        } else {
            holder.binding.tvSeenStatus.visibility = View.GONE
        }

        // Ticks Logic
        when (message.status) {
            3 -> {
                holder.binding.ivMessageTicks.setImageResource(android.R.drawable.ic_menu_send)
                holder.binding.ivMessageTicks.setColorFilter(0xFF2196F3.toInt()) // Blue
            }
            2 -> {
                holder.binding.ivMessageTicks.setImageResource(android.R.drawable.ic_menu_send)
                holder.binding.ivMessageTicks.setColorFilter(0xFF757575.toInt()) // Gray
            }
            else -> {
                holder.binding.ivMessageTicks.setImageResource(android.R.drawable.ic_menu_send)
                holder.binding.ivMessageTicks.setColorFilter(0xFFBDBDBD.toInt()) // Light Gray
            }
        }
    }

    private fun bindReceivedMessage(holder: ReceivedViewHolder, message: Message, time: String) {
        if (message.isDeleted) {
            holder.binding.tvReceivedMessage.visibility = View.VISIBLE
            holder.binding.cardReceivedImage.visibility = View.GONE
            holder.binding.tvReceivedMessage.text = "This message was deleted"
            holder.binding.tvReceivedMessage.alpha = 0.5f
        } else {
            holder.binding.tvReceivedMessage.alpha = 1.0f
            if (message.type == "image") {
                holder.binding.tvReceivedMessage.visibility = View.GONE
                holder.binding.cardReceivedImage.visibility = View.VISIBLE
                val decodedByte = Base64.decode(message.message, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
                holder.binding.ivReceivedImage.setImageBitmap(bitmap)
            } else {
                holder.binding.tvReceivedMessage.visibility = View.VISIBLE
                holder.binding.cardReceivedImage.visibility = View.GONE
                holder.binding.tvReceivedMessage.text = message.message
            }
        }
        holder.binding.tvReceivedTime.text = time
    }

    private fun formatSeenStatus(seenAt: Long?): String {
        if (seenAt == null) return "seen just now"
        val diff = System.currentTimeMillis() - seenAt
        val mins = diff / (1000 * 60)
        val hours = mins / 60
        
        return when {
            mins < 1 -> "seen just now"
            mins < 60 -> "seen $mins mins ago"
            hours < 24 -> "seen $hours hours ago"
            else -> "seen " + SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(seenAt))
        }
    }

    private fun showOptionsDialog(message: Message) {
        val options = arrayOf("Copy", "Delete", "Star")
        AlertDialog.Builder(context)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> copyToClipboard(message.message ?: "")
                    1 -> showDeleteOptions(message)
                    2 -> Toast.makeText(context, "Starred", Toast.LENGTH_SHORT).show()
                }
            }.show()
    }

    private fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("message", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteOptions(message: Message) {
        val options = arrayOf("Delete for me", "Delete for everyone", "Cancel")
        AlertDialog.Builder(context)
            .setTitle("Delete Message?")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> deleteForMe(message)
                    1 -> deleteForEveryone(message)
                }
            }.show()
    }

    private fun deleteForMe(message: Message) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().reference.child("Chats").child(chatRoomId)
            .child(message.messageId!!).child("deletedFor").setValue(uid)
    }

    private fun deleteForEveryone(message: Message) {
        FirebaseDatabase.getInstance().reference.child("Chats").child(chatRoomId)
            .child(message.messageId!!).child("isDeleted").setValue(true)
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