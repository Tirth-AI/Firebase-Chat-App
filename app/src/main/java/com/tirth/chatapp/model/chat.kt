package com.tirth.chatapp.model

import java.sql.Timestamp

data class Chat(val senderId: String, val receiverId: String, val message: String, val time: String){
    constructor(): this("", "", "","" )
}