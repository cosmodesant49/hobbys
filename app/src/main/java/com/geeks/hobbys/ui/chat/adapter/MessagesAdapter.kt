package com.geeks.hobbys.ui.chat.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.geeks.hobbys.R
import com.geeks.hobbys.databinding.DeleteLayoutBinding
import com.geeks.hobbys.databinding.SendMsgBinding
import com.geeks.hobbys.ui.model.MessageModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MessagesAdapter(
    var context: Context,
    message: ArrayList<MessageModel>?,
    senderRoom:String,
    receiverRoom:String
):RecyclerView.Adapter<RecyclerView.ViewHolder?>()
{
    lateinit var messages:ArrayList<MessageModel>
    val ITEM_SENT = 1
    val ITEM_RECEIVE = 2
    val senderRoom: String
    val receiverRoom: String


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return  if (viewType == ITEM_SENT){
            val view:View = LayoutInflater.from(context).inflate(R.layout.send_msg,
                parent,false)
                SentMsgHolder(view)
        }
        else{
            val view:View = LayoutInflater.from(context).inflate(R.layout.receive_msg,
                parent,false)
            ReceiveMsgHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if(FirebaseAuth.getInstance().uid == message.senderId){
            ITEM_SENT
        }else{
            ITEM_RECEIVE
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val message = messages[position]
        if (holder.javaClass == SentMsgHolder::class.java){
            val viewHolder = holder as SentMsgHolder
            if (message.message.equals("photo")){
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.mLinear.visibility = View.GONE
                viewHolder.binding.message.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(viewHolder.binding.image)
            }
            viewHolder.binding.message.text = message.message
            viewHolder.itemView.setOnClickListener{
                val view = LayoutInflater.from(context)
                        .inflate(R.layout.delete_layout,null)
                val binding:DeleteLayoutBinding = DeleteLayoutBinding.bind(view)

                val dialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()
                binding.everyone.setOnClickListener{
                    message.message = "This message is removed"
                    message.messageId?.let {it1->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1).setValue(message)
                    }
                message.messageId.let {it1->
                    FirebaseDatabase.getInstance().reference.child("chats")
                        .child(receiverRoom)
                        .child("message")
                        .child(it1!!).setValue(message)
                    }
                    dialog.dismiss()
                }
                binding.delete.setOnClickListener{
                    message.messageId.let {it1->
                    FirebaseDatabase.getInstance().reference.child("chats")
                        .child(senderRoom)
                        .child("message")
                        .child(it1!!).setValue(null)
                    }
                    dialog.dismiss()
                }
                binding.cancel.setOnClickListener{dialog.dismiss()}
                dialog.show()
                false
            }
        }
        else{
            val viewHolder = holder as ReceiveMsgHolder
            if (message.message.equals("photo")){
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.mLinear.visibility = View.GONE
                viewHolder.binding.message.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(viewHolder.binding.image)
            }
            viewHolder.binding.message.text = message.message
            viewHolder.itemView.setOnClickListener {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.delete_layout, null)
                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)

                val dialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()
                binding.everyone.setOnClickListener {
                    message.message = "This message is removed"
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1).setValue(message)
                    }
                    message.messageId.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(receiverRoom)
                            .child("message")
                            .child(it1!!).setValue(message)
                    }
                    dialog.dismiss()
                }
                binding.delete.setOnClickListener {
                    message.messageId.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1!!).setValue(null)
                    }
                    dialog.dismiss()
                }
                binding.cancel.setOnClickListener { dialog.dismiss() }
                dialog.show()
                false
            }
        }
    }

    inner class SentMsgHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var binding:SendMsgBinding = SendMsgBinding.bind(itemView)
    }
    inner class ReceiveMsgHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var binding:SendMsgBinding = SendMsgBinding.bind(itemView)
    }
    init{
        if (message != null){
            this.messages = message
        }

        this.senderRoom = senderRoom
        this.receiverRoom = receiverRoom
    }

}