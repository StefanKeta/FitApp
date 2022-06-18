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
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.licenta.R
import com.example.licenta.data.LoggedUserData
import com.example.licenta.data.LoggedUserGoals
import com.example.licenta.firebase.db.ActivityDB
import com.example.licenta.math.BMICalculator
import com.example.licenta.model.activity.DailyUserActivity
import com.example.licenta.util.*
import com.example.licenta.util.Date
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.util.*
import java.util.concurrent.TimeUnit

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
    private lateinit var caloriesBurntTV: TextView
    private lateinit var circularProgressSteps: CircularProgressIndicator
    private lateinit var datePickerBtn: Button
    private lateinit var tomorrowBtn: Button
    private lateinit var yesterdayBtn: Button

    private var dateTrackingFor: String = Date.getCurrentDate()
    private lateinit var sensorManager: SensorManager
    private var loggedUserId = LoggedUserData.getLoggedUser().uuid
    private var sensorRunning = false
    private var currentSteps = 0
    private var totalSteps = 0f
    private var previousTotalSteps = 0f
    private var distanceInKm = 0.0
    private var calories = 0
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
        distanceTV = view.findViewById(R.id.fragment_activity_distance_tv)
        caloriesBurntTV = view.findViewById(R.id.fragment_activity_calories_tv)
        totalStepsTV = view.findViewById(R.id.fragment_activity_steps_tv)
        circularProgressSteps = view.findViewById(R.id.fragment_activity_circular_progress)
        circularProgressSteps.max = LoggedUserGoals.getGoals().steps
        datePickerBtn = view.findViewById(R.id.fragment_activity_pick_date_btn)
        tomorrowBtn = view.findViewById<Button>(R.id.fragment_activity_next_navigation_btn)
            .also { it.setOnClickListener(this) }
        yesterdayBtn = view.findViewById<Button>(R.id.fragment_activity_back_navigation_btn)
            .also { it.setOnClickListener(this) }
        loadState()
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
            if (activity != null) {
                totalSteps = event!!.values[0]
                currentSteps = (totalSteps.toLong() - previousTotalSteps.toLong()).toInt()
                circularProgressSteps.progress = currentSteps
                distanceInKm = updateDistance(currentSteps)
                calories = updateCalories(currentSteps)
                if (dateTrackingFor == datePickerBtn.text) {
                    totalStepsTV.text = currentSteps.toString()
                    distanceTV.text = distanceInKm.toString()
                    caloriesBurntTV.text = calories.toString()
                }
                if (Date.getCurrentDate() != dateTrackingFor) {
                    saveActivityForTheDay()
                }
            }
        }
    }

    private fun saveActivityForTheDay() {
        val userActivity = DailyUserActivity(
            UUID.randomUUID().toString(),
            LoggedUserData.getLoggedUser().uuid,
            dateTrackingFor,
            currentSteps,
            distanceInKm,
            calories
        )
        ActivityDB
            .saveActivityForTheDay(userActivity) { added ->
                if (!added)
                    Toast.makeText(
                        requireContext(),
                        "Could not add activity to the database, check your connection",
                        Toast.LENGTH_SHORT
                    ).show()
                else {
                    resetState()
                    saveState()
                }
            }
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
        caloriesBurntTV.text = todaysActivity.calories.toString()
        circularProgressSteps.progress = totalStepsTV.text.toString().toInt()
    }

    private fun saveTodaysActivity() {
        todaysActivity = todaysActivity.copy(
            date = dateTrackingFor,
            steps = totalStepsTV.text.toString().toInt(),
            distance = distanceInKm,
            calories = calories,
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
                caloriesBurntTV.text = dailyUserActivity.calories.toString()
                circularProgressSteps.progress = dailyUserActivity.steps
            } else if (date != dateTrackingFor) {
                totalStepsTV.text = 0.toString()
                distanceTV.text = 0.toString()
                caloriesBurntTV.text = 0.toString()
                circularProgressSteps.progress = 0
            }
        }
    }

    private fun updateDistance(steps: Int): Double {
        val userHeight = LoggedUserData.getLoggedUser().height
        val stepLength = (userHeight.toDouble() * 0.414)
        return Util.roundDouble(stepLength * steps / 100 / 1000) // cm -> m -> km
    }

    private fun updateCalories(steps: Int, averageSpeed: Double = 6.45): Int {
        val userWeight = LoggedUserData.getLoggedUser().weight
        val userHeight = LoggedUserData.getLoggedUser().height
        val age = Date.parseAge(LoggedUserData.getLoggedUser().dob)
        val bmi = BMICalculator.calculateBMI(userWeight, userHeight)
        val calories = steps * 0.05 * bmi * age * averageSpeed
        return if (calories - calories.toInt() > 0.5) calories.toInt() + 1 else calories.toInt()
    }

    private fun loadState() {
        val prefs =
            requireActivity().getSharedPreferences(SharedPrefsConstants.STEPS, Context.MODE_PRIVATE)
        previousTotalSteps = prefs.getFloat(SharedPrefsConstants.PREVIOUS_STEPS_KEY, 0f)
        totalSteps = prefs.getFloat(SharedPrefsConstants.TOTAL_STEPS, 0f)
        distanceInKm =
            Util.roundDouble(prefs.getFloat(SharedPrefsConstants.DISTANCE, 0f).toDouble(), 2)
        calories = prefs.getInt(SharedPrefsConstants.CALORIES, 0)
        circularProgressSteps.progress = previousTotalSteps.toInt()
        loggedUserId =
            prefs.getString(SharedPrefsConstants.USER_ID, LoggedUserData.getLoggedUser().uuid)!!
        setTexts()
    }

    private fun resetState() {
        totalStepsTV.text = 0.toString()
        distanceTV.text = 0.toString()
        previousTotalSteps = totalSteps
        calories = 0
        distanceInKm = 0.0
        dateTrackingFor = Date.getCurrentDate()
    }

    private fun saveState() {
        val prefs =
            requireActivity().getSharedPreferences(SharedPrefsConstants.STEPS, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(SharedPrefsConstants.USER_ID, loggedUserId)
        editor.putFloat(SharedPrefsConstants.PREVIOUS_STEPS_KEY, previousTotalSteps)
        editor.putFloat(SharedPrefsConstants.DISTANCE, distanceInKm.toFloat())
        editor.putInt(SharedPrefsConstants.CALORIES, calories)
        editor.apply()
    }

    private fun setTexts() {
        totalStepsTV.text = (totalSteps.toInt() - previousTotalSteps.toInt()).toString()
        distanceTV.text = distanceInKm.toString()
        caloriesBurntTV.text = calories.toString()
        datePickerBtn.text = dateTrackingFor
    }

    private fun scheduleJob() {
        val refreshDate = Calendar.getInstance()
        refreshDate.set(Calendar.HOUR_OF_DAY, 1)
        refreshDate.set(Calendar.MINUTE, 0)
        refreshDate.set(Calendar.SECOND, 0)
        val now = Calendar.getInstance()

        if (refreshDate.before(now))
            refreshDate.add(Calendar.HOUR_OF_DAY, 24)

        val workRequest = OneTimeWorkRequestBuilder<DailyUpdate>()
            .setInitialDelay(refreshDate.timeInMillis - now.timeInMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                Data.Builder().putFloat(SharedPrefsConstants.TOTAL_STEPS, totalSteps).build()
            )
            .build()

        WorkManager.getInstance(requireContext()).enqueue(workRequest)
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
        saveState()
        scheduleJob()
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