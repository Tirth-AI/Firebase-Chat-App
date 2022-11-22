package com.tirth.chatapp.activity


import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tirth.chatapp.R
import com.tirth.chatapp.adapter.UserAdapter
import com.tirth.chatapp.model.User


class NewMessageActivity : AppCompatActivity() {

    private lateinit var newRecyclerView: RecyclerView
    private lateinit var userArrayList: ArrayList<User>
    private var customProgressDialog: Dialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        supportActionBar?.title = "Select User"
        newRecyclerView = findViewById(R.id.recyclerView_Select_User)
        newRecyclerView.setHasFixedSize(true)
        userArrayList = arrayListOf()
        showCustomDialog()
        fetchUser()
    }

    private fun fetchUser(){
        val currentUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
        val ref = FirebaseDatabase.getInstance().getReference("/Users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()){
                        snapshot.children.forEach {
                            Log.d("NewMessage", it.toString())
                            val user = it.getValue(User::class.java)
                            if(user!!.uid != currentUser.uid)
                            {
                                userArrayList.add(user!!)
                            }
                        }
                    }
                val userAdapter = UserAdapter(userArrayList, this@NewMessageActivity)
                newRecyclerView.adapter = userAdapter
                newRecyclerView.addItemDecoration(
                        DividerItemDecoration(
                            baseContext,
                            LinearLayoutManager.VERTICAL
                        )
                    )
                    cancelCustomDialog()
                }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@NewMessageActivity, "No saved users found", Toast.LENGTH_SHORT).show()
            }
        }
        )
    }

    private fun showCustomDialog(){
        customProgressDialog = Dialog(this@NewMessageActivity)
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

