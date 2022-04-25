package com.example.licenta.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

object PermissionsChecker {
    const val CAMERA_REQUEST_CODE = 100
    private const val STORAGE_REQUEST_CODE = 200
    private const val PEDOMETER_REQUEST_CODE = 300

    fun isCameraPermissionAccepted(activity: Activity): Boolean {
        return ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isStoragePermissionAccepted(activity: Activity): Boolean {
        return ActivityCompat
            .checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun isPedometerPermissionAccepted(activity: Activity):Boolean{
        return ActivityCompat
            .checkSelfPermission(
                activity,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
    }

    fun isCameraAndStoragePermissionAccepted(activity: Activity): Boolean {
        return isCameraPermissionAccepted(activity) && isStoragePermissionAccepted(activity)
    }

    fun askForCameraPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity, arrayOf(Manifest.permission.CAMERA),
            CAMERA_REQUEST_CODE
        )
    }

    fun askForStoragePermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_REQUEST_CODE
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun askForPedometerPermission(activity: Activity){
        ActivityCompat.requestPermissions(
            activity, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
            PEDOMETER_REQUEST_CODE
        )
    }
}