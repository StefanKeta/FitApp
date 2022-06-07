package com.example.licenta.firebase

import android.content.Context
import android.widget.Toast
import com.example.licenta.R
import com.example.licenta.firebase.db.UsersDB
import com.example.licenta.model.user.Gender
import com.example.licenta.model.user.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser

object Auth {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun registerUser(context: Context, firstName: String, lastName: String, email: String, dob: Long, gender: Gender, height: Int, weight: Int, password: String) {
        auth
            .createUserWithEmailAndPassword(
                email,
                password
            )
            .addOnSuccessListener {
                val user = User(
                    it.user!!.uid,
                    firstName,
                    lastName,
                    email,
                    dob,
                    gender,
                    height,
                    weight
                )
                UsersDB
                    .addUser(user, context)
            }
            .addOnFailureListener {
                if (it is FirebaseAuthUserCollisionException)
                    Toast.makeText(
                        context,
                        context.getString(R.string.register_activity_auth_exception_email_exists),
                        Toast.LENGTH_SHORT
                    )
                        .show()
            }
    }

    fun loginUser(
        email: String,
        password: String,
        callback: (Boolean) -> Unit
    ) {
        auth.signInWithEmailAndPassword(
            email,
            password
        )
            .addOnCompleteListener { authResult ->
                if (authResult.isSuccessful) {
                    callback(true)
                } else {
                    callback(false)
                }
            }
    }

    fun logUserOut() {
        auth.signOut()
    }

    fun currentUser(): FirebaseUser? {
        return auth.currentUser
    }
}