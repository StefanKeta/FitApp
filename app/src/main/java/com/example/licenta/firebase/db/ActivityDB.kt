package com.example.licenta.firebase.db

import com.example.licenta.model.activity.DailyUserActivity
import com.google.firebase.firestore.FirebaseFirestore

object ActivityDB {
    private val db by lazy {
        FirebaseFirestore.getInstance()
    }

    fun saveActivityForTheDay(dailyUserActivity: DailyUserActivity, callback: (Boolean) -> Unit){
        db
            .collection(CollectionsName.DAILY_USER_ACTIVITY)
            .add(dailyUserActivity)
            .addOnCompleteListener{ task ->
                if(task.isSuccessful)
                    callback(true)
                else{
                    callback(false)
                }
            }
    }
}