package com.example.connectproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import com.example.connectproject.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.google.firebase.Firebase

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var signupFirstname: EditText
    private lateinit var signupLastname: EditText
    private lateinit var signupEmail: EditText
    private lateinit var signupPassword: EditText
    private lateinit var signupAge: EditText
    private lateinit var signupTeacher: ToggleButton
    private lateinit var signupPhone: EditText

    private var teacher: Boolean = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        signupFirstname = findViewById(R.id.signupFirstname)
        signupLastname = findViewById(R.id.signupLastname)
        signupEmail = findViewById(R.id.signupEmail)
        signupPassword = findViewById(R.id.signupPassword)
        signupAge = findViewById(R.id.signupAge)
        signupTeacher = findViewById(R.id.signupTeacher)
        signupPhone = findViewById(R.id.signupPhone)

        binding.signupButton.setOnClickListener {
            val firstname = signupFirstname.text.toString()
            val lastname = signupLastname.text.toString()
            val email = signupEmail.text.toString()
            val password = signupPassword.text.toString()
            val ageText = signupAge.text.toString()
            val phoneText = signupPhone.text.toString()
            if (validateForm(firstname, lastname, email, password, ageText, phoneText)) {
                val age = ageText.toInt()
                val phone = phoneText.toInt()
                saveUserData(firstname, lastname, email, password, age, phone)
            }
        }

        binding.loginRedirect.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateForm(firstname: String, lastname: String, email: String, password: String, ageText: String, phoneText: String): Boolean {
        var valid = true
        if (firstname.isEmpty()) {
            signupFirstname.error = "Please enter firstname"
            valid = false
        } else if (firstname.length<3) {
            signupFirstname.error = "Invalid firstname format"
            valid = false
        }

        if (lastname.isEmpty()) {
            signupLastname.error = "Please enter lastname"
            valid = false
        } else if (lastname.length<3) {
            signupLastname.error = "Invalid lastname format"
            valid = false
        }

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

        if (phoneText.isEmpty()) {
            signupPhone.error = "Please enter phone number"
            valid = false
        } else if (phoneText.length!=8){
            signupPhone.error = "Phone number must be 8 digits"
            valid = false
        }
        return valid
    }

    private fun saveUserData(firstname: String, lastname: String, email: String, password: String, age: Int, phone: Int) {
        teacher = signupTeacher.isChecked
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {  // Check for successful task completion
                    val userMap = UserData(
                        firstName = firstname,
                        lastName = lastname,
                        email = email,
                        password = password,
                        age = age,
                        teacher = teacher,
                        phone = phone
                    )
                    database.child("users").push().setValue(userMap)
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)

                } else {
                    // Handle unsuccessful authentication (exception handling)
                    Log.e("saveUserData", "Error creating user", it.exception)
                }
            }
    }
}