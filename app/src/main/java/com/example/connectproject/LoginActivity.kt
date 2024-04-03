package com.example.connectproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.connectproject.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Base_Theme_ConnectProject)
        super.onCreate(savedInstanceState)
        //for loading app
        installSplashScreen()
        //
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        sharedPreferences = getPreferences(Context.MODE_PRIVATE)

        //when clicking the login button
        binding.loginButton.setOnClickListener {
            val loginUsername = binding.loginEmail.text.toString()
            val loginPassword = binding.loginPassword.text.toString()
            if (loginUsername.isNotEmpty() && loginPassword.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(loginUsername, loginPassword).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
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
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        //restore the state
        val isDarkModeEnabled = sharedPreferences.getBoolean("DarkMode", false)
//        if(isDarkModeEnabled) {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//        } else {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//        }
    }
}
