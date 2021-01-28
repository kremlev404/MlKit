package com.kremlev.mlkit.safe.adapters

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.RecyclerView

class RecyclerSafeUpdate(
        val context: Context,
        var recycler: RecyclerView
) {
    private var adapter: RecyclerView.Adapter<RecyclerViewSafeAdapter.ViewSafeHolder>? = null

    fun refreshList() {
        Log.e("ADAPETER", "$recycler")
        adapter = RecyclerViewSafeAdapter(context)
        recycler.adapter = adapter
    }
}