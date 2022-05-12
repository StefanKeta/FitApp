package com.example.licenta.fragment.main.diary

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.licenta.R
import com.example.licenta.adapter.OnShareClickListener
import com.example.licenta.adapter.food.FoodAdapter
import com.example.licenta.adapter.food.MealsAdapter
import com.example.licenta.animation.CircularProgressIndicatorAnimation
import com.example.licenta.animation.LinearProgressIndicatorAnimation
import com.example.licenta.contract.AddFoodForUserContract
import com.example.licenta.data.LoggedUserData
import com.example.licenta.data.LoggedUserGoals
import com.example.licenta.firebase.db.FoodDB
import com.example.licenta.firebase.db.MealsDB
import com.example.licenta.firebase.db.SelectedFoodDB
import com.example.licenta.fragment.main.OnDateChangedListener
import com.example.licenta.model.food.Food
import com.example.licenta.model.food.Meal
import com.example.licenta.model.food.SelectedFood
import com.example.licenta.util.Date
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FoodFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FoodFragment(private var date: String = Date.getCurrentDate()) : Fragment(),
    View.OnClickListener, OnDateChangedListener, MealsAdapter.MealAdapterToFoodFragmentBridge,
    OnShareClickListener {
    private lateinit var remainingProteinTV: TextView
    private lateinit var proteinPB: LinearProgressIndicator
    private lateinit var remainingCarbsTV: TextView
    private lateinit var carbsPB: LinearProgressIndicator
    private lateinit var remainingFatTV: TextView
    private lateinit var fatPB: LinearProgressIndicator
    private lateinit var remainingCaloriesTV: TextView
    private lateinit var caloriesPB: CircularProgressIndicator
    private lateinit var addFoodForUserContract: ActivityResultLauncher<Bundle>

    private lateinit var mealsRV: RecyclerView
    private lateinit var mealsAdapter: MealsAdapter
    private var foodAdapters: MutableList<FoodAdapter> = emptyList<FoodAdapter>().toMutableList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_food, container, false)
        initComponents(view)
        return view
    }

    private fun initComponents(view: View) {
        setUpProgressBars(view)
        setUpRecyclerView(view)
        addFoodForUserContract = registerForActivityResult(AddFoodForUserContract()) { isSaved ->
            if (isSaved) {
                updateMacros()
            }
        }
    }

    override fun onClick(v: View?) {
//        when (v!!.id) {
//         //   R.id.fragment_diary_food_add_food_btn -> goToAddFoodActivity()
//        }
    }

    override fun changeDate(date: String) {
        this.date = date
        foodAdapters.forEach { it.stopListening() }
        foodAdapters.clear()
        checkIfMealsAreGeneratedAndSaveEmpty()
    }

    override fun goToAddFoodActivity(mealId: String) {
        Bundle().also { bundle ->
            bundle.putString(SelectedFood.MEAL_ID, mealId)
            bundle.putString(SelectedFood.DATE_SELECTED, date)
            addFoodForUserContract.launch(bundle)
        }
    }

    override fun updateMacros() {
        SelectedFoodDB.getUserSelectedFoodByDate(
            date,
            ::updateMacrosAndCalories
        )
    }

    override fun addAdapter(foodAdapter: FoodAdapter) {
        foodAdapter.startListening()
        foodAdapters.add(foodAdapter)
    }

    override fun onShare(id: String) {
        constructSharingMessage(id) { message ->
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "text/plain"
            startActivity(
                intent.putExtra(Intent.EXTRA_TEXT, message)
            )
        }
    }

    private fun checkIfMealsAreGeneratedAndSaveEmpty() {
        MealsDB.getMealsFromDate(date) { meals ->
            if (meals.isEmpty()) {
                MealsDB.saveEmptyMeals(generateEmptyMeals()) {
                    refreshMeals()
                    updateMacros()
                }
            } else {
                refreshMeals()
                updateMacros()
            }
        }
    }

    private fun generateEmptyMeals(): MutableList<Meal> {
        return mutableListOf(
            generateMeal(1),
            generateMeal(2),
            generateMeal(3)
        )
    }

    private fun generateMeal(mealNo: Int): Meal {
        return Meal(
            UUID.randomUUID().toString(),
            LoggedUserData.getLoggedUser().uuid,
            mealNo,
            date
        )
    }

    private fun setUpProgressBars(view: View) {
        remainingProteinTV = view.findViewById(R.id.fragment_diary_food_protein_remaining_tv)
        proteinPB = view.findViewById(R.id.fragment_diary_food_progress_bar_protein)
        proteinPB.max = LoggedUserGoals.getGoals().protein
        remainingCarbsTV = view.findViewById(R.id.fragment_diary_food_carbs_remaining_tv)
        carbsPB = view.findViewById(R.id.fragment_diary_food_progress_bar_carbs)
        carbsPB.max = LoggedUserGoals.getGoals().carbs
        remainingFatTV = view.findViewById(R.id.fragment_diary_food_fat_remaining_tv)
        fatPB = view.findViewById(R.id.fragment_diary_food_progress_bar_fat)
        fatPB.max = LoggedUserGoals.getGoals().fat
        remainingCaloriesTV = view.findViewById(R.id.fragment_diary_food_calories_remaining_tv)
        caloriesPB = view.findViewById(R.id.fragment_diary_food_calories_remaining_pb)
        caloriesPB.max = LoggedUserGoals.getGoals().calories
        SelectedFoodDB.getUserSelectedFoodByDate(
            date,
            ::updateMacrosAndCalories
        )
    }

    private fun setUpRecyclerView(view: View) {
        mealsRV = view.findViewById(R.id.fragment_diary_meals_rv)
        mealsRV.layoutManager = LinearLayoutManager(context)
        mealsRV.hasFixedSize()
        mealsRV.itemAnimator = null
        mealsAdapter =
            MealsAdapter(requireContext(), MealsDB.getMealsByDateOptions(date), this, this)
        mealsRV.adapter = mealsAdapter
        checkIfMealsAreGeneratedAndSaveEmpty()
    }

    private fun refreshMeals() {
        mealsAdapter.updateOptions(MealsDB.getMealsByDateOptions(date))
        mealsRV.scrollToPosition(0)
    }

    private fun updateMacrosAndCalories(selectedFoods: List<SelectedFood>) {
        proteinPB.progress = 0
        carbsPB.progress = 0
        fatPB.progress = 0
        caloriesPB.progress = 0
        remainingProteinTV.text = proteinPB.max.toString()
        remainingCarbsTV.text = carbsPB.max.toString()
        remainingFatTV.text = fatPB.max.toString()
        remainingCaloriesTV.text = caloriesPB.max.toString()
        var proteinProgress = 0
        var carbsProgress = 0
        var fatProgress = 0
        var calorieProgress = 0
        selectedFoods.forEach { selectedFood ->
            FoodDB.getFoodById(selectedFood.foodId) { food ->
                val quantity = selectedFood.quantity
                proteinProgress += (food.protein * quantity).toInt()
                carbsProgress += (food.carbs * quantity).toInt()
                fatProgress += (food.fat * quantity).toInt()
                calorieProgress += (food.calories * quantity).toInt()
                val caloriesIndicatorAnimation = CircularProgressIndicatorAnimation(
                    caloriesPB,
                    remainingCaloriesTV,
                    0f,
                    calorieProgress.toFloat()
                )
                caloriesIndicatorAnimation.duration = 1000
                caloriesPB.startAnimation(caloriesIndicatorAnimation)
                animateLinearProgressBars(proteinPB, remainingProteinTV, proteinProgress)
                animateLinearProgressBars(carbsPB, remainingCarbsTV, carbsProgress)
                animateLinearProgressBars(fatPB, remainingFatTV, fatProgress)
            }
        }
    }

    private fun animateLinearProgressBars(
        linearProgressIndicator: LinearProgressIndicator,
        progressIndicatorTextView: TextView,
        currentProgress: Int
    ) {
        val indicatorAnimation =
            LinearProgressIndicatorAnimation(
                linearProgressIndicator,
                progressIndicatorTextView,
                0f,
                currentProgress.toFloat()
            )
        indicatorAnimation.duration = 1000
        linearProgressIndicator.startAnimation(indicatorAnimation)
    }

    private fun constructSharingMessage(mealId: String, callback: (String) -> Unit) {
        SelectedFoodDB.getSelectedFoodsInMeal(mealId) { selectedFoods ->
            val allFoods = mutableListOf<Food>()
            var foodsLength = 0
            selectedFoods.forEach { selectedFood ->
                FoodDB.getFoodById(selectedFood.foodId) { food ->
                    foodsLength += 1
                    allFoods.add(
                        Food(
                            food.id,
                            food.name,
                            food.barcode,
                            (food.calories * selectedFood.quantity).toInt(),
                            (food.protein * selectedFood.quantity).toInt(),
                            (food.carbs * selectedFood.quantity).toInt(),
                            (food.fat * selectedFood.quantity).toInt()
                        )
                    )
                    val totalProtein = allFoods.sumOf { it.protein }
                    val totalCarbs = allFoods.sumOf { it.carbs }
                    val totalFat = allFoods.sumOf { it.fat }
                    val totalCalories = allFoods.sumOf { it.calories }
                    val concatenatedNames = allFoods.map { it.name }

                    if (foodsLength == selectedFoods.size) {
                        callback(
                            "For a meal on $date, I had: \n${
                                concatenatedNames.joinToString("\n")
                            }.\nMeal macros are: \n Protein: $totalProtein \n Carbs:$totalCarbs \n Fat: $totalFat \n Calories: $totalCalories"
                        )
                    }
                }
            }
        }
    }


    override fun onStart() {
        super.onStart()
        mealsAdapter.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        mealsAdapter.stopListening()
        foodAdapters.forEach { it.startListening() }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FoodFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FoodFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}