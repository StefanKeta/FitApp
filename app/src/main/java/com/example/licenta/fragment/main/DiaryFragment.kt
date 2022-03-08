package com.example.licenta.fragment.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import com.example.licenta.R
import com.example.licenta.fragment.main.diary.ExerciseFragment
import com.example.licenta.fragment.main.diary.FoodFragment
import com.example.licenta.util.Date
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DiaryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DiaryFragment(private val selectedFragment: String = FOOD_FRAGMENT_CODE) : Fragment(),
    View.OnClickListener {
    private lateinit var tabLayout: TabLayout
    private lateinit var fragmentLayout: FrameLayout
    private lateinit var dateBtn: Button
    private lateinit var previousDayBtn: Button
    private lateinit var nextDayBtn: Button
    private lateinit var onDateChangedListener: OnDateChangedListener
    private var date = Date.setCurrentDay()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_diary, container, false)
        initComponents(view)
        return view
    }

    private fun initComponents(view: View) {
        tabLayout = view.findViewById(R.id.fragment_diary_tab_layout)
        fragmentLayout = view.findViewById(R.id.fragment_diary_fragment_frame_layout)
        switchFragment(FoodFragment())
        if (selectedFragment == EXERCISE_FRAGMENT_CODE) {
            tabLayout.getTabAt(1)!!.select()
            switchFragment(ExerciseFragment())
        } else {
            tabLayout.getTabAt(0)!!.select()
            switchFragment(FoodFragment())
        }
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab!!.position == 0) switchFragment(FoodFragment(date))
                else switchFragment(ExerciseFragment(date))
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                //
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                //
            }
        })
        dateBtn = view.findViewById(R.id.fragment_diary_pick_date_btn)
        dateBtn.setOnClickListener(this)
        nextDayBtn = view.findViewById(R.id.fragment_diary_next_navigation_btn)
        nextDayBtn.setOnClickListener(this)
        previousDayBtn = view.findViewById(R.id.fragment_diary_back_navigation_btn)
        previousDayBtn.setOnClickListener(this)
        dateBtn.text = Date.setCurrentDay()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.fragment_diary_pick_date_btn -> openDatePicker()
            R.id.fragment_diary_next_navigation_btn -> {
                val dayAfter = Date.goToDayAfter(dateBtn.text.toString())
                dateBtn.text = dayAfter
                date = dayAfter
                onDateChangedListener.changeDate(dayAfter)
            }
            R.id.fragment_diary_back_navigation_btn -> {
                val dayBefore = Date.goToDayBefore(dateBtn.text.toString())
                dateBtn.text = dayBefore
                date = dayBefore
                onDateChangedListener.changeDate(dayBefore)
            }
        }
    }

    private fun switchFragment(fragment: Fragment) {
        val fragmentManager = childFragmentManager
        onDateChangedListener = if (tabLayout.selectedTabPosition == 0) fragment as FoodFragment
        else fragment as ExerciseFragment
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_diary_fragment_frame_layout, fragment)
        fragmentTransaction.commit()
    }

    private fun openDatePicker() {
        val picker = MaterialDatePicker
            .Builder
            .datePicker()
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setTitleText("Please select a date")
            .build()

        picker.addOnNegativeButtonClickListener {
            picker.dismiss()
        }
        picker.addOnPositiveButtonClickListener {
            val selection = picker.selection!!
            val date = Date.getDateFromTimestamp(selection)
            dateBtn.text = date
            picker.dismiss()
        }

        picker.show(childFragmentManager, null)
    }

    companion object {
        const val FOOD_FRAGMENT_CODE: String = "FoodFragment"
        const val EXERCISE_FRAGMENT_CODE: String = "ExerciseFragment"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FoodFragment().apply {
                arguments = Bundle().apply {
                    putString(com.example.licenta.fragment.main.ARG_PARAM1, param1)
                    putString(com.example.licenta.fragment.main.ARG_PARAM2, param2)
                }
            }
    }
}


interface OnDateChangedListener {
    fun changeDate(date: String)
}