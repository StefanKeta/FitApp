package com.example.licenta.activity.camera

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Size
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.licenta.R
import com.example.licenta.util.BarcodeAnalyzer
import com.example.licenta.util.PermissionsChecker
import com.google.android.gms.tasks.OnSuccessListener
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.annotation.Nullable

class ScanBarcodeActivity : AppCompatActivity() {

    private lateinit var executorService: ExecutorService
    private lateinit var previewView: PreviewView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_barcode)
        initComponents()
    }

    private fun initComponents() {
        previewView = findViewById(R.id.activity_barcode_scanner_camera_preview)
        this.window.setFlags(1024, 1024)
        executorService = Executors.newSingleThreadExecutor()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionsChecker.CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(
                    this@ScanBarcodeActivity,
                    "Please accept camera permissions",
                    Toast.LENGTH_SHORT
                )
                    .show()
                PermissionsChecker.askForCameraPermission(this)
            } else {
                Toast.makeText(this@ScanBarcodeActivity, "Permission Granted!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun bindImage(provider: ProcessCameraProvider) {
        val preview = Preview
            .Builder()
            .build()

    }

    private fun setUpCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this@ScanBarcodeActivity)

        providerFuture.addListener({
            val provider = providerFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val imageAnalysis = ImageAnalysis
                .Builder()
                .setTargetResolution(Size(1200,720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(executorService, BarcodeAnalyzer { barcode ->
                        scanningCallback(barcode)
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun scanningCallback(barcode: String) {
        //
    }

    override fun onStart() {
        super.onStart()
        if (!PermissionsChecker.isCameraPermissionAccepted(this)) {
            PermissionsChecker.askForCameraPermission(this)
        } else {
            setUpCamera()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executorService.shutdown()
    }

}