package com.adafruit.bluefruit.le.connect.app.neopixel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.adafruit.bluefruit.le.connect.R

class NeopixelActivity : AppCompatActivity() {
    lateinit var mNeopixelManager: NeopixelManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val i = intent
        val peripheralId = i.getStringExtra("PERIPHERAL_ID")

        mNeopixelManager = NeopixelManager(this, peripheralId)

        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right, R.animator.slide_in_right, R.animator.slide_out_left)
        ft.replace(R.id.contentLayout, NeopixelFragment.newInstance(peripheralId), "Modules")
        ft.addToBackStack(null)
        ft.commit()
    }
}