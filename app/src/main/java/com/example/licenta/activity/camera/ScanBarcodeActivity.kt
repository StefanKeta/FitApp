package com.example.licenta.activity.camera

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.licenta.R
import com.example.licenta.activity.diary.AddFoodActivity
import com.example.licenta.firebase.db.FoodDB
import com.example.licenta.model.food.Food
import com.example.licenta.util.IntentConstants
import com.example.licenta.util.PermissionsChecker
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.android.synthetic.main.activity_scan_barcode.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanBarcodeActivity : AppCompatActivity() {

    private lateinit var executorService: ExecutorService
    private lateinit var previewView: PreviewView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_barcode)
        initComponents()
    }

    private fun initComponents() {
        /*
        previewView este suportul pentru cameră, care va scana codul de bare
        executorService va executa scanarea pe un thread diferit, pentru a nu bloca
        aplicația
         */
        previewView = findViewById(R.id.activity_barcode_scanner_camera_preview)
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
                setUpCamera()
            }
        }
    }

    private fun setUpCamera() {
        val options = BarcodeScannerOptions.Builder().setBarcodeFormats(
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39,
            Barcode.FORMAT_CODE_93,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_PDF417,
            Barcode.FORMAT_AZTEC
        ).build()
        val scanner = BarcodeScanning.getClient(options)
        val imageAnalysis = ImageAnalysis.Builder()
            .build()

        imageAnalysis.setAnalyzer(
            executorService,
            { imageProxy ->
                processImageProxy(scanner, imageProxy)
            }
        )
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            //configurarea camerei
            val cameraProvider = cameraProviderFuture.get() //Future-ul este așteptat
            val previewUseCase = Preview.Builder()
                .build()
                .also {
                    /*
                    setează provider-ul de suprafață (activitate) pe care se
                    găsește previewView-ul, în cazul nostru ScanBarcodeActivity
                     */
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA //selectam camera din spate
            try {
                //Încercăm să pornim camera, împreună cu analizatorul de imagini
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, previewUseCase, imageAnalysis
                )
            } catch (illegalStateException: IllegalStateException) {
                Log.e("illegalException", illegalStateException.message.orEmpty())
            } catch (illegalArgumentException: IllegalArgumentException) {
                Log.e("illegalException", illegalArgumentException.message.orEmpty())
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun scanningCallback(barcode: String) {
        FoodDB.foodExists(barcode) { exists: Boolean, id: String ->
            val intent = Intent(this@ScanBarcodeActivity, AddFoodActivity::class.java)
            val bundle = Bundle()
            if (exists) {
                bundle.putString(Food.ID, id)
                bundle.putBoolean(IntentConstants.EXISTS, true)
            } else {
                bundle.putString(Food.BARCODE, barcode)
                bundle.putBoolean(IntentConstants.EXISTS, false)
            }
            intent.putExtra(IntentConstants.BUNDLE, bundle)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(
        barcodeScanner: BarcodeScanner,
        imageProxy: ImageProxy
    ) {
        if (imageProxy.image != null) {
            imageProxy.image?.let { image ->
                val inputImage =
                    InputImage.fromMediaImage(
                        image,
                        imageProxy.imageInfo.rotationDegrees
                    ) //ajustarea corectă a imaginii

                barcodeScanner.process(inputImage)
                    .addOnSuccessListener { barcodeList ->
                        if (barcodeList.size == 1) {
                            val barcodeRawValue = barcodeList[0].rawValue ?: ""
                            scanningCallback(barcodeRawValue)
                            imageProxy.image?.close()
                            imageProxy.close()
                            executorService.shutdown()
                        }
                    }
                    .addOnFailureListener {
                        it.printStackTrace()
                    }
                    .addOnCompleteListener {
                        imageProxy.image?.close()
                        imageProxy.close()
                    }
            }
        }
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