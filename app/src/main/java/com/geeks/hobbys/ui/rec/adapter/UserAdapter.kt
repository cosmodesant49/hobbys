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
import com.geeks.hobbys.ui.chat.ChatFragment
import com.geeks.hobbys.ui.model.User

class UserAdapter(var context: Context, var userList:ArrayList<User>):
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {




    inner class UserViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        val binding: ItemRecBinding = ItemRecBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        var v = LayoutInflater.from(context).inflate(
            R.layout.item_rec,parent,false)
        return UserViewHolder(v)
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user= userList[position]
        holder.binding.tvUsername.text = user.name
        Glide.with(context).load(user.profileImg)
            .placeholder(R.drawable.avatar)
            .into(holder.binding.civProfile)
        holder.itemView.setOnClickListener{
            val intent = Intent(context,ChatFragment::class.java)
            intent.putExtra("name",user.name)
            intent.putExtra("image",user.profileImg)
            intent.putExtra("uid",user.uid)
            context.startActivity(intent)
        }
    }
}