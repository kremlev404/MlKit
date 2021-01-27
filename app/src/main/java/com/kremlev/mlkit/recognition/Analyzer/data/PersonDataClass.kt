package com.kremlev.mlkit.recognition.Analyzer.data

import android.graphics.RectF

data class PersonDataClass(
        var box: RectF,
        var label: String
)