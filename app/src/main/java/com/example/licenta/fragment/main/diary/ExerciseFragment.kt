package com.example.licenta.fragment.main.diary

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.licenta.R
import com.example.licenta.adapter.exercise.ExerciseAdapter
import com.example.licenta.contract.AddExerciseContract
import com.example.licenta.firebase.db.PersonalRecordsDB
import com.example.licenta.firebase.db.WeightExerciseRecordDB
import com.example.licenta.fragment.main.OnDateChangedListener
import com.example.licenta.util.Date
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ExerciseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ExerciseFragment(private var date: String = Date.setCurrentDay()) : Fragment(),
    View.OnClickListener, OnDateChangedListener, ExerciseAdapter.OnExerciseLongClickListener,
    ExerciseAdapter.OnExerciseClickListener {
    private lateinit var addExerciseBtn: Button
    private lateinit var exercisesRV: RecyclerView
    private lateinit var adapter: ExerciseAdapter
    private lateinit var addExerciseContract: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_exercise, container, false)
        initComponents(view)
        return view
    }

    private fun initComponents(view: View) {
        addExerciseBtn = view.findViewById(R.id.fragment_diary_exercise_add_btn)
        addExerciseBtn.setOnClickListener(this)
        exercisesRV = view.findViewById(R.id.fragment_diary_exercise_rv)
        exercisesRV.layoutManager = LinearLayoutManager(context!!)
        exercisesRV.hasFixedSize()
        exercisesRV.itemAnimator = null
        setUpAdapter()
        addExerciseContract = registerForActivityResult(
            AddExerciseContract(),
            ::handleResponseFromAddExerciseActivity
        )
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.fragment_diary_exercise_add_btn -> addExerciseContract.launch(date)
        }
    }

    override fun changeDate(date: String) {
        this.date = date
        refreshAdapter()
    }

    override fun onExerciseLongClick(recordId: String, exerciseId: String): Boolean {
        AlertDialog
            .Builder(context!!)
            .setIcon(R.drawable.ic_baseline_delete_24)
            .setTitle("Are you sure you want to remove the exercise?")
            .setPositiveButton("Yes") { dialog, _ ->
                handleExerciseRecordRemoval(recordId, exerciseId, dialog)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
        return true
    }

    override fun onExerciseClick(recordId: String, exerciseId: String) {
        setUpEditDialogAndHandleEditing(recordId, exerciseId)
    }

    private fun handleResponseFromAddExerciseActivity(isAdded: Boolean) {
        if (isAdded)
            refreshAdapter()
    }

    private fun refreshAdapter() {
        val options = WeightExerciseRecordDB.exerciseAdapterOptions(date)
        adapter.updateOptions(options)
        adapter.notifyDataSetChanged()
    }

    private fun setUpAdapter() {
        val options = WeightExerciseRecordDB.exerciseAdapterOptions(date)
        adapter = ExerciseAdapter(context!!, options, this, this)
        exercisesRV.adapter = adapter
    }

    private fun handleExerciseRecordRemoval(
        recordId: String,
        exerciseId: String,
        dialog: DialogInterface
    ) {
        WeightExerciseRecordDB.removeRecord(recordId) { isRemoved ->
            if (isRemoved) {
                PersonalRecordsDB.removeIfPersonalRecord(exerciseId) { isPersonalRecordRemoved ->
                    Log.d("onRemove", "handleExerciseRecordRemoval: $exerciseId")
                    if (isPersonalRecordRemoved)
                        updatePersonalRecord(exerciseId)
                    dialog.dismiss()
                }
            } else {
                Toast.makeText(context!!, "Could not remove this record", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
    }

    private fun updatePersonalRecord(exerciseId: String) {
        WeightExerciseRecordDB
            .getMaximumWeightLifted(exerciseId) { isFound, newRecordWeight ->
                if (isFound) {
                    PersonalRecordsDB.addRecord(exerciseId, newRecordWeight)
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun setUpEditDialogAndHandleEditing(recordId: String, exerciseId: String) {
        val view = LayoutInflater
            .from(context!!)
            .inflate(R.layout.dialog_edit_exercise_record, null, false)
        var setsTIL: TextInputLayout = view.findViewById(R.id.dialog_edit_exercise_sets_tila)
        var setsET: TextInputEditText = view.findViewById(R.id.dialog_edit_exercise_sets_et)
        var repsTIL: TextInputLayout = view.findViewById(R.id.dialog_edit_exercise_reps_tila)
        var repsET: TextInputEditText = view.findViewById(R.id.dialog_edit_exercise_reps_et)
        var weightTIL: TextInputLayout = view.findViewById(R.id.dialog_edit_exercise_weight_tila)
        var weightET: TextInputEditText = view.findViewById(R.id.dialog_edit_exercise_weight_et)
        var cancelBtn: Button = view.findViewById(R.id.dialog_edit_exercise_cancel_btn)
        var editBtn: Button = view.findViewById(R.id.dialog_edit_exercise_edit_btn)

        val dialog = AlertDialog
            .Builder(context!!)
            .setView(view)
            .setIcon(R.drawable.ic_baseline_edit_24)
            .setTitle("Edit your exercise record")
            .show()

        cancelBtn.setOnClickListener { _ -> dialog.dismiss() }
        editBtn.setOnClickListener { _ ->
            if (setsET.text.isNullOrEmpty())
                setsTIL.error = "Please enter sets"
            else if (repsET.text.isNullOrEmpty())
                repsTIL.error = "Please enter reps"
            else if (weightET.text.isNullOrEmpty())
                weightTIL.error = "Please enter weight"
            else {
                WeightExerciseRecordDB.updateRecord(
                    recordId,
                    setsET.text.toString().trim().toInt(),
                    repsET.text.toString().trim().toInt(),
                    weightET.text.toString().trim().toDouble(),
                ) { isUpdated ->
                    if (isUpdated) {
                        PersonalRecordsDB.checkAndUpdateRecordIfNeeded(
                            exerciseId,
                            weightET.text.toString().trim().toDouble()
                        )
                        adapter.notifyDataSetChanged()
                    }
                    dialog.dismiss()
                }
            }
        }
    }


    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.stopListening()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ExerciseFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ExerciseFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}