package com.kremlev.mlkit.recognition.bottom_dialogs

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kremlev.mlkit.R
import com.kremlev.mlkit.recognition.activity.SplashScreen
import kotlinx.android.synthetic.main.bottom_sheet_rotation.view.*


class BottomSheetRotationDialog : BottomSheetDialogFragment() {
    @Suppress("DEPRECATION")
    private lateinit var pref: SharedPreferences
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val rotationView = inflater.inflate(R.layout.bottom_sheet_rotation, container, false)

        pref = PreferenceManager.getDefaultSharedPreferences(context)

        rotationView.rotation_0.setOnClickListener {

            saveRotation(0)

            wipePref()

            dismiss()
        }

        rotationView.rotation_90.setOnClickListener {
            saveRotation(90)

            wipePref()

            dismiss()
        }
        rotationView.rotation_180.setOnClickListener {
            saveRotation(180)

            wipePref()

            dismiss()
        }
        rotationView.rotation_270.setOnClickListener {

            saveRotation(270)
            wipePref()

            dismiss()
        }
        return rotationView
    }


    private fun wipePref() {
        Toast.makeText(
                requireContext(),
                "Changes Are Being Made, Wait",
                Toast.LENGTH_SHORT
        ).show()

        //clear userData to rescan photos with new rotation
        try {
            pref.edit().remove("UserData").apply()
            pref.edit().remove("DataChanged").apply()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        restartActivity()
    }

    fun saveRotation(angle: Int) {
        pref.edit().putInt("Angle", angle).apply()
    }

    private fun restartActivity() {
        val intent = Intent(requireContext(), SplashScreen::class.java)
        startActivity(intent)
        activity?.finish()
    }
}