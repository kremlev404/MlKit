package com.kremlev.mlkit.recognition.overlay

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.kremlev.mlkit.recognition.Analyzer.data.PersonDataClass

class FaceDraw(context: Context, attributeSet: AttributeSet)
    : View(context, attributeSet) {

    //A structure describing general information about a display,
    //Such as its size, density, and font scaling.
    private val displayMetrics = context.resources.displayMetrics
    private val dpHeight = displayMetrics.heightPixels
    private val dpWidth = displayMetrics.widthPixels

    //set default value
    private var detectorImageWidth = 480
    private var detectorImageHeight = 360

    //net time
    private var timeToProcess: Long = 0
    private val faceCorrValue: Int = 0

    //flags to process different cases
    private var unknownNewBoxes: Boolean = false
    private var newUserFrameBoxes: Boolean = false
    private var noface: Boolean = false
    var someUserData: Boolean = false
    private var haveToDrawLandMarks: Boolean = false

    //L2 distance between current detected face and database
    var currentNorm: Double = 0.0

    //Detector Points
    private var personData: ArrayList<PersonDataClass> = arrayListOf()
    private var landmarkData: ArrayList<FloatArray> = arrayListOf()
    private var unknownBoxes: Rect = Rect(0, 1, 1, 1)

    fun setImageSourceInfo(detectorImageWidth: Int, detectorImageHeight: Int) {
        this.detectorImageWidth = detectorImageWidth
        this.detectorImageHeight = detectorImageHeight
    }

/*
    //fun to scale detector points to draw it correctly
    private val output2OverlayTransformFrontLens = Matrix().apply {
        //scale image
        preScale(scaleFactorX, scaleFactorY)
        //flip image
        postScale(-1f, 1f, dpWidth / 2f, dpHeight / 2f)
    }
*/

    //
    //Paint
    //
    private val textPaint = Paint().apply {
        textSize = 48f
        color = Color.WHITE
    }

    private val landPaint = Paint().apply {
        color = Color.RED
    }

    private val boxpaint = Paint().apply {
        color = Color.parseColor("#FF3700B3")
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }
    //

    override fun onDraw(canvas: Canvas?) {
        var ms = (timeToProcess.toInt())

        if ((personData.isNotEmpty() || personData !== null) && newUserFrameBoxes) {
            canvas?.drawText(
                    "NET SPENDS ON CALCULATIONS:$ms ms",
                    0f,
                    48f,
                    Paint().apply {
                        color = boxpaint.color
                        textSize = 48f
                    }
            )

            try {
                for (face in personData) {
                    val processedBbox =
                            RectF(
                                    personData.last().box.left,
                                    personData.last().box.top - faceCorrValue,
                                    personData.last().box.right,
                                    personData.last().box.bottom - faceCorrValue

                            )

                    canvas?.drawRoundRect(
                            processedBbox,
                            24f,
                            24f,
                            boxpaint
                    )
                    canvas?.drawText(
                            personData.last().label,
                            processedBbox.left + 20,
                            processedBbox.top - 3,
                            textPaint
                    )

                    canvas?.drawText(
                            "${String.format("%.3f", currentNorm)}",
                            processedBbox.left + 20,
                            processedBbox.bottom - 3,
                            textPaint
                    )
                }
                newUserFrameBoxes = false
                //invalidate()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (unknownNewBoxes) {
            canvas?.drawText(
                    "NET SPENDS ON CALCULATIONS:$ms ms",
                    0f,
                    48f,
                    Paint().apply {
                        color = boxpaint.color
                        textSize = 48f
                    }
            )
            val processedBbox = RectF(unknownBoxes)

            canvas?.drawRoundRect(
                    processedBbox,
                    24f,
                    24f,
                    boxpaint
            )

            canvas?.drawText(
                    "Unknown User",
                    processedBbox.left + 20,
                    processedBbox.top - 3,
                    textPaint
            )

            if (someUserData) {
                canvas?.drawText(
                        "${String.format("%.3f", currentNorm)}",
                        processedBbox.left + 20,
                        processedBbox.bottom - 3,
                        textPaint
                )
                someUserData = false
            }
            unknownNewBoxes = false
            //invalidate()

        }
        if (noface) {
            canvas?.drawText(
                    "NET DOESN'T COMPUTE",
                    0f,
                    48f,
                    Paint().apply {
                        color = boxpaint.color
                        textSize = 48f
                    }
            )
            canvas?.drawText(
                    "THERE ARE NO FACE DETECTED",
                    0f,
                    48 * 2f,
                    Paint().apply {
                        color = boxpaint.color
                        textSize = 48f
                    }
            )
            noface = false
        }
        if (haveToDrawLandMarks) {
            //output2OverlayTransformFrontLens.mapPoints(arr2)
            canvas?.drawCircle(landmarkData[0][0], landmarkData[0][1], 5f, landPaint)
            canvas?.drawCircle(landmarkData[1][0], landmarkData[1][1], 5f, landPaint)
            canvas?.drawCircle(landmarkData[2][0], landmarkData[2][1], 5f, landPaint)
            canvas?.drawCircle(landmarkData[3][0], landmarkData[3][1], 5f, landPaint)
            canvas?.drawCircle(landmarkData[4][0], landmarkData[4][1], 5f, landPaint)

            haveToDrawLandMarks = false
        }
    }

    fun drawFaceBounds(
            personData: List<PersonDataClass>,
            landmarkData: ArrayList<FloatArray>,
            timeToProcess: Long,
            newUserFrameBoxes: Boolean,
            haveToDrawLandMarks: Boolean,
    ) {
        this.newUserFrameBoxes = newUserFrameBoxes
        this.personData.clear()
        this.landmarkData.clear()
        this.timeToProcess = timeToProcess
        this.personData.addAll(personData)
        this.landmarkData.addAll(landmarkData)
        this.haveToDrawLandMarks = haveToDrawLandMarks
        invalidate()
    }

    //THERE ARE NO FACE DETECTED
    fun drawNoFace(noface: Boolean) {
        this.noface = noface
        this.timeToProcess = timeToProcess
        invalidate()
    }

    fun drawFaceBoundsWithoutData(
            box: RectF,
            timeToProcess: Long,
            unknownNewBoxes: Boolean
    ) {
        this.timeToProcess = timeToProcess
        this.unknownNewBoxes = unknownNewBoxes
        this.unknownBoxes.set(
                box.left.toInt(),
                box.top.toInt() - faceCorrValue,
                box.right.toInt(),
                box.bottom.toInt() - faceCorrValue)
        invalidate()
    }
}




