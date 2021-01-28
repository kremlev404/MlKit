package com.kremlev.mlkit.safe.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kremlev.mlkit.R
import com.kremlev.mlkit.safe.data.dataFile
import com.kremlev.mlkit.safe.fileNav.Explorer.CustomExplorer
import com.kremlev.mlkit.safe.fileNav.current
import com.kremlev.mlkit.safe.dialogs.BottomSheetFileSafeDialog
import com.kremlev.mlkit.safe.dialogs.BottomSheetFolderDialog
import com.kremlev.mlkit.safe.dialogs.BottomSheetSafeDialog
import java.io.File

class RecyclerViewManagerAdapter(
        val context: Context
) : RecyclerView.Adapter<RecyclerViewManagerAdapter.ViewManagerHolder>() {
    var explorer = CustomExplorer()

    class ViewManagerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var FileName_tv: TextView
        var RootFolder_tv: TextView
        var File_preview_view: ImageView

        init {
            File_preview_view = itemView.findViewById(R.id.fileIconView)
            FileName_tv = itemView.findViewById(R.id.FileName_tv)
            RootFolder_tv = itemView.findViewById(R.id.RootFolder_tv)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewManagerHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_manager_item, parent, false)
        return ViewManagerHolder(v)
    }

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: ViewManagerHolder, position: Int) {
        val files: dataFile = current.fileList[holder.adapterPosition]
        holder.FileName_tv.text = files.FileName
        holder.RootFolder_tv.text = files.FilePath
        val size = 196
        current.position = holder.adapterPosition
        if (File(files.FilePath).isFile()) {
            var isMedia = false
            if (explorer.checkIfImage(files.FilePath) || explorer.checkIfVideo(files.FilePath)) {
                val imgFile = File(files.FilePath)
                val uri = Uri.fromFile(imgFile)

                Glide.with(context)
                        .load(uri)
                        .override(size, size)
                        .into(holder.File_preview_view)
                try {
                    holder.itemView.setOnClickListener { v: View ->

                        current.position = holder.adapterPosition

                        val bottomSheetDialog = BottomSheetSafeDialog()
                        val manager: FragmentManager = (context as AppCompatActivity).supportFragmentManager

                        bottomSheetDialog.show(manager, "TAG")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                isMedia = true
            } else if ((explorer.checkType(files.FilePath) != (Int.MAX_VALUE - 1)) && !isMedia) {

                val type = explorer.checkType(files.FilePath)

                //draw file type icon
                holder.File_preview_view.setImageDrawable(
                        ContextCompat.getDrawable(context, type)
                )
                try {
                    holder.itemView.setOnClickListener { v: View ->

                        current.position = holder.adapterPosition
                        val bottomSheetDialog = BottomSheetFileSafeDialog()
                        val manager: FragmentManager = (context as AppCompatActivity).supportFragmentManager

                        bottomSheetDialog.show(manager, "TAG")
                        current.refresh()

                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            } else {
                //draw unknown file icon
                Glide.with(context)
                        .load(R.drawable.ic_unknown)
                        .override(size, size)
                        .into(holder.File_preview_view)
                try {
                    holder.itemView.setOnClickListener { v: View ->

                        current.position = holder.adapterPosition
                        val bottomSheetDialog = BottomSheetFileSafeDialog()
                        val manager: FragmentManager = (context as AppCompatActivity).supportFragmentManager

                        bottomSheetDialog.show(manager, "TAG")
                        current.refresh()
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }

            }
        } else {
            //draw default folder icon
            Glide.with(context)
                    .load(R.drawable.ic_folder_icon)
                    .override(size, size)
                    .into(holder.File_preview_view)
            try {
                //to Open pressed folder
                holder.itemView.setOnClickListener { v: View ->
                    current.path = files.FilePath
                    current.refresh()
                }
                //to view BottomDialog
                holder.itemView.setOnLongClickListener { v: View ->
                    current.position = holder.adapterPosition
                    val bottomSheetDialog = BottomSheetFolderDialog()
                    val manager: FragmentManager = (context as AppCompatActivity).supportFragmentManager
                    bottomSheetDialog.show(manager, "TAG")
                    current.refresh()
                    true
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun getItemCount(): Int {
        return current.fileList.size
    }
}