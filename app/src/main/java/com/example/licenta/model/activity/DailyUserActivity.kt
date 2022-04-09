package com.example.licenta.model.activity

import com.google.firebase.firestore.DocumentId

data class DailyUserActivity(
    @DocumentId
    val id: String,
    val userId: String,
    val date: String,
    val steps: Int,
    val distance: Double,
    val averageSpeed: Double,
    val calories: Int,
) {
    companion object {
        const val ACTIVITY_ID = "id"
        const val USER_ID = "userId"
        const val DATE = "date"
        const val STEPS = "steps"
        const val CALORIES = "calories"
        const val DISTANCE = "distance"
    }
}
