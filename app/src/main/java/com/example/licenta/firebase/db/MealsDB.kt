package com.example.licenta.firebase.db

import com.example.licenta.data.LoggedUserData
import com.example.licenta.model.food.Meal
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.lang.RuntimeException

object MealsDB {
    private val db by lazy {
        FirebaseFirestore.getInstance()
    }

    fun getMealById(mealId: String, callback: (Meal?) -> Unit) {
        db
            .collection(CollectionsName.MEALS)
            .document(mealId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val meal = task.result.toObject(Meal::class.java)
                    if (meal != null)
                        callback(meal)
                    else
                        throw RuntimeException("Could not parse the meal!")
                } else {
                    callback(null)
                }
            }
    }

    fun getMealsFromDate(date: String, callback: (List<Meal>) -> Unit) {
        db
            .collection(CollectionsName.MEALS)
            .whereEqualTo(Meal.USER_ID, LoggedUserData.getLoggedUser().uuid)
            .whereEqualTo(Meal.DATE, date)
            .limit(3)
            .get()
            .addOnCompleteListener { snapshot ->
                if (snapshot.isSuccessful) {
                    val documents = snapshot.result.documents
                    val meals = documents.map { documentSnapshot ->
                        documentSnapshot.toObject(Meal::class.java)
                            .let { meal -> meal ?: throw RuntimeException("Could not parse meal!") }
                    }
                    callback(meals)
                } else
                    throw RuntimeException("Could not retrieve meals from db")
            }
    }

    fun saveEmptyMeals(meals: List<Meal>, callback: (Unit) -> Unit) {
        var mealsSaved = 0
        meals.forEach { meal ->
            db
                .collection(CollectionsName.MEALS)
                .add(meal)
                .addOnSuccessListener { _ ->
                    mealsSaved += 1
                    if (mealsSaved == 3)
                        callback(Unit)
                }
                .addOnFailureListener { e ->
                    throw RuntimeException(e.message)
                }
        }
    }

    fun getMealsByDateOptions(date: String): FirestoreRecyclerOptions<Meal> {
        val query = db
            .collection(CollectionsName.MEALS)
            .whereEqualTo(Meal.USER_ID, LoggedUserData.getLoggedUser().uuid)
            .whereEqualTo(Meal.DATE, date)
            .orderBy(Meal.MEAL_NO, Query.Direction.ASCENDING)
            .limit(3)

        return FirestoreRecyclerOptions.Builder<Meal>()
            .setQuery(query, Meal::class.java)
            .build()
    }
}