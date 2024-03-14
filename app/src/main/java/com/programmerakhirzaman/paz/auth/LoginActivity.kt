package com.programmerakhirzaman.paz.auth

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.programmerakhirzaman.paz.R
import com.programmerakhirzaman.paz.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.loginRl.visibility = View.VISIBLE
        binding.signupRl.visibility = View.GONE

        binding.loginTxt.setOnClickListener {
            binding.loginTxt.setTextColor(Color.parseColor("#8FE1A6"))
            binding.signupTxt.setTextColor(Color.parseColor("#CBCBCB"))
            binding.signupRl.visibility = View.GONE
            binding.loginRl.visibility = View.VISIBLE
        }

        binding.signupTxt.setOnClickListener {
            binding.signupTxt.setTextColor(Color.parseColor("#8FE1A6"))
            binding.loginTxt.setTextColor(Color.parseColor("#CBCBCB"))
            binding.loginRl.visibility = View.GONE
            binding.signupRl.visibility = View.VISIBLE
        }
    }


}