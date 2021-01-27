package com.kremlev.mlkit.safe.fileNav.NavigationInterfaces

interface State<T> {
    //to getData
    fun getData(): T

    //to refresh data
    fun refresh()

    //to refresh old value after Utils find Diff
    fun refrshOld()
}