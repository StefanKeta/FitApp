package com.example.licenta.math

object BMICalculator {
    fun calculateBMI(weight: Int, height: Int): Double {
        return (weight.toDouble() / (height * height).toDouble())
    }
}