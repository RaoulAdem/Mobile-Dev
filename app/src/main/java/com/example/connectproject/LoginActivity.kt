package com.example.connectproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.connectproject.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.google.firebase.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //for loading app
        Thread.sleep(2000) //2s
        installSplashScreen()
        //
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //when clicking the login button
        binding.loginButton.setOnClickListener {
            val loginUsername = binding.loginEmail.text.toString()
            val loginPassword = binding.loginPassword.text.toString()
            if (loginUsername.isNotEmpty() && loginPassword.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(loginUsername, loginPassword).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent = Intent(this, ProfileActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        Log.e("LoginActivity", it.exception.toString())
                    }
                }
            } else {
                Toast.makeText(this, "Please fill in both username and password", Toast.LENGTH_SHORT).show()
            }
        }

        //when clicking the redirect text
        binding.signupRedirect.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        if(firebaseAuth.currentUser != null){
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}
