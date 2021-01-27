package com.kremlev.mlkit.safe.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.graphics.vectormath.rotation
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import com.kremlev.mlkit.R
import com.kremlev.mlkit.safe.adapters.ListDiffUtils
import com.kremlev.mlkit.safe.adapters.RecyclerUpdate
import com.kremlev.mlkit.safe.adapters.RecyclerViewManagerAdapter
import com.kremlev.mlkit.safe.fileNav.current
import com.kremlev.mlkit.safe.fileNav.Explorer.CustomExplorer
import kotlinx.android.synthetic.main.fragment_safe_home.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.lang.Exception


class SafeHomeFragment : Fragment() {
    private lateinit var recycler_file_manager: RecyclerView
    private lateinit var current_path_folder: TextView
    private lateinit var btn_fodlerBack: Button
    private lateinit var btn_refresh: Button
    private var layoutManager: RecyclerView.LayoutManager? = null

    @SuppressLint("ResourceType")
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        //INFLATER
        val home = inflater.inflate(R.layout.fragment_safe_home, container, false)

        //if first start - set home folder to singleton
        if (current.haveToChange)
            current.path = Environment.getExternalStorageDirectory()!!.absolutePath

        //in other case we dont need to setup home directory
        current.haveToChange = false

        btn_fodlerBack = home.findViewById(R.id.btn_fodlerBack)
        current_path_folder = home.findViewById(R.id.current_path_folder)
        recycler_file_manager = home.findViewById(R.id.recycler_file_manager)
        btn_refresh = home.findViewById(R.id.btn_refresh)

        //create explorer to scan files
        val explorer = CustomExplorer()

        //FIRST directory scan
        explorer.scanFolder()

        //Create RecyclerManager
        val mAdapter = RecyclerViewManagerAdapter(requireContext())

        //Set liner
        layoutManager = LinearLayoutManager(requireContext())
        recycler_file_manager.layoutManager = layoutManager

        //init recyc
        RecyclerUpdate(requireContext(), recycler_file_manager).refreshList()

        //RECYCLER
        //
        recycler_file_manager.adapter = mAdapter

        //CURRENT PATH OBSERVER
        val livePath = current.getData()

        var stringObserver = Observer<String> { newString ->
            //update root path
            current_path_folder.text = newString

            //ProgressBar
            if (current.isProcessing)
                circular_encrypt_bar.visibility = View.VISIBLE
            else
                circular_encrypt_bar.visibility = View.GONE

            //scan & update filelist
            explorer.scanFolder()

            //create diff utill to find diff
            val utils = ListDiffUtils(current.oldItemList, current.fileList)
            val diffResult = DiffUtil.calculateDiff(utils)

            //refreshOldValues
            current.refrshOld()

            //refresh recycler if diff has detected
            diffResult.dispatchUpdatesTo(mAdapter)
        }

        livePath?.observe(viewLifecycleOwner, stringObserver)

        //BTNLSTNNR
        //
        btn_fodlerBack.setOnClickListener {

            //root folder check
            if (current.path == Environment.getExternalStorageDirectory()!!.absolutePath) {
                try {
                    GlobalScope.launch {
                        vibratePhone()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                Toast.makeText(requireContext(), "THIS IS ALREADY ROOT FOLDER", Toast.LENGTH_LONG).show()
                btn_fodlerBack.animate().apply {
                    duration = 350
                    rotation(140f)

                }.withEndAction {
                    btn_fodlerBack.animate().apply {
                        duration = 350
                        rotation(0f)
                    }
                }

            } else {
                btn_fodlerBack.animate().apply {
                    duration = 400
                    rotationXBy(360f)
                }.start()

                explorer.folderBack()
                current.refresh()
            }
        }

        btn_refresh.setOnClickListener {
            btn_refresh.animate().apply {
                duration = 350
                rotation(360f)
            }.start()

            explorer.scanFolder()
            current.refresh()
        }
        return home
    }

    private fun vibratePhone() {
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val canVibrate: Boolean = vibrator.hasVibrator()

        if (canVibrate) {
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(200)
            }
        }
    }

    companion object {
        private const val TAG = "SafeHomeFragment"
    }
}