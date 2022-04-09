package com.example.licenta.activity.diary.exercise

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.licenta.R
import com.example.licenta.activity.MainActivity
import com.example.licenta.adapter.exercise.AddExerciseAdapter
import com.example.licenta.contract.AddCustomExerciseContract
import com.example.licenta.data.LoggedUserData
import com.example.licenta.firebase.db.ExercisesDB
import com.example.licenta.firebase.db.PersonalRecordsDB
import com.example.licenta.firebase.db.WeightExerciseRecordDB
import com.example.licenta.model.exercise.Exercise
import com.example.licenta.model.exercise.PersonalRecord
import com.example.licenta.model.exercise.WeightExerciseRecord
import com.example.licenta.util.Date
import com.example.licenta.util.IntentConstants
import com.example.licenta.util.Util
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.*
import java.util.stream.Collectors

class AddExerciseActivity : AppCompatActivity(), AdapterView.OnItemClickListener,
    View.OnClickListener, AddExerciseAdapter.OnExerciseItemClickListener {
    private lateinit var mainLL: LinearLayout
    private lateinit var searchTIL: TextInputLayout
    private lateinit var searchET: TextInputEditText
    private lateinit var addCustomBtn: Button
    private lateinit var groupsTIL: TextInputLayout
    private lateinit var groupsACTV: AutoCompleteTextView
    private lateinit var exercisesRV: RecyclerView
    private lateinit var exercisesList: MutableList<Exercise>
    private lateinit var groupsSet: MutableSet<String>
    private lateinit var recordsList: MutableList<PersonalRecord>
    private lateinit var addCustomExerciseContract: ActivityResultLauncher<Unit>
    private lateinit var setsTIL: TextInputLayout
    private lateinit var setsET: TextInputEditText
    private lateinit var repsTIL: TextInputLayout
    private lateinit var repsET: TextInputEditText
    private lateinit var weightTIL: TextInputLayout
    private lateinit var weightET: TextInputEditText
    private lateinit var date: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_exercise)
        initComponents()
    }

    private fun initComponents() {
        date = intent.getStringExtra(WeightExerciseRecord.DATE) ?: Date.getCurrentDate()
        mainLL = findViewById(R.id.activity_exercise_main_ll)
        searchTIL = findViewById(R.id.activity_exercise_search_exercise_til)
        searchET = findViewById(R.id.activity_exercise_search_exercise_et)
        addCustomBtn = findViewById(R.id.activity_exercise_add_custom_btn)
        addCustomBtn.setOnClickListener(this)
        mainLL.setOnClickListener(this)
        exercisesRV = findViewById(R.id.activity_exercise_exercises_rv)
        exercisesRV.layoutManager = LinearLayoutManager(this@AddExerciseActivity)
        exercisesRV.hasFixedSize()
        addCustomExerciseContract = registerForActivityResult(
            AddCustomExerciseContract(),
            ::handleResponseFromAddingCustomExercise
        )
        PersonalRecordsDB.getAllRecords { recordsList = it }
        ExercisesDB.getAllExercises(::initGroups)
    }

    private fun initGroups(exercisesList: MutableList<Exercise>) {
        this.exercisesList = exercisesList
        groupsTIL = findViewById(R.id.activity_exercise_group_til)
        groupsACTV = findViewById(R.id.activity_exercise_group_actv)
        refreshGroups()
        groupsACTV.onItemClickListener = this
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.activity_exercise_add_custom_btn -> launchAddingContract()
            R.id.activity_exercise_main_ll -> Util.hideKeyboard(this)
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        Util.hideKeyboard(this)
        val groupExercises = exercisesList
            .filter { exercise ->
                exercise.group == parent!!.getItemAtPosition(position).toString()
            }
            .toMutableList()
        populateExercises(groupExercises)
    }

    override fun onExerciseItemClick(exerciseId: String) {
        openAddDialog(exerciseId)
    }

    override fun onBackPressed() {
        cancelAdding()
    }

    private fun refreshGroups() {
        groupsSet = exercisesList
            .stream()
            .map { exercise ->
                exercise
                    .group
            }
            .collect(Collectors.toSet())
            .toMutableSet()

        val adapter = ArrayAdapter(
            this@AddExerciseActivity,
            android.R.layout.simple_spinner_dropdown_item,
            groupsSet.toMutableList()
        )
        groupsACTV.setAdapter(adapter)
    }

    private fun populateExercises(exercises: MutableList<Exercise>) {
        val adapter = AddExerciseAdapter(this, this, exercises, recordsList)
        exercisesRV.adapter = adapter
    }

    private fun launchAddingContract() {
        addCustomExerciseContract.launch(Unit)
    }

    private fun handleResponseFromAddingCustomExercise(isAdded: Boolean) {
        if (isAdded) {
            ExercisesDB.getAllExercises { exercises ->
                this.exercisesList = exercises
                refreshGroups()
            }
        }
    }

    private fun cancelAdding() {
        val intent = Intent(this@AddExerciseActivity, MainActivity::class.java)
        intent.putExtra(IntentConstants.IS_EXERCISE_ADDED, false)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun openAddDialog(exerciseId: String) {
        val view = LayoutInflater
            .from(this@AddExerciseActivity)
            .inflate(R.layout.dialog_add_exercise, null, false)
        setsTIL = view.findViewById(R.id.dialog_add_exercise_sets_til)
        setsET = view.findViewById(R.id.dialog_add_exercise_sets_et)
        repsTIL = view.findViewById(R.id.dialog_add_exercise_reps_til)
        repsET = view.findViewById(R.id.dialog_add_exercise_reps_et)
        weightTIL = view.findViewById(R.id.dialog_add_exercise_weight_til)
        weightET = view.findViewById(R.id.dialog_add_exercise_weight_et)
        val dialog = AlertDialog
            .Builder(this@AddExerciseActivity)
            .setView(view)
            .setNegativeButton(
                R.string.button_cancel
            ) { dialog, _ -> dialog!!.dismiss() }
            .setPositiveButton(R.string.activity_register_food_save_btn) { dialog, _ ->
                if (dialogInputsFilled()) {
                    registerExerciseToDB(dialog, exerciseId)
                }
            }
            .create()

        dialog.show()
    }

    private fun dialogInputsFilled(): Boolean {
        return if (setsET.text!!.isNotEmpty() && repsET.text!!.isNotEmpty() && weightET.text!!.isNotEmpty()) true
        else if (setsET.text!!.isEmpty()) {
            setsTIL.error = "Please provide sets"
            false
        } else if (repsET.text!!.isEmpty()) {
            repsTIL.error = "Please provide reps"
            false
        } else {
            weightTIL.error = "Please provide weight"
            false
        }
    }

    private fun registerExerciseToDB(dialog: DialogInterface, exerciseId: String) {
        val weightExerciseRecord = WeightExerciseRecord(
            UUID.randomUUID().toString(),
            exerciseId,
            LoggedUserData.getLoggedUser().uuid,
            setsET.text.toString().trim().toInt(),
            repsET.text.toString().trim().toInt(),
            weightET.text.toString().toDouble(),
            date
        )
        WeightExerciseRecordDB.saveRecord(weightExerciseRecord) { isAdded ->
            dialog.dismiss()
            if (!isAdded) {
                Toast.makeText(
                    this@AddExerciseActivity,
                    "Oops! Could not add exercise record",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val intent = Intent(this@AddExerciseActivity, MainActivity::class.java)
                    .also {
                        it.putExtra(IntentConstants.IS_EXERCISE_ADDED, isAdded)
                        val weight = weightET.text.toString().toDouble()
                        PersonalRecordsDB.checkAndUpdateRecordIfNeeded(exerciseId, weight)
                    }
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

}