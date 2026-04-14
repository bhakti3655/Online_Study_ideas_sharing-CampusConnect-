package com.example.campusconnect

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusconnect.databinding.FragmentChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: FirebaseDatabase
    private var receiverId: String? = null
    private var senderId: String? = null
    private var chatRoom: String? = null

    private lateinit var messageAdapter: MessageAdapter
    private val chatListItems = mutableListOf<ChatListItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        senderId = FirebaseAuth.getInstance().currentUser?.uid
        receiverId = arguments?.getString("receiverId")
        val receiverName = arguments?.getString("receiverName")

        binding.tvChatUserName.text = receiverName

        if (senderId != null && receiverId != null) {
            chatRoom = if (senderId!! < receiverId!!) senderId + receiverId else receiverId + senderId
            setupRecyclerView()
            fetchMessages()
            markMessagesAsSeen()
        }

        binding.btnSendMessage.setOnClickListener {
            val messageText = binding.etMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText, "text")
            }
        }

        binding.btnAddAttachment.setOnClickListener {
            openGallery()
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(chatListItems, chatRoom!!, requireContext())
        binding.rvChatMessages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChatMessages.adapter = messageAdapter
    }

    private fun fetchMessages() {
        chatRoom?.let { room ->
            database.reference.child("Chats").child(room)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (_binding == null) return
                        chatListItems.clear()
                        var lastDate = ""
                        
                        for (messageSnapshot in snapshot.children) {
                            val message = messageSnapshot.getValue(Message::class.java)
                            if (message != null) {
                                val currentDate = formatDateHeader(message.timestamp ?: 0L)
                                if (currentDate != lastDate) {
                                    chatListItems.add(ChatListItem.DateHeader(currentDate))
                                    lastDate = currentDate
                                }
                                chatListItems.add(ChatListItem.MessageItem(message))
                            }
                        }
                        messageAdapter.notifyDataSetChanged()
                        if (chatListItems.isNotEmpty()) {
                            binding.rvChatMessages.scrollToPosition(chatListItems.size - 1)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    private fun formatDateHeader(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        val now = Calendar.getInstance()
        
        return if (calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
            "Today"
        } else if (calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) - 1) {
            "Yesterday"
        } else {
            SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date(timestamp))
        }
    }

    private fun markMessagesAsSeen() {
        chatRoom?.let { room ->
            database.reference.child("Chats").child(room)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (messageSnapshot in snapshot.children) {
                            val msg = messageSnapshot.getValue(Message::class.java)
                            if (msg != null && msg.receiverId == senderId && msg.status < 3) {
                                messageSnapshot.ref.child("status").setValue(3)
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    private fun sendMessage(text: String, type: String) {
        val messageId = database.reference.child("Chats").child(chatRoom!!).push().key
        val message = Message(
            messageId = messageId,
            senderId = senderId,
            receiverId = receiverId,
            message = text,
            timestamp = System.currentTimeMillis(),
            status = 1,
            type = type
        )

        chatRoom?.let { room ->
            database.reference.child("Chats").child(room).child(messageId!!).setValue(message)
                .addOnSuccessListener {
                    if (type == "text") binding.etMessage.setText("")
                }
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val base64Image = encodeImage(bitmap)
                sendMessage(base64Image, "image")
            }
        }
    }

    private fun encodeImage(bm: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}