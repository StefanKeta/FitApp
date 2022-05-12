package com.example.licenta.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.licenta.R
import com.google.firebase.storage.FirebaseStorage
import java.lang.RuntimeException

object LoggedUserProfilePhoto {
    private var profilePhoto: Bitmap? = null

    fun getProfilePhoto(): Bitmap? {
        return profilePhoto
    }

    fun resetProfilePhoto(){
        profilePhoto = null
    }

    fun saveProfilePhoto(uri: Uri,loggedUserId: String,callback: (Boolean) -> Unit){
        FirebaseStorage
            .getInstance()
            .reference
            .child("profile-pics/JPEG_$loggedUserId.jpg")
            .putFile(uri)
            .addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    callback(true)
                }else{
                    Log.d("uploadingError", "saveProfilePhoto: ${task.result}")
                    callback(false)
                }
            }
    }

    fun setProfilePhoto(ctx:Context,loggedUserId: String, callback: ()->Unit) {
        FirebaseStorage.getInstance()
            .reference
            .child("profile-pics/JPEG_$loggedUserId.jpg")
            .getBytes(Long.MAX_VALUE)
            .addOnSuccessListener { photoBytesArray ->
                Log.d("profilePhoto", "setProfilePhoto: $photoBytesArray")
                profilePhoto =
                    BitmapFactory.decodeByteArray(photoBytesArray, 0, photoBytesArray.size)
                callback()
            }
            .addOnFailureListener{ exception ->
                exception.printStackTrace()
                profilePhoto = BitmapFactory.decodeResource(ctx.resources,R.drawable.icon_user)
                callback()
            }
    }
}