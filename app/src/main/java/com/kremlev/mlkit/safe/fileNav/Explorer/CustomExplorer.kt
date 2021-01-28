package com.kremlev.mlkit.safe.fileNav.Explorer

import android.os.Environment
import com.kremlev.mlkit.R
import com.kremlev.mlkit.safe.data.dataFile
import com.kremlev.mlkit.safe.fileNav.SafeState
import com.kremlev.mlkit.safe.fileNav.current

import java.io.File

class CustomExplorer {
    val rootPath = Environment.getExternalStorageDirectory()!!.absolutePath + "/.MLSafe/.Vault/"

    fun scanFolder() {
        current.fileList.clear()
        //current.position = 0
        val filesArray = File(current.path).listFiles()
        var fileName: String

        filesArray?.forEach { f->
            fileName = f.toString().substringAfterLast("/")
            current.fileList.add(dataFile(fileName, "$f"))
        }
        //current.fileList.sortedWith(compareBy { it.FileName })

        if (current.path == rootPath)
            scanSafeFolder()
    }

    @Deprecated("Logic in onBindView")
    fun folderNext() {
        //val newPath = File(rootFolder.toString().substringBeforeLast("/"))
        val filesArray = File(current.path).listFiles()
        current.fileList.clear()
        var fileName: String

        filesArray?.let {
            for (f in filesArray) {
                fileName = f.toString().substringAfterLast("/")
                current.fileList.add(dataFile(fileName, "$f"))
            }
        }
    }

    fun folderBack() {
        current.path = current.path.substringBeforeLast("/")
        val filesArray = File(current.path).listFiles()
        current.fileList.clear()
        var fileName: String

        filesArray?.forEach { f->
                fileName = f.toString().substringAfterLast("/")
                current.fileList.add(dataFile(fileName, "$f"))
        }
    }

    fun scanSafeFolder() {
        if (File(rootPath).isDirectory) {
            SafeState.fileList.clear()
            val filesArray = File(rootPath).listFiles()
            var fileName: String
            filesArray?.forEach { f->
                    fileName = f.toString().substringAfterLast("/")
                    SafeState.fileList.add(dataFile(fileName, "$f"))
            }
        } else {
            File(rootPath).mkdirs()
        }
    }

    fun checkIfImage(imagePath: String): Boolean {
        var type = imagePath.substringAfterLast(".")
        val list = ArrayList<String>(5)
        list.add("png")
        list.add("jpeg")
        list.add("jpg")
        list.add("raw")
        list.add("dng")

        return list.contains(type)
    }

    fun checkIfVideo(imagePath: String): Boolean {
        var type = imagePath.substringAfterLast(".")
        val list = arrayListOf<String>("avi", "mp4", "gif", "mov")

        return list.contains(type)
    }

    fun checkType(imagePath: String): Int {
        var type = imagePath.substringAfterLast(".")

        when {
            checkIfPdf(type) -> return R.drawable.ic_pdf
            checkIfApk(type) -> return R.drawable.ic_apk
            checkIfWord(type) -> return R.drawable.ic_word
            checkIfExcel(type) -> return R.drawable.ic_excel
            checkIfPPTX(type) -> return R.drawable.ic_powerpoint
            checkIfMusic(type) -> return R.drawable.ic_music
            checkIfText(type) -> return R.drawable.ic_txt
        }
        return Int.MAX_VALUE - 1
    }

    fun checkIfApk(imagePath: String): Boolean {
        return imagePath == "apk"
    }

    fun checkIfText(type: String): Boolean {
        return type == "txt" || type == "text"
    }

    fun checkIfPdf(imagePath: String): Boolean {
        return imagePath == "pdf"
    }

    private fun checkIfMusic(type: String): Boolean {
        val list: ArrayList<String> = arrayListOf("aac", "mp3", "flac", "wav", "dsd")
        return list.contains(type)
    }

    private fun checkIfPPTX(type: String): Boolean {
        return type == "pptx" || type == "ppt"
    }

    private fun checkIfExcel(type: String): Boolean {
        return type == "xls" || type == "xlsx" || type == "xml"
    }

    private fun checkIfWord(type: String): Boolean {
        return type == "doc" || type == "docx"
    }

}