package com.example.licenta.adapter.exercise

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.licenta.R
import com.example.licenta.model.exercise.Exercise
import com.example.licenta.model.exercise.PersonalRecord

class AddExerciseAdapter(
    private val context: Context,
    private val onExerciseItemClickListener: OnExerciseItemClickListener,
    private val exercisesList: MutableList<Exercise>,
    private val recordsList: MutableList<PersonalRecord>
) : RecyclerView.Adapter<AddExerciseAdapter.ExercisesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExercisesViewHolder {
        return ExercisesViewHolder(
            LayoutInflater
                .from(context)
                .inflate(
                    R.layout.item_holder_exercise,
                    parent,
                    false
                ),
            onExerciseItemClickListener
        )
    }

    override fun onBindViewHolder(holder: ExercisesViewHolder, position: Int) {
        holder.update(position)
    }

    override fun getItemCount(): Int {
        return exercisesList.size
    }


    inner class ExercisesViewHolder(
        view: View,
        private val onExerciseItemClickListener: OnExerciseItemClickListener
    ) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private var nameTV: TextView = view.findViewById(R.id.item_exercise_name_tv)
        private var recordTV: TextView = view.findViewById(R.id.item_exercise_record_tv)

        init {
            view.setOnClickListener(this)
        }

        fun update(position: Int) {
            val record = recordsList
                .find { personalRecord ->
                    personalRecord.exerciseId == exercisesList[position].id
                }
                ?.record ?: 0.0
            nameTV.text = exercisesList[position].name
            recordTV.text = record.toString()
        }

        override fun onClick(view: View?) {
            val id = exercisesList[absoluteAdapterPosition].id
            onExerciseItemClickListener.onExerciseItemClick(id,exercisesList[absoluteAdapterPosition].name)
        }
    }

    interface OnExerciseItemClickListener {
        fun onExerciseItemClick(exerciseId: String,exerciseName:String)
    }
}