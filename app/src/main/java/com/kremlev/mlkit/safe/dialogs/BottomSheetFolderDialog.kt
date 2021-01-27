package com.kremlev.mlkit.safe.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kremlev.mlkit.R
import com.kremlev.mlkit.safe.fileNav.Explorer.CustomExplorer
import com.kremlev.mlkit.safe.fileNav.SafeState
import com.kremlev.mlkit.safe.fileNav.current
import kotlinx.android.synthetic.main.bottom_sheet_folder_dialog.view.*
import java.io.File

class BottomSheetFolderDialog : BottomSheetDialogFragment() {
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val bottom_sheet_folder_view = inflater.inflate(
                R.layout.bottom_sheet_folder_dialog,
                container,
                false
        )

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


            bottom_sheet_folder_view.tv_bottom_folder_name.text = path.substringAfterLast("/")

            bottom_sheet_folder_view.delete_folder_btn.setOnClickListener {
                var filename: String = path.substringAfterLast("/")
                dismiss()
                File(path).deleteRecursively()
                Toast.makeText(context, "Folder ${filename} deleted", Toast.LENGTH_SHORT).show()
                explorer.scanFolder()
                SafeState.refresh()
                current.refresh()
            }
        } else {
            dismiss()
            explorer.scanSafeFolder()
            SafeState.refresh()
            current.refresh()
        }

        return bottom_sheet_folder_view
    }
}