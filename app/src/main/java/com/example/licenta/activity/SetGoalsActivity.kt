package com.example.licenta.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.example.licenta.R
import com.example.licenta.data.LoggedUserData
import com.example.licenta.data.LoggedUserGoals
import com.example.licenta.firebase.db.GoalsDB
import com.example.licenta.math.CalorieCalculator
import com.example.licenta.math.MacroCalculator
import com.example.licenta.model.user.Goals
import com.example.licenta.util.IntentConstants
import com.example.licenta.util.PersonalWeightPreference
import java.util.*

class SetGoalsActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var goalRadioGroup: RadioGroup
    private lateinit var activityRadioGroup: RadioGroup
    private lateinit var calculateBtn: Button
    private lateinit var saveBtn: Button
    private lateinit var caloriesAndMacrosLayout: LinearLayout
    private lateinit var proteinTV: TextView
    private lateinit var fatTV: TextView
    private lateinit var carbsTV: TextView
    private lateinit var caloriesTV: TextView
    private lateinit var heightTV: TextView
    private lateinit var weightTV: TextView
    private var isUpdate: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_goals)
        initComponents()
    }

    private fun initComponents() {
        goalRadioGroup = findViewById(R.id.activity_set_goal_goal_rg)
        activityRadioGroup = findViewById(R.id.activity_set_goal_activity_rg)
        caloriesAndMacrosLayout = findViewById(R.id.activity_set_goal_calories_and_macros_ll)
        proteinTV = findViewById(R.id.activity_set_goal_protein_tv)
        carbsTV = findViewById(R.id.activity_set_goal_carbs_tv)
        fatTV = findViewById(R.id.activity_set_goal_fat_tv)
        caloriesTV = findViewById(R.id.activity_set_goal_calories_tv)
        heightTV = findViewById(R.id.activity_set_goal_height_tv)
        heightTV.text = LoggedUserData.getLoggedUser().height.toString()
        weightTV = findViewById(R.id.activity_set_goal_weight_tv)
        weightTV.text = LoggedUserData.getLoggedUser().weight.toString()
        calculateBtn = findViewById(R.id.activity_set_goal_calculate_btn)
        calculateBtn.setOnClickListener(this)
        saveBtn = findViewById(R.id.activity_set_goal_save_btn)
        saveBtn.setOnClickListener(this)
        isUpdate = intent.extras?.getBoolean(IntentConstants.EXISTS, false) ?: false
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.activity_set_goal_calculate_btn -> calculateCaloriesAndMacros(
                getPreference(),
                getActivityType()
            )
            R.id.activity_set_goal_save_btn -> save(
                getPreference(),
                getActivityType()
            )
        }
    }

    private fun calculateCaloriesAndMacros(preference: PersonalWeightPreference, activity: Double) {
        val calories = CalorieCalculator.calculateCalories(preference, activity)
        val macros = MacroCalculator.calculateMacros(preference, calories)
        Log.d("setGoals", "calculatedMacros: $macros")
        Log.d("setGoals", "calculatedCalories: $calories")
        caloriesAndMacrosLayout.visibility = View.VISIBLE
        saveBtn.visibility = View.VISIBLE
        proteinTV.text = macros.first.toString()
        fatTV.text = macros.second.toString()
        carbsTV.text = macros.third.toString()
        caloriesTV.text = calories.toString()
    }

    private fun save(preference: PersonalWeightPreference, activity: Double) {
        val calories = CalorieCalculator.calculateCalories(preference, activity)
        val macros = MacroCalculator.calculateMacros(preference, calories)
        if (isUpdate) {
            val id = LoggedUserGoals.getGoals().goalsID
            GoalsDB.updateGoals(id, macros, calories) {
                goToMainActivityAttemptCallback(it, LoggedUserGoals.getGoals())
            }
        } else {
            val goals = Goals(
                UUID.randomUUID().toString(),
                LoggedUserData.getLoggedUser().uuid,
                calories,
                macros.first,
                macros.third,
                macros.second
            )
            GoalsDB.addUserGoals(goals) { areAdded, userGoals ->
                goToMainActivityAttemptCallback(areAdded, userGoals)
            }
        }
    }

    private fun goToMainActivityAttemptCallback(addedSuccessfully: Boolean, goals: Goals?) {
        if (addedSuccessfully) {
            startActivity(Intent(this@SetGoalsActivity, MainActivity::class.java))
            LoggedUserGoals.setGoals(goals!!)
        } else
            Toast
                .makeText(
                    this@SetGoalsActivity,
                    "Oops! Something went wrong, try again",
                    Toast.LENGTH_SHORT
                )
                .show()

    }

    private fun getPreference(): PersonalWeightPreference {
        return when (goalRadioGroup.checkedRadioButtonId) {
            R.id.activity_set_goal_lose_fat_rb -> PersonalWeightPreference.FAT_LOSS
            R.id.activity_set_goal_maintain_rb -> PersonalWeightPreference.MAINTAIN
            else -> PersonalWeightPreference.GAIN_MUSCLE
        }
    }

    private fun getActivityType(): Double {
        return when (activityRadioGroup.checkedRadioButtonId) {
            R.id.activity_set_goal_activity_sedentary_rb -> 1.2
            R.id.activity_set_goal_activity_light_rb -> 1.375
            R.id.activity_set_goal_activity_moderate_rb -> 1.55
            R.id.activity_set_goal_activity_active_rb -> 1.725
            else -> 1.9
        }
    }

}