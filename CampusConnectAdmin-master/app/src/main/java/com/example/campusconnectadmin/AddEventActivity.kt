package com.example.campusconnectadmin

import android.Manifest
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class AddEventActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etDate: EditText
    private lateinit var etTime: EditText
    private lateinit var etLocation: EditText
    private lateinit var etDescription: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var imgBanner: ImageView
    private lateinit var uploadPlaceholder: View
    private lateinit var progressDialog: ProgressDialog
    private var imageUri: Uri? = null
    
    private var existingEventId: String? = null
    private var existingImageUrl: String? = null

    private val UPLOAD_PRESET = "hzf4cguh" 
    private val DATABASE_URL = "https://campusconnectdb-default-rtdb.firebaseio.com/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        initViews()
        setupPickers()
        setupSpinner()
        checkEditMode()
    }

    private fun initViews() {
        etTitle = findViewById(R.id.etTitle)
        etDate = findViewById(R.id.etDate)
        etTime = findViewById(R.id.etTime)
        etLocation = findViewById(R.id.etLocation)
        etDescription = findViewById(R.id.etDescription)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        imgBanner = findViewById(R.id.imgBanner)
        uploadPlaceholder = findViewById(R.id.uploadPlaceholder)
        
        progressDialog = ProgressDialog(this).apply {
            setMessage("Publishing Event...")
            setCancelable(false)
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnCreateEvent).setOnClickListener { validateAndUpload() }
        findViewById<View>(R.id.cardUpload).setOnClickListener { checkPermissionAndPickImage() }
    }

    private fun checkEditMode() {
        existingEventId = intent.getStringExtra("EVENT_ID")
        if (existingEventId != null) {
            etTitle.setText(intent.getStringExtra("TITLE"))
            etDate.setText(intent.getStringExtra("DATE"))
            etTime.setText(intent.getStringExtra("TIME"))
            etLocation.setText(intent.getStringExtra("LOCATION"))
            etDescription.setText(intent.getStringExtra("DESC"))
            existingImageUrl = intent.getStringExtra("IMAGE")
            
            if (!existingImageUrl.isNullOrEmpty()) {
                Glide.with(this).load(existingImageUrl).into(imgBanner)
                imgBanner.visibility = View.VISIBLE
                uploadPlaceholder.visibility = View.GONE
            }
        }
    }

    private fun setupPickers() {
        etDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                etDate.setText(String.format("%02d/%02d/%d", day, month + 1, year))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        etTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                val amPm = if (hour < 12) "AM" else "PM"
                val displayHour = if (hour % 12 == 0) 12 else hour % 12
                etTime.setText(String.format("%02d:%02d %s", displayHour, minute, amPm))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
        }
    }

    private fun setupSpinner() {
        val categories = arrayOf("Tech", "Culture", "Coding", "Festivals")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinnerCategory.adapter = adapter
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) pickImage() else Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
    }

    private fun checkPermissionAndPickImage() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            pickImage()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            imageUri = result.data?.data
            if (imageUri != null) {
                imgBanner.setImageURI(imageUri)
                imgBanner.visibility = View.VISIBLE
                uploadPlaceholder.visibility = View.GONE
            }
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun validateAndUpload() {
        val title = etTitle.text.toString().trim()
        val location = etLocation.text.toString().trim()
        val date = etDate.text.toString().trim()
        val time = etTime.text.toString().trim()
        val desc = etDescription.text.toString().trim()

        // Requirement: Proper Data Validation
        if (title.isEmpty() || location.isEmpty() || date.isEmpty() || time.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri == null && existingImageUrl == null) {
            Toast.makeText(this, "Please select an event banner", Toast.LENGTH_SHORT).show()
            return
        }

        progressDialog.show()

        if (imageUri != null) {
            MediaManager.get().upload(imageUri)
                .option("upload_preset", UPLOAD_PRESET)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                        val url = resultData["secure_url"].toString()
                        saveToFirebase(url)
                    }
                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        progressDialog.dismiss()
                        Toast.makeText(this@AddEventActivity, "Upload failed: ${error?.description}", Toast.LENGTH_SHORT).show()
                    }
                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                }).dispatch()
        } else {
            saveToFirebase(existingImageUrl ?: "")
        }
    }

    private fun saveToFirebase(imageUrl: String) {
        val database = FirebaseDatabase.getInstance(DATABASE_URL)
        val ref = database.getReference("Events")
        
        // Requirement: Unique Event ID using push().key
        val eventId = existingEventId ?: ref.push().key ?: System.currentTimeMillis().toString()
        
        val event = Event(
            eventId = eventId,
            title = etTitle.text.toString().trim(),
            date = etDate.text.toString().trim(),
            time = etTime.text.toString().trim(),
            venue = etLocation.text.toString().trim(),
            category = spinnerCategory.selectedItem.toString(),
            description = etDescription.text.toString().trim(),
            imageUrl = imageUrl
        )

        ref.child(eventId).setValue(event).addOnSuccessListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Event Published Successfully!", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Firebase error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
