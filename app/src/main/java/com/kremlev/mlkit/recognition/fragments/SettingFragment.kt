package com.kremlev.mlkit.recognition.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.kremlev.mlkit.R
import com.kremlev.mlkit.recognition.activity.UserAdding
import com.kremlev.mlkit.recognition.bottom_dialogs.BottomSheetRotationDialog
import kotlinx.android.synthetic.main.fragment_setting.view.*
import java.util.concurrent.Executor


class SettingFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_setting, container, false)

        rootView.select_rotation.setOnClickListener {
            val bottomSheetDialog = BottomSheetRotationDialog()
            val manager: FragmentManager = (context as AppCompatActivity).supportFragmentManager

            bottomSheetDialog.show(manager, "TAG")
        }

        rootView.new_user.setOnClickListener {
            //finger print
            getAuthStatus()
        }

        return rootView
    }

    private fun getAuthStatus() {
        val executor = ContextCompat.getMainExecutor(requireContext())
        val biometricManager = BiometricManager.from(requireContext())

        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                authUser(executor)
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(
                        requireContext(),
                        "BIOMETRIC_ERROR_NO_HARDWARE",
                        Toast.LENGTH_SHORT
                ).show()
                toUserAdd()
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Toast.makeText(
                        requireContext(),
                        "BIOMETRIC_ERROR_HW_UNAVAILABLE",
                        Toast.LENGTH_SHORT
                ).show()

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(
                        requireContext(),
                        "BIOMETRIC_ERROR_NONE_ENROLLED",
                        Toast.LENGTH_SHORT
                ).show()
                toUserAdd()
            }
        }
    }

    fun toUserAdd() {
        Toast.makeText(requireContext(),
                "GOING TO USER SETUP ACTIVITY",
                Toast.LENGTH_SHORT).show()
        try {
            val intent = Intent(requireContext(), UserAdding::class.java)
            startActivity(intent)
            activity?.finish()
        } catch (e: java.lang.Exception) {
            Log.e("SettingFragment", "Going to UserAdding to home error")
        }
    }

    private fun authUser(executor: Executor) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("ML KIT")
                .setSubtitle("Please Log In")
                .setDeviceCredentialAllowed(true)
                .build()

        BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        toUserAdd()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Toast.makeText(
                                requireContext(),
                                "Authentication Error",
                                Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(
                                requireContext(),
                                "Authentication Failed",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                }).authenticate(promptInfo)
    }
}