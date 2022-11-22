package com.tirth.chatapp.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.tirth.chatapp.R
import com.tirth.chatapp.model.Chat
import com.tirth.chatapp.model.User


class ChatAdapter(private val chatList: ArrayList<Chat>, private val context : Context)
    : RecyclerView.Adapter<ChatAdapter.MyViewHolder>()
{
    private var message_type_left = 0
    private var message_type_right = 1
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var reference: DatabaseReference


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return if(viewType == message_type_right) {
            val itemView = LayoutInflater.from(parent.context).inflate(
                R.layout.right_side_msg,
                parent, false)
            MyViewHolder(itemView)
        } else {
            val itemView = LayoutInflater.from(parent.context).inflate(
                R.layout.left_side_msg,
                parent, false)
            MyViewHolder(itemView)
        }

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentChat = chatList[position]
        holder.messageTextView.text = currentChat.message
        val senderId = currentChat.senderId

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.forEach {
                        val user = it.getValue(User::class.java)
                        if (user!!.uid == senderId) {
                            if(senderId == firebaseUser.uid)
                            {
                                Glide.with(holder.itemView).load(user.profileImageURL).into(
                                    holder.itemView.findViewById(
                                        R.id.imageView_Sender_Pic
                                    )
                                )
                            }
                            else{
                                Glide.with(holder.itemView).load(user.profileImageURL).into(
                                    holder.itemView.findViewById(
                                        R.id.imageView_Receiver_Pic
                                    )
                                )
                            }
                        }
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ChatActivity", error.toString())
            }
        }
        )
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val messageTextView : TextView = itemView.findViewById(R.id.textView_Msg)
    }

    override fun getItemViewType(position: Int): Int {
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        return if(chatList[position].senderId == firebaseUser.uid) {
            message_type_right
        } else{
            message_type_left
        }
    }
}


