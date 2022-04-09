package com.example.licenta.fragment.main

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.licenta.R
import com.example.licenta.data.LoggedUserData
import com.example.licenta.firebase.db.ActivityDB
import com.example.licenta.math.BMICalculator
import com.example.licenta.model.activity.DailyUserActivity
import com.example.licenta.util.Date
import com.example.licenta.util.PermissionsChecker
import com.example.licenta.util.SharedPrefsConstants
import com.example.licenta.util.Util
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlin.math.pow

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ActivityFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ActivityFragment : Fragment(), SensorEventListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var totalStepsTV: TextView
    private lateinit var distanceTV: TextView
    private lateinit var averageSpeedTV: TextView
    private lateinit var caloriesBurntTV: TextView
    private lateinit var circularProgressSteps: CircularProgressIndicator
    private lateinit var datePickerBtn: Button
    private lateinit var tomorrowBtn: Button
    private lateinit var yesterdayBtn: Button

    private lateinit var sensorManager: SensorManager
    private var sensorRunning = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f
    private var currentSteps = 0
    private var hoursWalked = 0.0
    private var date = Date.getCurrentDate()
    private var dateTrackingFor = Date.getCurrentDate()

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
        return inflater.inflate(R.layout.fragment_activity, container, false)
            .also { view -> initComponents(view) }
    }

    private fun initComponents(view: View) {
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        distanceTV = view.findViewById(R.id.fragment_activity_distance_walked)
        averageSpeedTV = view.findViewById(R.id.fragment_activity_speed_tv)
        caloriesBurntTV = view.findViewById(R.id.fragment_activity_calories_tv)
        totalStepsTV = view.findViewById(R.id.fragment_activity_steps_tv)
        circularProgressSteps = view.findViewById(R.id.fragment_activity_circular_progress)
        datePickerBtn = view.findViewById(R.id.fragment_activity_pick_date_btn)
        datePickerBtn.text = dateTrackingFor
        tomorrowBtn = view.findViewById(R.id.fragment_activity_next_navigation_btn)
        yesterdayBtn = view.findViewById(R.id.fragment_activity_back_navigation_btn)
        loadStepsState()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (sensorRunning) {
            hoursWalked += 1.666 * 10.0.pow(-5.0) // SENSOR_DELAY_UI = 60 000µs
            totalSteps = event!!.values[0]
            currentSteps = (totalSteps.toLong() - previousTotalSteps.toLong()).toInt()
            totalStepsTV.text = currentSteps.toString()
            circularProgressSteps.progress = currentSteps
            val distanceInKm = updateDistance(currentSteps)
            distanceTV.text = distanceInKm.toString()
            val averageSpeed = updateAverageSpeed(distanceInKm, hoursWalked)
            averageSpeedTV.text = averageSpeed.toString()
            val calories = updateCalories(currentSteps, averageSpeed)
            caloriesBurntTV.text = calories.toString()
            if (Date.getCurrentDate() != dateTrackingFor) {
                val userActivity = DailyUserActivity(
                    LoggedUserData.getLoggedUser().uuid,
                    dateTrackingFor,
                    currentSteps,
                    calories,
                    distanceInKm
                )
                saveActivityForTheDay(userActivity)
                dateTrackingFor = Date.getCurrentDate()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, p1: Int) {
    }


    private fun saveActivityForTheDay(userActivity: DailyUserActivity) {
        ActivityDB
            .saveActivityForTheDay(userActivity) { added ->
                if (!added)
                    Toast.makeText(
                        requireContext(),
                        "Could not add activity to the database, check your connection",
                        Toast.LENGTH_SHORT
                    ).show()
            }
        resetSteps()
        saveStepsState()
    }

    private fun updateDistance(steps: Int): Double {
        val userHeight = LoggedUserData.getLoggedUser().height
        val stepLength = ((userHeight.toDouble() / 100) * 0.414) / 1000
        return Util.roundDouble(stepLength * steps, 3)
    }

    private fun updateCalories(steps: Int, averageSpeed: Double = 4.823): Int {
        val userWeight = LoggedUserData.getLoggedUser().weight
        val userHeight = LoggedUserData.getLoggedUser().height
        val age = Date.parseAge(LoggedUserData.getLoggedUser().dob)
        val bmi = BMICalculator.calculateBMI(userWeight, userHeight)
        val calories = steps * 0.04 * bmi * age * averageSpeed
        return if (calories - calories.toInt() > 0.5) calories.toInt() + 1 else calories.toInt()
    }

    private fun updateAverageSpeed(distance: Double, hoursWalked: Double): Double {
        return Util.roundDouble(distance / hoursWalked, 2) / 100
    }

    private fun saveStepsState() {
        val prefs =
            requireActivity().getSharedPreferences(SharedPrefsConstants.STEPS, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putFloat(SharedPrefsConstants.PREVIOUS_STEPS_KEY, previousTotalSteps)
        editor.apply()
    }

    private fun loadStepsState() {
        val prefs =
            requireActivity().getSharedPreferences(SharedPrefsConstants.STEPS, Context.MODE_PRIVATE)
        previousTotalSteps = prefs.getFloat(SharedPrefsConstants.PREVIOUS_STEPS_KEY, 0f)
        circularProgressSteps.progress = previousTotalSteps.toInt()
    }

    private fun resetSteps() {
        totalStepsTV.text = 0.toString()
        previousTotalSteps = totalSteps
        hoursWalked = 0.0
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()
        if (PermissionsChecker.isPedometerPermissionAccepted(requireActivity())) {
            val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            if (stepSensor != null) {
                sensorRunning = true
                sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
            } else {
                Toast.makeText(
                    requireContext(),
                    "The phone has no step sensors",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        } else {
            PermissionsChecker.askForPedometerPermission(requireActivity())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        saveStepsState()
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ActivityFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ActivityFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}