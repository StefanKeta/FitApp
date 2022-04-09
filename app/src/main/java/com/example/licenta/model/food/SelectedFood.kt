package com.example.licenta.model.food

import com.example.licenta.util.Date
import com.google.firebase.firestore.DocumentId

data class SelectedFood(
    @DocumentId
    val id: String = "",
    val foodId: String = "",
    val userId: String = "",
    val mealId: String = "",
    val quantity: Double = 0.0,
    val unit: FoodMeasureUnitEnum = FoodMeasureUnitEnum.GRAM,
    val dateSelected: String = Date.getCurrentDate()
) {
    companion object {
        const val ID = "id"
        const val FOOD_ID = "foodId"
        const val USER_ID = "userId"
        const val MEAL_ID = "mealId"
        const val QUANTITY = "quantity"
        const val UNIT = "unit"
        const val DATE_SELECTED = "dateSelected"
    }
}
