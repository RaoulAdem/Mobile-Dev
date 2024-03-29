package com.example.connectproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.connectproject.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    private lateinit var signupEmail: EditText
    private lateinit var signupPassword: EditText
    private lateinit var signupAge: EditText

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().getReference("users")

        signupEmail = findViewById(R.id.signupEmail)
        signupPassword = findViewById(R.id.signupPassword)
        signupAge = findViewById(R.id.signupAge)

        binding.signupButton.setOnClickListener {
            val email = signupEmail.text.toString()
            val password = signupPassword.text.toString()
            val ageText = signupAge.text.toString()
            if (validateForm(email, password, ageText)) {
                val age = ageText.toInt()
                saveUserData(email, password, age)
            }
        }

        binding.loginRedirect.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateForm(email: String, password: String, ageText: String): Boolean {
        var valid = true
        if (email.isEmpty()) {
            signupEmail.error = "Please enter email"
            valid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            signupEmail.error = "Invalid email format"
            valid = false
        }

        if (password.isEmpty()) {
            signupPassword.error = "Please enter password"
            valid = false
        }

        if (ageText.isEmpty()) {
            signupAge.error = "Please enter age"
            valid = false
        } else {
            try {
                val age = ageText.toInt()
                if (age <= 0) {
                    signupAge.error = "Age must be greater than 0"
                    valid = false
                } else if (age > 120) {
                    signupAge.error = "Age seems unlikely, please enter a valid age"
                    valid = false
                }
            } catch (e: NumberFormatException) {
                signupAge.error = "Invalid age format"
                valid = false
            }
        }
        return valid
    }

    private fun saveUserData(email: String, password: String, age: Int) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        val userId = email.replace(".","")
                        val userData = UserData(userId, email, age.toString())
                        dbRef.child(userId).setValue(userData).addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Data inserted and user created successfully.",
                                    Toast.LENGTH_LONG
                                ).show()
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                            } else {
                                Log.e("SignupActivity", "Error inserting data:", it.exception)
                                Toast.makeText(
                                    this,
                                    "Error saving user data: ${it.exception}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        Log.e("SignupActivity", "Error: Could not get current user ID")
                        Toast.makeText(this, "Signup failed.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.w(
                        "SignupActivity",
                        "createUserWithEmailAndPassword failed.",
                        task.exception
                    )
                    Toast.makeText(this, "Signup failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}