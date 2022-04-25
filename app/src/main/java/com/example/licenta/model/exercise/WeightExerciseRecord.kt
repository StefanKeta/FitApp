package com.example.licenta.model.exercise

import com.example.licenta.util.Date
import com.google.firebase.firestore.DocumentId

data class WeightExerciseRecord(
    @DocumentId
    val id: String = "",
    val exerciseId: String = "",
    val userId: String = "",
    val sets: Int = 0,
    val reps: Int = 0,
    val weight: Double = 0.0,
    val date: String = Date.getCurrentDate()
) {
    companion object {
        const val ID = "id"
        const val EXERCISE_ID = "exerciseId"
        const val USER_ID = "userId"
        const val SETS = "sets"
        const val REPS = "reps"
        const val WEIGHT = "weight"
        const val DATE = "date"
    }
}
