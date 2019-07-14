package com.adafruit.bluefruit.le.connect.app.neopixel

import android.os.Bundle
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import com.adafruit.bluefruit.le.connect.R
import com.adafruit.bluefruit.le.connect.ble.central.BlePeripheral
import com.adafruit.bluefruit.le.connect.ble.central.BlePeripheralUart
import com.adafruit.bluefruit.le.connect.ble.central.BleScanner
import com.adafruit.bluefruit.le.connect.ble.central.UartPacketManager

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