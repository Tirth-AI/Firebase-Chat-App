package com.tirth.chatapp.activity


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.tirth.chatapp.R
import com.tirth.chatapp.adapter.ChatAdapter
import com.tirth.chatapp.model.Chat
import com.tirth.chatapp.model.User
import de.hdodenhof.circleimageview.CircleImageView


class ChatActivity : AppCompatActivity() {
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var reference: DatabaseReference
    private lateinit var backBtn: ImageView
    private lateinit var chatProfilePicActionBar: CircleImageView
    private lateinit var chatActionBarUsername: TextView
    private lateinit var sendMessageBtn: ImageButton
    private lateinit var senderMessage: EditText
    private lateinit var newChatRecyclerView: RecyclerView
    private lateinit var chatArrayList: ArrayList<Chat>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        var stamp: String
        newChatRecyclerView = findViewById(R.id.recyclerView_Chat)
        newChatRecyclerView.layoutManager =
            LinearLayoutManager(this@ChatActivity, RecyclerView.VERTICAL, false)

        chatArrayList = arrayListOf()

        backBtn = findViewById(R.id.imageView_ChatActionBarBackBtn)
        chatProfilePicActionBar = findViewById(R.id.cImageView_ChatActionBarProfilePic)
        chatActionBarUsername = findViewById(R.id.textView_ChatActionBarUsername)
        backBtn.setOnClickListener {
            onBackPressed()
        }
        val intent = intent
        val userId = intent.getStringExtra("userId")
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        if (userId != null) {
            reference = FirebaseDatabase.getInstance().getReference("Users").child(userId)
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(User::class.java)
                        chatActionBarUsername.text = user?.username
                        Glide.with(this@ChatActivity).load(user?.profileImageURL)
                            .into(chatProfilePicActionBar)
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ChatActivity", error.toString())
                }
            }
            )
        }

        sendMessageBtn = findViewById(R.id.imageBtn_sendBtn)
        senderMessage = findViewById(R.id.editText_typeMessage)

        newChatRecyclerView.addOnLayoutChangeListener {
                _, _, _, _, bottom, _, _, _, oldBottom ->
            if(bottom < oldBottom)
            {
                if(chatArrayList.size > 1)
                {
                    newChatRecyclerView.smoothScrollToPosition(chatArrayList.size - 1)
                }
            }
        }

        sendMessageBtn.setOnClickListener {
            val message: String = senderMessage.text.toString()
            if (message.isEmpty()) {
                Toast.makeText(this, "Please type a message", Toast.LENGTH_SHORT).show()
                senderMessage.setText("")
            } else {
                stamp = System.currentTimeMillis().toString()
                sendMessageToDatabase(firebaseUser.uid, userId!!, message, stamp)

                chatArrayList.add(
                    Chat(firebaseUser.uid,
                        userId,
                        senderMessage.text.toString(),
                        stamp)
                )
                senderMessage.setText("")
                newChatRecyclerView.scrollToPosition(chatArrayList.size - 1)
                sendUserToPersonalDatabase(userId)
            }
        }
        readMessage(firebaseUser.uid, userId!!)
    }

    private fun sendUserToPersonalDatabase(receiverId: String) {
        val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
        val uid = firebaseUser.uid
        var userDatabase: DatabaseReference = FirebaseDatabase.getInstance().getReference("Personal_Users")
        userDatabase.child(uid).child(receiverId).setValue(receiverId)
            .addOnSuccessListener {
                Log.d("ChatActivity","Personal user successfully added")
            }
            .addOnFailureListener {
                Log.d("ChatActivity", it.toString())
            }
    }



    private fun sendMessageToDatabase(senderId: String, receiverId: String, message: String, time: String) {

        var newId: String = if(receiverId > senderId){
            "$senderId $receiverId"
        } else{
            "$receiverId $senderId"
        }
        var reference: DatabaseReference = FirebaseDatabase.getInstance().reference
        var hashMap: HashMap<String, String> = HashMap()
        hashMap["senderId"] = senderId
        hashMap["receiverId"] = receiverId
        hashMap["message"] = message
        hashMap["time"] = time

        reference.child("Chat/$newId").push().setValue(hashMap)
    }

    private fun readMessage(senderId: String, receiverId: String) {
        var newId: String = if(receiverId > senderId){
            "$senderId $receiverId"
        } else{
            "$receiverId $senderId"
        }

        val ref = FirebaseDatabase.getInstance().getReference("Chat/$newId")
        chatArrayList.clear()
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.forEach {
                        val chat = it.getValue(Chat::class.java)
                        if (chat!!.senderId == senderId && chat.receiverId == receiverId ||
                            chat.senderId == receiverId && chat.receiverId == senderId
                        ) {
                            chatArrayList.add(chat)
                        }
                    }
                }

                val chatAdapter = ChatAdapter(chatArrayList, this@ChatActivity)
                newChatRecyclerView.adapter = chatAdapter
                newChatRecyclerView.scrollToPosition(chatArrayList.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
        )
    }
}