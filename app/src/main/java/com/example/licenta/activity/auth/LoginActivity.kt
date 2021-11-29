package com.example.licenta.activity.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.example.licenta.R
import com.example.licenta.activity.MainActivity
import com.example.licenta.activity.SetGoalsActivity
import com.example.licenta.data.LoggedUserData
import com.example.licenta.data.LoggedUserGoals
import com.example.licenta.firebase.Auth
import com.example.licenta.firebase.db.GoalsDB
import com.example.licenta.firebase.db.UsersDB
import com.example.licenta.model.user.Goals
import com.example.licenta.model.user.User
import com.example.licenta.util.Util
import com.google.android.material.textfield.TextInputLayout
import java.util.*
import java.util.function.Supplier
import kotlin.NoSuchElementException

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var emailLayout: TextInputLayout
    private lateinit var emailET: EditText
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var passwordET: EditText
    private lateinit var loginBtn: Button
    private lateinit var signUpBtn: Button
    private lateinit var rootLayout: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initComponents()
    }

    private fun initComponents() {
        supportActionBar?.hide()
        rootLayout = findViewById(R.id.activity_login_parent_linear_layout)
        rootLayout.setOnClickListener(this)
        emailLayout = findViewById(R.id.activity_login_email_layout)
        emailET = findViewById(R.id.activity_login_email_et)
        passwordLayout = findViewById(R.id.activity_login_password_layout)
        passwordET = findViewById(R.id.activity_login_password_et)
        loginBtn = findViewById(R.id.activity_login_login_btn)
        loginBtn.setOnClickListener(this)
        signUpBtn = findViewById(R.id.activity_login_sign_up_btn)
        signUpBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.activity_login_login_btn -> loginUser(
                emailET.text.toString().trim(),
                passwordET.text.toString().trim()
            )
            R.id.activity_login_sign_up_btn -> startActivity(
                Intent(
                    this@LoginActivity,
                    RegisterActivity::class.java
                )
            )
            R.id.activity_login_parent_linear_layout -> Util.hideKeyboard(this@LoginActivity)
        }
    }


    private fun loginUser(email: String, password: String) {
        if (!validateEmail(email) || !validatePassword(password)) {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT)
                .show()
        } else {
            Auth.loginUser(email, password)
                .addOnSuccessListener {
                    UsersDB.getUser(Auth.currentUser().uid) {
                        getUserCallback(it)
                    }
                }

        }
    }


    private fun validateEmail(email: String): Boolean {
        return if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                .matches()
        ) {
            emailLayout.error = getString(R.string.activity_register_invalid_email_error)
            false
        } else {
            emailLayout.isErrorEnabled = false
            true
        }
    }

    private fun validatePassword(password: String): Boolean {
        return if (password.isEmpty() || password.length < 8) {
            passwordLayout.error =
                getString(R.string.activity_register_error_password_invalid_length)
            false
        } else {
            passwordLayout.isErrorEnabled = false
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }


    private fun getUserCallback(user: User?) {
        if (user != null) {
            LoggedUserData.setLoggedUser(user)
            checkIfUserHasGoals()
        } else {
            Toast.makeText(this@LoginActivity, "Invalid credentials!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun checkIfUserHasGoals() {
        val userId = LoggedUserData.getLoggedUser().uuid
        GoalsDB.userHasGoals(userId) {
            if (it) {
                GoalsDB.getUserGoals(userId) {
                    checkIfGoalsAreSetCallback(it)
                }
            } else {
                startActivity(Intent(this@LoginActivity, SetGoalsActivity::class.java))
            }
        }
    }

    private fun checkIfGoalsAreSetCallback(areSet: Boolean) {
        if(areSet){
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        }else{
            Toast.makeText(this@LoginActivity, "Oops! Something went wrong!",Toast.LENGTH_SHORT)
                .show()
        }
    }

}