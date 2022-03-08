package com.example.licenta.activity.diary.exercise

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.example.licenta.R
import com.example.licenta.firebase.db.ExercisesDB
import com.example.licenta.model.exercise.Exercise
import com.example.licenta.util.IntentConstants
import com.example.licenta.util.Util
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.*

class AddCustomExerciseActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mainLL: LinearLayout
    private lateinit var groupTIL: TextInputLayout
    private lateinit var groupET: TextInputEditText
    private lateinit var nameTIL: TextInputLayout
    private lateinit var nameET: TextInputEditText
    private lateinit var cancelBtn: Button
    private lateinit var saveBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_custom_exercise)
        initComponents()
    }

    private fun initComponents() {
        mainLL = findViewById(R.id.activity_add_custom_exercise_main_ll)
        groupTIL = findViewById(R.id.activity_add_custom_exercise_group_til)
        groupET = findViewById(R.id.activity_add_custom_exercise_group_et)
        nameTIL = findViewById(R.id.activity_add_custom_exercise_name_til)
        nameET = findViewById(R.id.activity_add_custom_exercise_name_et)
        cancelBtn = findViewById(R.id.activity_add_custom_exercise_cancel_btn)
        saveBtn = findViewById(R.id.activity_add_custom_exercise_save_btn)
        mainLL.setOnClickListener(this)
        cancelBtn.setOnClickListener(this)
        saveBtn.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.activity_add_custom_exercise_save_btn -> addExercise()
            R.id.activity_add_custom_exercise_cancel_btn -> cancelAdding()
            R.id.activity_add_custom_exercise_main_ll -> Util.hideKeyboard(this)
        }
    }

    private fun addExercise() {
        if (groupET.text.isNullOrEmpty()) {
            groupTIL.error = "Please enter muscle group"
        } else if (nameET.text.isNullOrEmpty()) {
            nameET.error = "Please enter exercise name"
        } else {
            val exercise = Exercise(
                UUID.randomUUID().toString(),
                groupET.text.toString().trim().lowercase()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                nameET.text.toString().trim().lowercase()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            )
            ExercisesDB.addExercise(exercise, ::addExerciseCallback)
        }
    }

    private fun addExerciseCallback(isAdded: Boolean) {
        val intent = Intent(this@AddCustomExerciseActivity, AddExerciseActivity::class.java)
        intent.putExtra(IntentConstants.IS_EXERCISE_ADDED, isAdded)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun cancelAdding() {
        addExerciseCallback(false)
    }
}