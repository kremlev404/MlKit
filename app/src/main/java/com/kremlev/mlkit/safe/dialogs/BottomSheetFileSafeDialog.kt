package com.kremlev.mlkit.safe.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kremlev.mlkit.R
import com.kremlev.mlkit.safe.crypt.Cryption
import com.kremlev.mlkit.safe.fileNav.Explorer.CustomExplorer
import com.kremlev.mlkit.safe.fileNav.SafeState
import com.kremlev.mlkit.safe.fileNav.current
import kotlinx.android.synthetic.main.bottom_sheet_file_dialog.view.*
import kotlinx.coroutines.*
import java.io.File

class BottomSheetFileSafeDialog : BottomSheetDialogFragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val bottom_sheet_file_view = inflater.inflate(R.layout.bottom_sheet_file_dialog, container, false)

        val explorer: CustomExplorer = CustomExplorer()

        var posOk = false

        try {
            File(current.fileList[current.position].FilePath).isFile
            posOk = true
        } catch (e: Exception) {
            posOk = false
            e.printStackTrace()
        }
        if (posOk) {
            val path = current.fileList[current.position].FilePath
            val encryption = Cryption(path)

            bottom_sheet_file_view.tv_bottom_files_name.text = path.substringAfterLast("/")
            current.refresh()

            bottom_sheet_file_view.file_bottom_copy_link.setOnClickListener {
                val myClipboard: ClipboardManager = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val myClip = ClipData.newPlainText("path", path);
                myClipboard?.setPrimaryClip(myClip)
                Toast.makeText(requireContext(), "Copied $path", Toast.LENGTH_SHORT).show()
                dismiss()
            }

            bottom_sheet_file_view.file_bottom_encrypt.setOnClickListener {
                var viewModelJob = Job()
                val myScope = CoroutineScope(Dispatchers.Main + viewModelJob)

                myScope.launch {
                    withContext(Dispatchers.Main) {

                        //set loading gui
                        current.isProcessing = true
                        current.refresh()
                        dismiss()

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
                Toast.makeText(
                        requireContext(),
                        "Encrypting...",
                        Toast.LENGTH_SHORT
                ).show()
            }
            bottom_sheet_file_view.file_bottom_delete.setOnClickListener {
                var filename: String = path.substringAfterLast("/")
                File(path).deleteRecursively()
                Toast.makeText(
                        requireContext(),
                        "Folder ${filename} deleted",
                        Toast.LENGTH_SHORT
                ).show()
                dismiss()
                explorer.scanFolder()
                SafeState.refresh()
                current.refresh()
            }
        } else {
            dismiss()
            explorer.scanFolder()
            SafeState.refresh()
            current.refresh()
        }
        return bottom_sheet_file_view
    }
}