package com.example.licenta.activity.diary.food

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.licenta.R
import com.example.licenta.activity.MainActivity
import com.example.licenta.activity.auth.LoginActivity
import com.example.licenta.adapter.food.AddFoodAdapter
import com.example.licenta.contract.BarcodeScannerContract
import com.example.licenta.contract.FoodInfoContract
import com.example.licenta.contract.RegisterFoodContract
import com.example.licenta.firebase.db.FoodDB
import com.example.licenta.model.food.Food
import com.example.licenta.model.food.SelectedFood
import com.example.licenta.util.IntentConstants
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import java.lang.RuntimeException

class AddFoodActivity : AppCompatActivity(), View.OnClickListener,
    AddFoodAdapter.OnAddFoodItemClickListener {
    private lateinit var searchFoodLayout: TextInputLayout
    private lateinit var searchFoodET: TextInputEditText
    private lateinit var barcodeBtn: Button
    private lateinit var addCustomFoodBtn: Button
    private lateinit var foodRV: RecyclerView
    private lateinit var adapter: AddFoodAdapter
    private lateinit var barcodeScannerActivityResult: ActivityResultLauncher<Unit>
    private lateinit var foodInfoActivityResult: ActivityResultLauncher<Bundle>
    private lateinit var registerFoodActivityResult: ActivityResultLauncher<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_food)
        initComponents()
        initContracts()
    }

    private fun initComponents() {
        searchFoodLayout = findViewById(R.id.activity_add_food_search_for_food_til)
        searchFoodET = findViewById(R.id.activity_add_food_search_for_food_et)
        barcodeBtn = findViewById(R.id.activity_add_food_barcode_btn)
        barcodeBtn.setOnClickListener(this)
        addCustomFoodBtn = findViewById(R.id.activity_add_food_add_btn)
        addCustomFoodBtn.setOnClickListener(this)
        searchFoodET.addTextChangedListener(setTextWatcher())
        foodRV = findViewById(R.id.activity_add_food_rv)
        setInitialFoods()
    }

    private fun initContracts() {
        registerFoodActivityResult =
            registerForActivityResult(
                RegisterFoodContract()
            ) { isAdded ->
                if (isAdded)
                    setInitialFoods()
            }

        foodInfoActivityResult = registerForActivityResult(
            FoodInfoContract()
        ) { isAdded ->
            handleResponseFromFoodInfoActivity(isAdded)
        }

        barcodeScannerActivityResult = registerForActivityResult(
            BarcodeScannerContract()
        ) { extras ->
            if (extras != null)
                handleResponseFromCamera(extras)
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.activity_add_food_barcode_btn -> openCameraToScan()
            R.id.activity_add_food_add_btn -> launchRegisterFoodActivity()
        }
    }

    override fun onAdapterItemClick(food: Food) {
        val bundle = intent.getBundleExtra(IntentConstants.BUNDLE)
        if (bundle != null) {
            val mealId = bundle.getString(SelectedFood.MEAL_ID)
            val date = bundle.getString(SelectedFood.DATE_SELECTED)
            launchFoodInfoActivity(food.id, mealId!!, date!!)
        } else {
            throw RuntimeException("The data about meal is not available!")
        }
    }

    private fun setTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    val searchQuery = s.toString()
                    displayFoods(searchQuery)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    val searchQuery = s.toString()
                    displayFoods(searchQuery)
                }
            }
        }
    }

    private fun setInitialFoods(limit: Long = 20) {
        setUpRecyclerView(limit)
    }

    private fun openCameraToScan() {
        barcodeScannerActivityResult.launch(Unit)
    }

    private fun displayFoods(nameToMatch: String) {
        val options = FoodDB.searchForFoodByNamePrefix(nameToMatch)
        adapter.updateOptions(options)
        adapter.notifyDataSetChanged()
    }

    private fun handleResponseFromCamera(extras: Bundle) {
        val foodExists = extras.getBoolean(IntentConstants.EXISTS)
        if (foodExists) {
            if (extras.getString(Food.ID) != null) {
                val bundle = intent.getBundleExtra(IntentConstants.BUNDLE)
                if (bundle != null) {
                    val id = extras.getString(Food.ID)
                    val dateSelected = bundle.getString(SelectedFood.DATE_SELECTED)
                    val mealId = bundle.getString(SelectedFood.MEAL_ID)
                    launchFoodInfoActivity(id!!, mealId!!, dateSelected!!)
                }
            }
        } else {
            if (extras.getString(Food.BARCODE) != null) {
                val foodBarcode = extras.getString(Food.BARCODE)
                launchRegisterFoodActivity(foodBarcode!!)
            }
        }
    }

    private fun handleResponseFromFoodInfoActivity(isSaved: Boolean) {
        val intent = Intent(this@AddFoodActivity, MainActivity::class.java)
        if (isSaved) {
            intent.putExtra(IntentConstants.IS_SELECTED_FOOD_SAVED, true)
            setResult(RESULT_OK, intent)
            finish()
        } else {
            intent.putExtra(IntentConstants.IS_SELECTED_FOOD_SAVED, false)
        }
    }

    private fun setUpRecyclerView(limit: Long) {
        val options = FoodDB.initialAddFoodAdapterListOptions(limit)
        adapter = AddFoodAdapter(this@AddFoodActivity, this, options)
        foodRV.layoutManager = LinearLayoutManager(this)
        foodRV.itemAnimator = null
        foodRV.adapter = adapter
    }

    private fun launchFoodInfoActivity(foodID: String, mealId: String, date: String) {
        val bundle = Bundle()
        bundle.putString(Food.ID, foodID)
        bundle.putString(SelectedFood.DATE_SELECTED, date)
        bundle.putString(SelectedFood.MEAL_ID, mealId)
        foodInfoActivityResult.launch(bundle)
    }

    private fun launchRegisterFoodActivity(barcode: String = "") {
        registerFoodActivityResult.launch(barcode)
    }


    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser == null)
            startActivity(Intent(this@AddFoodActivity, LoginActivity::class.java))
        adapter.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.stopListening()
    }
}
