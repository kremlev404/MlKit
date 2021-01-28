package com.kremlev.mlkit.safe.adapters

import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kremlev.mlkit.safe.fileNav.current

class RecyclerUpdate(
        val context: Context,
        var recycler_file_manager: RecyclerView
) {
    private var adapter: RecyclerView.Adapter<RecyclerViewManagerAdapter.ViewManagerHolder>? = null
    private var first = true

    fun refreshList() {

        val utils = ListDiffUtils(current.oldItemList, current.fileList)
        current.refrshOld()
        val diffResult = DiffUtil.calculateDiff(utils)

        if (first)
        //adapter = RecyclerViewManagerAdapter(context)
            recycler_file_manager.adapter = adapter
        else
            diffResult.dispatchUpdatesTo(adapter as RecyclerViewManagerAdapter)
        first = false
    }
}