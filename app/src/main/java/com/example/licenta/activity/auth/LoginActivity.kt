package com.example.licenta.activity.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.licenta.R
import com.example.licenta.activity.MainActivity
import com.example.licenta.activity.SetGoalsActivity
import com.example.licenta.data.LoggedUserData
import com.example.licenta.firebase.Auth
import com.example.licenta.firebase.db.GoalsDB
import com.example.licenta.firebase.db.UsersDB
import com.example.licenta.model.user.User
import com.example.licenta.util.Util
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputLayout
import java.lang.RuntimeException


class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var emailLayout: TextInputLayout
    private lateinit var emailET: EditText
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var passwordET: EditText
    private lateinit var loginBtn: Button
    private lateinit var signUpBtn: Button
    private lateinit var rootLayout: LinearLayout
    private lateinit var checkingCredentialsLayout: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        if (Auth.currentUser() != null) {
            userLoginCallback(true)
        } else {
            initComponents()
        }
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
        checkingCredentialsLayout = findViewById(R.id.activity_login_checking_credentials_layout)

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
        checkingCredentialsLayout.visibility = View.VISIBLE
        loginBtn.isClickable = false
        Util.hideKeyboard(this)
        if (!validateEmail(email) || !validatePassword(password)) {
            checkingCredentialsLayout.visibility = View.INVISIBLE
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT)
                .show()
            loginBtn.isClickable = true
        } else {
            Auth.loginUser(email, password, ::userLoginCallback)
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

    private fun userLoginCallback(isLoggedIn: Boolean) {
        if (isLoggedIn) {
            UsersDB.getUser(Auth.currentUser()!!.uid, ::getUserCallback)
        } else {
            Toast.makeText(
                this,
                "Invalid credentials!",
                Toast.LENGTH_SHORT
            )
                .show()
            loginBtn.isClickable = true
            checkingCredentialsLayout.visibility = View.INVISIBLE
        }
    }

    private fun getUserCallback(user: User?) {
        if (user != null) {
            LoggedUserData.setLoggedUser(user)
            checkIfUserHasGoals()
        } else {
            throw RuntimeException("The user has no data in the database")
        }
    }

    private fun checkIfUserHasGoals() {
        val userId = LoggedUserData.getLoggedUser().uuid
        GoalsDB.userHasGoals(userId) {
            if (it) {
                GoalsDB.getUserGoals(userId) { hasSetGoals ->
                    checkIfGoalsAreSetCallback(hasSetGoals)
                }
            } else {
                startActivity(Intent(this@LoginActivity, SetGoalsActivity::class.java))
                finish()
            }
        }
    }

    private fun checkIfGoalsAreSetCallback(areSet: Boolean) {
        if (areSet) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        } else {
            Toast.makeText(this@LoginActivity, "Oops! Something went wrong!", Toast.LENGTH_SHORT)
                .show()
        }
    }

}