package com.kremlev.mlkit.safe.adapters

import androidx.recyclerview.widget.DiffUtil
import com.kremlev.mlkit.safe.data.dataFile

class ListDiffUtils(
        val oldList: MutableList<dataFile>,
        val newList: MutableList<dataFile>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].FileName == newList[newItemPosition].FileName
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return (oldList[oldItemPosition].FilePath == newList[newItemPosition].FilePath
                && oldList[oldItemPosition].FileName == (newList[newItemPosition].FileName))
    }
}