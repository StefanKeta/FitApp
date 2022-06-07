package com.example.licenta.activity

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.licenta.R
import com.example.licenta.activity.auth.LoginActivity
import com.example.licenta.data.LoggedUserData
import com.example.licenta.data.LoggedUserProfilePhoto
import com.example.licenta.firebase.Auth
import com.example.licenta.firebase.db.GoalsDB
import com.example.licenta.firebase.db.UsersDB
import com.example.licenta.model.user.User
import com.example.licenta.util.InternetConnectionTracker
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.lang.RuntimeException

class LoadingActivity : AppCompatActivity() {
    private lateinit var loadingBar: CircularProgressIndicator
    private lateinit var loadingTv: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        loadingBar = findViewById(R.id.activity_loading_progress_bar)
        loadingTv = findViewById(R.id.activity_loading_tv)
        checkConnection()
    }

    private fun checkConnection() {
        val connectivityManager = this.getSystemService(CONNECTIVITY_SERVICE)
                as ConnectivityManager
        val networkInfo: Array<NetworkInfo> = connectivityManager.allNetworkInfo
        for (info in networkInfo)
            if (info.state == NetworkInfo.State.CONNECTED) {
                loadingBar.visibility = View.VISIBLE
                trackInternetConnection()
                return
            }
        showError()
    }

    private fun trackInternetConnection() {
        InternetConnectionTracker.trackConnection(this)
        InternetConnectionTracker.observe(this@LoadingActivity) { isConnected ->
            if (isConnected) {
                loadingTv.text = getString(R.string.activity_loading_retrieving_user_text)
                tryToAuthenticateUser()
            } else showError()
        }
    }

    private fun tryToAuthenticateUser() {
        if (Auth.currentUser() != null) {
            userLoginCallback()
        } else {
            startActivity(Intent(this@LoadingActivity, LoginActivity::class.java))
        }
    }

    private fun showError() {
        loadingBar.visibility = View.INVISIBLE
        loadingTv.text = getString(R.string.activity_loading_turn_on_connection)
    }

    private fun userLoginCallback() {
        UsersDB.getUser(Auth.currentUser()!!.uid, ::getUserDataAndLogin)
    }

    private fun getUserDataAndLogin(user: User?) {
        if (user != null) {
            LoggedUserData.setLoggedUser(user)
            LoggedUserProfilePhoto.setProfilePhoto(this@LoadingActivity,user.uuid, ::checkIfUserHasGoals)
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
                startActivity(Intent(this@LoadingActivity, SetGoalsActivity::class.java))
                finish()
            }
        }
    }

    private fun checkIfGoalsAreSetCallback(areSet: Boolean) {
        if (areSet) {
            startActivity(Intent(this@LoadingActivity, MainActivity::class.java))
            finish()
        } else {
            Toast.makeText(this@LoadingActivity, "Oops! Something went wrong!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


}
