package com.kremlev.mlkit.safe.fragments

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kremlev.mlkit.R
import com.kremlev.mlkit.safe.adapters.*
import com.kremlev.mlkit.safe.fileNav.Explorer.CustomExplorer
import com.kremlev.mlkit.safe.fileNav.SafeState
import com.kremlev.mlkit.safe.fileNav.current
import kotlinx.android.synthetic.main.fragment_safe_home.*
import kotlinx.android.synthetic.main.fragment_safe_home.circular_encrypt_bar
import kotlinx.android.synthetic.main.safe_fragment.*
import java.util.Observer

class SafeFragment : Fragment() {

    private var layoutManager: RecyclerView.LayoutManager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val safe = inflater.inflate(R.layout.safe_fragment, container, false)

        var navi = CustomExplorer()
        navi.scanSafeFolder()
        //if first start - set home folder to singleton

        val recycler_file_safe: RecyclerView
        recycler_file_safe = safe.findViewById(R.id.recycler_file_safe)

        val mAdapter = RecyclerViewSafeAdapter(requireContext())

        //init recyc
        RecyclerSafeUpdate(requireContext(), recycler_file_safe).refreshList()
        recycler_file_safe.adapter = mAdapter

        val liveList = SafeState.getData()
        SafeState.refrshOld()

        liveList?.observe(viewLifecycleOwner, { _ ->
            navi.scanSafeFolder()
            val utils = ListDiffUtils(SafeState.oldItemList, SafeState.fileList)
            val diffResult = DiffUtil.calculateDiff(utils)
            //refreshOldValues
            SafeState.refrshOld()
            //ProgressBar
            if (SafeState.isProcessing)
                horizontal_progressbar.visibility = View.VISIBLE
            else
                horizontal_progressbar.visibility = View.GONE

            //refresh recycler if diff has detected
            diffResult.dispatchUpdatesTo(mAdapter)

            //scan & update filelist
            navi.scanSafeFolder()
        })

        return safe
    }
}