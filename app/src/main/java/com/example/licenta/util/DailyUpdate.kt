package com.example.licenta.util

import android.content.Context
import android.widget.Toast
import androidx.work.*
import com.example.licenta.firebase.db.ActivityDB
import com.example.licenta.model.activity.DailyUserActivity
import java.util.*
import java.util.concurrent.TimeUnit

class DailyUpdate(private val ctx: Context, private val workerParameters: WorkerParameters) :
    Worker(ctx, workerParameters) {
    private var previousTotalSteps = 0.0f
    private var distanceInKm = 0.0
    private var calories = 0
    private var totalSteps = 0f
    private lateinit var loggedUserId: String
    private lateinit var dateTrackingFor: String

    override fun doWork(): Result {
        val refreshDate = Calendar.getInstance()
        refreshDate.set(Calendar.HOUR_OF_DAY, 1)
        refreshDate.set(Calendar.MINUTE, 0)
        refreshDate.set(Calendar.SECOND, 0)
        val now = Calendar.getInstance()

        if (refreshDate.before(now))
            refreshDate.add(Calendar.HOUR_OF_DAY, 24)

        totalSteps =
            workerParameters.inputData.getFloat(SharedPrefsConstants.TOTAL_STEPS, totalSteps)
        loadState()
        if (dateTrackingFor != Date.getCurrentDate()) {
            saveActivityForTheDay()
        }

        val workRequest = OneTimeWorkRequestBuilder<DailyUpdate>()
            .setInitialDelay(refreshDate.timeInMillis - now.timeInMillis, TimeUnit.MILLISECONDS)
            .setInputData(Data.Builder().putFloat(SharedPrefsConstants.TOTAL_STEPS, 0f).build())
            .build()

        WorkManager.getInstance(ctx).enqueue(workRequest)
        return Result.success()
    }

    private fun loadState() {
        val prefs =
            ctx.getSharedPreferences(SharedPrefsConstants.STEPS, Context.MODE_PRIVATE)
        previousTotalSteps = prefs.getFloat(SharedPrefsConstants.PREVIOUS_STEPS_KEY, 0f)
        distanceInKm =
            Util.roundDouble(prefs.getFloat(SharedPrefsConstants.DISTANCE, 0f).toDouble(), 2)
        calories = prefs.getInt(SharedPrefsConstants.CALORIES, 0)
        dateTrackingFor =
            prefs.getString(SharedPrefsConstants.DATE_TRACKING_FOR, Date.getCurrentDate())!!
        loggedUserId =
            prefs.getString(SharedPrefsConstants.USER_ID, loggedUserId)!!
    }

    private fun resetState() {
        previousTotalSteps =
            workerParameters.inputData.getFloat(SharedPrefsConstants.TOTAL_STEPS, 0f)
        calories = 0
        distanceInKm = 0.0
        dateTrackingFor = Date.getCurrentDate()
    }

    private fun saveState() {
        val prefs =
            ctx.getSharedPreferences(SharedPrefsConstants.STEPS, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(SharedPrefsConstants.USER_ID, loggedUserId)
        editor.putFloat(SharedPrefsConstants.PREVIOUS_STEPS_KEY, previousTotalSteps)
        editor.putFloat(SharedPrefsConstants.DISTANCE, distanceInKm.toFloat())
        editor.putInt(SharedPrefsConstants.CALORIES, calories)
        editor.putString(SharedPrefsConstants.DATE_TRACKING_FOR, dateTrackingFor)
        editor.apply()
    }

    private fun saveActivityForTheDay() {
        val userActivity = DailyUserActivity(
            UUID.randomUUID().toString(),
            loggedUserId,
            dateTrackingFor,
            (totalSteps-previousTotalSteps).toInt(),
            distanceInKm,
            calories
        )
        ActivityDB
            .saveActivityForTheDay(userActivity) { added ->
                if (!added)
                    Toast.makeText(
                        ctx,
                        "Could not add activity to the database, check your connection",
                        Toast.LENGTH_SHORT
                    ).show()
                else {
                    resetState()
                    saveState()
                }
            }
    }
}