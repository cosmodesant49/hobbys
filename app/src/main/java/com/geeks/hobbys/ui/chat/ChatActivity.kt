package com.geeks.hobbys.ui.chat

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.geeks.hobbys.MainActivity
import com.geeks.hobbys.R
import com.geeks.hobbys.databinding.ActivityChatBinding
import com.geeks.hobbys.ui.chat.adapter.MessagesAdapter
import com.geeks.hobbys.ui.model.MessageModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private var adapter: MessagesAdapter? = null
    private var messages: ArrayList<MessageModel>? = null
    private var senderRoom: String? = null
    private var receiveRoom: String? = null
    private var database: FirebaseDatabase? = null
    private var storage: FirebaseStorage? = null
    private var dialog: ProgressDialog? = null
    private var senderUid: String? = null
    private var receiveUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog = ProgressDialog(this)
        dialog!!.setMessage("Updating Photo...")
        dialog!!.setCancelable(false)
        messages = ArrayList()

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        val name = intent.getStringExtra("name") ?: "Unknown"
        val profile = intent.getStringExtra("image")
        receiveUid = intent.getStringExtra("uid")
        senderUid = FirebaseAuth.getInstance().uid

        binding.name.text = name
        Glide.with(this)
            .load(profile)
            .placeholder(R.drawable.placeholder)
            .into(binding.profile01)

        if (receiveUid != null) {
            database!!.reference.child("Presence").child(receiveUid!!)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val status = snapshot.getValue(String::class.java)
                            if (status == "offline") {
                                binding.status.visibility = View.GONE
                            } else {
                                binding.status.text = status
                                binding.status.visibility = View.VISIBLE
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }

        senderRoom = senderUid + receiveUid
        receiveRoom = receiveUid + senderUid
        adapter = MessagesAdapter(this, messages!!, senderRoom!!, receiveRoom!!)

        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvChat.adapter = adapter
        database!!.reference.child("chats")
            .child(senderRoom!!)
            .child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages!!.clear()
                    for (snapshot1 in snapshot.children) {
                        val message: MessageModel? = snapshot1.getValue(MessageModel::class.java)
                        if (message != null) {
                            message.messageId = snapshot1.key
                            messages!!.add(message)
                        }
                    }
                    adapter!!.notifyDataSetChanged()
                    binding.rvChat.scrollToPosition(messages!!.size - 1)
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        binding.ivSend.setOnClickListener {
            val messageTxt: String = binding.messageBox.text.toString()
            if (messageTxt.isNotEmpty()) {
                val date = Date()
                val message = MessageModel(messageTxt, senderUid, date.time)

                binding.messageBox.setText("")
                val randomKey = database!!.reference.push().key
                val lastMsgObj = HashMap<String, Any>()
                lastMsgObj["lastMsg"] = message.message!!
                lastMsgObj["lastMsgTime"] = date.time

                database!!.reference.child("chats").child(senderRoom!!)
                    .updateChildren(lastMsgObj)
                database!!.reference.child("chats").child(receiveRoom!!)
                    .updateChildren(lastMsgObj)
                database!!.reference.child("chats").child(senderRoom!!)
                    .child("messages")
                    .child(randomKey!!)
                    .setValue(message).addOnSuccessListener {
                        database!!.reference.child("chats")
                            .child(receiveRoom!!)
                            .child("messages")
                            .child(randomKey)
                            .setValue(message)
                    }
            }
        }
        binding.ivBack.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.attachment.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 25)
        }

        val handler = Handler()
        binding.messageBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                database!!.reference.child("Presence")
                    .child(senderUid!!)
                    .setValue("typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 1000)
            }

            var userStoppedTyping = Runnable {
                database!!.reference.child("Presence")
                    .child(senderUid!!)
                    .setValue("Online")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence")
            .child(currentId!!)
            .setValue("Online")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 25 && data != null && data.data != null) {
            val selectedImage = data.data
            val calendar = Calendar.getInstance()
            val reference = storage!!.reference.child("chats")
                .child(calendar.timeInMillis.toString() + "")
            dialog!!.show()
            reference.putFile(selectedImage!!).addOnCompleteListener { task ->
                dialog!!.dismiss()
                if (task.isSuccessful) {
                    reference.downloadUrl.addOnSuccessListener { uri ->
                        val filePath = uri.toString()
                        val date = Date()
                        val message = MessageModel("photo", senderUid, date.time)
                        message.imageUrl = filePath
                        binding.messageBox.setText("")
                        val randomKey = database!!.reference.push().key
                        val lastMsgObj = HashMap<String, Any>()
                        lastMsgObj["lastMsg"] = "photo"
                        lastMsgObj["lastMsgTime"] = date.time
                        database!!.reference.child("chats")
                            .child(senderRoom!!)
                            .updateChildren(lastMsgObj)
                        database!!.reference.child("chats")
                            .child(receiveRoom!!)
                            .updateChildren(lastMsgObj)
                        database!!.reference.child("chats")
                            .child(senderRoom!!)
                            .child("messages")
                            .child(randomKey!!)
                            .setValue(message).addOnSuccessListener {
                                database!!.reference.child("chats")
                                    .child(receiveRoom!!)
                                    .child("messages")
                                    .child(randomKey)
                                    .setValue(message)
                            }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence")
            .child(currentId!!)
            .setValue("offline")
    }
}
