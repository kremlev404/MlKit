@file:Suppress("DEPRECATION")

package com.kremlev.mlkit.recognition.fragments

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.preference.PreferenceManager.getDefaultSharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.gms.tasks.OnSuccessListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceDetectorOptions.LANDMARK_MODE_ALL
import com.kremlev.mlkit.R
import com.kremlev.mlkit.auth.VaultAuth
import com.kremlev.mlkit.recognition.Analyzer.AccessValue
import com.kremlev.mlkit.recognition.Analyzer.NET.FaceNetModel
import com.kremlev.mlkit.recognition.Analyzer.NET.Normalize
import com.kremlev.mlkit.recognition.Analyzer.ObjectDetectorImageAnalyzer
import com.kremlev.mlkit.recognition.Analyzer.data.UserDescriptor
import com.kremlev.mlkit.recognition.Analyzer.data.UserLabel
import com.kremlev.mlkit.recognition.overlay.FaceDraw
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class HomeFragment : Fragment() {

    private lateinit var executor: ExecutorService
    private lateinit var prevFaceDetector: ObjectDetectorImageAnalyzer
    private lateinit var preferences: SharedPreferences
    private var isNeedToRunModel: Boolean = true
    private var imageLabelPairs = ArrayList<UserLabel>()
    private var model: FaceNetModel? = null
    private var progressDialog: ProgressDialog? = null
    private var imageData = ArrayList<UserDescriptor>()
    private val normalize: Normalize = Normalize()

    ///DETECTOR PARAM
    private val realTimeOpts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(LANDMARK_MODE_ALL)
            .build()
    private val detector = FaceDetection.getClient(realTimeOpts)

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        Log.e("Life", "HOME FRAGMENT onCreateView")

        val home = inflater.inflate(R.layout.fragment_home, container, false)

        //FINDVIEW
        val faceDrawOverlay = home.findViewById<FaceDraw>(R.id.faceDraw)
        val vault_entrance_btn = home.findViewById<RelativeLayout>(R.id.vault_entrance_btn)

        //INIT DETECTOR
        if (isNeedToRunModel) {
            prevFaceDetector = ObjectDetectorImageAnalyzer(
                    requireActivity(),
                    detector,
                    faceDrawOverlay,
                    isNeedToRunModel = true,
                    detectorImageWidth = 1f,
                    detectorImageHeight = 1f)
        }
        //to vault
        AccessValue.denyAccess()

        //EXECUTOR
        executor = Executors.newSingleThreadExecutor()

        //SCAN STORAGE->CAMERA->ANYLYZER
        //
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        scanUserData()
        startCamera()

        vault_entrance_btn.setOnClickListener {
            Toast.makeText(requireContext(),
                    "GOING TO USER SAFE ACTIVITY",
                    Toast.LENGTH_LONG).show()
            try {
                val intent = Intent(requireContext(), VaultAuth::class.java)
                startActivity(intent)
                AccessValue.denyAccess()
                activity?.finish()
            } catch (e: java.lang.Exception) {
                Log.e("SettingFragment", "Going to SAFE to home error")
            }
        }

        val liveAccess = AccessValue.getData()

        var accessObserver = Observer<Boolean> { _ ->
            if (AccessValue.getData().value == true)
                vault_entrance_btn.visibility = View.VISIBLE
        }

        liveAccess?.observe(viewLifecycleOwner, accessObserver)
        //liveAccess.removeObserver(accessObserver)

        return home
    }

    private fun scanUserData() {
        imageData.clear()
        val userData = getUserData()

        var dataChanged = false

        try {
            dataChanged = preferences.getBoolean("DataChanged", false)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        if (userData.isNotEmpty() && dataChanged !== true) {
            imageData.addAll(getUserData())
            prevFaceDetector.faceList = imageData
        } else {
            scanStorageForImages()
            try {
                val prefEditor = preferences.edit()
                prefEditor.putString("DataChanged", "false").apply()

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun saveUserData() {
        try {
            val prefEditor = getDefaultSharedPreferences(context).edit()
            val jsonString = Gson().toJson(imageData)
            prefEditor.putString("UserData", jsonString).apply()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    private fun getUserData(): ArrayList<UserDescriptor> {

        val jsonString = preferences.getString("UserData", null)
        //val obj  = jsonArray
        return jsonString.let {
            Gson().fromJson(jsonString, object : TypeToken<ArrayList<UserDescriptor>>() {}.type)
        } ?: arrayListOf()
    }

    private fun scanStorageForImages() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {

            //dir
            val imagesDir = File(Environment.getExternalStorageDirectory()!!.absolutePath + "/.MLSafe/.NetPhotos")
            val imageSubDirs = imagesDir.listFiles()

            //if there are no user data
            imageSubDirs?.let {
                if (imageSubDirs.isNotEmpty()) {
                    //Dialog
                    progressDialog = ProgressDialog(requireActivity(), R.style.MyAlertDialogStyle)
                    progressDialog?.setMessage(" CALCULATING USER NORM ")
                    progressDialog?.setCancelable(false)
                    progressDialog?.show()

                    //net to check user photos
                    model = FaceNetModel(requireActivity())

                    isNeedToRunModel = true
                    try {
                        for (imageSubDir in imagesDir.listFiles()) {
                            for (image in imageSubDir.listFiles()) {
                                imageLabelPairs.add(
                                        UserLabel(
                                                BitmapFactory.decodeFile(image.absolutePath),
                                                imageSubDir.name
                                        )
                                )
                            }
                        }

                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }

                    scanImage(0)
                }

            } ?: run {
                isNeedToRunModel = false
                Toast.makeText(
                        requireActivity(),
                        "No user Data, Add it on Settings",
                        Toast.LENGTH_LONG
                ).show()

            }
        }
    }

    private fun scanImage(counter: Int) {
        val dataUser = imageLabelPairs[counter]

        val inputImage = InputImage.fromByteArray(
                //byte array
                normalize.bitmaptoNv21(dataUser.img),
                dataUser.img.width,
                dataUser.img.height,
                // image rotation
                preferences.getInt("Angle", 270),
                InputImage.IMAGE_FORMAT_NV21
        )

        val faceList =
                OnSuccessListener<List<Face?>> { faces ->
                    //for (face in faces) {
                    try {
                        if (faces.isNotEmpty()) {
                            imageData.add(
                                    UserDescriptor(
                                            dataUser.name,
                                            model!!.getFaceEmbedding(
                                                    dataUser.img,
                                                    faces.get(0)!!.boundingBox,
                                                    preRotate = false
                                            )
                                    )
                            )
                        }

                        //if we checked all photo - DONE
                        if (counter + 1 == imageLabelPairs.size) {
                            Toast.makeText(
                                    requireActivity(),
                                    "\tProcessing completed. \n\tFound ${imageData.size} UserFace's",
                                    Toast.LENGTH_LONG
                            ).show()
                            progressDialog?.dismiss()
                            prevFaceDetector.faceList = imageData

                            //create json file with userdata
                            saveUserData()

                        } else {
                            progressDialog?.setMessage("Processed ${counter + 1} UserFace's")
                            scanImage(counter + 1)
                        }
                    } catch (e: java.lang.Exception) {
                        Toast.makeText(
                                requireContext(),
                                "${e.printStackTrace()}",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        detector.process(inputImage).addOnSuccessListener(faceList)
    }

    private fun createFaceDetector(
            width: Float,
            height: Float,
    ): ObjectDetectorImageAnalyzer {
        val faceDetector = ObjectDetectorImageAnalyzer(
                requireContext(),
                detector,
                faceDraw,
                isNeedToRunModel,
                width,
                height
        )
        if (isNeedToRunModel)
            faceDetector.faceList = imageData

        return faceDetector
    }

    private fun setFaceDetector(imageAnalysis: ImageAnalysis) {
        viewFinder.previewStreamState.observe(
                viewLifecycleOwner,
                object : Observer<PreviewView.StreamState> {
                    override fun onChanged(streamState: PreviewView.StreamState?) {
                        if (streamState != PreviewView.StreamState.STREAMING)
                            return

                        //reversed
                        var height = viewFinder.width * viewFinder.scaleX
                        var width = viewFinder.height * viewFinder.scaleY

                        imageAnalysis.setAnalyzer(executor,
                                createFaceDetector(
                                        width,
                                        height
                                )
                        )

                        viewFinder.previewStreamState.removeObserver(this)
                    }
                }
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                    .build()

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

            setFaceDetector(imageAnalysis)

            try {
                cameraProvider.unbindAll()
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageAnalysis
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

            preview.setSurfaceProvider(
                    viewFinder.getSurfaceProvider())
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("Life", "HOME FRAGMENT onDestroyView")
        executor.shutdown()
        val manager = requireActivity().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        Log.e("Life", "onDestroy")
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}