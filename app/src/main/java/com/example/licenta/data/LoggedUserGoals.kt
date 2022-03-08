package com.example.licenta.data

import com.example.licenta.model.user.Goals

object LoggedUserGoals {
    private var goals: Goals? = Goals()

    fun setGoals(goals: Goals?) {
        this.goals = goals
    }

    fun getGoals(): Goals {
        return goals!!
    }
}