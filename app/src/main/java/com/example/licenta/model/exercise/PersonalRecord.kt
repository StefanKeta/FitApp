package com.example.licenta.model.exercise

import com.google.firebase.firestore.DocumentId

data class PersonalRecord(
    @DocumentId
    val id: String = "",
    val exerciseId: String = "",
    val userId: String = "",
    val record: Double = 0.0
) {
    companion object {
        const val ID = "id"
        const val EXERCISE_ID = "exerciseId"
        const val USER_ID = "userId"
        const val RECORD = "record"
    }
}