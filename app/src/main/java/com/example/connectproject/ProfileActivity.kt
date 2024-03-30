package com.example.connectproject

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.connectproject.databinding.ActivityMainBinding
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask

class ProfileActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var storagepath: String
    private lateinit var uid: String
    private lateinit var set: ImageView
    private lateinit var profilepic: TextView
    private lateinit var editMail: TextView
    private lateinit var editpassword: TextView
    private lateinit var pd: ProgressDialog
    private val CAMERA_REQUEST = 100
    private val STORAGE_REQUEST = 200
    private val IMAGEPICK_GALLERY_REQUEST = 300
    private val IMAGE_PICKCAMERA_REQUEST = 400
    private lateinit var cameraPermission: Array<String>
    private lateinit var storagePermission: Array<String>
    private var imageuri: Uri? = null
    private lateinit var profileOrCoverPhoto: String
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        profilepic = findViewById(R.id.profile_button)
        logoutButton= findViewById(R.id.logoutButton)
        editMail= findViewById(R.id.change_mail)
        set = findViewById(R.id.profile_image)
        pd = ProgressDialog(this)
        pd.setCanceledOnTouchOutside(false)
        editpassword = findViewById(R.id.change_password)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!
        firebaseDatabase = FirebaseDatabase.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        databaseReference = firebaseDatabase.getReference("Users")
        storagepath = "Users_Profile_Cover_image/"
        cameraPermission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val query: Query = databaseReference.orderByChild("email").equalTo(firebaseUser.email)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (dataSnapshot1 in dataSnapshot.children) {
                    val image = dataSnapshot1.child("image").value.toString()
                    try {
                        Glide.with(this@ProfileActivity).load(image).into(set)
                    } catch (e: Exception) {
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        editpassword.setOnClickListener {
            pd.setMessage("Changing Password")
            showPasswordChangeDailog()
        }

        profilepic.setOnClickListener {
            pd.setMessage("Updating Profile Picture")
            profileOrCoverPhoto = "image"
            showImagePicDialog()
        }

        editMail.setOnClickListener {
            pd.setMessage("Updating Mail")
            showMailChangeDialog();
        }
        logoutButton.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onPause() {
        super.onPause()
        val query: Query = databaseReference.orderByChild("email").equalTo(firebaseUser.email)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (dataSnapshot1 in dataSnapshot.children) {
                    val image = dataSnapshot1.child("image").value.toString()
                    try {
                        Glide.with(this@ProfileActivity).load(image).into(set)
                    } catch (e: Exception) {
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onStart() {
        super.onStart()
        val query: Query = databaseReference.orderByChild("email").equalTo(firebaseUser.email)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (dataSnapshot1 in dataSnapshot.children) {
                    val image = dataSnapshot1.child("image").value.toString()
                    try {
                        Glide.with(this@ProfileActivity).load(image).into(set)
                    } catch (e: Exception) {
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    // checking storage permission ,if given then we can add something in our storage
    private fun checkStoragePermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        return result
    }

    // requesting for storage permission
    private fun requestStoragePermission() {
        requestPermissions(storagePermission, STORAGE_REQUEST)
    }

    // checking camera permission ,if given then we can click image using our camera
    private fun checkCameraPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        return result && result1
    }

    // requesting for camera permission if not given
    private fun requestCameraPermission() {
        requestPermissions(cameraPermission, CAMERA_REQUEST)
    }

    // We will show an alert box where we will write our old and new password
    private fun showPasswordChangeDailog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_update_password, null)
        val oldpass = view.findViewById<EditText>(R.id.oldpasslog)
        val newpass = view.findViewById<EditText>(R.id.newpasslog)
        val editpass = view.findViewById<Button>(R.id.updatepass)
        val builder = AlertDialog.Builder(this)
        builder.setView(view)
        val dialog = builder.create()
        dialog.show()
        editpass.setOnClickListener {
            val oldp = oldpass.text.toString().trim()
            val newp = newpass.text.toString().trim()
            if (TextUtils.isEmpty(oldp)) {
                Toast.makeText(this@ProfileActivity, "Current Password cant be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(newp)) {
                Toast.makeText(this@ProfileActivity, "New Password cant be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            dialog.dismiss()
            updatePassword(oldp, newp)
        }
    }

    // Now we will check that if old password was authenticated
    // correctly then we will update the new password
    private fun updatePassword(oldp: String, newp: String) {
        pd.show()
        val user = firebaseAuth.currentUser
        val authCredential: AuthCredential = EmailAuthProvider.getCredential(user?.email!!, oldp)
        user.reauthenticate(authCredential)
            .addOnSuccessListener {
                user.updatePassword(newp)
                    .addOnSuccessListener {
                        pd.dismiss()
                        Toast.makeText(this@ProfileActivity, "Changed Password", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener {
                        pd.dismiss()
                        Toast.makeText(this@ProfileActivity, "Failed", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener {
                pd.dismiss()
                Toast.makeText(this@ProfileActivity, "Failed", Toast.LENGTH_LONG).show()
            }
    }

    // Updating name
    private fun showMailChangeDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_update_email, null)
        val oldEmail = view.findViewById<EditText>(R.id.oldEmailog)
        val newEmail = view.findViewById<EditText>(R.id.newmaillog)
        val updateEmailButton = view.findViewById<Button>(R.id.updateEmailButton)
        val builder = AlertDialog.Builder(this)
        builder.setView(view)
        val dialog = builder.create()
        dialog.show()
        updateEmailButton.setOnClickListener {
            val oldEmailAddress = oldEmail.text.toString().trim()
            val newEmailAddress = newEmail.text.toString().trim()

            if (TextUtils.isEmpty(oldEmailAddress)) {
                Toast.makeText(this@ProfileActivity, "Current Email cannot be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(newEmailAddress)) {
                Toast.makeText(this@ProfileActivity, "New Email cannot be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            dialog.dismiss()
            updateEmail(oldEmailAddress, newEmailAddress)
        }
    }

    // Function to update the user's email address
    private fun updateEmail(oldEmail: String, newEmail: String) {
        pd.show()
        val user = firebaseAuth.currentUser
        val authCredential: AuthCredential = EmailAuthProvider.getCredential(user?.email!!, oldEmail)
        user.reauthenticate(authCredential)
            .addOnSuccessListener {
                user.verifyBeforeUpdateEmail(newEmail)
                    .addOnSuccessListener {
                        pd.dismiss()
                        Toast.makeText(this@ProfileActivity, "Email changed successfully", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { exception ->
                        pd.dismiss()
                        Toast.makeText(this@ProfileActivity, "Failed to update email: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { exception ->
                pd.dismiss()
                Toast.makeText(this@ProfileActivity, "Re-authentication failed: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }


    // Here we are showing image pic dialog where we will select
    // and image either from camera or gallery
    private fun showImagePicDialog() {
        val options = arrayOf("Camera", "Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Image From")
        builder.setItems(options) { dialog, which ->
            // if access is not given then we will request for permission
            if (which == 0) {
                if (!checkCameraPermission()) {
                    requestCameraPermission()
                } else {
                    pickFromCamera()
                }
            } else if (which == 1) {
                if (!checkStoragePermission()) {
                    requestStoragePermission()
                } else {
                    pickFromGallery()
                }
            }
        }
        builder.create().show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGEPICK_GALLERY_REQUEST) {
                imageuri = data?.data
                uploadProfileCoverPhoto(imageuri)
            }
            if (requestCode == IMAGE_PICKCAMERA_REQUEST) {
                uploadProfileCoverPhoto(imageuri)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST -> {
                if (grantResults.isNotEmpty()) {
                    val camera_accepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val writeStorageaccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if (camera_accepted && writeStorageaccepted) {
                        pickFromCamera()
                    } else {
                        Toast.makeText(this, "Please Enable Camera and Storage Permissions", Toast.LENGTH_LONG).show()
                    }
                }
            }
            STORAGE_REQUEST -> {
                if (grantResults.isNotEmpty()) {
                    val writeStorageaccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (writeStorageaccepted) {
                        pickFromGallery()
                    } else {
                        Toast.makeText(this, "Please Enable Storage Permissions", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // Here we will click a photo and then go to startactivityforresult for updating data
    private fun pickFromCamera() {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_pic")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description")
        imageuri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        val camerIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        camerIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri)
        startActivityForResult(camerIntent, IMAGE_PICKCAMERA_REQUEST)
    }

    // We will select an image from gallery
    private fun pickFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, IMAGEPICK_GALLERY_REQUEST)
    }

    // We will upload the image from here.
    private fun uploadProfileCoverPhoto(uri: Uri?) {
        pd.show()

        // We are taking the filepath as storagepath + firebaseauth.getUid()+".png"
        val filepathname = "$storagepath$profileOrCoverPhoto${firebaseUser.uid}"
        val storageReference1 = storageReference.child(filepathname)
        storageReference1.putFile(uri!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);

                // We will get the url of our image using uritask
                val downloadUri = uriTask.result
                if (uriTask.isSuccessful) {

                    // updating our image url into the realtime database
                    val hashMap = HashMap<String, Any>()
                    hashMap[profileOrCoverPhoto] = downloadUri.toString()
                    databaseReference.child(firebaseUser.uid).updateChildren(hashMap)
                        .addOnSuccessListener {
                            pd.dismiss()
                            Toast.makeText(this@ProfileActivity, "Updated", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener {
                            pd.dismiss()
                            Toast.makeText(this@ProfileActivity, "Error Updating ", Toast.LENGTH_LONG).show()
                        }
                } else {
                    pd.dismiss()
                    Toast.makeText(this@ProfileActivity, "Error", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                pd.dismiss()
                Toast.makeText(this@ProfileActivity, "Error", Toast.LENGTH_LONG).show()
            }
    }

}
