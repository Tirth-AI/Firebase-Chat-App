package com.tirth.chatapp

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.tirth.chatapp.activity.LatestMessageActivity
import com.tirth.chatapp.model.User
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var selectPhotoButton : Button
    private var selectedPhotoURI: Uri? = null
    private lateinit var storage : FirebaseStorage
    var customProgressDialog: Dialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {

//        val firebaseUser = FirebaseAuth.getInstance().currentUser!!
//        if(firebaseUser != null){
//            val intent = Intent(this, LatestMessageActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
//        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val registerButton : Button = findViewById(R.id.register_Button_Register)
        registerButton.setOnClickListener {
            showProgressDialog()
            userRegistration()
        }
        val alreadyHaveAccount : TextView = findViewById(R.id.already_Have_Account_TextView)
        alreadyHaveAccount.setOnClickListener {
            finish()
        }

        selectPhotoButton = findViewById(R.id.select_Photo_Button_Register)
        selectPhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == RESULT_OK){
            Log.d("RegisterActivity", "Photo was selected.")
            selectedPhotoURI = data?.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoURI)
            val profileImageView : ImageView = findViewById(R.id.profileImage_ImageView_Register)
            profileImageView.setImageBitmap(bitmap)
            selectPhotoButton.alpha = 0f
//            val bitmapDrawable = BitmapDrawable(bitmap)
//            selectPhotoButton.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun userRegistration(){
        val emailView : EditText = findViewById(R.id.editText_Email_Register)
        val passwordView : EditText = findViewById(R.id.editText_Password_Register)
        val usernameView : EditText = findViewById(R.id.editText_UserName_Register)

        val email = emailView.text.toString()
        val password = passwordView.text.toString()
        val username = usernameView.text.toString()

        if(email.isEmpty() || password.isEmpty() || username.isEmpty()){
            cancelCustomDialog()
            Toast.makeText(this, "All fields are necessary", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("RegisterActivity", "Email is : ${email}")
        Log.d("RegisterActivity", "Password is : ${password}")

        //Firebase Authentication to create user with email and password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                if(!it.isSuccessful) return@addOnCompleteListener
                Log.d("RegisterActivity","Successfully created user with uid: ${it.result.user?.uid}")
                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener{
                cancelCustomDialog()
                Log.d("RegisterActivity", "Failed to create the user: ${it.message}")
                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageToFirebaseStorage() {
        if(selectedPhotoURI == null) return

        val filename = UUID.randomUUID().toString()
        storage = FirebaseStorage.getInstance()
        val ref = storage.reference.child("images/$filename")

        ref.putFile(selectedPhotoURI!!)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Image successfully uploaded")
                ref.downloadUrl.addOnSuccessListener {
                    Log.d("RegisterActivity", "Profile Image Location: $it")
                    saveUserToFirebaseDatabase(it.toString())
            }
            .addOnFailureListener{
                Log.d("RegisterActivity", it.message.toString())
            }
        }
    }

    private fun saveUserToFirebaseDatabase(profileImageURL: String) {
        val usernameView : EditText = findViewById(R.id.editText_UserName_Register)
        val uid = FirebaseAuth.getInstance().uid ?: ""
        var userDatabase = Firebase.database.reference.child("Users/$uid")
        val user = User(uid, usernameView.text.toString(), profileImageURL)

        userDatabase.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity","User saved to the Firebase Database")
                cancelCustomDialog()
                val intent = Intent(this, LatestMessageActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                cancelCustomDialog()
                Log.d("RegisterActivity", it.toString())
            }
    }

    private fun showProgressDialog(){
        customProgressDialog = Dialog(this@RegisterActivity)
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