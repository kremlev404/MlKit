package com.kremlev.mlkit.recognition.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.kremlev.mlkit.R
import com.kremlev.mlkit.ViewPageAdapter
import com.kremlev.mlkit.recognition.fragments.HomeFragment
import com.kremlev.mlkit.recognition.fragments.SettingFragment
import kotlinx.android.synthetic.main.activity_main.*

open class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.e("Life", " MAIN onCreate")

        //setup fragments
        setUpTabs()
    }

    private fun setUpTabs() {
        val adapter = ViewPageAdapter(supportFragmentManager)
        adapter.addFragment(HomeFragment(), "Recognizer")
        adapter.addFragment(SettingFragment(), "Setting")
        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager)
        tabs.getTabAt(0)!!.setIcon(R.drawable.ic_facial_recognition)
        tabs.getTabAt(1)!!.setIcon(R.drawable.ic_stream_settings)
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.e("Life", "MAIN onDestroy")
    }
}