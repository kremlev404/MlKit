package com.kremlev.mlkit.safe.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.kremlev.mlkit.R
import com.kremlev.mlkit.safe.data.dataCrypt
import com.kremlev.mlkit.safe.data.dataFile
import com.kremlev.mlkit.safe.dialogs.BottomSheet_SAFE_Dialog
import com.kremlev.mlkit.safe.fileNav.SafeState
import java.io.File


class RecyclerViewSafeAdapter(
        val context: Context
) : RecyclerView.Adapter<RecyclerViewSafeAdapter.ViewSafeHolder>() {
    class ViewSafeHolder(itemSafeView: View) : RecyclerView.ViewHolder(itemSafeView) {

        var SafeName_tv: TextView
        var SafePreviewView: ImageView
        var oldfileSafeName_tv: TextView

        init {
            SafePreviewView = itemSafeView.findViewById(R.id.fileSafeIconView)
            SafeName_tv = itemSafeView.findViewById(R.id.fileSafeName_tv)
            oldfileSafeName_tv = itemSafeView.findViewById(R.id.oldfileSafeName_tv)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewSafeHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_safe_item, parent, false)
        return ViewSafeHolder(v)
    }

    override fun onBindViewHolder(holder: ViewSafeHolder, position: Int) {
        val files: dataFile = SafeState.fileList[holder.adapterPosition]

        SafeState.position = holder.adapterPosition
        holder.SafeName_tv.text = files.FileName

        var mPathReference: DatabaseReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child(files.FileName)
        //var to get data
        var receivedData: dataCrypt = dataCrypt()

        mPathReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds: DataSnapshot in dataSnapshot.children) {
                    receivedData = (ds.getValue(dataCrypt::class.java)!!)
                    Log.e("oldPAth", " ${receivedData.oldFilePath}")
                    val fileName = receivedData.oldFilePath.substringAfterLast("/")
                    holder.oldfileSafeName_tv.text = fileName
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.e("veListener", "Failed to read value.", error.toException())
            }
        })


        if (File(files.FilePath).isFile()) {
            holder.itemView.setOnClickListener {
                val bottomSheetDialog = BottomSheet_SAFE_Dialog()
                val manager: FragmentManager = (context as AppCompatActivity).supportFragmentManager
                SafeState.position = holder.adapterPosition
                bottomSheetDialog.show(manager, "TAG")
            }
        }

    }

    override fun getItemCount(): Int {
        return SafeState.fileList.size
    }
}



