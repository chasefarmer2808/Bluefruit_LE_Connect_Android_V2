package com.adafruit.bluefruit.le.connect.app.neopixel

import android.bluetooth.BluetoothGatt
import android.content.Context
import com.adafruit.bluefruit.le.connect.BuildConfig
import com.adafruit.bluefruit.le.connect.ble.BleUtils
import com.adafruit.bluefruit.le.connect.ble.central.BlePeripheral
import com.adafruit.bluefruit.le.connect.ble.central.BlePeripheralUart
import com.adafruit.bluefruit.le.connect.ble.central.BleScanner
import com.adafruit.bluefruit.le.connect.ble.central.UartPacketManager
import com.adafruit.bluefruit.le.connect.ble.central.UartPacketManager.GET_VERSION_COMMAND

const val SKETCH_VERSION = "Neopixel v2."

class NeopixelManager(val mContext: Context, peripheralId: String) {
    var ready: Boolean = false

    private val mUartManager: UartPacketManager = UartPacketManager(mContext, null, false, null)
    private val mNeopixelComponents = NeopixelComponents(if (BuildConfig.DEBUG) NeopixelComponents.kComponents_grbw else NeopixelComponents.kComponents_grb)
    private val mBlePeripheralUart: BlePeripheralUart
    private var mNeopixelBoard: NeopixelBoard
    private var mBlePeripheral: BlePeripheral? = null

    init {
        mBlePeripheral = BleScanner.getInstance().getPeripheralWithIdentifier(peripheralId)
        mBlePeripheralUart = BlePeripheralUart(mBlePeripheral!!)
        mNeopixelBoard = NeopixelBoard(mContext, 0)
    }

    private fun hasValidSketch(): Boolean {
        var res = false
        mUartManager.sendAndWaitReply(mBlePeripheralUart, ByteArray(GET_VERSION_COMMAND)) { status, value ->
            if (status == BluetoothGatt.GATT_SUCCESS && value != null) {
                val result = BleUtils.bytesToText(value, false)
                if (result.startsWith(SKETCH_VERSION)) {
                    res = true
                }
            }
        }

        return res
    }

    private fun initNeopixel() {
        ready = false
        val command = byteArrayOf(
                GET_VERSION_COMMAND,
                mNeopixelBoard.width,
                mNeopixelBoard.height,
                mNeopixelBoard.stride)
    }
}