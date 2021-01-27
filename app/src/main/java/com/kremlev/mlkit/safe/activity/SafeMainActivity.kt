package com.kremlev.mlkit.safe.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.kremlev.mlkit.R
import com.kremlev.mlkit.ViewPageAdapter
import com.kremlev.mlkit.safe.fragments.SafeFragment
import com.kremlev.mlkit.safe.fragments.SafeHomeFragment
import com.kremlev.mlkit.safe.fragments.SafeSettingFragment
import kotlinx.android.synthetic.main.activity_safe_main.*


@IgnoreExtraProperties
data class User(
        var username: String? = "",
        var email: String? = ""
)

class SafeMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_safe_main)

        setUpTabs()
    }

    private fun setUpTabs() {
        val adapter = ViewPageAdapter(supportFragmentManager)
        adapter.addFragment(SafeHomeFragment(), "EXPLORER")
        adapter.addFragment(SafeFragment(), "VAULT")
        adapter.addFragment(SafeSettingFragment(), "SETTING")
        safeViewPager.adapter = adapter
        safe_tabs.setupWithViewPager(safeViewPager)
        safe_tabs.getTabAt(0)!!.setIcon(R.drawable.ic_folder_icon)
        safe_tabs.getTabAt(1)!!.setIcon(R.drawable.ic_on_security)
        safe_tabs.getTabAt(2)!!.setIcon(R.drawable.ic_safe_settings)
    }
}