package com.kremlev.mlkit.safe.fileNav

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kremlev.mlkit.safe.data.dataFile
import com.kremlev.mlkit.safe.fileNav.NavigationInterfaces.State

object SafeState : LiveData<String>(), State<MutableLiveData<MutableList<dataFile>>?> {
    private val liveData = MutableLiveData<MutableList<dataFile>>()

    override fun refresh() {
        liveData.value = fileList
    }

    override fun refrshOld() {
        oldItemList.clear()
        oldItemList.addAll(fileList)
    }

    override fun getData(): MutableLiveData<MutableList<dataFile>>? {
        return liveData
    }

    var isProcessing = false
    var position: Int = 0
    var oldItemList: MutableList<dataFile> = mutableListOf<dataFile>()
    var fileList: MutableList<dataFile> = mutableListOf<dataFile>()
}