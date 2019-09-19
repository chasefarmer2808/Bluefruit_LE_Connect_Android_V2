package com.adafruit.bluefruit.le.connect.app.neopixel

import android.bluetooth.BluetoothGatt
import android.content.Context
import android.graphics.Color
import com.adafruit.bluefruit.le.connect.app.ConnectedPeripheralFragment.SuccessHandler
import com.adafruit.bluefruit.le.connect.ble.BleUtils
import com.adafruit.bluefruit.le.connect.ble.central.BlePeripheral
import com.adafruit.bluefruit.le.connect.ble.central.BlePeripheralUart
import com.adafruit.bluefruit.le.connect.ble.central.BleScanner
import com.adafruit.bluefruit.le.connect.ble.central.UartPacketManager
import com.adafruit.bluefruit.le.connect.ble.central.UartPacketManager.*

const val SKETCH_VERSION = "Neopixel v2."

internal class NeopixelManager(val mContext: Context, peripheralId: String) {

    companion object {
        fun wheel(pos: Byte): Color {
            var tempPos: Int = 255 - pos
            var red = 0
            var green = 0
            var blue = 0

            when {
                tempPos < 85 -> {
                    red = 255 - tempPos * 3
                    blue = tempPos * 3
                }
                tempPos < 170 -> {
                    tempPos -= 85
                    green = tempPos * 3
                    blue = 255 - tempPos * 3
                }
                else -> {
                    tempPos -= 170
                    red = tempPos * 3
                    green = 255 - tempPos * 3
                }
            }

            return Color.valueOf(red.toFloat(), green.toFloat(), blue.toFloat())
        }
    }

    var ready: Boolean = false
    var sketchChecked = false
    var neopixelComponents = NeopixelComponents(NeopixelComponents.kComponents_grb)
    val uartManager: UartPacketManager by lazy { UartPacketManager(mContext, null, false, null) }
    var blePeripheral: BlePeripheral? = null
    val blePeripheralUart: BlePeripheralUart
    var neopixelBoard: NeopixelBoard
    var m400HzEnabled = false
    private val mUsingWhite: Boolean by lazy { neopixelComponents.numComponents == 4 }

    init {
        blePeripheral = BleScanner.getInstance().getPeripheralWithIdentifier(peripheralId)
        blePeripheralUart = BlePeripheralUart(blePeripheral!!)
        neopixelBoard = NeopixelBoard(mContext, 0)
    }

    fun initNeopixel(board: NeopixelBoard, successHandler: SuccessHandler) {
        neopixelBoard = board
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

    fun setPixelColor(color: Int, colorW: Float = 0.toFloat(), row: Byte = 0, col: Byte = 0, successHandler: SuccessHandler? = null) {
        val red: Byte = Color.red(color).toByte()
        val green: Byte = Color.green(color).toByte()
        val blue: Byte = Color.blue(color).toByte()

        val command = arrayListOf(SET_COLOR_COMMAND, row, col, red, green, blue)

        if (mUsingWhite) {
            val white: Byte = (colorW * 255).toByte()
            command.add(white)
        }

        sendCommand(command.toByteArray(), successHandler)
    }

    fun setBrightness(brightness: Float, successHandler: SuccessHandler? = null) {
        val brightnessValue = (brightness * 255).toByte()
        val command = byteArrayOf(SET_BRIGHTNESS_COMMAND, brightnessValue)
        sendCommand(command, successHandler)
    }

    fun setAllPixelColor(color: Int, colorW: Float = 0.toFloat(), successHandler: SuccessHandler? = null) {
        val red = Color.red(color).toByte()
        val green = Color.green(color).toByte()
        val blue = Color.blue(color).toByte()

        val command = arrayListOf(SET_ALL_COMMAND, red, green, blue)

        if (mUsingWhite) {
            val colorWValue = (colorW * 255).toByte()
            command.add(colorWValue)
        }
        sendCommand(command.toByteArray(), successHandler)
    }

    fun sendCommand(command: ByteArray, successHandler: SuccessHandler? = null) {
        uartManager.sendAndWaitReply(blePeripheralUart, command) { status, value ->
            var success = false
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (value != null) {
                    val result = BleUtils.bytesToText(value, false)
                    success = result.startsWith("OK")
                }
            }

            successHandler?.result(success)
        }
    }
}