package com.example.licenta.model.exercise

import com.example.licenta.util.Date
import com.google.firebase.firestore.DocumentId

data class CardioExerciseRecord(
    @DocumentId
    val id: String,
    val exerciseId: String,
    val distance: Double,
    val time: Long,
    val date: String = Date.getCurrentDate()
) {
    companion object {
        const val ID = "id"
        const val EXERCISE_ID = "exerciseId"
        const val DISTANCE = "distance"
        const val TIME = "time"
        const val DATE = "date"
    }
}
