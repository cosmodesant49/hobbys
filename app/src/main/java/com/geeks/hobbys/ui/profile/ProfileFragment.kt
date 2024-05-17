package com.geeks.hobbys.ui.profile

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.geeks.hobbys.databinding.FragmentProfileBinding
import com.geeks.hobbys.ui.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private var dialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog = ProgressDialog(activity)
        dialog!!.setMessage("Loading Profile...")
        dialog!!.setCancelable(false)
        dialog!!.show()

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        loadProfile()
    }

    private fun loadProfile() {
        val uid = auth.uid ?: return
        val reference = database.reference.child("users").child(uid)

        reference.get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            if (user != null) {
                binding.tvUsername.text = user.name
                binding.tvHobby.text = user.hobby
                Picasso.get().load(user.profileImg).into(binding.ivAvatar)
            } else {
                Toast.makeText(activity, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
            dialog?.dismiss()
        }.addOnFailureListener {
            dialog?.dismiss()
            Toast.makeText(activity, "Failed to load profile", Toast.LENGTH_SHORT).show()
            Log.e("ProfileFragment", "Error loading profile", it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
