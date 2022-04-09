package com.example.licenta.model.food

import com.example.licenta.util.Date
import com.google.firebase.firestore.DocumentId

data class Meal(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val mealNumber: Int = 0,
    val date: String = Date.getCurrentDate(),
) {
    companion object {
        const val ID = "id"
        const val USER_ID = "userId"
        const val MEAL_NO = "mealNumber"
        const val DATE = "date"
    }
}
