package com.kremlev.mlkit.safe.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kremlev.mlkit.R
import com.kremlev.mlkit.safe.crypt.Cryption
import com.kremlev.mlkit.safe.fileNav.Explorer.CustomExplorer
import com.kremlev.mlkit.safe.fileNav.SafeState
import com.kremlev.mlkit.safe.fileNav.current
import kotlinx.android.synthetic.main.bottom_sheet_safe_dialog.view.*
import java.io.File

class BottomSheet_SAFE_Dialog : BottomSheetDialogFragment() {
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val bottom_sheet_safe_view = inflater.inflate(
                R.layout.bottom_sheet_safe_dialog,
                container,
                false
        )

        var posOk = false
        val explorer: CustomExplorer = CustomExplorer()
        try {
            File(current.fileList[current.position].FilePath).isFile
            posOk = true
        } catch (e: Exception) {
            posOk = false
            e.printStackTrace()
        }
        if (posOk) {

            val path = SafeState.fileList[SafeState.position].FilePath

            val decryption = Cryption(path)

            bottom_sheet_safe_view.tv_bottom_safe_file_name.text = path.substringAfterLast("/")

            bottom_sheet_safe_view.file_bottom_decrypt_safe_file.setOnClickListener {
                dismiss()
                decryption.decrypt()
            }

            bottom_sheet_safe_view.file_bottom_delete_safe_file.setOnClickListener {
                File(path).delete()
                dismiss()
                explorer.scanSafeFolder()
                SafeState.refresh()
                current.refresh()
            }
        } else {
            dismiss()
            explorer.scanSafeFolder()
            SafeState.refresh()
            current.refresh()
        }

        return bottom_sheet_safe_view
    }

}