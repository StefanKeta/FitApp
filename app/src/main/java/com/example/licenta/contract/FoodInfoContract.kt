package com.example.licenta.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import com.example.licenta.activity.diary.food.FoodInfoActivity
import com.example.licenta.util.IntentConstants

class FoodInfoContract : ActivityResultContract<Bundle, Boolean>() {
    override fun createIntent(context: Context, input: Bundle): Intent {
        return Intent(context, FoodInfoActivity::class.java).apply {
            this.putExtra(IntentConstants.BUNDLE, input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        if (resultCode == Activity.RESULT_OK && intent != null) {
            return intent.getBooleanExtra(IntentConstants.IS_SELECTED_FOOD_SAVED, false)
        }
        return false
    }
}