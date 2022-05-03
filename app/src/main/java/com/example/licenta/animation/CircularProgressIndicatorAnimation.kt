package com.example.licenta.animation

import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.TextView
import com.google.android.material.progressindicator.CircularProgressIndicator

class CircularProgressIndicatorAnimation(
    private val progressIndicator: CircularProgressIndicator,
    private val progressTextView: TextView,
    private val from: Float,
    private val to: Float
) : Animation() {
    override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
        super.applyTransformation(interpolatedTime, t)
        val progress = (from - (to - from) * interpolatedTime).toInt()
        progressIndicator.progress = progress * (-1)
        val finalProgress = progressIndicator.max + progress
        progressTextView.text = finalProgress.toString()
    }
}