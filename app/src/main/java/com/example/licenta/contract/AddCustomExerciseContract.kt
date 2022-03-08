package com.example.licenta.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.example.licenta.activity.diary.exercise.AddCustomExerciseActivity
import com.example.licenta.util.IntentConstants

class AddCustomExerciseContract : ActivityResultContract<Unit, Boolean>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(context, AddCustomExerciseActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                return intent.getBooleanExtra(IntentConstants.IS_EXERCISE_ADDED, false)
            }
        }
        return false
    }
}