package com.kremlev.mlkit.recognition.Analyzer.data

import android.graphics.PointF


data class LandmarkData(
        val leftEye: PointF,
        val rightEye: PointF,
        val nose: PointF,
        val leftMouth: PointF,
        val rightMouth: PointF,
)