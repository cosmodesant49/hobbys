package com.geeks.hobbys.ui.other_profile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.geeks.hobbys.MainActivity
import com.geeks.hobbys.R
import com.geeks.hobbys.databinding.ActivityOtherProfBinding

class OtherProfActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtherProfBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtherProfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = intent.getStringExtra("name")
        val hobby = intent.getStringExtra("hobby")
        val imageUrl = intent.getStringExtra("image")

        binding.tvUsername.text = name
        binding.tvHobby.text = hobby
        Glide.with(this).load(imageUrl).placeholder(R.drawable.avatar).into(binding.ivAvatar)

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
