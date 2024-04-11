package com.example.connectproject

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import com.example.connectproject.databinding.ActivitySignupBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var signupFirstname: EditText
    private lateinit var signupLastname: EditText
    private lateinit var signupEmail: EditText
    private lateinit var signupPassword: EditText
    private lateinit var signupYears: EditText
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
        signupYears = findViewById(R.id.signupYears)
        signupTeacher = findViewById(R.id.signupTeacher)
        signupPhone = findViewById(R.id.signupPhone)

        binding.signupButton.setOnClickListener {
            val firstname = signupFirstname.text.toString()
            val lastname = signupLastname.text.toString()
            val email = signupEmail.text.toString()
            val password = signupPassword.text.toString()
            val years = signupYears.text.toString()
            val phone = signupPhone.text.toString()
            if (validateForm(firstname, lastname, email, password, years, phone)) {
                saveUserData(firstname, lastname, email, password, years, phone)
            }
        }

        binding.loginRedirect.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateForm(firstname: String, lastname: String, email: String, password: String, years: String, phone: String): Boolean {
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
        } else if (password.length<5) {
            signupPassword.error = "Password is too short"
            valid = false
        }

        if (years.isEmpty()) {
            signupYears.error = "Please enter years of experience"
            valid = false
        } else {
            try {
                val years = years.toInt()
                if (years < 0) {
                    signupYears.error = "Years of experience must be at least equal to 0"
                    valid = false
                } else if (years > 60) {
                    signupYears.error = "Years of experience seems unlikely, please enter a valid one"
                    valid = false
                }
            } catch (e: NumberFormatException) {
                signupYears.error = "Invalid years of experience format"
                valid = false
            }
        }

        if (phone.isEmpty()) {
            signupPhone.error = "Please enter phone number"
            valid = false
        } else if (phone.length!=8){
            signupPhone.error = "Phone number must be 8 digits"
            valid = false
        }
        return valid
    }

    //add toast messages
    private fun saveUserData(firstname: String, lastname: String, email: String, password: String, years: String, phone: String) {
        teacher = signupTeacher.isChecked
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val userMap = UserData(
                        firstName = firstname,
                        lastName = lastname,
                        email = email,
                        password = password,
                        years = years,
                        teacher = teacher,
                        phone = phone,
                        beListed = false
                    )
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    database.child("users").child(currentUser?.uid ?: "").setValue(userMap)
                    database.child(firstname.capitalize() + " " + lastname.uppercase()).setValue(currentUser?.uid ?: " ")
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
            }
    }
}