package com.example.campusconnectadmin

import android.app.ProgressDialog
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class AddStartupActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etCategory: EditText
    private lateinit var etStudent: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSubmit: Button
    
    private var existingIdeaId: String? = null
    private val DATABASE_URL = "https://campusconnectdb-default-rtdb.firebaseio.com/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_startup)

        etTitle = findViewById(R.id.etIdeaTitle)
        etCategory = findViewById(R.id.etIdeaCategory)
        etStudent = findViewById(R.id.etIdeaStudent)
        etDescription = findViewById(R.id.etIdeaDescription)
        btnSubmit = findViewById(R.id.btnSubmitIdea)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        // CHECK FOR EDIT MODE
        existingIdeaId = intent.getStringExtra("IDEA_ID")
        if (existingIdeaId != null) {
            etTitle.setText(intent.getStringExtra("TITLE"))
            etCategory.setText(intent.getStringExtra("CATEGORY"))
            etStudent.setText(intent.getStringExtra("STUDENT"))
            etDescription.setText(intent.getStringExtra("DESC"))
            btnSubmit.text = "UPDATE STARTUP"
        }

        btnSubmit.setOnClickListener {
            hideKeyboard()
            saveStartupToFirebase()
        }
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun saveStartupToFirebase() {
        val title = etTitle.text.toString().trim()
        val category = etCategory.text.toString().trim()
        val student = etStudent.text.toString().trim()
        val description = etDescription.text.toString().trim()

        if (title.isEmpty() || category.isEmpty() || student.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = ProgressDialog(this).apply {
            setMessage("Saving Startup...")
            show()
        }

        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("Startups")
        val ideaId = existingIdeaId ?: ref.push().key ?: System.currentTimeMillis().toString()

        val idea = mapOf(
            "ideaId" to ideaId,
            "title" to title,
            "category" to category,
            "studentName" to student,
            "description" to description,
            "status" to "approved",
            "timestamp" to System.currentTimeMillis()
        )

        ref.child(ideaId).setValue(idea).addOnCompleteListener { task ->
            progressDialog.dismiss()
            if (task.isSuccessful) {
                Toast.makeText(this, "Startup Saved!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
