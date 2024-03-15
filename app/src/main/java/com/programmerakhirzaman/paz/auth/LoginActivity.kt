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

        auth = FirebaseAuth.getInstance()

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
        } else if (!isEmailValid(email)) {
            binding.emailRegisET.error = "Format email tidak valid!"
            return
        } else if (password.isEmpty() || password.length < 8) {
            binding.passwordRegisEt.error = "Isi field password, password tidak kurang dari 8"
            return
        }

        val auth = FirebaseAuth.getInstance()
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
                    Log.w(TAG, "createUserWithEmail:failure", it.exception)
                    Toast.makeText(
                        baseContext, "Gagal registrasi: ${it.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun loginUser() {
        val email = binding.emailET.text.toString()
        val pass = binding.passwordET.text.toString()

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Isi field tersebut", Toast.LENGTH_SHORT).show()
            return
        }
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else {
                    Log.w(TAG, "signInWithEmail:failure", it.exception)
                    Toast.makeText(baseContext, "Auth failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }

    /*private fun loginUser() {
        val email = binding.emailET.text.toString()
        val pass = binding.passwordET.text.toString()

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Isi field tersebut", Toast.LENGTH_SHORT).show()
            return
        }

        val auth = FirebaseAuth.getInstance()

        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userEmail = user?.email

                    val firestore = FirebaseFirestore.getInstance()
                    val usersCollection = firestore.collection("users")

                    if (userEmail != null) {
                        usersCollection.document(userEmail)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val status = document.getString("status")
                                    if (status == "1") {
                                        // Login berhasil dan status aktif
                                        val level = document.getString("level")
                                        val sharedPref = SharePref(this@LoginActivity)
                                        sharedPref.setSessionNIK("user", userEmail)
                                        level?.let { sharedPref.setSessionString(key_level, it) }
                                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        // Status pengguna tidak aktif
                                        Toast.makeText(
                                            this@LoginActivity,
                                            "Akun tidak aktif",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    // Data pengguna tidak ditemukan di Firestore
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Data tidak ditemukan",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("LoginActivity", "Error retrieving user data: ${e.message}")
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Terjadi kesalahan saat login",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        // Email pengguna null
                        Toast.makeText(
                            this@LoginActivity,
                            "Email pengguna tidak tersedia",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // Login gagal, tampilkan pesan kesalahan
                    Toast.makeText(
                        this@LoginActivity,
                        "Gagal login: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    } */



}