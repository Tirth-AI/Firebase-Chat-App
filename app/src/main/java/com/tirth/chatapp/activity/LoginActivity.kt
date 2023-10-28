package com.tirth.chatapp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.tirth.chatapp.R
import com.tirth.chatapp.RegisterActivity

class LoginActivity: AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton : Button = findViewById(R.id.button_Login)
        loginButton.setOnClickListener {
            performLogin()
        }

        val noAccount : TextView = findViewById(R.id.have_No_Account_TextView)
        noAccount.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performLogin(){
        val emailView : EditText = findViewById(R.id.editText_Email_Login)
        val passwordView : EditText = findViewById(R.id.editText_Password_Login)
        val email = emailView.text.toString()
        val password = passwordView.text.toString()

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "All fields are necessary", Toast.LENGTH_SHORT).show()
            return
        }
        else{
            firebaseUser = FirebaseAuth.getInstance()
            firebaseUser.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener{
                    if(!it.isSuccessful) {
                        Log.d("LoginActivity",it.toString())
//                    Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(this, "Successfully Logged In", Toast.LENGTH_SHORT).show()
                        Log.d("LoginActivity", "Successfully Logged In")
                        val intent = Intent(this, LatestMessageActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                }
                .addOnFailureListener {
                    Log.d("LoginActivity", "Failed to login to the user: ${it.message}")
                    Toast.makeText(this, "Failed to login: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onStart() {
        super.onStart()
        val firebaseUser: FirebaseAuth = FirebaseAuth.getInstance()
        if(firebaseUser.currentUser != null){
            Log.d("LoginActivity", "User logged in")
            val intent = Intent(this@LoginActivity, LatestMessageActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}