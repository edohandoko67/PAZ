package com.programmerakhirzaman.paz.auth

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.cast.framework.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.programmerakhirzaman.paz.MainActivity
import com.programmerakhirzaman.paz.R
import com.programmerakhirzaman.paz.databinding.ActivityLoginBinding
import com.programmerakhirzaman.paz.model.LoginUser
import com.programmerakhirzaman.paz.model.SharePref
import com.programmerakhirzaman.paz.model.SharePref.Companion.key_level

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sessionManager: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.loginRl.visibility = View.VISIBLE
        binding.signupRl.visibility = View.GONE

        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.btnLogin.setOnClickListener {
            loginUser()
        }

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

    private fun registerUser() {
        val name = binding.namesRegisET.text.toString()
        val email = binding.emailRegisET.text.toString()
        val password = binding.passwordRegisEt.text.toString()

        if (name.isEmpty()){
            binding.namesRegisET.error = "Isi field name!"
            return
        } else if (email.isEmpty()) {
            binding.emailRegisET.error = "Isi field email!"
            return
        } else if (password.isEmpty() || password.length < 8) {
            binding.passwordRegisEt.error = "Isi field password, password tidak kurang dari 8"
            return
        }

        auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val user = auth.currentUser
                        Toast.makeText(
                            this,
                            "Berhasil registrasi",
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(this@LoginActivity, LoginActivity::class.java))
                        finish()
                    }
                    else {
                        // If registration fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", it.exception)
                        Toast.makeText(
                            baseContext, "Gagal registrasi: ${it.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
    }

    private fun loginUser() {
        val email = binding.emailET.text.toString()
        val pass = binding.passwordET.text.toString()
        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Isi field tersebut", Toast.LENGTH_SHORT).show()
        } else {
            FirebaseDatabase.getInstance().getReference("/session/login").orderByChild("email")
                .equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val level = snapshot.child("level").value.toString()
                                val status = snapshot.child("status").value.toString()
                                val sharedPref = SharePref(this@LoginActivity)
                                if (status == "1") {
                                    if (level == "admin") {
                                        sharedPref.setSessionNIK("user", email)
                                        sharedPref.setSessionString(key_level, level)
                                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    else {
                                        sharedPref.setSessionNIK("user", email)
                                        sharedPref.setSessionString(key_level, level)
                                        val intent =
                                            Intent(this@LoginActivity, MainActivity::class.java)
                                        //intent.putExtra("username", username_ET.text.toString())
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                                else {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Data tidak ditemukan",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                               }
                            }
                        }
                    override fun onCancelled(error: DatabaseError) {
                        Log.e("LoginActivity", "Database error: ${error.message}")
                        Toast.makeText(
                            this@LoginActivity,
                            "Terjadi kesalahan saat login",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                })
        }
    }

}