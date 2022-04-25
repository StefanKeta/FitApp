package com.example.licenta.firebase.db

import com.example.licenta.data.LoggedUserGoals
import com.example.licenta.model.user.Goals
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.RuntimeException

object GoalsDB {
    private val db by lazy {
        FirebaseFirestore.getInstance()
    }

    fun userHasGoals(id: String, callback: (Boolean) -> Unit) {
        db.collection(CollectionsName.GOALS)
            .whereEqualTo(Goals.USER_ID, id)
            .get()
            .addOnCompleteListener {
                val documents = it.result?.documents
                if (documents != null) {
                    if (documents.size == 1)
                        callback(true)
                    else
                        callback(false)
                } else throw NoSuchElementException("User does not exist!")
            }
            .addOnFailureListener {
                throw RuntimeException("Something went wrong!")
            }
    }

    fun addUserGoals(goals: Goals, callback: (Boolean, Goals?) -> Unit) {
        db.collection(CollectionsName.GOALS)
            .add(goals)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, goals)
                } else
                    callback(false, null)
            }
    }

    fun getUserGoals(userId: String, callback: (Boolean) -> Unit) {
        db.collection(CollectionsName.GOALS)
            .whereEqualTo(Goals.USER_ID, userId)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val results = it.result!!.documents
                    if (results.size == 1) {
                        val goals = results[0].toObject(Goals::class.java)
                        LoggedUserGoals.setGoals(goals!!)
                        callback(true)
                    } else {
                        callback(false)
                    }
                } else {
                    callback(false)
                }
            }
    }

    fun updateGoals(
        id: String,
        macros: Triple<Int, Int, Int>,
        calories: Int,
        steps: Int,
        callback: (Boolean) -> Unit
    ) {
        db.collection(CollectionsName.GOALS)
            .document(id)
            .update(
                Goals.PROTEIN, macros.first,
                Goals.FAT, macros.second,
                Goals.CARBS, macros.third,
                Goals.CALORIES, calories,
                Goals.STEPS, steps
            )
            .addOnCompleteListener {
                if (it.isSuccessful)
                    db.collection(CollectionsName.GOALS)
                        .document(id)
                        .get()
                        .addOnCompleteListener { goals ->
                            if (goals.isSuccessful) {
                                val goal = goals.result.toObject(Goals::class.java)
                                LoggedUserGoals.setGoals(goal)
                                callback(true)
                            } else {
                                callback(false)
                            }
                        } else {
                    callback(false)
                }
            }
    }
}