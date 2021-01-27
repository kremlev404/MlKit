package com.kremlev.mlkit.safe.fileNav

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kremlev.mlkit.safe.data.dataFile
import com.kremlev.mlkit.safe.fileNav.NavigationInterfaces.State

object current : LiveData<String>(), State<LiveData<String>?> {
    private val liveData = MutableLiveData<String>()

    override fun refresh() {
        liveData.value = path
    }

    override fun refrshOld() {
        oldItemList.clear()
        oldItemList.addAll(fileList)
    }

    override fun getData(): LiveData<String>? {
        return liveData
    }

    var isProcessing = false
    var haveToChange = true
    var path: String = " "
    var position: Int = 0
    var oldItemList: MutableList<dataFile> = mutableListOf<dataFile>()
    var fileList: MutableList<dataFile> = mutableListOf<dataFile>()
}