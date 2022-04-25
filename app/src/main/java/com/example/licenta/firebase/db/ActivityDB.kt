package com.example.licenta.firebase.db

import android.util.Log
import com.example.licenta.model.activity.DailyUserActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.RuntimeException

object ActivityDB {
    private val db by lazy {
        FirebaseFirestore.getInstance()
    }

    fun saveActivityForTheDay(dailyUserActivity: DailyUserActivity, callback: (Boolean) -> Unit) {
        db
            .collection(CollectionsName.DAILY_USER_ACTIVITY)
            .add(dailyUserActivity)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                    callback(true)
                else {
                    callback(false)
                }
            }
    }

    fun loadActivityForTheDay(
        userId: String,
        date: String,
        callback: (DailyUserActivity?) -> Unit
    ) {
        db
            .collection(CollectionsName.DAILY_USER_ACTIVITY)
            .whereEqualTo(DailyUserActivity.DATE, date)
            .whereEqualTo(DailyUserActivity.USER_ID, userId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val docs = task.result.documents
                    if (docs.size == 1) {
                        val activity =
                            docs[0].toObject(DailyUserActivity::class.java)
                        callback(activity)
                    } else if (docs.size > 2)
                        throw RuntimeException("There should be only one record for a specific date")
                    else {
                        callback(null)
                    }
                } else {
                    throw RuntimeException("There activities retrieval failed. Check your connection")
                }
            }
    }
}