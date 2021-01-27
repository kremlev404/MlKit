package com.kremlev.mlkit.recognition.Analyzer.NET

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.nio.ByteBuffer

class FaceNetModel(context: Context) {
    var timeToProcess: Long = -1

    private val imgSizeX = 160
    private val imgSizeY = 160

    private var interpreter: Interpreter = Interpreter(
            FileUtil.loadMappedFile(context, "facenet.tflite"),
            Interpreter.Options().setNumThreads(4)
    )

    private val imageTensorProcessor = ImageProcessor.Builder()
            .add(ResizeOp(imgSizeX, imgSizeY, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(127.5f, 127.5f))
            .build()

    fun getFaceEmbedding(image: Bitmap, crop: Rect, preRotate: Boolean): FloatArray {
        val inputTensor = convertBitmapToBuffer(
                cropRectFromBitmap(image, crop, preRotate)
        )

        return runFaceNet(inputTensor)[0]
    }

    private fun runFaceNet(inputTensor: ByteBuffer): Array<FloatArray> {
        val t1 = System.currentTimeMillis()
        val outputs = Array(1) {
            FloatArray(128)
        }

        interpreter.run(inputTensor, outputs)
        timeToProcess = System.currentTimeMillis() - t1

        return outputs
    }

    private fun convertBitmapToBuffer(image: Bitmap): ByteBuffer {
        val imageTensor = imageTensorProcessor.process(TensorImage.fromBitmap(image))
        return imageTensor.buffer
    }

    private fun cropRectFromBitmap(bit: Bitmap, rect: Rect, preRotate: Boolean): Bitmap {

        var width = rect.width()
        var height = rect.height()

        if ((rect.left + width) > bit.width) {
            width = bit.width - rect.left
        }

        if ((rect.top + height) > bit.height) {
            height = bit.height - rect.top
        }

        val croppedBitmap = Bitmap.createBitmap(
                if (preRotate)
                    rotateBitmap(bit, -90f)!!
                else
                    bit,
                rect.left,
                rect.top,
                width,
                height
        )
        return croppedBitmap
    }

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)

        return Bitmap.createBitmap(
                source,
                0,
                0,
                source.width,
                source.height,
                matrix,
                false
        )
    }
}

