package com.tirth.chatapp.activity

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
import com.tirth.chatapp.adapter.ChatAdapter
import com.tirth.chatapp.adapter.UserAdapter
import com.tirth.chatapp.model.Chat
import com.tirth.chatapp.model.PersonalUser
import com.tirth.chatapp.model.User

class LatestMessageActivity : AppCompatActivity() {

    private lateinit var newRecyclerView: RecyclerView
    private lateinit var personalReceiverIdList: ArrayList<String>
    private lateinit var personalUserArrayList: ArrayList<User>
    private var customProgressDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_message)


        newRecyclerView = findViewById(R.id.recyclerView_Personal_User)
        newRecyclerView.setHasFixedSize(true)
        personalUserArrayList = arrayListOf()
        personalReceiverIdList = arrayListOf()
        fetchPersonalUserId()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_enu_new_message ->{
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }

            R.id.nav_menu_sign_out ->{
                val firebaseUser: FirebaseAuth = FirebaseAuth.getInstance()
                firebaseUser.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_menu_my_profile ->{
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun fetchPersonalUserId(){
        Log.d("LatestMessageActivity", "Entered fetchPersonalUserId()")
        showCustomDialog()
        val currentUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
        val uid = currentUser.uid
        val ref = FirebaseDatabase.getInstance().getReference("Personal_Users/$uid")
        personalReceiverIdList.clear()
        Log.d("LatestMessageActivity", "Reference: $ref")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    snapshot.children.forEach {
                        Log.d("LatestMessageActivity", "Snapshotss: $it")
                        val receiverId = it.value
                        personalReceiverIdList.add(receiverId.toString())
                    }
                }
                fetchPersonalUser()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LatestMessageActivity, "No saved users found", Toast.LENGTH_SHORT).show()
            }
        }
        )
    }

    private fun fetchPersonalUser()
    {
        val ref2 = FirebaseDatabase.getInstance().getReference("Users")
        ref2.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {//
                if(snapshot.exists()){
                    snapshot.children.forEach {
                        Log.d("PersonalUsers", it.toString())
                        val user = it.getValue(User::class.java)
                        if(user!!.uid in personalReceiverIdList)
                        {
                            Log.d("LatestMessageActivity","$user")
                            personalUserArrayList.add(user)
                        }
                    }
                }


                val userAdapter = UserAdapter(personalUserArrayList, this@LatestMessageActivity)
                newRecyclerView.adapter = userAdapter
                newRecyclerView.addItemDecoration(
                    DividerItemDecoration(
                        baseContext,
                        LinearLayoutManager.VERTICAL
                    )
                )
                cancelCustomDialog()
            }
            //
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LatestMessageActivity, "No Personal users found", Toast.LENGTH_SHORT).show()
            }
        }
        )
    }

    private fun showCustomDialog(){
        customProgressDialog = Dialog(this@LatestMessageActivity)
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