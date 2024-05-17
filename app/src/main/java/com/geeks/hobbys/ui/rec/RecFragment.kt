package com.geeks.hobbys.ui.rec

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.geeks.hobbys.databinding.FragmentHomeBinding
import com.geeks.hobbys.ui.model.User
import com.geeks.hobbys.ui.rec.adapter.UserAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RecFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var database: FirebaseDatabase? = null
    private var users: ArrayList<User>? = null
    private var userAdapter: UserAdapter? = null
    private var dialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = ProgressDialog(requireContext())
        dialog!!.setMessage("Updating Profile...")
        dialog!!.setCancelable(false)

        database = FirebaseDatabase.getInstance()
        users = ArrayList()
        userAdapter = UserAdapter(requireContext(), users!!)
        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvRecommendation.layoutManager = layoutManager
        binding.rvRecommendation.adapter = userAdapter

        database!!.reference.child("users")
            .child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(User::class.java)?.let {
                        // Здесь вы можете сохранить данные пользователя, если необходимо
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        database!!.reference.child("users").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    users!!.clear()
                    for (snapshot1 in snapshot.children) {
                        val user: User? = snapshot1.getValue(User::class.java)
                        if (user != null && user.uid != FirebaseAuth.getInstance().uid) {
                            users!!.add(user)
                        }
                    }
                    userAdapter!!.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence")
            .child(currentId!!).setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence")
            .child(currentId!!).setValue("Offline")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
