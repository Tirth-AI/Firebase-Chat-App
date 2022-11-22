package com.tirth.chatapp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tirth.chatapp.R
import com.tirth.chatapp.activity.ChatActivity
import com.tirth.chatapp.model.User


class UserAdapter(private val usersList: ArrayList<User>, private val context: Context) : RecyclerView.Adapter<UserAdapter.MyViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.user_row_new_messages,
            parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int){
        val currentUser = usersList[position]
        holder.usernameTextView.text = currentUser.username
        Glide.with(holder.itemView).load(currentUser.profileImageURL).into(holder.itemView.findViewById(
            R.id.imageView_UserRow_NewMessage
        ))
        holder.layoutUserRow.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("userId",currentUser.uid)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val usernameTextView : TextView = itemView.findViewById(R.id.textView_UserRow_NewMessage)
        val layoutUserRow: LinearLayout = itemView.findViewById(R.id.llayout_UserRow)
    }
}