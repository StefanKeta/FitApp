package com.example.licenta.data

import com.example.licenta.model.user.User
import java.util.*

object LoggedUserData {
    private lateinit var user : User

    fun setLoggedUser(user:User){
        this.user = user
    }

    fun getLoggedUser(): User{
        return user
    }
}