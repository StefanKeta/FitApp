package com.example.licenta.adapter

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

class AddFoodAdapter(
    private val ctx: Context,
    private val onAddFoodItemClickListener: OnAddFoodItemClickListener,
    options: FirestoreRecyclerOptions<Food>,
) : FirestoreRecyclerAdapter<Food, AddFoodAdapter.AddFoodViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddFoodViewHolder {
        return AddFoodViewHolder(
            LayoutInflater
                .from(ctx)
                .inflate(
                    R.layout.item_holder_add_food,
                    parent,
                    false
                ),
            this.onAddFoodItemClickListener
        )

    }

    override fun onBindViewHolder(holder: AddFoodViewHolder, position: Int, food: Food) {
        holder.updateFields(food)
    }


    inner class AddFoodViewHolder(
        view: View,
        private val onAddFoodItemClickListener: OnAddFoodItemClickListener
    ) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private val foodName: TextView = view.findViewById(R.id.add_food_view_holder_food_name_tv)
        private val foodCalories: TextView =
            view.findViewById(R.id.add_food_view_holder_food_kcal_tv)

        init {
            view.setOnClickListener(this)
        }

        fun updateFields(food: Food) {
            foodName.text = food.name
            foodCalories.text = food.calories.toString()
        }

        override fun onClick(v: View?) {
            val food = snapshots[absoluteAdapterPosition]
            onAddFoodItemClickListener.onAdapterItemClick(food)
        }
    }

    interface OnAddFoodItemClickListener {
        fun onAdapterItemClick(food: Food)
    }

}