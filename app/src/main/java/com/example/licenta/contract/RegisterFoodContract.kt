package com.example.licenta.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.example.licenta.activity.diary.RegisterFoodToDbActivity
import com.example.licenta.model.food.Food
import com.example.licenta.util.IntentConstants

class RegisterFoodContract : ActivityResultContract<String, Boolean>() {
    override fun createIntent(context: Context, input: String): Intent {
        return Intent(context, RegisterFoodToDbActivity::class.java).apply {
            this.putExtra(Food.BARCODE, input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        if (resultCode == Activity.RESULT_OK && intent != null) {
            return intent.getBooleanExtra(IntentConstants.IS_FOOD_SAVED, false)
        }
        return false
    }
}