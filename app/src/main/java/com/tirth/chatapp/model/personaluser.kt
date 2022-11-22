package com.tirth.chatapp.model

data class PersonalUser(val senderId: String, val receiverId: String){
    constructor(): this("", "")
}