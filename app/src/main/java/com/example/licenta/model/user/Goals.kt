package com.example.licenta.model.user

import com.google.firebase.firestore.DocumentId
import java.util.*


data class Goals(
    @DocumentId
    val goalsID: String = UUID.randomUUID().toString(),
    val userID: String = "",
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0,
    val steps: Int = 10000
) {
    companion object {
        const val GOALS_ID = "goalsID"
        const val USER_ID = "userID"
        const val CALORIES = "calories"
        const val PROTEIN = "protein"
        const val CARBS = "carbs"
        const val FAT = "fat"
        const val STEPS = "steps"
    }
}