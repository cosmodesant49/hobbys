package com.geeks.hobbys.ui.rec.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.geeks.hobbys.R
import com.geeks.hobbys.databinding.ItemRecBinding
import com.geeks.hobbys.ui.chat.ChatActivity
import com.geeks.hobbys.ui.model.User
import com.geeks.hobbys.ui.other_profile.OtherProfActivity

class UserAdapter(private val context: Context, private val userList: ArrayList<User>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ItemRecBinding = ItemRecBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_rec, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.binding.tvUsername.text = user.name
        holder.binding.tvHobby.text = user.hobby
        Glide.with(context).load(user.profileImg)
            .placeholder(R.drawable.avatar)
            .into(holder.binding.civProfile)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java).apply {
                putExtra("name", user.name)
                putExtra("hobby", user.hobby)
                putExtra("image", user.profileImg)
                putExtra("uid", user.uid)
            }
            context.startActivity(intent)
        }

        holder.binding.btnSeeProfile.setOnClickListener {
            val intent = Intent(context, OtherProfActivity::class.java).apply {
                putExtra("name", user.name)
                putExtra("hobby", user.hobby)
                putExtra("image", user.profileImg)
                putExtra("uid", user.uid)
            }
            context.startActivity(intent)
        }
    }
}
