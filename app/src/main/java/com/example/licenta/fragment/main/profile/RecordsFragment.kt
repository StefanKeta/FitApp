package com.example.licenta.fragment.main.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.licenta.R
import com.example.licenta.adapter.exercise.AddExerciseAdapter
import com.example.licenta.firebase.db.ExercisesDB
import com.example.licenta.firebase.db.PersonalRecordsDB
import com.example.licenta.model.exercise.Exercise
import com.example.licenta.model.exercise.PersonalRecord
import com.google.android.material.textfield.TextInputLayout
import java.util.stream.Collectors

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RecordsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecordsFragment : Fragment(), AdapterView.OnItemClickListener,
    AddExerciseAdapter.OnExerciseItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var groupTIL: TextInputLayout
    private lateinit var groupACTV: AutoCompleteTextView
    private lateinit var exercisesRV: RecyclerView
    private lateinit var usersRecords: MutableList<PersonalRecord>
    private lateinit var exercises: MutableList<Exercise>
    private lateinit var exercisesAdapter: AddExerciseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater
            .inflate(R.layout.fragment_records, container, false)
        groupTIL = view.findViewById(R.id.fragment_records_group_til)
        groupACTV = view.findViewById(R.id.fragment_records_group_actv)
        groupACTV.onItemClickListener = this
        setUpRecyclerView(view)
        PersonalRecordsDB.getAllRecords {
            usersRecords = it
            retrieveExercisesForAllRecords()
        }
        return view
    }


    override fun onItemClick(adapterView: AdapterView<*>?, view: View?, position: Int, p3: Long) {
        refreshRecords(adapterView!!.getItemAtPosition(position).toString())
    }

    override fun onExerciseItemClick(exerciseId: String) {
        //No implementation so far
    }

    private fun retrieveExercisesForAllRecords() {
        Log.d("clicked", "records size: ${usersRecords.size}")
        exercises = ArrayList()
        for (record in usersRecords) {
            ExercisesDB.getExerciseById(record.exerciseId) {
                exercises.add(it)
                if (exercises.size == usersRecords.size) {
                    Log.d("clicked", "exercises size: ${exercises.size}")
                    initGroups()
                }
            }
        }
    }

    private fun setUpRecyclerView(view: View) {
        exercisesRV = view.findViewById(R.id.fragment_records_rv)
        exercisesRV.itemAnimator = null
        exercisesRV.hasFixedSize()
        exercisesRV.layoutManager = LinearLayoutManager(context)
    }

    private fun refreshRecords(group: String) {
        val filteredExercises = exercises
            .stream()
            .filter {
                it.group == group
            }
            .collect(Collectors.toList())
            .toMutableList()

        Log.d("clicked", "filteredExercises : ${filteredExercises.size}")
        exercisesAdapter = AddExerciseAdapter(context!!, this, filteredExercises, usersRecords)
        exercisesRV.adapter = exercisesAdapter
    }

    private fun initGroups() {
        val groups = exercises
            .stream()
            .map {
                it.group
            }
            .distinct()
            .collect(Collectors.toSet())
            .toMutableList()
        Log.d("clicked", "onItemClick: ")
        val adapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_dropdown_item, groups)
        groupACTV.setAdapter(adapter)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RecordsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RecordsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}