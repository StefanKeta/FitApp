package com.example.licenta.adapter.food

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.licenta.R
import com.example.licenta.adapter.OnShareClickListener
import com.example.licenta.firebase.db.SelectedFoodDB
import com.example.licenta.model.food.FoodMeasureUnitEnum
import com.example.licenta.model.food.Meal
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class MealsAdapter(
    private val ctx: Context,
    options: FirestoreRecyclerOptions<Meal>,
    private val mealAdapterToFoodFragmentBridge: MealAdapterToFoodFragmentBridge,
    private val onShareClickListener: OnShareClickListener,
) : FirestoreRecyclerAdapter<Meal, MealsAdapter.MealsViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealsViewHolder {
        return MealsViewHolder(
            LayoutInflater.from(ctx)
                .inflate(R.layout.item_holder_meal, parent, false),
            mealAdapterToFoodFragmentBridge,
            onShareClickListener
        )
    }

    override fun onBindViewHolder(holder: MealsViewHolder, position: Int, meal: Meal) {
        return holder.update(meal)
    }

    inner class MealsViewHolder(
        view: View,
        private val mealAdapterToFoodFragmentBridge: MealAdapterToFoodFragmentBridge,
        private val onShareClickListener: OnShareClickListener
    ) :
        View.OnClickListener, RecyclerView.ViewHolder(view),
        FoodAdapter.OnSelectedFoodClickListener, FoodAdapter.OnSelectedFoodLongClickListener {
        private var mealNo: TextView = view.findViewById(R.id.meal_item_meal_no_tv)
        private var foodRV: RecyclerView = view.findViewById(R.id.meal_item_food_rv)
        private lateinit var foodAdapter: FoodAdapter
        private var addFoodBtn: Button = view.findViewById(R.id.meal_item_add_btn)
        private var shareFoodBtn: ImageView = view.findViewById(R.id.meal_item_button_share)
        private lateinit var mealId: String

        init {
            addFoodBtn.setOnClickListener(this)
            shareFoodBtn.setOnClickListener(this)
        }

        fun update(meal: Meal) {
            mealNo.text = "Meal ${meal.mealNumber}"
            this.mealId = meal.id
            setUpRecyclerView(meal)
        }

        private fun setUpRecyclerView(meal: Meal) {
            foodRV.itemAnimator = null
            foodRV.layoutManager = LinearLayoutManager(ctx)
            foodRV.hasFixedSize()
            val itemDecoration = DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL)
            foodRV.addItemDecoration(itemDecoration)
            foodAdapter = FoodAdapter(
                ctx,
                SelectedFoodDB.getSelectedFoodsInMealOption(meal.id),
                this,
                this
            )
            foodRV.adapter = foodAdapter
            mealAdapterToFoodFragmentBridge.addAdapter(foodAdapter)
        }

        override fun onClick(view: View?) {
            when (view!!.id) {
                R.id.meal_item_add_btn -> mealAdapterToFoodFragmentBridge.goToAddFoodActivity(mealId)
                R.id.meal_item_button_share -> onShareClickListener.onShare(mealId)
            }
        }

        override fun onSelectedFoodClick(id: String) {
            val view = LayoutInflater
                .from(ctx)
                .inflate(R.layout.dialog_edit_selected_food, null, false)
            val quantityTIL: TextInputLayout =
                view.findViewById(R.id.dialog_edit_selected_food_quantity_til)
            val quantityET: TextInputEditText =
                view.findViewById(R.id.dialog_edit_selected_food_quantity_et)
            val unitGroup: MaterialButtonToggleGroup =
                view.findViewById(R.id.dialog_edit_selected_food_quantity_tbg)
            val gBtn: Button = view.findViewById(R.id.dialog_edit_selected_food_g_rb)

            AlertDialog
                .Builder(ctx)
                .setView(view)
                .setTitle("Edit selected food")
                .setIcon(R.drawable.ic_baseline_edit_24)
                .setPositiveButton("Edit") { dialog, _ ->
                    if (quantityET.text.isNullOrEmpty()) {
                        quantityTIL.error = "Please set the quantity"
                    } else {
                        val quantity = quantityET.text.toString().trim().toDouble() / 100.0
                        val unit =
                            if (unitGroup.checkedButtonId == gBtn.id) FoodMeasureUnitEnum.GRAM else FoodMeasureUnitEnum.OZ
                        SelectedFoodDB
                            .updateSelectedFood(id, quantity, unit) { isEdited ->
                                if (isEdited) {
                                    mealAdapterToFoodFragmentBridge.updateMacros()
                                    dialog.dismiss()
                                } else {
                                    dialog.dismiss()
                                }
                            }
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        override fun onSelectedFoodLongClick(id: String): Boolean {
            AlertDialog
                .Builder(ctx)
                .setTitle("Are you sure you want to delete selected food?")
                .setIcon(R.drawable.ic_baseline_delete_24)
                .setPositiveButton("Yes") { dialog, _ ->
                    SelectedFoodDB.removeSelectedFood(id) { isRemoved ->
                        if (isRemoved) {
                            dialog.dismiss()
                            mealAdapterToFoodFragmentBridge.updateMacros()
                        } else {
                            dialog.dismiss()
                            Toast.makeText(
                                ctx,
                                "Could not remove food!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
            return true
        }
    }

    interface MealAdapterToFoodFragmentBridge {
        fun goToAddFoodActivity(mealId: String)
        fun updateMacros()
        fun addAdapter(foodAdapter: FoodAdapter)
    }
}