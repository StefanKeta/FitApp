package com.example.licenta.firebase.db

import com.example.licenta.model.exercise.Exercise
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.RuntimeException

object ExercisesDB {
    private val db by lazy {
        FirebaseFirestore.getInstance()
    }

    fun addExercise(exercise: Exercise, callback: (Boolean) -> Unit) {
        db
            .collection(CollectionsName.EXERCISES)
            .add(exercise)
            .addOnCompleteListener { ref ->
                if (ref.isSuccessful)
                    callback(true)
                else
                    callback(false)
            }
    }

    fun getExerciseById(id: String, callback: (Exercise) -> Unit) {
        db
            .collection(CollectionsName.EXERCISES)
            .document(id)
            .get()
            .addOnCompleteListener { snapshot ->
                if (snapshot.isSuccessful) {
                    snapshot.result.toObject(Exercise::class.java)
                        .also {
                            if (it != null)
                                callback(it)
                            else
                                throw RuntimeException("Could not parse the document as Exercise object")
                        }
                }
            }
    }

    fun getAllExercises(callback: (MutableList<Exercise>) -> Unit) {
        db
            .collection(CollectionsName.EXERCISES)
            .get()
            .addOnCompleteListener { snapshot ->
                if (snapshot.isSuccessful) {
                    val documents = snapshot.result.documents
                    val exerciseList: MutableList<Exercise> = ArrayList()
                    for (document in documents)
                        document.toObject(Exercise::class.java)?.let { exerciseList.add(it) }
                    callback(exerciseList)
                } else {
                    throw RuntimeException("Oops! Something went wrong")
                }
            }
    }

    fun saveAnExerciseToDB(exercise: Exercise) {
        db
            .collection(CollectionsName.EXERCISES)
            .add(exercise)
            .addOnFailureListener { it ->
                it.printStackTrace()
            }
    }

    fun getExercisesByGroup(group: String, callback: (MutableList<Exercise>) -> Unit) {
        db
            .collection(CollectionsName.EXERCISES)
            .whereEqualTo(Exercise.GROUP, group)
            .get()
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    val documents = result.result.documents
                    val exercises: MutableList<Exercise> = ArrayList()
                    for (document in documents) {
                        exercises.add(document.toObject(Exercise::class.java)!!)
                    }
                    callback(exercises)
                } else
                    throw RuntimeException("Oops!Something went wrong")
            }
    }
}