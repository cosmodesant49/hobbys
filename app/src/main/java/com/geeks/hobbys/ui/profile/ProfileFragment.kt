package com.geeks.hobbys.ui.profile

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.geeks.hobbys.MainActivity
import com.geeks.hobbys.databinding.FragmentProfileBinding
import com.geeks.hobbys.ui.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.Date

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var database: FirebaseDatabase? = null
    private var auth: FirebaseAuth? = null
    private var storage: FirebaseStorage? = null
    private var selectedImg: Uri? = null
    private var dialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog = ProgressDialog(activity)
        dialog!!.setMessage("Updating Profile...")
        dialog!!.setCancelable(false)

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

        if (selectedImg == null) {
            // Обработка случая, когда изображение не выбрано
            // Возможно, вы хотите вывести какое-то сообщение об ошибке
            return
        }

        dialog!!.show()

        val uid = auth?.uid ?: ""
        val email = auth?.currentUser?.email ?: ""
        val imageUrl = selectedImg.toString()

        val user = User(uid, name, email, imageUrl)

        val reference = storage?.reference?.child("Profile")?.child(uid) ?: return
        reference.putFile(selectedImg!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    reference.downloadUrl.addOnSuccessListener { uri ->
                        user.profileImg = uri.toString()
                        database?.reference
                            ?.child("users")
                            ?.child(uid)
                            ?.setValue(user)
                            ?.addOnCompleteListener {
                                dialog?.dismiss()
                                val intent = Intent(activity, MainActivity::class.java)
                                startActivity(intent)
                                activity?.finish()
                            }
                    }
                }
                else {
                    dialog?.dismiss()
                    Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Error uploading image: ${task.exception}")
                }
            }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data!=null){
            if (data.data !=null){
                val uri = data.data
                val storage = FirebaseStorage.getInstance()
                val time = Date().time
                val reference = storage.reference
                    .child("Profile")
                    .child(time.toString() + "")
                reference.putFile(uri!!).addOnCompleteListener{task ->
                    if (task.isSuccessful){
                        reference.downloadUrl.addOnCompleteListener { uri->
                            val filePath = uri.toString()
                            val obj = HashMap<String,Any>()
                            obj["image"] = filePath
                            database!!.reference
                                .child("users")
                                .child(FirebaseAuth.getInstance().uid!!)
                                .updateChildren(obj).addOnSuccessListener{}
                        }
                    }
                }
                binding!!.ivAvatar.setImageURI(data.data)
                selectedImg = data.data
            }
        }
        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImg = data.data
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUEST_IMAGE = 45
    }
}
