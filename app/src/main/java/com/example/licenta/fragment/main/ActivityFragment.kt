package com.example.licenta.fragment.main

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import java.util.*
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
class ActivityFragment : Fragment(), SensorEventListener, View.OnClickListener {
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
    private var totalHoursWalked = 0.0
    private var previousHoursWalked = 0.0
    private var dateTrackingFor = Date.getCurrentDate()
    private var loggedUserId = LoggedUserData.getLoggedUser().uuid
    private var todaysActivity = DailyUserActivity(
        userId = loggedUserId
    )

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
        tomorrowBtn = view.findViewById<Button>(R.id.fragment_activity_next_navigation_btn)
            .also { it.setOnClickListener(this) }
        yesterdayBtn = view.findViewById<Button>(R.id.fragment_activity_back_navigation_btn)
            .also { it.setOnClickListener(this) }
        loadStepsState()
    }

    override fun onAccuracyChanged(sensor: Sensor?, p1: Int) {
    }

    override fun onClick(clickedView: View?) {
        when (clickedView!!.id) {
            R.id.fragment_activity_next_navigation_btn -> {
                handleDateChange(Date.goToDayAfter(datePickerBtn.text.toString()))
            }
            R.id.fragment_activity_back_navigation_btn -> {
                handleDateChange(Date.goToDayBefore(datePickerBtn.text.toString()))
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (sensorRunning) {
            totalHoursWalked += 1.666 * SharedPrefsConstants.FLOAT_NUMBER_SAVED_MULTIPLIER * 10.0.pow(
                -5.0
            )// SENSOR_DELAY_UI ~ 60 000µs -> converted to hour
            totalSteps = event!!.values[0]
            val currentWalkingHours = totalHoursWalked - previousHoursWalked
            val currentSteps = (totalSteps.toLong() - previousTotalSteps.toLong()).toInt()
            circularProgressSteps.progress = currentSteps
            val distanceInKm = updateDistance(currentSteps)
            val averageSpeed = updateAverageSpeed(distanceInKm, currentWalkingHours)
            val calories = updateCalories(currentSteps, averageSpeed)
            if (dateTrackingFor == datePickerBtn.text) {
                totalStepsTV.text = currentSteps.toString()
                distanceTV.text = distanceInKm.toString()
                averageSpeedTV.text = averageSpeed.toString()
                caloriesBurntTV.text = calories.toString()
            }
            if (Date.getCurrentDate() != dateTrackingFor) {
                totalHoursWalked = previousHoursWalked
                val userActivity = DailyUserActivity(
                    UUID.randomUUID().toString(),
                    loggedUserId,
                    dateTrackingFor,
                    currentSteps,
                    distanceInKm,
                    averageSpeed,
                    calories
                )
                saveActivityForTheDay(userActivity)
            }
        }
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
        dateTrackingFor = Date.getCurrentDate()
    }

    private fun handleDateChange(date: String) {
        if (date == dateTrackingFor) {
            loadTodaysActivity()
        } else if (datePickerBtn.text.toString() == dateTrackingFor) {
            saveTodaysActivity()
        }
        datePickerBtn.text = date
        displayActivityForDate(date)
    }

    private fun loadTodaysActivity() {
        totalStepsTV.text = todaysActivity.steps.toString()
        distanceTV.text = todaysActivity.distance.toString()
        averageSpeedTV.text = todaysActivity.averageSpeed.toString()
        caloriesBurntTV.text = todaysActivity.calories.toString()
        circularProgressSteps.progress = totalStepsTV.text.toString().toInt()
    }

    private fun saveTodaysActivity() {
        todaysActivity = todaysActivity.copy(
            date = dateTrackingFor,
            steps = totalStepsTV.text.toString().toInt(),
            averageSpeed = averageSpeedTV.text.toString().toDouble(),
            distance = distanceTV.text.toString().toDouble(),
            calories = caloriesBurntTV.text.toString().toInt()
        )
    }

    private fun displayActivityForDate(date: String) {
        ActivityDB.loadActivityForTheDay(
            LoggedUserData.getLoggedUser().uuid,
            date
        ) { dailyUserActivity ->
            if (dailyUserActivity != null) {
                totalStepsTV.text = dailyUserActivity.steps.toString()
                distanceTV.text = Util.roundDouble(dailyUserActivity.distance, 3).toString()
                averageSpeedTV.text = Util.roundDouble(dailyUserActivity.averageSpeed, 2).toString()
                caloriesBurntTV.text = dailyUserActivity.calories.toString()
                circularProgressSteps.progress = dailyUserActivity.steps
            } else if (date != dateTrackingFor) {
                totalStepsTV.text = 0.toString()
                distanceTV.text = 0.toString()
                averageSpeedTV.text = 0.toString()
                caloriesBurntTV.text = 0.toString()
                circularProgressSteps.progress = 0
            }
        }
    }

    private fun updateDistance(steps: Int): Double {
        val userHeight = LoggedUserData.getLoggedUser().height
        val stepLength = (userHeight.toDouble() * 0.414)
        return stepLength * steps / 100 / 1000 // cm -> m -> km
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
        Log.d("pedometer", "updateAverageSpeed: $distance / $hoursWalked")
        return distance / hoursWalked
    }

    private fun saveStepsState() {
        val prefs =
            requireActivity().getSharedPreferences(SharedPrefsConstants.STEPS, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(SharedPrefsConstants.USER_ID, LoggedUserData.getLoggedUser().uuid)
        editor.putFloat(SharedPrefsConstants.PREVIOUS_STEPS_KEY, previousTotalSteps)
        editor.putFloat(
            SharedPrefsConstants.PREVIOUS_HOURS_WALKED,
            previousHoursWalked.toFloat()
        )
        editor.apply()
    }

    private fun loadStepsState() {
        val prefs =
            requireActivity().getSharedPreferences(SharedPrefsConstants.STEPS, Context.MODE_PRIVATE)
        loggedUserId =
            prefs.getString(SharedPrefsConstants.USER_ID, LoggedUserData.getLoggedUser().uuid)!!
        previousTotalSteps = prefs.getFloat(SharedPrefsConstants.PREVIOUS_STEPS_KEY, 0f)
        circularProgressSteps.progress = previousTotalSteps.toInt()
        val previousHoursFloat =
            prefs.getFloat(SharedPrefsConstants.PREVIOUS_HOURS_WALKED, 0f)
        previousHoursWalked =
            (previousHoursFloat / SharedPrefsConstants.FLOAT_NUMBER_SAVED_MULTIPLIER).toDouble()
    }

    private fun resetSteps() {
        totalStepsTV.text = 0.toString()
        previousTotalSteps = totalSteps
        previousHoursWalked = totalHoursWalked
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
        } else {
            Toast.makeText(
                requireContext(),
                "Your device does not have access to the pedometer",
                Toast.LENGTH_SHORT
            ).show()
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