package com.example.campusconnect

import android.app.Activity
import android.app.AlertDialog
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
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.campusconnect.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.ByteArrayOutputStream
import java.util.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var selectedImageBitmap: Bitmap? = null
    
    private lateinit var mySkillAdapter: SkillAdapter
    private val mySkillList = mutableListOf<Skill>()
    
    private var targetUserId: String? = null
    private var currentUser: User? = null
    private var userListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        targetUserId = arguments?.getString("userId") ?: auth.currentUser?.uid

        setupMenuOptions()
        setupRecyclerView()
        loadUserData()
        fetchMySkills()

        val isOwnProfile = targetUserId == auth.currentUser?.uid

        if (isOwnProfile) {
            binding.btnEditProfile.visibility = View.VISIBLE
            binding.btnLogout.visibility = View.VISIBLE
            binding.llSelfOptions.visibility = View.VISIBLE
            binding.btnChatListFAB.visibility = View.VISIBLE
            binding.tvMySkillsHeader.text = "My Skills"
            
            binding.cardProfileImage.setOnClickListener { openGallery() }
            binding.btnSaveChanges.setOnClickListener {
                if (selectedImageBitmap != null) {
                    saveImageToFirebase()
                } else {
                    Toast.makeText(requireContext(), "Please select an image first", Toast.LENGTH_SHORT).show()
                }
            }
            binding.btnEditProfile.setOnClickListener { showEditProfileDialog() }
            binding.btnLogout.setOnClickListener {
                auth.signOut()
                findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
            }
            binding.btnChatListFAB.setOnClickListener {
                findNavController().navigate(R.id.action_profileFragment_to_chatListFragment)
            }
        } else {
            binding.btnEditProfile.visibility = View.GONE
            binding.btnLogout.visibility = View.GONE
            binding.llSelfOptions.visibility = View.GONE
            binding.btnChatListFAB.visibility = View.GONE
            binding.btnSaveChanges.visibility = View.GONE
            binding.tvMySkillsHeader.text = "Skills Details"
            // tvProfileTitle was removed from XML as per RKU logo branding request
        }
    }

    private fun setupMenuOptions() {
        binding.optionMySkills.ivOptionIcon.setImageResource(R.drawable.skills)
        binding.optionMySkills.tvOptionTitle.text = "My Skills"
        binding.optionMySkills.tvOptionSubtitle.text = "Create and Manage Skills"

        binding.optionMyStartups.ivOptionIcon.setImageResource(R.drawable.send)
        binding.optionMyStartups.tvOptionTitle.text = "My StartUps"
        binding.optionMyStartups.tvOptionSubtitle.text = "View and Manage Your Ideas"

        binding.optionMyEvents.ivOptionIcon.setImageResource(R.drawable.event)
        binding.optionMyEvents.tvOptionTitle.text = "My Events"
        binding.optionMyEvents.tvOptionSubtitle.text = "Check Your Saved and Hosted Events"

        binding.optionPostIdeas.ivOptionIcon.setImageResource(R.drawable.add)
        binding.optionPostIdeas.tvOptionTitle.text = "Post Ideas"
        binding.optionPostIdeas.tvOptionSubtitle.text = "Add new StartUp Ideas"

        binding.btnAboutUs.ivOptionIcon.setImageResource(R.drawable.about)
        binding.btnAboutUs.tvOptionTitle.text = "About Us"
        binding.btnAboutUs.tvOptionSubtitle.text = "Learn More About CampusConnect"

        binding.optionPostIdeas.root.setOnClickListener { showAddStartupDialog() }
        binding.btnAboutUs.root.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.profileFragment) {
                findNavController().navigate(R.id.action_profileFragment_to_aboutUsFragment)
            }
        }
        binding.optionMySkills.root.setOnClickListener { showAddSkillDialog() }
    }

    private fun setupRecyclerView() {
        val isOwnProfile = targetUserId == auth.currentUser?.uid
        mySkillAdapter = SkillAdapter(
            mySkillList, 
            isProfilePage = isOwnProfile,
            onEditClick = { skill -> if (isOwnProfile) showEditSkillDialog(skill) },
            onDeleteClick = { skill -> if (isOwnProfile) deleteSkill(skill) }
        )
        binding.rvMySkills.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMySkills.adapter = mySkillAdapter
    }

    private fun fetchMySkills() {
        val userId = targetUserId ?: return
        database.reference.child("Skills").orderByChild("studentId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (_binding == null) return
                    mySkillList.clear()
                    for (skillSnapshot in snapshot.children) {
                        val skill = skillSnapshot.getValue(Skill::class.java)
                        if (skill != null) mySkillList.add(skill)
                    }
                    mySkillAdapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                selectedImageBitmap = BitmapFactory.decodeStream(inputStream)
                binding.ivProfileImage.setImageBitmap(selectedImageBitmap)
                binding.tvProfileInitial.visibility = View.GONE
                binding.btnSaveChanges.visibility = View.VISIBLE
            }
        }
    }

    private fun saveImageToFirebase() {
        val userId = auth.currentUser?.uid ?: return
        val base64Image = encodeImage(selectedImageBitmap!!)
        
        binding.btnSaveChanges.isEnabled = false
        binding.btnSaveChanges.text = "Saving..."

        database.reference.child("Users").child(userId).child("profileImageUrl").setValue(base64Image)
            .addOnSuccessListener {
                if (_binding == null) return@addOnSuccessListener
                binding.btnSaveChanges.visibility = View.GONE
                binding.btnSaveChanges.isEnabled = true
                binding.btnSaveChanges.text = "Save Image"
                selectedImageBitmap = null
                Toast.makeText(requireContext(), "Profile Image Updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update image", Toast.LENGTH_SHORT).show()
                binding.btnSaveChanges.isEnabled = true
            }
    }

    private fun encodeImage(bm: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    private fun loadUserData() {
        val userId = targetUserId ?: return
        val userRef = database.reference.child("Users").child(userId)
        
        userListener = userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return
                currentUser = snapshot.getValue(User::class.java)
                currentUser?.let { user ->
                    binding.tvProfileName.text = user.fullName
                    binding.tvProfileEmailDisplay.text = user.email
                    
                    if (!user.profileImageUrl.isNullOrEmpty()) {
                        val decodedByte = Base64.decode(user.profileImageUrl, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
                        binding.ivProfileImage.setImageBitmap(bitmap)
                        binding.tvProfileInitial.visibility = View.GONE
                    } else {
                        binding.tvProfileInitial.text = user.fullName?.take(1)?.uppercase() ?: "U"
                        binding.tvProfileInitial.visibility = View.VISIBLE
                        binding.ivProfileImage.setImageResource(R.drawable.profile_circle)
                    }

                    updateCounts(userId)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateCounts(userId: String) {
        database.reference.child("Skills").orderByChild("studentId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (_binding == null) return
                    binding.tvSkillCount.text = "${snapshot.childrenCount} Skills"
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        database.reference.child("Ideas").orderByChild("studentId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (_binding == null) return
                    binding.tvStartupCount.text = "${snapshot.childrenCount} StartUps"
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun showEditProfileDialog() {
        val bundle = Bundle()
        currentUser?.let {
            bundle.putString("userId", it.uid)
        }
        findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment, bundle)
    }

    private fun showAddStartupDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Post New Idea")
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }
        val etTitle = EditText(requireContext()).apply { hint = "Startup Title" }
        val etDesc = EditText(requireContext()).apply { hint = "Description" }
        layout.addView(etTitle)
        layout.addView(etDesc)
        builder.setView(layout)
        builder.setPositiveButton("Submit") { _, _ ->
            val title = etTitle.text.toString().trim()
            val desc = etDesc.text.toString().trim()
            if (title.isNotEmpty()) {
                val userId = auth.currentUser?.uid ?: return@setPositiveButton
                val ideaId = database.reference.child("Ideas").push().key ?: return@setPositiveButton
                val startup = mapOf(
                    "id" to ideaId,
                    "title" to title,
                    "description" to desc,
                    "studentName" to currentUser?.fullName,
                    "studentId" to userId,
                    "status" to "pending"
                )
                database.reference.child("Ideas").child(ideaId).setValue(startup)
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showAddSkillDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add New Skill")
        val layout = LinearLayout(requireContext()).apply { 
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }
        val inputSkill = EditText(requireContext()).apply { hint = "Skill Name" }
        val inputDesc = EditText(requireContext()).apply { hint = "Description" }
        layout.addView(inputSkill)
        layout.addView(inputDesc)
        builder.setView(layout)
        builder.setPositiveButton("Add") { _, _ ->
            val name = inputSkill.text.toString().trim()
            val desc = inputDesc.text.toString().trim()
            if (name.isNotEmpty()) {
                val userId = auth.currentUser?.uid ?: return@setPositiveButton
                val skillId = database.reference.child("Skills").push().key ?: return@setPositiveButton
                val skill = Skill(
                    id = skillId,
                    skillName = name,
                    studentName = currentUser?.fullName,
                    studentMobile = currentUser?.mobile,
                    studentEmail = currentUser?.email,
                    studentId = userId,
                    description = desc,
                    status = "approved"
                )
                database.reference.child("Skills").child(skillId).setValue(skill)
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showEditSkillDialog(skill: Skill) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Edit Skill")
        val layout = LinearLayout(requireContext()).apply { 
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }
        val inputSkill = EditText(requireContext()).apply { 
            hint = "Skill Name"
            setText(skill.skillName)
        }
        val inputDesc = EditText(requireContext()).apply { 
            hint = "Description"
            setText(skill.description)
        }
        layout.addView(inputSkill)
        layout.addView(inputDesc)
        builder.setView(layout)
        builder.setPositiveButton("Update") { _, _ ->
            val updates = mapOf(
                "skillName" to inputSkill.text.toString().trim(),
                "description" to inputDesc.text.toString().trim()
            )
            skill.id?.let { database.reference.child("Skills").child(it).updateChildren(updates) }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun deleteSkill(skill: Skill) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Skill")
            .setMessage("Are you sure you want to delete ${skill.skillName}?")
            .setPositiveButton("Delete") { _, _ ->
                skill.id?.let { database.reference.child("Skills").child(it).removeValue() }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val userId = targetUserId
        if (userId != null && userListener != null) {
            database.reference.child("Users").child(userId).removeEventListener(userListener!!)
        }
        _binding = null
    }
}