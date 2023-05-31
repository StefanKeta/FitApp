package com.example.licenta.firebase.db

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.licenta.activity.auth.LoginActivity
import com.example.licenta.data.LoggedUserData
import com.example.licenta.model.user.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object UsersDB {
    private val db by lazy {
        FirebaseFirestore.getInstance()
    }

    private val collection = db
        .collection(CollectionsName.USERS)

    fun addUser(user: User, ctx: Context) {
        collection
            .add(user)
            .addOnSuccessListener {
                ctx.startActivity(Intent(ctx, LoginActivity::class.java))
            }
            .addOnFailureListener {
                Toast.makeText(ctx, it.toString(), Toast.LENGTH_SHORT)
                    .show()
            }
    }

    fun getUser(id: String, callback: (User?) -> Unit) {
        collection
            .get()
            .addOnCompleteListener{query ->
                query.result.documents.forEach{ doc -> Log.d("[Users]", "getUser: ${doc.data}")}
                collection
                    .whereEqualTo(User.USER_UUID, id)
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            if (it.result.documents[0] != null)
                                callback(it.result!!.documents[0].toObject(User::class.java))
                        } else
                            callback(null)
                    }
            }
    }

    fun updateWeight(newWeight: Int, callback: (User?) -> Unit) {
        collection
            .whereEqualTo(User.USER_UUID, LoggedUserData.getLoggedUser().uuid)
            .get()
            .addOnCompleteListener { snapshots ->
                val docs = snapshots.result.documents
                val userDoc = docs[0]
                if (userDoc != null) {
                    collection
                        .document(userDoc.id)
                        .update(User.WEIGHT, newWeight)
                        .addOnCompleteListener { result ->
                            if (result.isSuccessful) {
                                getUser(LoggedUserData.getLoggedUser().uuid) {
                                    callback(it)
                                }
                            } else
                                callback(null)
                        }
                } else
                    callback(null)
            }
    }
}