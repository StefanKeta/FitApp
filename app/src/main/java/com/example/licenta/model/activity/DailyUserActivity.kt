package com.example.licenta.model.activity

data class DailyUserActivity(
    val userId:String,
    val date:String,
    val steps:Int,
    val calories:Int,
    val distance:Double
){
    companion object{
        const val USER_ID = "userId"
        const val DATE = "date"
        const val STEPS = "steps"
        const val CALORIES = "calories"
        const val DISTANCE = "distance"
    }
}
