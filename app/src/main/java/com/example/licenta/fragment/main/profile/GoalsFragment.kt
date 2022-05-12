package com.example.licenta.fragment.main.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.licenta.R
import com.example.licenta.activity.SetGoalsActivity
import com.example.licenta.data.LoggedUserData
import com.example.licenta.data.LoggedUserGoals
import com.example.licenta.firebase.db.UsersDB
import com.example.licenta.util.IntentConstants
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GoalsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GoalsFragment : Fragment(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var heightTV: TextView
    private lateinit var weightTV: TextView
    private lateinit var proteinTV: TextView
    private lateinit var carbsTV: TextView
    private lateinit var fatTV: TextView
    private lateinit var caloriesTV: TextView
    private lateinit var weightET: TextInputEditText
    private lateinit var updateBtn: Button
    private lateinit var weightTBG: MaterialButtonToggleGroup
    private lateinit var kgRB: Button
    private lateinit var lbsRB: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_goals, container, false)
        initComponents(view)
        return view
    }

    private fun initComponents(view: View) {
        heightTV = view.findViewById(R.id.fragment_goals_height_tv)
        weightTV = view.findViewById(R.id.fragment_goals_weight_tv)
        proteinTV = view.findViewById(R.id.fragment_goals_protein_tv)
        carbsTV = view.findViewById(R.id.fragment_goals_carbs_tv)
        fatTV = view.findViewById(R.id.fragment_goals_fat_tv)
        caloriesTV = view.findViewById(R.id.fragment_goals_calories_tv)
        weightET = view.findViewById(R.id.fragment_goals_new_weight_et)
        updateBtn = view.findViewById(R.id.fragment_goals_update_weight_btn)
        updateBtn.setOnClickListener(this)
        fillTextViews()
    }

    override fun onClick(view: View?) {
        if (view!!.id == R.id.fragment_goals_update_weight_btn) {
            val intent = Intent(context!!, SetGoalsActivity::class.java)
                .also { it.putExtra(IntentConstants.EXISTS, true) }
            if (!weightET.text.isNullOrEmpty()) {
                val newWeight = weightET.text.toString().trim().toInt()
                UsersDB.updateWeight(newWeight) { user ->
                    if (user != null) {
                        LoggedUserData.setLoggedUser(user)
                        startActivity(intent)
                    }
                }
            } else {
                startActivity(intent)
            }
        }
    }

    private fun fillTextViews() {
        heightTV.text = "${LoggedUserData.getLoggedUser().height}cm"
        weightTV.text = "${LoggedUserData.getLoggedUser().weight}kg"
        proteinTV.text = LoggedUserGoals.getGoals().protein.toString()
        carbsTV.text = LoggedUserGoals.getGoals().carbs.toString()
        fatTV.text = LoggedUserGoals.getGoals().fat.toString()
        caloriesTV.text = LoggedUserGoals.getGoals().calories.toString()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GoalsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GoalsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}