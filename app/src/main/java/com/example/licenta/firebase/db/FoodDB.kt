package com.example.licenta.firebase.db

import com.example.licenta.model.food.Food
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.lang.RuntimeException
import java.util.*
import kotlin.collections.ArrayList

object FoodDB {
    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    fun getInitialFoodList(limit: Long, callback: (List<Food>) -> Unit) {
        db
            .collection(CollectionsName.FOOD)
            .limit(limit)
            .get()
            .addOnCompleteListener { snapshot ->
                if (snapshot.isSuccessful) {
                    val results = ArrayList<Food>()
                    val documents = snapshot.result.documents
                    for (document in documents) {
                        val food = document.toObject(Food::class.java)
                        if (food != null)
                            results.add(food)
                    }
                } else {
                    throw RuntimeException("Oops!Something went wrong")
                }
            }
    }

    fun getFoodById(id: String, callback: (Food) -> Unit) {
        db
            .collection(CollectionsName.FOOD)
            .document(id)
            .get()
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    val document = result.result
                    val food = document.toObject(Food::class.java)
                    if (food != null) {
                        callback(food)
                    } else
                        throw RuntimeException("Could not parse document to a Food object!")
                } else
                    throw RuntimeException("Searching failed!")
            }
    }

    fun initialAddFoodAdapterListOptions(limit: Long = 20): FirestoreRecyclerOptions<Food> {
        val query = db
            .collection(CollectionsName.FOOD)
            .orderBy(Food.NAME, Query.Direction.ASCENDING)
            .limit(limit)

        return FirestoreRecyclerOptions.Builder<Food>()
            .setQuery(query, Food::class.java)
            .build()
    }


    fun searchForFoodByNamePrefix(nameToMatch: String): FirestoreRecyclerOptions<Food> {
        val query = db
            .collection(CollectionsName.FOOD)
            .whereGreaterThanOrEqualTo(Food.NAME,
                nameToMatch.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
            .whereLessThanOrEqualTo(Food.NAME,
                "$nameToMatch~".replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
            .orderBy(Food.NAME, Query.Direction.ASCENDING)

        return FirestoreRecyclerOptions.Builder<Food>()
            .setQuery(query, Food::class.java)
            .build()
    }


    fun foodExists(barcodeRawValue: String, existsCallback: (Boolean, String) -> Unit) {
        db
            .collection(CollectionsName.FOOD)
            .whereEqualTo(Food.BARCODE, barcodeRawValue)
            .get()
            .addOnCompleteListener { snapshotResult ->
                if (snapshotResult.isSuccessful) {
                    val documents = snapshotResult.result.documents
                    if (documents.isEmpty()) {
                        existsCallback(false, "")
                    } else {
                        val foodID = documents[0]
                            .toObject(Food::class.java)
                            ?.id ?: ""
                        existsCallback(true, foodID)
                    }
                } else {
                    throw RuntimeException("Oops! Something went wrong!")
                }
            }
    }

    fun saveFood(food: Food, isSaved: (Boolean) -> Unit) {
        if (food.barcode.isEmpty()) {
            db
                .collection(CollectionsName.FOOD)
                .add(food)
                .addOnCompleteListener { documentRef ->
                    if (documentRef.isSuccessful) {
                        isSaved(true)
                    } else {
                        isSaved(false)
                    }
                }
        } else {
            db.collection(CollectionsName.FOOD)
                .whereEqualTo(Food.BARCODE, food.barcode)
                .get()
                .addOnCompleteListener { snapshot ->
                    if (snapshot.isSuccessful) {
                        val documents = snapshot.result.documents
                        if (documents.isEmpty()) {
                            db
                                .collection(CollectionsName.FOOD)
                                .add(food)
                                .addOnCompleteListener { documentRef ->
                                    if (documentRef.isSuccessful) {
                                        isSaved(true)
                                    } else {
                                        isSaved(false)
                                    }
                                }
                        } else {
                            isSaved(false)
                        }
                    } else {
                        throw RuntimeException("Failed to retrieve documents!")
                    }
                }
        }

    }

}