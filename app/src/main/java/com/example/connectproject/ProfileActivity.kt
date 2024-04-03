package com.example.connectproject

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ExpandableListAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.example.connectproject.databinding.ActivityProfileBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var navigationView: BottomNavigationView
    private lateinit var database: DatabaseReference
    private lateinit var adapter: ExpandableListAdapter
    private lateinit var storageReference: StorageReference
    private var uri: Uri? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().getReference("UserImages")
        database = Firebase.database.reference
        val currentUser = FirebaseAuth.getInstance().currentUser
        sharedPreferences = getPreferences(Context.MODE_PRIVATE)

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri ->
            if (imageUri != null) {
                uri = imageUri
                binding.profileImage.setImageURI(imageUri)
                storageReference.child(currentUser!!.uid).putFile(imageUri)
                    .addOnSuccessListener { uploadedImage ->
                        uploadedImage.metadata?.reference?.downloadUrl?.addOnSuccessListener {
                            database.child("users").child(currentUser.uid).child("profileImg").setValue(imageUri.toString())
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this,
                            "Image upload failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        }

        binding.profileImage.setOnClickListener {
            pickImage.launch("image/*")
            uri?.let { imageUri ->
                val profileImageRef = storageReference.child(currentUser!!.uid)
                profileImageRef.putFile(imageUri)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Image upload succesful",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        }

        currentUser?.let { user ->
            val profileRef = storageReference.child(user.uid)
            profileRef.downloadUrl.addOnSuccessListener { url ->
                val profileImgUrl = url.toString()
                Glide.with(this@ProfileActivity)
                    .load(profileImgUrl)
                    .placeholder(R.drawable.profile_placeholder) //set placeholder while loading
                    .into(binding.profileImage)
            }
                .addOnFailureListener {
                    binding.profileImage.setImageResource(R.drawable.profile_placeholder) //set default placeholder on error
                }
        }

        currentUser?.let { user ->
            database.child("users").child(user.uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val firstName =
                            dataSnapshot.child("firstName").value.toString().capitalize()
                        val lastName = dataSnapshot.child("lastName").value.toString().uppercase()
                        val fullName = "$firstName $lastName"
                        binding.profileInfo.text = "Welcome back, $fullName!"
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }

        //add email format check & red edittext & CHECK ISSUE
        binding.changeMail.setOnClickListener {
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_update_email, null)
            val oldEmail = view.findViewById<EditText>(R.id.oldEmail)
            val newEmail = view.findViewById<EditText>(R.id.newMail)
            val password = view.findViewById<EditText>(R.id.password)
            val updateEmailButton = view.findViewById<Button>(R.id.updateEmailButton)
            val builder = AlertDialog.Builder(this)
            builder.setView(view)
            val dialog = builder.create()
            dialog.show()
            updateEmailButton.setOnClickListener {
                val olde = oldEmail.text.toString()
                val newe = newEmail.text.toString()
                val pass = password.text.toString()
                if (olde.isEmpty()) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Current Email cannot be empty",
                        Toast.LENGTH_LONG
                    ).show()
                    oldEmail.error = "Please enter old mail"
                    return@setOnClickListener
                }

                if (newe.isEmpty()) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "New Email cannot be empty",
                        Toast.LENGTH_LONG
                    ).show()
                    newEmail.error = "Please enter new mail"
                    return@setOnClickListener
                }
                val ref = database.child("users").child(currentUser?.uid ?: "").child("email")
                val authCredential: AuthCredential =
                    EmailAuthProvider.getCredential(currentUser?.email!!, pass)
                currentUser.reauthenticate(authCredential)
                    .addOnSuccessListener {
                        currentUser.updateEmail(newe)
                            .addOnSuccessListener {
                                ref.setValue(newe)
                                dialog.dismiss()
                                Toast.makeText(
                                    this@ProfileActivity,
                                    "Changed Password",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@ProfileActivity, "Failedee", Toast.LENGTH_LONG)
                                    .show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@ProfileActivity, "Failed", Toast.LENGTH_LONG).show()
                    }
            }
        }

        //add password format check & red edittext
        binding.changePassword.setOnClickListener {
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_update_password, null)
            val oldpassword = view.findViewById<EditText>(R.id.oldPassword)
            val newpassword = view.findViewById<EditText>(R.id.newPassword)
            val updatepassword = view.findViewById<Button>(R.id.updatePasswordButton)
            val builder = AlertDialog.Builder(this)
            builder.setView(view)
            val dialog = builder.create()
            dialog.show()
            updatepassword.setOnClickListener {
                val oldp = oldpassword.text.toString()
                val newp = newpassword.text.toString()
                if (oldp.isEmpty()) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Current Password cant be empty",
                        Toast.LENGTH_LONG
                    ).show()
                    oldpassword.error = "Please enter old password"
                    return@setOnClickListener
                }
                if (newp.isEmpty()) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "New Password cant be empty",
                        Toast.LENGTH_LONG
                    ).show()
                    newpassword.error = "Please enter new password"
                    return@setOnClickListener
                }
                val ref = database.child("users").child(currentUser?.uid ?: "").child("password")
                val authCredential: AuthCredential =
                    EmailAuthProvider.getCredential(currentUser?.email!!, oldp)
                currentUser.reauthenticate(authCredential)
                    .addOnSuccessListener {
                        currentUser.updatePassword(newp)
                            .addOnSuccessListener {
                                ref.setValue(newp)
                                dialog.dismiss()
                                Toast.makeText(
                                    this@ProfileActivity,
                                    "Changed Password",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@ProfileActivity, "Failed", Toast.LENGTH_LONG)
                                    .show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@ProfileActivity, "Failed", Toast.LENGTH_LONG).show()
                    }
            }
        }

        binding.changePhone.setOnClickListener {
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_update_phone, null)
            val oldphone = view.findViewById<EditText>(R.id.oldPhone)
            val newphone = view.findViewById<EditText>(R.id.newPhone)
            val updatephone = view.findViewById<Button>(R.id.updatePhoneButton)
            val builder = AlertDialog.Builder(this)
            builder.setView(view)
            val dialog = builder.create()
            dialog.show()
            updatephone.setOnClickListener {
                val oldp = oldphone.text.toString()
                val newp = newphone.text.toString()
                if (oldp.isEmpty()) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Current phone number can't be empty",
                        Toast.LENGTH_LONG
                    ).show()
                    oldphone.error = "Please enter phone number"
                    return@setOnClickListener
                }
                if (newp.isEmpty()) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "New phone number can't be empty",
                        Toast.LENGTH_LONG
                    ).show()
                    newphone.error = "Please enter phone number"
                    return@setOnClickListener
                }
                if (newp == oldp) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "New phone number and old phone number can't be the same",
                        Toast.LENGTH_LONG
                    ).show()
                    oldphone.error = "Please enter phone number"
                    newphone.error = "Please enter phone number"
                    return@setOnClickListener
                }
                val ref = database.child("users").child(currentUser?.uid ?: "").child("phone")
                if (ref.get().toString() == newp) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "New phone number and saved phone number can't be the same",
                        Toast.LENGTH_LONG
                    ).show()
                    newphone.error = "Please enter new phone number"
                    return@setOnClickListener
                }
                ref.get().addOnSuccessListener { snapshot ->
                    if (snapshot.value.toString() != oldp) {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Old phone number is incorrect. Please enter the correct old phone number",
                            Toast.LENGTH_LONG
                        ).show()
                        oldphone.error = "Please enter correct old phone number"
                        return@addOnSuccessListener
                    }
                    if (newp.length != 8) {
                        Toast.makeText(
                            this@ProfileActivity,
                            "New phone number format incorrect",
                            Toast.LENGTH_LONG
                        ).show()
                        newphone.error = "Please enter a 8 digits number"
                        return@addOnSuccessListener
                    }
                    ref.setValue(newp)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                dialog.dismiss()
                                Toast.makeText(
                                    this@ProfileActivity,
                                    "Number changed!",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                // Handle database write error
                                Toast.makeText(
                                    this@ProfileActivity,
                                    "Error updating phone number",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
            }
        }

        val expandableListView = binding.expandableListView
        //list users
        database.child("users").child(currentUser!!.uid).child("favorites").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val titles = mutableListOf<String>()
                    val listData = HashMap<String, List<String>>()
                    val userFavorites = mutableListOf<Pair<String,String>>() //uid & fullname
                    for (favoriteSnapshot in dataSnapshot.children) {
                        val fullnameFavorites = favoriteSnapshot.getValue<String>() ?: ""
                        val uidFavorites = fullnameFavorites
                        userFavorites.add(Pair(uidFavorites,fullnameFavorites))
                    }
                    for ((uidFavorites,fullnameFavorites) in userFavorites) {
                        titles.add(fullnameFavorites)
                        listData[fullnameFavorites] = listOf("""
                            (Still has not been accepted)
                            Remove from favorites
                            $uidFavorites
                        """.trimIndent())
                    }
                    adapter = ExpandableListAdapter(this@ProfileActivity, titles, listData, userFavorites.map{it.first}, storageReference)
                    expandableListView.setAdapter(adapter)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@ProfileActivity, "Error fetching users!", Toast.LENGTH_SHORT).show()
            }
        })

        //when we click
        expandableListView.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            val selectedTitle = adapter.getGroup(groupPosition) as String
            val selectedChild = adapter.getChild(groupPosition, childPosition) as String
            val userRef = database.child("users").child(currentUser?.uid ?: "")
            userRef.get().addOnSuccessListener { dataSnapshot ->
                val favorites: MutableList<String> = dataSnapshot.child("favorites").getValue<List<String>>()?.toMutableList() ?: mutableListOf()
                favorites.remove(selectedTitle)
                userRef.child("favorites").setValue(favorites)
                Toast.makeText(
                    applicationContext,
                    "Removed from favorites: $selectedTitle",
                    Toast.LENGTH_SHORT
                ).show()
            }.addOnFailureListener {
                Toast.makeText(applicationContext, "Error fetching user data", Toast.LENGTH_SHORT).show()
            }
            true
        }

        val isDarkModeEnabled = sharedPreferences.getBoolean("DarkMode", false)
        binding.darkMode.isChecked = isDarkModeEnabled
        binding.darkMode.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("DarkMode", isChecked).apply()
            if(isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        binding.logoutButton.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        navigationView = findViewById(R.id.navigation)
        navigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    return@setOnItemSelectedListener true
                }
            }
            false
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
    override fun onPause() {
        super.onPause()
        //save the state
//        val editor = sharedPreferences.edit()
//        editor.putBoolean("isDarkMode", binding.darkMode.isChecked)
//        editor.apply()
    }
}