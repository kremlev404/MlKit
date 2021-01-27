package com.kremlev.mlkit.recognition.overlay

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View

class UserAddDraw(
        context: Context,
        attributeSet: AttributeSet
) : View(context, attributeSet) {

    //get data about screen
    private val displayMetrics = context.resources.displayMetrics
    private val dpHeight = displayMetrics.heightPixels
    private val dpWidth = displayMetrics.widthPixels

    //paint for style
    private var textPaint = Paint()

    var userAddingFlag = false
    var captureFlag: Boolean = true

    //user info
    var number_of_this_user_photo = 0
    var unknownBoxes: Rect = Rect(0, 1, 1, 1)

    override fun onDraw(canvas: Canvas?) {
        textPaint.textAlign = Paint.Align.CENTER;

        if (userAddingFlag) {
            captureFlag = true
            textPaint = Paint().apply {
                textSize = 48f
                color = Color.parseColor("#FF3700B3")
            }

            canvas?.drawRect(
                    0f, 0f, dpWidth.toFloat(), dpHeight.toFloat(),
                    Paint().apply {
                        color = Color.parseColor("#FF3700B3")
                        style = Paint.Style.STROKE
                        strokeWidth = 10f
                    }
            )

            canvas?.drawText(
                    "FACE DETECTED: READY TO CAPTURE",
                    12f,
                    64f,
                    textPaint
            )

            Log.e("onDraw", "userAdding face detected ")
            userAddingFlag = false

        } else {
            textPaint = Paint().apply {
                textSize = 48f
                color = Color.parseColor("#d42493")
            }

            canvas?.drawRect(
                    0f, 0f, dpWidth.toFloat(), dpHeight.toFloat(),
                    Paint().apply {
                        color = Color.parseColor("#d42493")
                        style = Paint.Style.STROKE
                        strokeWidth = 10f
                    }
            )

            canvas?.drawText(
                    "FACE NOT FOUND: CAPTURING NOT AVAILABLE",
                    12f,
                    64f,
                    textPaint
            )
        }

        userAddingFlag = false
        canvas?.drawText(
                "PHOTOS OF THIS USER: $number_of_this_user_photo",
                12f,
                128f,
                textPaint
        )
    }

    fun draw(box: Rect, userAddingFlag: Boolean) {
        this.userAddingFlag = userAddingFlag
        this.unknownBoxes.set(box)
        invalidate()
    }
}