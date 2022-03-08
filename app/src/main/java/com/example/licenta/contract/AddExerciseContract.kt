package com.example.licenta.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.example.licenta.activity.diary.exercise.AddExerciseActivity
import com.example.licenta.model.exercise.WeightExerciseRecord
import com.example.licenta.util.IntentConstants

class AddExerciseContract : ActivityResultContract<String, Boolean>() {
    override fun createIntent(context: Context, input: String): Intent {
        return Intent(context, AddExerciseActivity::class.java)
            .also { it.putExtra(WeightExerciseRecord.DATE, input) }
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