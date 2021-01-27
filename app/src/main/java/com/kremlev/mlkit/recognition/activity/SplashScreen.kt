package com.kremlev.mlkit.recognition.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("Life", "SplashScreen onCreate")

        reqPermissions()
    }

    private fun reqPermissions() {

        //PERMISSION->RECOGNIZE
        if (allPermissionsGranted()) {

            //start Main
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {

            //require permission
            ActivityCompat.requestPermissions(
                    this,
                    SplashScreen.REQUIRED_PERMISSIONS,
                    SplashScreen.REQUEST_CODE_PERMISSIONS
            )

            //to avoid dead end
            reqPermissions()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
                this, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.VIBRATE,
                Manifest.permission.INTERNET)
    }
}