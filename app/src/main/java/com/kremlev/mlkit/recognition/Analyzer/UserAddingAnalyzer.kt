package com.kremlev.mlkit.recognition.Analyzer

import android.annotation.SuppressLint
import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.kremlev.mlkit.recognition.Analyzer.NET.Normalize
import com.kremlev.mlkit.recognition.overlay.UserAddDraw

class UserAddingAnalyzer(
        private var userAddingOverlay: UserAddDraw,
        private var userAddingFlag: Boolean
) : ImageAnalysis.Analyzer {

    private val normalize: Normalize = Normalize()
    private val detector = FaceDetection.getClient()

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val bitmap = normalize.toBitmap(imageProxy?.image!!)
        val rotation = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromByteArray(
                normalize.bitmaptoNv21(bitmap),
                bitmap.width,
                bitmap.height,
                rotation,
                InputImage.IMAGE_FORMAT_NV21
        )
        detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    Thread {
                        for (face in faces) {
                            try {
                                userAddingOverlay.draw(face.boundingBox, userAddingFlag)
                            } catch (e: Exception) {
                                Log.e("Model", "Exception in FrameAnalyser : ${e.message}")
                                continue
                            }
                        }
                        if (faces.isEmpty()) {
                            userAddingOverlay.draw(Rect(0, 0, 1, 1), false)
                        }
                        imageProxy.close()
                    }.start()
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
    }
}
