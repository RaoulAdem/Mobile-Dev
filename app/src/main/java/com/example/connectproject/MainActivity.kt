package com.example.connectproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ExpandableListAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.connectproject.databinding.ActivityHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var adapter: ExpandableListAdapter
    private lateinit var storageReference: StorageReference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var navigationView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().getReference("UserImages")
        database = Firebase.database.reference
        val expandableListView = binding.expandableListView
        val currentUser = FirebaseAuth.getInstance().currentUser
        sharedPreferences = getPreferences(Context.MODE_PRIVATE)

        //image
        currentUser?.let { user ->
            val profileRef = storageReference.child(user.uid)
            profileRef.downloadUrl.addOnSuccessListener { url ->
                val profileImgUrl = url.toString()
                Glide.with(this@MainActivity)
                    .load(profileImgUrl)
                    .placeholder(R.drawable.profile_placeholder) //set placeholder while loading
                    .into(binding.cardImage)
            }
                .addOnFailureListener {
                    binding.cardImage.setImageResource(R.drawable.profile_placeholder) //set default placeholder on error
                }
        }

        //profile info
        currentUser?.let { user ->
            database.child("users").child(user.uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val firstName = dataSnapshot.child("firstName").value.toString().capitalize()
                        val lastName = dataSnapshot.child("lastName").value.toString().uppercase()
                        val email = dataSnapshot.child("email").value.toString()
                        val age = dataSnapshot.child("age").value.toString()
                        val fullName = "$firstName $lastName"
                        binding.homeInfo.text = "Welcome back, $fullName!"
                        binding.cardInfo.text = """
                            Name: $fullName
                            Email: $email
                            Age: $age
                            You can change them in the profile section!
                        """.trimIndent()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }

        //list users
        database.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val titles = mutableListOf<String>()
                    val listData = HashMap<String, List<String>>()
                    val userUids = mutableListOf<String>()
                    val isTeacherCurrentUser = dataSnapshot.child(currentUser!!.uid).child("teacher").getValue(Boolean::class.java)
                    for (userSnapshot in dataSnapshot.children) {
                        val isTeacher = userSnapshot.child("teacher").getValue<Boolean>() ?: false
                        if (isTeacherCurrentUser != isTeacher) {
                            val firstname = userSnapshot.child("firstName").getValue<String>()?.capitalize() ?: ""
                            val lastname = userSnapshot.child("lastName").getValue<String>()?.toUpperCase() ?: ""
                            val fullName = "$firstname $lastname"
                            val useruid = userSnapshot.key ?: ""
                            userUids.add(useruid)
                            val userOptions = listOf("Add to favorite")
                            val beDisplayed = userSnapshot.child("beDisplayed").getValue<Boolean>() ?: false
                            if (beDisplayed) {
                                titles.add(fullName)
                                listData[fullName] = userOptions
                            }
                        }
                    }
                    adapter = ExpandableListAdapter(this@MainActivity, titles, listData, userUids, storageReference)
                    expandableListView.setAdapter(adapter)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error fetching users!", Toast.LENGTH_SHORT).show()
            }
        })

        //when we click
        expandableListView.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            val selectedTitle = adapter.getGroup(groupPosition) as String
            val selectedChild = adapter.getChild(groupPosition, childPosition) as String
            val userRef = database.child("users").child(currentUser?.uid ?: "")
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val firstName = dataSnapshot.child("firstName").getValue(String::class.java)?.capitalize() ?: ""
                    val lastName = dataSnapshot.child("lastName").getValue(String::class.java)?.uppercase() ?: ""
                    val fullName = "$firstName $lastName"
                    database.child("favorites").child(fullName).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(favoritesSnapshot: DataSnapshot) {
                            val favorites: MutableList<String> = favoritesSnapshot.getValue<List<String>>()?.toMutableList() ?: mutableListOf()
                            if (favorites.contains(selectedTitle)) {
                                Toast.makeText(applicationContext, "$selectedTitle is already in your favorites", Toast.LENGTH_SHORT).show()
                            } else {
                                if (favorites.size == 3) {
                                    Toast.makeText(applicationContext, "Your list of favorites is full (3 users)", Toast.LENGTH_SHORT).show()
                                } else {
                                    favorites.add(selectedTitle)
                                    userRef.child("favorites").setValue(favorites)
                                    database.child("favorites").child(fullName).setValue(favorites)
                                    Toast.makeText(applicationContext, "Added to favorites: $selectedTitle", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            // Handle database error
                            Toast.makeText(applicationContext, "Error fetching favorites data", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                    Toast.makeText(applicationContext, "Error fetching user data", Toast.LENGTH_SHORT).show()
                }
            })
            true
        }

        //when clicking the redirect text
//        binding.button.setOnClickListener {
//            usersRef.addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(dataSnapshot: DataSnapshot) {
//                    if (dataSnapshot.exists()) {
//                        for (userSnapshot in dataSnapshot.children) {
//                            val text = edittext.text.toString()
//                            if((userSnapshot.child("teacher").getValue<Boolean>() ?: "") == false) {
//                            } else {
//
//                            }
//                        }
//                    }
//                }
//                override fun onCancelled(databaseError: DatabaseError) {
//                    Toast.makeText(this@MainActivity, "Error fetching users!", Toast.LENGTH_SHORT).show()
//                }
//            })
//        }

        binding.beDisplayed.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.beDisplayedInfo.text = "You will be listed"
                database.child("users").child(currentUser?.uid ?: "").child("beDisplayed").setValue(true)
            } else {
                binding.beDisplayedInfo.text = "You will no longer be listed"
                database.child("users").child(currentUser?.uid ?: "").child("beDisplayed").setValue(false)
            }
        }

        navigationView = findViewById(R.id.navigation)
        navigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    return@setOnItemSelectedListener true
                }
                R.id.esib -> {
                    startActivity(Intent(this, ESIBActivity::class.java))
                    return@setOnItemSelectedListener true
                }
                else -> false
            }
        }
    }
    override fun onResume() {
        super.onResume()
        //restore the state
        binding.beDisplayed.isChecked = sharedPreferences.getBoolean("toggleButtonStateDisplayed", false)
//        val isDarkModeEnabled = sharedPreferences.getBoolean("DarkMode", false)
//        if(isDarkModeEnabled) {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//        } else {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//        }
    }
    override fun onPause() {
        super.onPause()
        //save the state
        val editor = sharedPreferences.edit()
        editor.putBoolean("toggleButtonStateDisplayed", binding.beDisplayed.isChecked)
        editor.apply()
    }
}