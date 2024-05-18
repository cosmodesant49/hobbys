package com.geeks.hobbys.ui.set_profile

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.geeks.hobbys.MainActivity
import com.geeks.hobbys.databinding.ActivitySetProfileBinding
import com.geeks.hobbys.ui.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class SetProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetProfileBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private var selectedImg: Uri? = null
    private lateinit var dialog: ProgressDialog

    companion object {
        private const val REQUEST_IMAGE = 45
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog = ProgressDialog(this)
        dialog.setMessage("Updating Profile...")
        dialog.setCancelable(false)

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.ivAvatar.setOnClickListener {
            openImageChooser()
        }

        binding.btnSetupProfile.setOnClickListener {
            setupProfile()
        }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE)
    }

    private fun setupProfile() {
        val name = binding.etProfileBox.text.toString().trim()
        if (name.isEmpty()) {
            binding.etProfileBox.error = "Please type name"
            return
        }
        val hobby = binding.etHobbyBox.text.toString().trim()
        if (hobby.isEmpty()) {
            binding.etHobbyBox.error = "Please type hobby"
            return
        }
        if (selectedImg == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }

        dialog.show()

        val uid = auth.uid ?: ""
        val email = auth.currentUser?.email ?: ""
        val imageUrl = selectedImg.toString()

        val user = User(uid, name, hobby, email, imageUrl)

        val reference = storage.reference.child("Profile").child(uid)
        reference.putFile(selectedImg!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    reference.downloadUrl.addOnSuccessListener { uri ->
                        user.profileImg = uri.toString()
                        database.reference
                            .child("users")
                            .child(uid)
                            .setValue(user)
                            .addOnCompleteListener {
                                dialog.dismiss()
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                    }
                } else {
                    dialog.dismiss()
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Error uploading image: ${task.exception}")
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImg = data.data
            binding.ivAvatar.setImageURI(selectedImg)
        }
    }
}
