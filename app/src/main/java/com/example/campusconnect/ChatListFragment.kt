package com.example.campusconnect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusconnect.databinding.FragmentChatListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatListFragment : Fragment() {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userAdapter: UserAdapter
    private val activeUserList = mutableListOf<User>()
    private val lastMessageTimes = mutableMapOf<String, Long>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupRecyclerView()
        fetchActiveChats()

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnNewChat.setOnClickListener {
            // "+" button opens the list of all users to start a new chat
            findNavController().navigate(R.id.action_chatListFragment_to_userListFragment)
        }
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(activeUserList) { user ->
            val bundle = Bundle().apply {
                putString("receiverId", user.uid)
                putString("receiverName", user.fullName)
            }
            findNavController().navigate(R.id.action_chatListFragment_to_chatFragment, bundle)
        }
        binding.rvChatList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChatList.adapter = userAdapter
    }

    private fun fetchActiveChats() {
        val currentUserId = auth.currentUser?.uid ?: return
        
        database.reference.child("Chats").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return
                
                val activeIds = mutableSetOf<String>()
                lastMessageTimes.clear()

                for (roomSnapshot in snapshot.children) {
                    val roomId = roomSnapshot.key ?: continue
                    if (roomId.contains(currentUserId)) {
                        // Get the other user's ID
                        val otherId = roomId.replace(currentUserId, "")
                        activeIds.add(otherId)
                        
                        // Get last message timestamp for sorting
                        var latestTime = 0L
                        for (msgSnapshot in roomSnapshot.children) {
                            val time = msgSnapshot.child("timestamp").value as? Long ?: 0L
                            if (time > latestTime) latestTime = time
                        }
                        lastMessageTimes[otherId] = latestTime
                    }
                }
                fetchUsersDetails(activeIds)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchUsersDetails(activeIds: Set<String>) {
        database.reference.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return
                activeUserList.clear()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null && activeIds.contains(user.uid)) {
                        activeUserList.add(user)
                    }
                }
                
                // Sort by most recent message (timestamp)
                activeUserList.sortByDescending { lastMessageTimes[it.uid] ?: 0L }
                userAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}