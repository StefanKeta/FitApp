package com.example.licenta.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.licenta.R
import com.example.licenta.model.food.Food
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class MealsFoodAdapter(
    private val ctx: Context,
    private var selectedFood: List<Food>,
    options: FirestoreRecyclerOptions<Food>
) : FirestoreRecyclerAdapter<Food, MealsFoodAdapter.MealsFoodViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealsFoodViewHolder {
        return MealsFoodViewHolder(
            LayoutInflater
                .from(ctx)
                .inflate(R.layout.item_holder_meal_food, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MealsFoodViewHolder, position: Int, food: Food) {
        holder.update(food)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeFoodList(newFoodsToDisplay: List<Food>) {
        this.selectedFood = newFoodsToDisplay
        notifyDataSetChanged()
    }

    class MealsFoodViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {
        private val foodNameTV: TextView =
            view.findViewById(R.id.meal_food_item_holder_food_name_tv)
        private val foodQuantityTV: TextView =
            view.findViewById(R.id.meal_food_item_holder_quantity_tv)
        private val foodMeasureUnit: TextView =
            view.findViewById(R.id.meal_food_item_holder_weight_unit_tv)
        private val foodCaloriesTV: TextView = view.findViewById(R.id.meal_food_item_holder_kcal_tv)

        fun update(food: Food?) {
            if (food != null) {
                foodNameTV.text = food.name
                foodQuantityTV.text = "100g"
            }
        }
    }
}