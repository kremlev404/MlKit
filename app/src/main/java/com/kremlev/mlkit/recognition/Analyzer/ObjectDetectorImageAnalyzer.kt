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
import java.util.concurrent.atomic.AtomicBoolean

class ObjectDetectorImageAnalyzer(
        private val context: Context,
        private val detector: FaceDetector,
        private var faceDrawOverlay: FaceDraw,
        private var isNeedToRunModel: Boolean,
        private val detectorImageWidth: Float,
        private val detectorImageHeight: Float
) : ImageAnalysis.Analyzer {

    private var isProcessing = AtomicBoolean(false)

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

    //TODO align
    /*
    fun alignFaces(srcTri: ArrayList<DoubleArray>, src: ImageProxy){
        val warpMat = Imgproc.getAffineTransform(
                MatOfPoint2f(
                        Point(srcTri[0]),
                        Point(srcTri[1]),
                        Point(srcTri[2]),
                        Point(srcTri[3]),
                        Point(srcTri[4]),
                ), MatOfPoint2f(
                Point(0.31556875000000000, 0.4615741071428571),
                Point(0.68262291666666670, 0.4615741071428571),
                Point(0.50026249999999990, 0.6405053571428571),
                Point(0.34947187500000004, 0.8246919642857142),
                Point(0.65343645833333330, 0.8246919642857142)
        )
        )

    }
     */

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

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        //get bitmap from camera to analyze
        val bitmap = normalize.toBitmap(imageProxy?.image!!)

        // wait while other frame is being processed
        if (isProcessing.get()) {
            return
        } else {

            // set that the current frame is being processed
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
                        val t1 = System.currentTimeMillis()
                        Thread {
                            if (faces.isNotEmpty() && faces != null) {
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

                                            for (i in 0 until faceList.size) {


                                                if (userNormHashMap[faceList[i].name] == null) {

                                                    // Compute the L2 norm and then append it to the ArrayList.
                                                    val l2norm = ArrayList<Float>()
                                                    l2norm.add(normalize.L2Norm(currentFace, faceList[i].faceEmbading))
                                                    userNormHashMap[faceList[i].name] = l2norm
                                                    Log.e("userNormHashMap" , " $userNormHashMap")
                                                    //fill user
                                                } else {
                                                    userNormHashMap[faceList[i].name]?.add(
                                                            normalize.L2Norm(
                                                                    currentFace,
                                                                    faceList[i].faceEmbading
                                                            )
                                                    )
                                                    Log.e("userNormHashMapA" , " $userNormHashMap")
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
                                                L2norms.toFloatArray().average()
                                            }

                                            if (avgNorms.isNotEmpty() || avgNorms !== null) {
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
                            //
                            isProcessing.set(false)
                            imageProxy.close()
                        }.start()
                    }
                    .addOnFailureListener { e ->
                        e.message?.let { Log.e("Model", it) }
                    }
        }
    }
}
