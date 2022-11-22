package com.tirth.chatapp.model

data class User(val uid: String, val username: String, val profileImageURL: String){
    constructor(): this("", "", "")
}