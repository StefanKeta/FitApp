package com.example.licenta.math

import com.example.licenta.data.LoggedUserData
import com.example.licenta.model.user.Gender
import com.example.licenta.util.Date
import com.example.licenta.util.PersonalWeightPreference

object CalorieCalculator {

    fun calculateCalories(
        preference: PersonalWeightPreference,
        activity: Double
    ): Int {
        val user = LoggedUserData.getLoggedUser()
        val userAge = Date.parseAge(user.dob)
        return when (preference) {
            PersonalWeightPreference.FAT_LOSS -> calculateForFatLoss(
                user.gender,
                user.height,
                user.weight,
                userAge,
                activity
            )
            PersonalWeightPreference.MAINTAIN -> calculateForMaintaining(
                user.gender,
                user.height,
                user.weight,
                userAge,
                activity
            )
            else -> calculateForMuscleGain(
                user.gender,
                user.height,
                user.weight,
                userAge,
                activity
            )
        }
    }

    private fun calculateForMaintaining(
        gender: Gender,
        height: Int,
        weight: Int,
        age: Int,
        activity: Double
    ): Int {
        val genericBMR =
            10.00 * weight.toDouble() + 6.25 * height.toDouble() - 5.00 * age.toDouble()
        return if (gender == Gender.MALE) {
            ((genericBMR + 5.00) * activity).toInt()
        } else {
            ((genericBMR - 161.00) * activity).toInt()
        }
    }

    private fun calculateForFatLoss(
        gender: Gender,
        height: Int,
        weight: Int,
        age: Int,
        activity: Double
    ): Int {
        val caloriesForMaintaining = calculateForMaintaining(gender, height, weight, age, activity)
        return when {
            caloriesForMaintaining < 2200 -> caloriesForMaintaining - (caloriesForMaintaining.toDouble() * 0.15).toInt()
            caloriesForMaintaining in 2000..3500 -> caloriesForMaintaining - (caloriesForMaintaining.toDouble() * 0.175).toInt()
            else -> caloriesForMaintaining - (caloriesForMaintaining.toDouble() * 0.2).toInt()
        }
    }

    private fun calculateForMuscleGain(
        gender: Gender,
        height: Int,
        weight: Int,
        age: Int,
        activity: Double
    ): Int {
        val caloriesForMaintaining = calculateForMaintaining(gender, height, weight, age, activity)
        return when {
            caloriesForMaintaining < 2200 -> caloriesForMaintaining + (caloriesForMaintaining.toDouble() * 0.2).toInt()
            caloriesForMaintaining in 2000..3500 -> caloriesForMaintaining + (caloriesForMaintaining.toDouble() * 0.175).toInt()
            else -> caloriesForMaintaining + (caloriesForMaintaining.toDouble() * 0.15).toInt()
        }
    }
}