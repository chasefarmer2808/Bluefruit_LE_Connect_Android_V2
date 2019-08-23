package com.adafruit.bluefruit.le.connect.app.neopixel

import android.bluetooth.BluetoothGatt
import android.content.Context
import com.adafruit.bluefruit.le.connect.BuildConfig
import com.adafruit.bluefruit.le.connect.app.ConnectedPeripheralFragment.SuccessHandler
import com.adafruit.bluefruit.le.connect.ble.BleUtils
import com.adafruit.bluefruit.le.connect.ble.central.BlePeripheral
import com.adafruit.bluefruit.le.connect.ble.central.BlePeripheralUart
import com.adafruit.bluefruit.le.connect.ble.central.BleScanner
import com.adafruit.bluefruit.le.connect.ble.central.UartPacketManager
import com.adafruit.bluefruit.le.connect.ble.central.UartPacketManager.GET_VERSION_COMMAND
import com.adafruit.bluefruit.le.connect.ble.central.UartPacketManager.SETUP_COMMAND

const val SKETCH_VERSION = "Neopixel v2."

class NeopixelManager(val mContext: Context, peripheralId: String) {
    var ready: Boolean = false
    var sketchChecked = false
    val uartManager: UartPacketManager = UartPacketManager(mContext, null, false, null)
    var blePeripheral: BlePeripheral? = null
    val blePeripheralUart: BlePeripheralUart
    var neopixelBoard: NeopixelBoard
    val m400HzEnabled = false
    private val neopixelComponents = NeopixelComponents(if (BuildConfig.DEBUG) NeopixelComponents.kComponents_grbw else NeopixelComponents.kComponents_grb)

    init {
        blePeripheral = BleScanner.getInstance().getPeripheralWithIdentifier(peripheralId)
        blePeripheralUart = BlePeripheralUart(blePeripheral!!)
        neopixelBoard = NeopixelBoard(mContext, 0)
    }

    fun initNeopixel(successHandler: SuccessHandler) {
        ready = false
        var enabledByte: Byte = 0

        if (m400HzEnabled) enabledByte = 1

        val command = byteArrayOf(
                SETUP_COMMAND,
                neopixelBoard.width,
                neopixelBoard.height,
                neopixelBoard.stride,
                neopixelComponents.componentValue,
                enabledByte)

        uartManager.sendAndWaitReply(blePeripheralUart, command) { status, value ->
            if (status == BluetoothGatt.GATT_SUCCESS && value != null) {
                val result = BleUtils.bytesToText(value, false)
                ready = result.startsWith("OK")
                successHandler.result(ready)
            }
        }
    }

    fun checkNeopixelSketch(successHandler: SuccessHandler) {
        var res = false
        uartManager.sendAndWaitReply(blePeripheralUart, byteArrayOf(GET_VERSION_COMMAND)) { status, value ->
            if (status == BluetoothGatt.GATT_SUCCESS && value != null) {
                val result = BleUtils.bytesToText(value, false)
                if (result.startsWith(SKETCH_VERSION)) {
                    res = true
                }
            }
            sketchChecked = res
            successHandler.result(res)
        }
    }

    fun isReady(): Boolean {
        return blePeripheralUart.isUartEnabled
    }
}