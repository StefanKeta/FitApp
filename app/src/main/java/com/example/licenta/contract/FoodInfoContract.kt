package com.example.licenta.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import com.example.licenta.activity.camera.ScanBarcodeActivity
import com.example.licenta.activity.diary.FoodInfoActivity
import com.example.licenta.model.food.Food
import com.example.licenta.util.IntentConstants

class FoodInfoContract : ActivityResultContract<String, Boolean>() {
    override fun createIntent(context: Context, input:String): Intent {
        val intent = Intent(context, FoodInfoActivity::class.java)
        intent.putExtra(Food.ID, input)
        return Intent(context, FoodInfoActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        if(resultCode == Activity.RESULT_OK && intent != null){
            return intent.getBooleanExtra(IntentConstants.IS_SELECTED_FOOD_SAVED,false)
        }
        return false
    }
}