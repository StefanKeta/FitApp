package com.example.licenta.firebase.db

import com.example.licenta.data.LoggedUserData
import com.example.licenta.model.exercise.WeightExerciseRecord
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.RuntimeException

object WeightExerciseRecordDB {
    private val db by lazy {
        FirebaseFirestore.getInstance()
    }

    private val collectionRef = db
        .collection(CollectionsName.WEIGHT_EXERCISE_RECORDS)

    fun exerciseAdapterOptions(date: String): FirestoreRecyclerOptions<WeightExerciseRecord> {
        val query = collectionRef
            .whereEqualTo(WeightExerciseRecord.USER_ID, LoggedUserData.getLoggedUser().uuid)
            .whereEqualTo(WeightExerciseRecord.DATE, date)

        return FirestoreRecyclerOptions
            .Builder<WeightExerciseRecord>()
            .setQuery(query, WeightExerciseRecord::class.java)
            .build()
    }

    fun saveRecord(record: WeightExerciseRecord, callback: (Boolean) -> Unit) {
        collectionRef
            .add(record)
            .addOnCompleteListener { ref ->
                if (ref.isSuccessful) {
                    callback(true)
                } else {
                    callback(false)
                    throw RuntimeException("Oops!Something went wrong")
                }
            }
    }

    fun removeRecord(recordId: String, callback: (Boolean) -> Unit) {
        collectionRef
            .document(recordId)
            .delete()
            .addOnCompleteListener { snapshot ->
                if (snapshot.isSuccessful) {
                    callback(true)
                } else {
                    callback(false)
                }
            }
    }

    fun getMaximumWeightLifted(exerciseId: String, callback: (Boolean, Double) -> Unit) {
        collectionRef
            .whereEqualTo(WeightExerciseRecord.EXERCISE_ID, exerciseId)
            .whereEqualTo(WeightExerciseRecord.USER_ID, LoggedUserData.getLoggedUser().uuid)
            .get()
            .addOnCompleteListener { snapshot ->
                if (snapshot.isSuccessful) {
                    if (snapshot.result.isEmpty)
                        callback(false, 0.0)
                    else {
                        var recordWeight = 0.0
                        val documents = snapshot.result.documents
                        for (document in documents) {
                            val exerciseRecord = document.toObject(WeightExerciseRecord::class.java)
                            if (exerciseRecord != null) {
                                if (exerciseRecord.weight > recordWeight)
                                    recordWeight = exerciseRecord.weight
                            }
                        }
                        callback(true, recordWeight)
                    }
                } else {
                    callback(false, 0.0)
                }
            }
    }

    fun updateRecord(
        id: String,
        sets: Int,
        reps: Int,
        weight: Double,
        callback: (Boolean) -> Unit
    ) {
        collectionRef
            .document(id)
            .update(
                WeightExerciseRecord.SETS, sets,
                WeightExerciseRecord.REPS, reps,
                WeightExerciseRecord.WEIGHT, weight,
            )
            .addOnCompleteListener { snapshot ->
                if (snapshot.isSuccessful)
                    callback(true)
                else
                    callback(false)
            }
    }
}