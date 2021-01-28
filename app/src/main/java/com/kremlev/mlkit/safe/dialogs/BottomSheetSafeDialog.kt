package com.kremlev.mlkit.safe.dialogs


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kremlev.mlkit.R
import com.kremlev.mlkit.safe.crypt.Cryption
import com.kremlev.mlkit.safe.fileNav.Explorer.CustomExplorer
import com.kremlev.mlkit.safe.fileNav.SafeState
import com.kremlev.mlkit.safe.fileNav.current
import kotlinx.android.synthetic.main.bottom_safe_sheet_layout.view.*
import kotlinx.coroutines.*
import java.io.File


class BottomSheetSafeDialog() : BottomSheetDialogFragment() {
    @SuppressLint("ResourceType")
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val bottom_sheet_view = inflater.inflate(R.layout.bottom_safe_sheet_layout, container, false)

        val explorer: CustomExplorer = CustomExplorer()

        var posOk = false

        try {
            File(current.fileList[current.position].FilePath).isFile
            posOk = true
        } catch (e: java.lang.Exception) {
            posOk = false
            e.printStackTrace()
        }
        if (posOk) {


            val path = current.fileList[current.position].FilePath
            val encryption = Cryption(path)


            val imageViewViewFull: ImageView
            val videoViewViewFull: VideoView

            val fullDialog = Dialog(requireContext())
            val fullVideoDialog = Dialog(requireContext())

            fullDialog.setContentView(R.layout.full_size_dialog_iamge_layout)
            fullVideoDialog.setContentView(R.layout.full_size_dialog_video_layout)

            val btn_video_pp: Button = fullVideoDialog.findViewById<Button>(com.kremlev.mlkit.R.id.btn_video_pp)
            val hint_for_play_tv: TextView = fullVideoDialog.findViewById<TextView>(com.kremlev.mlkit.R.id.hint_for_play_tv)
            videoViewViewFull = fullVideoDialog.findViewById(com.kremlev.mlkit.R.id.videoViewViewFull)
            imageViewViewFull = fullDialog.findViewById(com.kremlev.mlkit.R.id.imageViewViewFull)

            bottom_sheet_view.tv_bottom_file_name.setText(path.substringAfterLast("/"))

            //setupUri
            val uri: Uri = Uri.fromFile(File(path))

            bottom_sheet_view.bottom_encrypt.setOnClickListener {
                var viewModelJob = Job()
                val myScope = CoroutineScope(Dispatchers.Default + viewModelJob)

                myScope.launch {
                    withContext(Dispatchers.Main) {
                        //set loading gui
                        current.isProcessing = true
                        dismiss()
                        current.refresh()

                        //encrypt in background
                        withContext(Dispatchers.IO) {
                            encryption.encrypt()

                            //turn off loading gui in main thread
                            withContext(Dispatchers.Main) {
                                if (encryption.CRYPT_ERROR)
                                    Toast.makeText(requireContext(), "ENCTYPTION ERROR", Toast.LENGTH_SHORT)
                                current.isProcessing = false
                                current.refresh()
                                SafeState.refresh()
                            }
                        }
                    }
                }
            }

            bottom_sheet_view.viewFull.setOnClickListener {

                //forImage
                if (explorer.checkIfImage(path)) {

                    //setupImage
                    Glide.with(requireActivity())
                            .load(uri)
                            .into(imageViewViewFull)
                    try {
                        dismiss()
                        //Show
                        fullDialog.show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    //forVideo
                } else {

                    //setupVideoView
                    videoViewViewFull?.setVideoURI(uri)
                    btn_video_pp.setOnClickListener {
                        dismiss()
                        //toIconChange
                        val isPlaying = videoViewViewFull.isPlaying

                        //pause/start
                        btn_video_pp.setCompoundDrawablesWithIntrinsicBounds(0,
                                if (isPlaying)
                                    R.drawable.ic_video_play
                                else
                                    R.drawable.ic_pause,
                                0, 0)

                        if (isPlaying)
                            videoViewViewFull.pause()
                        else {
                            hint_for_play_tv.setVisibility(View.INVISIBLE)
                            videoViewViewFull.start()
                        }
                    }
                    fullVideoDialog.show()
                }
            }
            Log.e("POS", "${current.position}")
            bottom_sheet_view.bottom_delete.setOnClickListener {
                File(path).delete()
                Log.e("POS", "${current.position}")
                explorer.scanFolder()
                Log.e("POS", "${current.position}")
                current.refresh()
                Log.e("POS", "${current.position}")
                SafeState.refresh()
            }

            bottom_sheet_view.bottom_share.setOnClickListener {

                //to void Exception ( use CONTENT://(FileProvider) )
                val builder = StrictMode.VmPolicy.Builder()
                StrictMode.setVmPolicy(builder.build())

                try {

                    //setupIntent
                    val share = Intent(Intent.ACTION_SEND)
                    share.setType("*/*")
                    share.putExtra(Intent.EXTRA_STREAM, uri)

                    //shareFile
                    startActivity(share)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            dismiss()
            explorer.scanSafeFolder()
            SafeState.refresh()
            current.refresh()
        }
        return bottom_sheet_view
    }
}