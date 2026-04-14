package com.example.campusconnect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusconnect.databinding.FragmentUserListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserListFragment : Fragment() {

    private var _binding: FragmentUserListBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<User>()
    private val lastMessageMap = mutableMapOf<String, Long>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupRecyclerView()
        fetchAllUsers()

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(userList) { user ->
            val bundle = Bundle().apply {
                putString("receiverId", user.uid)
                putString("receiverName", user.fullName)
            }
            findNavController().navigate(R.id.action_userListFragment_to_chatFragment, bundle)
        }
        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsers.adapter = userAdapter
    }

    private fun fetchAllUsers() {
        val currentUserId = auth.currentUser?.uid ?: return
        database.reference.child("Users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                val tempUserList = mutableListOf<User>()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null && user.uid != currentUserId) {
                        tempUserList.add(user)
                        fetchLastMessageTime(currentUserId, user.uid!!, user)
                    }
                }
                userList.addAll(tempUserList)
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchLastMessageTime(currentUserId: String, otherUserId: String, user: User) {
        val chatRoom = if (currentUserId < otherUserId) currentUserId + otherUserId else otherUserId + currentUserId
        database.reference.child("Chats").child(chatRoom).limitToLast(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var lastTime = 0L
                    if (snapshot.exists()) {
                        val lastMsg = snapshot.children.iterator().next()
                        lastTime = lastMsg.child("timestamp").value as? Long ?: 0L
                    }
                    lastMessageMap[otherUserId] = lastTime
                    sortUsersByRecent()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun sortUsersByRecent() {
        userList.sortByDescending { lastMessageMap[it.uid] ?: 0L }
        userAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}