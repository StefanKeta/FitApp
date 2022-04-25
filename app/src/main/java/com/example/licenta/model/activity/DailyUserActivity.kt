package com.example.licenta.model.activity

import com.google.firebase.firestore.DocumentId
import java.util.*

data class DailyUserActivity(
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val date: String = "",
    val steps: Int = 0,
    val distance: Double = 0.0,
    val averageSpeed: Double = 0.0,
    val calories: Int = 0,
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
