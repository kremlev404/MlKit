package com.kremlev.mlkit.recognition.Analyzer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object AccessValue : LiveData<Boolean>() {
    var accessToVaultGranted = MutableLiveData<Boolean>(false)
    var counter = 0

    fun getData(): LiveData<Boolean> {
        return accessToVaultGranted
    }

    fun setAccess() {
        counter++
        if (counter > 21)
            accessToVaultGranted.postValue(true)
    }

    fun denyAccess() {
        counter = 0
        accessToVaultGranted.value = (false)
    }
}