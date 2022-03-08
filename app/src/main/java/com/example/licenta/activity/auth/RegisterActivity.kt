package com.example.licenta.activity.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.licenta.R
import com.example.licenta.firebase.Auth
import com.example.licenta.model.user.Gender
import com.example.licenta.util.Date
import com.example.licenta.util.Util
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialDatePicker.INPUT_MODE_CALENDAR
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var parentLayout: LinearLayout
    private lateinit var scrollView: ScrollView
    private lateinit var firstNameLayout: TextInputLayout
    private lateinit var firstNameET: TextInputEditText
    private lateinit var lastNameLayout: TextInputLayout
    private lateinit var lastNameET: EditText
    private lateinit var emailLayout: TextInputLayout
    private lateinit var emailET: EditText
    private lateinit var dobLayout: TextInputLayout
    private lateinit var dobET: EditText
    private lateinit var genderDropdown: AutoCompleteTextView
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var passwordET: EditText
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var confirmPasswordET: EditText
    private lateinit var heightLayout: TextInputLayout
    private lateinit var heightET: EditText
    private lateinit var weightLayout: TextInputLayout
    private lateinit var weightET: EditText
    private lateinit var heightGroup: MaterialButtonToggleGroup
    private lateinit var heightCmBtn: Button
    private lateinit var heightFeetBtn: Button
    private lateinit var weightGroup: MaterialButtonToggleGroup
    private lateinit var weightKgBtn: Button
    private lateinit var weightLbsBtn: Button
    private lateinit var signUpBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.hide()
        initComponents()
    }

    private fun initComponents() {
        parentLayout = findViewById(R.id.activity_register_parent_linear_layout)
        parentLayout.setOnClickListener(this)
        scrollView = findViewById(R.id.activity_register_scroll_view)
        scrollView.setOnClickListener(this)
        firstNameLayout = findViewById(R.id.activity_register_first_name_layout)
        firstNameET = findViewById(R.id.activity_register_first_name_et)
        lastNameLayout = findViewById(R.id.activity_register_last_name_layout)
        lastNameET = findViewById(R.id.activity_register_last_name_et)
        emailLayout = findViewById(R.id.activity_register_email_layout)
        emailET = findViewById(R.id.activity_register_email_et)
        dobLayout = findViewById(R.id.activity_register_dob_layout)
        dobET = findViewById(R.id.activity_register_dob_et)
        dobET.setOnClickListener(this)
        passwordLayout = findViewById(R.id.activity_register_password_layout)
        passwordET = findViewById(R.id.activity_register_password_et)
        confirmPasswordLayout = findViewById(R.id.activity_register_confirm_password_layout)
        confirmPasswordET = findViewById(R.id.activity_register_confirm_password_et)
        heightLayout = findViewById(R.id.activity_register_height_layout)
        heightET = findViewById(R.id.activity_register_height_et)
        heightGroup = findViewById(R.id.activity_register_height_tbg)
        heightCmBtn = findViewById(R.id.activity_register_cm_btn)
        heightCmBtn.setOnClickListener(this)
        heightFeetBtn = findViewById(R.id.activity_register_feet_btn)
        heightFeetBtn.setOnClickListener(this)
        weightLayout = findViewById(R.id.activity_register_weight_layout)
        weightET = findViewById(R.id.activity_register_weight_et)
        weightGroup = findViewById(R.id.activity_register_weight_tbg)
        weightKgBtn = findViewById(R.id.activity_register_kg_btn)
        weightKgBtn.setOnClickListener(this)
        weightLbsBtn = findViewById(R.id.activity_register_lbs_btn)
        weightLbsBtn.setOnClickListener(this)
        signUpBtn = findViewById(R.id.activity_register_sign_up_btn)
        signUpBtn.setOnClickListener(this)
        setUpDropdown()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.activity_register_parent_linear_layout -> Util.hideKeyboard(this)
            R.id.activity_register_gender_dropdown -> Util.hideKeyboard(this)
            R.id.activity_register_scroll_view -> Util.hideKeyboard(this)
            R.id.activity_register_dob_et -> openDatePicker()
            R.id.activity_register_cm_btn -> heightButtonClicked(R.id.activity_register_cm_btn)
            R.id.activity_register_feet_btn -> heightButtonClicked(R.id.activity_register_feet_btn)
            R.id.activity_register_kg_btn -> weightButtonClicked(R.id.activity_register_kg_btn)
            R.id.activity_register_lbs_btn -> weightButtonClicked(R.id.activity_register_lbs_btn)
            R.id.activity_register_sign_up_btn -> validateAndRegisterUser()
        }
    }

    private fun heightButtonClicked(id: Int) {
        if (id == R.id.activity_register_cm_btn) {
            heightET.hint =
                "${getString(R.string.activity_register_height_et)} (cm)"
            if (heightET.text.isNotEmpty()) {
                val feetInches = heightET.text.split(".")
                val height = if (feetInches.size == 1)
                    Util.convertFeetInchesToCm(feetInches[0].toInt())
                else
                    Util.convertFeetInchesToCm(feetInches[0].toInt(), feetInches[1].toInt())
                heightET.setText(height.toString())
            }
        } else {
            heightET.hint =
                "${getString(R.string.activity_register_height_et)} (ft.inch)"
            if (heightET.text.isNotEmpty()) {
                val pair = Util.convertCmToFeetInches(heightET.text.toString().toInt())
                heightET.setText("${pair.first}.${pair.second}")
            }
        }
    }

    private fun weightButtonClicked(id: Int) {
        if (id == R.id.activity_register_kg_btn) {
            weightET.hint =
                "${getString(R.string.activity_register_weight_et)} (kg)"
            if (weightET.text.isNotEmpty()) {
                val lbs = weightET.text.toString().toInt()
                val weight = Util.convertLbsToKg(lbs)
                weightET.setText(weight.toString())
            }
        } else {
            weightET.hint =
                "${getString(R.string.activity_register_weight_et)} lbs"
            if (weightET.text.isNotEmpty()) {
                val kg = weightET.text.toString().toDouble()
                val lbs = Util.convertKgToLbs(kg)
                weightET.setText(lbs.toString())
            }
        }
    }

    private fun setUpDropdown() {
        genderDropdown = findViewById(R.id.activity_register_gender_dropdown)
        genderDropdown.setOnClickListener(this)
        val list = listOf("M", "F")
        genderDropdown.setText(list[0])
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        genderDropdown.setAdapter(adapter)

    }


    private fun openDatePicker() {
        Util.hideKeyboard(this)
        val datePicker = MaterialDatePicker
            .Builder
            .datePicker()
            .setTitleText(getString(R.string.activity_register_dob_date_picker_title))
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker
            .show(supportFragmentManager, null)

        datePicker.addOnPositiveButtonClickListener {
            dobET.setText(Date.getDateFromTimestamp(datePicker.selection!! / 1000))
            datePicker.dismiss()
        }

        datePicker.addOnNegativeButtonClickListener {
            datePicker.dismiss()
        }
    }

    private fun validateAndRegisterUser() {
        Util.hideKeyboard(this)
        if (validateNames() &&
            validateEmail() &&
            validatePassword(passwordET) &&
            validatePassword(confirmPasswordET) &&
            arePasswordsEqual() &&
            dobET.text.isNotEmpty() &&
            isHeightValid() &&
            isWeightValid()
        ) {
            val dobTimestamp = Date.getTimestampFromDate(dobET.text.toString())
            Auth.registerUser(
                this,
                firstNameET.text.toString().trim(),
                lastNameET.text.toString().trim(),
                emailET.text.toString().trim(),
                dobTimestamp,
                getGender(),
                heightET.text.toString().trim().toInt(),
                weightET.text.toString().trim().toInt(), passwordET.text.toString().trim()
            )
            finish()
        }
    }

    private fun getGender(): Gender {
        return when (genderDropdown.text.toString().lowercase()) {
            "m" -> Gender.MALE
            else -> Gender.FEMALE
        }
    }

    private fun validateNames(): Boolean {
        if (firstNameET.text!!.isNotEmpty() && lastNameET.text!!.isNotEmpty()) {
            return true
        }
        return if (firstNameET.text!!.isEmpty()) {
            firstNameLayout.error = getString(R.string.activity_register_error_required_field)
            false
        } else {
            lastNameLayout.error = getString(R.string.activity_register_error_required_field)
            false
        }
    }


    private fun validateEmail(): Boolean {
        val email = emailET.text
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = getString(R.string.activity_register_invalid_email_error)
            return false
        }
        return true
    }

    private fun validatePassword(password: EditText): Boolean {
        if (password.text.length < 8) {
            if (password.id == R.id.activity_register_password_et)
                passwordLayout.error =
                    getString(R.string.activity_register_error_password_invalid_length)
            else
                confirmPasswordLayout.error =
                    getString(R.string.activity_register_error_password_invalid_length)
            return false
        }
        return true
    }

    private fun arePasswordsEqual(): Boolean {
        val password = passwordET.text.toString().trim()
        val confirmedPassword = confirmPasswordET.text.toString().trim()
        return password == confirmedPassword
    }

    private fun isHeightValid(): Boolean {
        if (heightCmBtn.isEnabled) {
            val height = if (heightET.text.isNotEmpty()) heightET.text.toString().toInt() else 0
            return checkIfTooSmallOrTooTall(height)
        } else {
            val heightList = heightET.text.split(".")
            return if (heightList.size > 2 || heightList[0].toDouble() > 8 || (heightList.size == 2 && heightList[1].toDouble() > 11)) {
                heightLayout.error = getString(R.string.activity_register_error_height_invalid)
                false
            } else if (heightList.size == 1) {
                val height = Util.convertFeetInchesToCm(heightList[0].toInt())
                checkIfTooSmallOrTooTall(height)
            } else {
                val feet = heightList[0].toInt()
                val inch = heightList[1].toInt()
                val height = Util.convertFeetInchesToCm(feet, inch)
                checkIfTooSmallOrTooTall(height)
            }
        }
    }

    private fun checkIfTooSmallOrTooTall(height: Int): Boolean {
        if (height < Util.MINIMUM_HEIGHT_CM || height > Util.MAXIMUM_HEIGHT_CM) {
            heightLayout.error = getString(R.string.activity_register_error_height_invalid)
            return false
        }
        return true
    }

    private fun isWeightValid(): Boolean {
        if (weightKgBtn.isEnabled) {
            if (weightET.text.isEmpty() || weightET.text.toString()
                    .toDouble() < Util.MINIMUM_WEIGHT_KG
            ) {
                weightLayout.error = getString(R.string.activity_register_error_weight_invalid)
                return false
            }
        } else {
            val lbs = weightET.text.toString().toInt().or(0)
            val kg = Util.convertLbsToKg(lbs)
            if (weightET.text.isEmpty() || weightET.text.contains(".") || kg > Util.MINIMUM_WEIGHT_KG) {
                weightLayout.error = getString(R.string.activity_register_error_weight_invalid)
                return false
            }
        }
        return true
    }


}