package com.example.licenta.math

import com.example.licenta.util.PersonalWeightPreference

object StepsCalculator {
    fun calculateRecommendedSteps(weightPreference: PersonalWeightPreference, age: Int): Int {
        return when (weightPreference) {
            PersonalWeightPreference.FAT_LOSS ->
                if (age > 19) 13_000
                else 12_000
            PersonalWeightPreference.GAIN_MUSCLE ->
                if (age > 19) 9_000 else 8_000
            else -> 10_000
        }
    }
}