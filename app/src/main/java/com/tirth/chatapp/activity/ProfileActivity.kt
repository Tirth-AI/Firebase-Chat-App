package com.tirth.chatapp.activity

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.tirth.chatapp.R
import com.tirth.chatapp.model.User
import java.util.*

class ProfileActivity : AppCompatActivity(){
    private var customProgressDialog: Dialog? = null
    private var selectedPhotoURI : Uri?= null
    private lateinit var userProfilename: TextView
    private lateinit var userProfilePic: ImageView
    private lateinit var saveProfilePic: Button
    private lateinit var ref: DatabaseReference
    private lateinit var storage: StorageReference
    private lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar!!.title = "Profile"
        showCustomDialog()
        userProfilename= findViewById(R.id.textView_ProfileUsername)
        userProfilePic = findViewById(R.id.imageView_ProfilePic)
        saveProfilePic = findViewById(R.id.save_Profile_Button)

        currentUser = FirebaseAuth.getInstance().currentUser!!
        ref = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.uid)
        Log.d("ProfileActivity", "reference: $ref")


        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("ProfileActivity", "Inside addListenerForSingleValueEvent")
                if (snapshot.exists()) {
                    Log.d("ProfileActivity", "Snapshot exists $snapshot")

                    val user = snapshot.getValue(User::class.java)
                    Log.d("ProfileActivity", "User: $user")
                    userProfilename.text = user?.username
                    Glide.with(this@ProfileActivity).load(user?.profileImageURL)
                        .into(userProfilePic)
                    cancelCustomDialog()
                }
                else {
                    Log.d("ProfileActivity", "Snapshot does not exist")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ProfileActivity", "Some error: $error")
                cancelCustomDialog()
                Toast.makeText(this@ProfileActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        }
        )
        userProfilePic.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        saveProfilePic.setOnClickListener {
            showCustomDialog()
            updateImageToFirebaseStorage()
        }
    }
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)

            if(requestCode == 0 && resultCode == RESULT_OK){
                Log.d("ProfileActivity", "Photo was selected.")
                selectedPhotoURI = data?.data
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoURI)
                userProfilePic.setImageBitmap(bitmap)
                saveProfilePic.visibility = VISIBLE
            }
        }


    private fun updateImageToFirebaseStorage() {
        if(selectedPhotoURI == null) return

        val filename = UUID.randomUUID().toString()
        storage = FirebaseStorage.getInstance().reference.child("images/$filename")

        storage.putFile(selectedPhotoURI!!)
            .addOnSuccessListener {
                Log.d("ProfileActivity", "Image successfully updated")
                storage.downloadUrl.addOnSuccessListener {
                    Log.d("ProfileActivity", "Profile Image Location: $it")
                    updateFirebaseDatabase(it.toString())
                }
                    .addOnFailureListener{
                        Log.d("ProfileActivity", it.message.toString())
                    }
            }
    }

    private fun updateFirebaseDatabase(profileImageURL: String) {
        val uid = currentUser.uid
        val user = User(uid, userProfilename.text.toString(), profileImageURL)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("ProfileActivity","User updated to the Firebase Database")
                cancelCustomDialog()
                saveProfilePic.visibility = View.GONE
            }
            .addOnFailureListener {
                cancelCustomDialog()
                Log.d("ProfileActivity", it.toString())
            }
    }


    private fun showCustomDialog(){
        customProgressDialog = Dialog(this@ProfileActivity)
        customProgressDialog?.setContentView(R.layout.custom_progress_dialog)
        customProgressDialog?.show()
    }

    private fun cancelCustomDialog(){
        if(customProgressDialog != null){
            customProgressDialog?.dismiss()
            customProgressDialog = null
        }
    }
}