package com.example.licenta.contract

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import com.example.licenta.activity.diary.AddFoodActivity
import com.example.licenta.model.food.SelectedFood
import com.example.licenta.util.IntentConstants

class AddFoodForUserContract : ActivityResultContract<Bundle, Boolean>() {
    override fun createIntent(context: Context, input: Bundle): Intent {
        return Intent(context, AddFoodActivity::class.java)
            .also { intent ->
                intent.putExtra(IntentConstants.BUNDLE, input)
            }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        if (resultCode == RESULT_OK) {
            if (intent != null) {
                return intent.getBooleanExtra(IntentConstants.IS_SELECTED_FOOD_SAVED, false)
            }
        }
        return false
    }
}