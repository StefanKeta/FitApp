package com.example.licenta.model.user

data class User(
    val uuid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val dob: Long = 0,
    val gender: Gender = Gender.MALE,
    val height: Int = 0,
    val weight: Int = 0,
) {
    companion object {
        const val USER_UUID = "uuid"
        const val FIRST_NAME = "firstName"
        const val LAST_NAME = "lastName"
        const val DATE_OF_BIRTH = "dob"
        const val GENDER = "gender"
        const val HEIGHT = "height"
        const val WEIGHT = "weight"
        const val EMAIL = "email"
    }
}