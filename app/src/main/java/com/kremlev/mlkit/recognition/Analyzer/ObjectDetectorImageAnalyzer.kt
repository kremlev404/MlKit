package com.kremlev.mlkit.recognition.Analyzer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import android.widget.EditText
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceLandmark
import com.kremlev.mlkit.R
import com.kremlev.mlkit.recognition.Analyzer.NET.FaceNetModel
import com.kremlev.mlkit.recognition.Analyzer.NET.Normalize
import com.kremlev.mlkit.recognition.Analyzer.data.PersonDataClass
import com.kremlev.mlkit.recognition.Analyzer.data.UserDescriptor
import com.kremlev.mlkit.recognition.overlay.FaceDraw
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.internal.synchronized
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class ObjectDetectorImageAnalyzer(
        private val context: Context,
        private val detector: FaceDetector,
        private var faceDrawOverlay: FaceDraw,
        private var isNeedToRunModel: Boolean,
        private val detectorImageWidth: Float,
        private val detectorImageHeight: Float
) : ImageAnalysis.Analyzer {

    //Net
    private val model = FaceNetModel(context)

    //object with useful functions for transformations
    private val normalize: Normalize = Normalize()
    private val predictions = ArrayList<PersonDataClass>()

    //threshold to compare distance between current & db faces
    private val threshold: Double = 12.5

    //user faces
    var faceList = ArrayList<UserDescriptor>()

    //draw a square if the user was not recognized
    private fun drawingForUnknownUser(box: RectF, t1: Long) {
        val t2: Long = System.currentTimeMillis()
        faceDrawOverlay.drawFaceBoundsWithoutData((box), t2 - t1, true)
    }

    //to draw on overlay correct data we have to scale point
    fun processLandmarks(arr: ArrayList<FloatArray>, bitmapHeight: Int,
                         bitmapWidth: Int): ArrayList<FloatArray> {
        val scaleFactorX = detectorImageHeight / bitmapHeight
        val scaleFactorY = detectorImageWidth / bitmapWidth


        for (arrI in arr)
            Matrix().apply {
                preScale(scaleFactorX, scaleFactorY)
                postScale(-1f, 1f, detectorImageHeight / 2f,
                        detectorImageWidth / 2f)
            }.mapPoints(arrI)
        return arr
    }

    //to draw on overlay correct data we have to scale point
    private fun processBoxes(box: Rect, bitmapHeight: Int,
                             bitmapWidth: Int): RectF {
        val rectf = RectF(box)
        val scaleFactorX = detectorImageHeight / bitmapHeight
        val scaleFactorY = detectorImageWidth / bitmapWidth

        Matrix().apply {
            preScale(scaleFactorX, scaleFactorY)
            postScale(-1f, 1f, detectorImageHeight / 2f,
                    detectorImageWidth / 2f)
        }.mapRect(rectf)

        return rectf
    }

    private var isProcessing = AtomicBoolean(false)

    override fun analyze(imageProxy: ImageProxy) {
        //get bitmap from camera to analyze
        val bitmap = normalize.toBitmap(imageProxy.image!!)

        if (isProcessing.get()) {
            return
        } else {
            isProcessing.set(true)

            // create inputImage to put it on detector
            val inputImage = InputImage.fromByteArray(
                    //byte array
                    normalize.bitmaptoNv21(bitmap),
                    imageProxy.width,
                    imageProxy.height,
                    imageProxy.imageInfo.rotationDegrees,
                    InputImage.IMAGE_FORMAT_NV21
            )

            detector.process(inputImage)
                    .addOnSuccessListener { faces ->
                        //set timer to show info about proccesing time in overlay
                        Thread {
                            val t1 = System.currentTimeMillis()

                            faces?.let { faces ->
                                if (faces.isNotEmpty()) {
                                    for (face in faces) {
                                        try {
                                            //if we dont have any user data, we dont need to calculate
                                            val bitmapHeight = bitmap.height
                                            val bitmapWidth = bitmap.width
                                            if (isNeedToRunModel) {
                                                //anther timer
                                                val t3 = System.currentTimeMillis()

                                                //get faceEmbadding
                                                val currentFace =
                                                        model.getFaceEmbedding(bitmap, face.boundingBox, preRotate = false)

                                                val userNormHashMap = HashMap<String, ArrayList<Float>>()

                                                faceList.forEach { flist ->
                                                    // Compute the L2 norm and then append it to the ArrayList.
                                                    userNormHashMap[flist.name]?.let { descriptor ->
                                                        descriptor.add(
                                                                normalize.L2Norm(
                                                                        currentFace,
                                                                        flist.faceEmbading
                                                                )
                                                        )
                                                        Log.e("userNormHashMap", " $userNormHashMap")

                                                    } ?: run {
                                                        val l2norm = ArrayList<Float>()
                                                        l2norm.add(normalize.L2Norm(currentFace, flist.faceEmbading))
                                                        userNormHashMap[flist.name] = l2norm

                                                        Log.e("userNormHashMapA", " $userNormHashMap")
                                                    }
                                                }

                                                // If landmark detection was enabled (mouth, eyes, and
                                                // nose available):
                                                val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE).position
                                                val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE).position
                                                val leftMouth = face.getLandmark(FaceLandmark.MOUTH_LEFT).position
                                                val rightMouth = face.getLandmark(FaceLandmark.MOUTH_RIGHT).position
                                                val nose = face.getLandmark(FaceLandmark.NOSE_BASE).position

                                                //var landmarkData = LandmarkData(leftEye, rightEye, nose, leftMouth, rightMouth)

                                                var arr = ArrayList<FloatArray>()

                                                arr.add(floatArrayOf(leftEye.x, leftEye.y))
                                                arr.add(floatArrayOf(rightEye.x, rightEye.y))
                                                arr.add(floatArrayOf(nose.x, nose.y))
                                                arr.add(floatArrayOf(leftMouth.x, leftMouth.y))
                                                arr.add(floatArrayOf(rightMouth.x, rightMouth.y))

                                                val processedLand = processLandmarks(arr, bitmapHeight, bitmapWidth)

                                                // Compute the average of all Euclidean Distance norms for each face.
                                                val avgNorms = userNormHashMap.values.map { L2norms ->
                                                    L2norms.average()
                                                }

                                                if (avgNorms.isNotEmpty()) {
                                                    val names = userNormHashMap.keys.map { key -> key }

                                                    val minL2value = avgNorms.min()!!
                                                    val minL2index = avgNorms.indexOf(minL2value)
                                                    val minL2NormName = names[minL2index]

                                                    faceDrawOverlay.currentNorm = minL2value

                                                    if (avgNorms.min()!! < threshold && avgNorms.min()!! > -1 * threshold) {
                                                        AccessValue.setAccess()

                                                        predictions.add(
                                                                PersonDataClass(
                                                                        processBoxes(face.boundingBox, bitmapHeight, bitmapWidth),
                                                                        //face.boundingBox.transform(width, height),
                                                                        minL2NormName
                                                                )
                                                        )

                                                        faceDrawOverlay.drawFaceBounds(
                                                                predictions,
                                                                processedLand,
                                                                model.timeToProcess,
                                                                newUserFrameBoxes = true,
                                                                haveToDrawLandMarks = true
                                                        )

                                                        //draw text & box for unknown user
                                                    } else {
                                                        faceDrawOverlay.someUserData = true
                                                        drawingForUnknownUser(processBoxes(face.boundingBox, bitmapHeight, bitmapWidth), t3)
                                                    }
                                                } else
                                                    drawingForUnknownUser(processBoxes(face.boundingBox, bitmapHeight, bitmapWidth), t3)
                                            } else
                                                drawingForUnknownUser(processBoxes(face.boundingBox, bitmapHeight, bitmapWidth), t1)

                                        } catch (e: Exception) {
                                            Log.e("MODEL ANALYZER", "${e.printStackTrace()} ")
                                            continue
                                        }
                                    }
                                } else {
                                    faceDrawOverlay.drawNoFace(noface = true)
                                }

                                isProcessing.set(false)
                            }
                            imageProxy.close()
                        }.start()
                    }
                    .addOnFailureListener { e ->
                        e.message?.let { Log.e("Model", it) }
                    }
        }

    }
}
