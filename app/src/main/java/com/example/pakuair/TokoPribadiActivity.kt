package com.example.pakuair

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import java.util.UUID

data class TokoData(
    val id: String = "",
    val userId: String = "",
    val namaToko: String = "",
    val alamatToko: String = "",
    val contactOwner: String = "",
    val photoUrl: String = ""
)

class TokoPribadiActivity : AppCompatActivity() {
    private lateinit var edtNamaToko: TextInputEditText
    private lateinit var edtAlamatToko: TextInputEditText
    private lateinit var edtContactOwner: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var backButton: ImageView
    private lateinit var btnUploadPhoto: MaterialButton
    private lateinit var ivPhotoPreview: ImageView
    private var selectedImageUri: Uri? = null
    
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    
    private var isEditMode = false
    private var existingPhotoUrl: String? = null
    private var tokoId: String? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { uri ->
                selectedImageUri = uri
                ivPhotoPreview.setImageURI(uri)
                btnUploadPhoto.text = "Ganti Foto"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toko_pribadi)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        // Initialize views
        edtNamaToko = findViewById(R.id.edtNamaToko)
        edtAlamatToko = findViewById(R.id.edtAlamatToko)
        edtContactOwner = findViewById(R.id.edtContactOwner)
        btnSave = findViewById(R.id.btnSave)
        backButton = findViewById(R.id.backButton)
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto)
        ivPhotoPreview = findViewById(R.id.ivPhotoPreview)

        // Check if we're in edit mode
        isEditMode = intent.getBooleanExtra("isEditMode", false)
        if (isEditMode) {
            tokoId = intent.getStringExtra("tokoId")
            loadExistingData(tokoId!!)
        }

        backButton.setOnClickListener {
            finish()
        }

        btnUploadPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getContent.launch(intent)
        }

        btnSave.setOnClickListener {
            if (validateInput()) {
                saveData()
            }
        }
    }

    private fun loadExistingData(tokoId: String) {
        val tokoRef = database.reference.child("toko").child(tokoId)
        tokoRef.get().addOnSuccessListener { snapshot ->
            val tokoData = snapshot.getValue(TokoData::class.java)
            tokoData?.let {
                edtNamaToko.setText(it.namaToko)
                edtAlamatToko.setText(it.alamatToko)
                edtContactOwner.setText(it.contactOwner)
                existingPhotoUrl = it.photoUrl
                // Load image using Glide
                if (it.photoUrl.isNotEmpty()) {
                    Glide.with(this)
                        .load(it.photoUrl)
                        .centerCrop()
                        .into(ivPhotoPreview)
                    btnUploadPhoto.text = "Ganti Foto"
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal memuat data toko", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true

        if (!isEditMode && selectedImageUri == null && existingPhotoUrl == null) {
            Toast.makeText(this, "Silakan upload foto toko", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        if (edtNamaToko.text.toString().trim().isEmpty()) {
            edtNamaToko.error = "Nama toko tidak boleh kosong"
            isValid = false
        }
        if (edtAlamatToko.text.toString().trim().isEmpty()) {
            edtAlamatToko.error = "Alamat toko tidak boleh kosong"
            isValid = false
        }
        if (edtContactOwner.text.toString().trim().isEmpty()) {
            edtContactOwner.error = "Contact owner tidak boleh kosong"
            isValid = false
        }

        return isValid
    }

    private fun saveData() {
        val userId = auth.currentUser?.uid ?: return
        val storeId = tokoId ?: UUID.randomUUID().toString()
        
        if (selectedImageUri != null) {
            uploadImage(selectedImageUri!!, storeId)
        } else {
            saveTokoData(storeId, existingPhotoUrl ?: "")
        }
    }

    private fun uploadImage(imageUri: Uri, storeId: String) {
        // Show loading state
        btnSave.isEnabled = false
        btnUploadPhoto.isEnabled = false
        Toast.makeText(this, "Mengupload gambar...", Toast.LENGTH_SHORT).show()

        try {
            // Create storage reference with user-specific path
            val userId = auth.currentUser?.uid ?: return
            val timestamp = System.currentTimeMillis()
            val filename = "${storeId}_${timestamp}.jpg"
            
            // Get root reference first
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("toko_images").child(userId).child(filename)
            
            // Upload file
            imageRef.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    // Get download URL
                    imageRef.downloadUrl
                        .addOnSuccessListener { downloadUrl ->
                            saveTokoData(storeId, downloadUrl.toString())
                        }
                        .addOnFailureListener { exception ->
                            btnSave.isEnabled = true
                            btnUploadPhoto.isEnabled = true
                            Toast.makeText(this, "Gagal mendapatkan URL gambar: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { exception ->
                    btnSave.isEnabled = true
                    btnUploadPhoto.isEnabled = true
                    when (exception) {
                        is StorageException -> {
                            when (exception.errorCode) {
                                StorageException.ERROR_NOT_AUTHENTICATED -> {
                                    Toast.makeText(this, "Silakan login ulang", Toast.LENGTH_SHORT).show()
                                    // Redirect to login
                                    startActivity(Intent(this, SignInActivity::class.java))
                                    finish()
                                }
                                StorageException.ERROR_NOT_AUTHORIZED -> {
                                    Toast.makeText(this, "Anda tidak memiliki izin untuk upload gambar", Toast.LENGTH_SHORT).show()
                                }
                                StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> {
                                    Toast.makeText(this, "Koneksi tidak stabil, silakan coba lagi", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    Toast.makeText(this, "Gagal mengupload gambar (${exception.errorCode}): ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        else -> {
                            Toast.makeText(this, "Gagal mengupload gambar: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                    btnUploadPhoto.text = "Uploading... $progress%"
                }
        } catch (e: Exception) {
            btnSave.isEnabled = true
            btnUploadPhoto.isEnabled = true
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveTokoData(storeId: String, photoUrl: String) {
        val userId = auth.currentUser?.uid ?: return
        
        val tokoData = TokoData(
            id = storeId,
            userId = userId,
            namaToko = edtNamaToko.text.toString().trim(),
            alamatToko = edtAlamatToko.text.toString().trim(),
            contactOwner = edtContactOwner.text.toString().trim(),
            photoUrl = photoUrl
        )

        database.reference.child("toko").child(storeId).setValue(tokoData)
            .addOnSuccessListener {
                Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
            }
    }
} 