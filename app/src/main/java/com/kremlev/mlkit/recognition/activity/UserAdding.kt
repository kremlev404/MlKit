package com.kremlev.mlkit.recognition.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.kremlev.mlkit.R
import com.kremlev.mlkit.recognition.Analyzer.UserAddingAnalyzer
import kotlinx.android.synthetic.main.activity_user_adding.*
import kotlinx.android.synthetic.main.dialog_add_user.*
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class UserAdding : AppCompatActivity() {
    private lateinit var cancel_button: Button
    private lateinit var make_photo_button: Button
    private lateinit var photoExecutor: ExecutorService
    private lateinit var remove_user_button: Button
    private lateinit var preferences: SharedPreferences
    private var username = ""
    private var photo_index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_adding)

        preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        //init views
        make_photo_button = findViewById(R.id.make_photo_button)
        remove_user_button = findViewById(R.id.remove_user_button)
        cancel_button = findViewById(R.id.cancel_button)

        remove_user_button.setOnClickListener {
            removeUser()
        }

        cancel_button.setOnClickListener {
            Toast.makeText(
                    this,
                    "COMPLETING USER SETUP, RETURN TO HOME PAGE",
                    Toast.LENGTH_LONG
            ).show()

            try {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } catch (e: java.lang.Exception) {
                Log.e("UserAdding", "Backing to home error")
                Toast.makeText(
                        this,
                        "ERROR $e",
                        Toast.LENGTH_SHORT
                ).show()
            }
        }

        startCamera()

        //exec init
        photoExecutor = Executors.newSingleThreadExecutor()
    }

    private fun showEditTextDialog() {
        tv_add_username.setOnClickListener() {
            // init dialog
            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.dialog_add_user, null)
            val builder = AlertDialog
                    .Builder(this)
                    .setView(dialogLayout)
            val dialog_add_editText = dialogLayout.findViewById<EditText>(R.id.dialog_add_editText)

            val mAlertDialog = builder.show()

            mAlertDialog.dialog_add_user_btn_accept.setOnClickListener {
                tv_add_username.text =
                        dialog_add_editText.text.toString().toLowerCase().capitalize()

                username = dialog_add_editText.text.toString().toLowerCase().capitalize()

                Log.e("photo_index", "$photo_index")

                countUserPhotos()

                //set data to overlay
                userDrawAdding.number_of_this_user_photo = photo_index

                mAlertDialog.dismiss()
            }

            mAlertDialog.dialog_add_user_btn_candel.setOnClickListener {
                Log.e("UserAdding", "Cancel button was clicked")
                mAlertDialog.dismiss()
            }
        }
    }

    private fun countUserPhotos() {
        var num_of_user_photos = 0
        val imagesDir =
                File(Environment.getExternalStorageDirectory()!!.absolutePath + "/.MLSafe/.NetPhotos/$username/")
        val listAllFiles = imagesDir.listFiles()
        Log.e("FILES ", "${imagesDir} ")
        Log.e("FILES ", "${listAllFiles}")
        if (listAllFiles?.isNotEmpty() == true){
            listAllFiles?.forEach { currentFile ->
                if (currentFile.name.endsWith(".png")) {
                    num_of_user_photos++
                    Log.e("FILES ", "num_of_user_photos ${num_of_user_photos}")
                    Log.e("FILES", "currentFile.getName()" + currentFile.getName())
                }
            }
        }
        this.photo_index = num_of_user_photos
    }

    private fun countAllUsersPhotos(): Int {
        //init
        var num_of_user_photos = 0
        val imagesDir =
                File(Environment.getExternalStorageDirectory()!!.absolutePath + "/.MLSafe/.NetPhotos/")

        val listAllFiles = imagesDir.listFiles()

        if (listAllFiles?.isNotEmpty() == true) {
            listAllFiles?.forEach { currentFile ->
                currentFile.listFiles()?.forEach { currentFileI ->
                    if (currentFileI.name.endsWith(".png")) {
                        num_of_user_photos++
                    }
                }
            }
        }

        return num_of_user_photos
    }

    private fun removeUser() {
        //dir to check
        val imagesDir = File(
                Environment.getExternalStorageDirectory()!!.absolutePath
                        + "/.MLSafe/.NetPhotos/$username/"
        )

        if (username.isNotEmpty()) {
            if (imagesDir.isDirectory) {
                Toast.makeText(
                        this, "DELETING $username COMPLETED SUCCESSFULLY",
                        Toast.LENGTH_SHORT
                ).show()

                //delete folder
                imagesDir.deleteRecursively()

                //check
                countUserPhotos()

                //reset counter ui
                userDrawAdding.number_of_this_user_photo = photo_index

                try {
                    preferences.edit().putBoolean("DataChanged", true).apply()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                preferences.edit().remove("UserData").apply()
            }


        } else {
            Toast.makeText(
                    this, "ENTER USERNAME TO DELETE ",
                    Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun makePhoto(imageCapture: ImageCapture) {

        //set Onclick listener
        make_photo_button.setOnClickListener {
            if (username.isNotEmpty()) {
                if (countAllUsersPhotos() < 15) {
                    //refresh index
                    photo_index++

                    //show index to ui
                    userDrawAdding.number_of_this_user_photo = photo_index

                    //create photo folder
                    val output_dir = File(
                            Environment.getExternalStorageDirectory()!!.absolutePath
                                    + "/.MLSafe/.NetPhotos/$username/"
                    )

                    output_dir.mkdirs()

                    var file_name = "${username.toLowerCase()}_$photo_index.png"
                    var output_file = File(output_dir, file_name)
                    val outputFileOptions =
                            ImageCapture.OutputFileOptions.Builder(output_file).build()

                    //write file
                    imageCapture.takePicture(outputFileOptions, photoExecutor,
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    val msg = "Photo capture succeeded: ${output_file} for $username"
                                    Log.d("CameraXApp", msg)

                                    try {
                                        preferences.edit().putBoolean("DataChanged", true).apply()
                                    } catch (e: java.lang.Exception) {
                                        e.printStackTrace()
                                    }
                                }

                                override fun onError(error: ImageCaptureException) {
                                    val msg = "Photo capture failed"
                                    Log.e("CameraXApp", msg)
                                }
                            })
                } else {
                    Toast.makeText(
                            this,
                            "Too many user photos",
                            Toast.LENGTH_SHORT
                    ).show()
                }

            } else {
                Toast.makeText(
                        this,
                        "ERROR, ENTER YOUR NAME",
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(
                Runnable {
                    showEditTextDialog()

                    // Camera provider is now guaranteed to be available
                    val cameraProvider = cameraProviderFuture.get()

                    // Set up the preview use case to display camera preview.
                    val preview = Preview.Builder().build()

                    // Choose the camera by requiring a lens facing
                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                    val imageCapture = ImageCapture.Builder()
                            .build()

                    val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                    // Attach use cases to the camera with the same lifecycle owner
                    val executor = ContextCompat.getMainExecutor(this)

                    //faceDrawOverlay = findViewById<FaceDraw>( R.id.faceDraw )
                    val faceDetector = UserAddingAnalyzer(
                            userDrawAdding,
                            true,
                    )

                    imageAnalysis.setAnalyzer(
                            executor,
                            faceDetector
                    )

                    makePhoto(imageCapture)

                    try {
                        cameraProvider.unbindAll()

                        // Bind use cases to camera
                        cameraProvider.bindToLifecycle(
                                this, cameraSelector, preview, imageCapture, imageAnalysis
                        )
                    } catch (exc: Exception) {
                        Toast.makeText(
                                this,
                                "Use case binding failed",
                                Toast.LENGTH_LONG
                        ).show()
                    }

                    // Connect the preview use case to the previewView
                    preview.setSurfaceProvider(
                            userAdding_viewFinder.surfaceProvider
                    )
                },
                ContextCompat.getMainExecutor(this)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        photoExecutor.shutdown()
    }
}