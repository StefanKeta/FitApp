package com.example.licenta.activity.diary.food

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.example.licenta.R
import com.example.licenta.firebase.db.FoodDB
import com.example.licenta.model.food.Food
import com.example.licenta.util.IntentConstants
import com.example.licenta.util.Util
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.*

class RegisterFoodToDbActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var parentLayout: LinearLayout
    private lateinit var nameTIL: TextInputLayout
    private lateinit var nameET: TextInputEditText
    private lateinit var caloriesTIL: TextInputLayout
    private lateinit var caloriesET: TextInputEditText
    private lateinit var proteinTIL: TextInputLayout
    private lateinit var proteinET: TextInputEditText
    private lateinit var carbsTIL: TextInputLayout
    private lateinit var carbsET: TextInputEditText
    private lateinit var fatTIL: TextInputLayout
    private lateinit var fatET: TextInputEditText
    private lateinit var cancelBtn: Button
    private lateinit var saveBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_food_to_db)
        initComponents()
    }

    private fun initComponents() {
        parentLayout = findViewById(R.id.activity_register_food_parent_linear_layout)
        nameTIL = findViewById(R.id.activity_register_food_name_til)
        nameET = findViewById(R.id.activity_register_food_name_et)
        caloriesTIL = findViewById(R.id.activity_register_food_calories_til)
        caloriesET = findViewById(R.id.activity_register_food_calories_et)
        proteinTIL = findViewById(R.id.activity_register_food_protein_til)
        proteinET = findViewById(R.id.activity_register_food_protein_et)
        carbsTIL = findViewById(R.id.activity_register_food_carbs_til)
        carbsET = findViewById(R.id.activity_register_food_carbs_et)
        fatTIL = findViewById(R.id.activity_register_food_fat_til)
        fatET = findViewById(R.id.activity_register_food_fat_et)
        cancelBtn = findViewById(R.id.activity_register_food_cancel_btn)
        saveBtn = findViewById(R.id.activity_register_food_save_btn)
        parentLayout.setOnClickListener(this)
        cancelBtn.setOnClickListener(this)
        saveBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.activity_register_food_cancel_btn -> buttonPressed(R.id.activity_register_food_cancel_btn)
            R.id.activity_register_food_save_btn -> buttonPressed(R.id.activity_register_food_save_btn)
            R.id.activity_register_food_parent_linear_layout -> Util.hideKeyboard(this)
        }
    }

    private fun buttonPressed(id: Int) {
        val intent = Intent(this@RegisterFoodToDbActivity, AddFoodActivity::class.java)
        if (id == R.id.activity_register_food_cancel_btn) {
            intent.putExtra(IntentConstants.IS_FOOD_SAVED, false)
        } else {
            if (areAllFieldsFilled()) {
                val barcode = getIntent()
                    .getStringExtra(Food.BARCODE)
                val food = Food(
                    UUID.randomUUID().toString(),
                    nameET.text.toString().trim(),
                    barcode ?: "",
                    caloriesET.text.toString().trim().toInt(),
                    proteinET.text.toString().trim().toInt(),
                    carbsET.text.toString().trim().toInt(),
                    fatET.text.toString().trim().toInt()
                )
                FoodDB.saveFood(food) { isSaved ->
                    intent.putExtra(IntentConstants.IS_FOOD_SAVED, isSaved)
                }
            }
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun areAllFieldsFilled(): Boolean {
        if (nameET.text!!.isNotEmpty() &&
            caloriesET.text!!.isNotEmpty() &&
            fatET.text!!.isNotEmpty() &&
            carbsET.text!!.isNotEmpty() &&
            proteinET.text!!.isNotEmpty()
        ) return true
        if (nameET.text!!.isEmpty())
            nameTIL.error = "Please enter food name"
        if (caloriesET.text!!.isEmpty())
            caloriesTIL.error = "Please enter calories"
        if (proteinET.text!!.isEmpty())
            proteinTIL.error = "Please enter protein"
        if (carbsET.text!!.isEmpty())
            carbsTIL.error = "Please enter carbs"
        if (fatET.text!!.isEmpty())
            fatTIL.error = "Please enter fat"
        return false
    }

}