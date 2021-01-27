package com.kremlev.mlkit.safe.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.firebase.auth.FirebaseAuth
import com.kremlev.mlkit.R
import com.kremlev.mlkit.auth.VaultAuth
import com.kremlev.mlkit.recognition.activity.MainActivity
import com.kremlev.mlkit.recognition.activity.UserAdding
import kotlinx.android.synthetic.main.fragment_safe_setting.*
import kotlinx.android.synthetic.main.fragment_safe_setting.view.*
import java.lang.Exception


class SafeSettingFragment : Fragment() {
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_safe_setting, container, false)

        try {
            root.firebase_username_tv.text = FirebaseAuth.getInstance().currentUser!!.email?.substringBeforeLast("@")
        } catch (e: Exception) {
            e.printStackTrace()
        }


        root.return_to_recognize.setOnClickListener {
            Toast.makeText(requireContext(),
                    "GOING TO RECOGNIZER",
                    Toast.LENGTH_LONG).show()
            try {
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                activity?.finish()
            } catch (e: java.lang.Exception) {
                Log.e("SAFESettingFragment", "Going to RECOGNIZER error")
            }
        }

        root.logout_view.setOnClickListener {
            Toast.makeText(requireContext(),
                    "logout successful".toUpperCase(),
                    Toast.LENGTH_LONG).show()
            try {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(requireContext(), VaultAuth::class.java)
                startActivity(intent)
                activity?.finish()

            } catch (e: java.lang.Exception) {
                Log.e("SAFESettingFragment", "Going to Authenticator ERROR")
            }

        }
        return root
    }

}