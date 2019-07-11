package com.adafruit.bluefruit.le.connect.app.neopixel

import android.content.Context
import android.util.Log
import com.adafruit.bluefruit.le.connect.utils.FileUtils
import org.json.JSONArray
import org.json.JSONException
import java.io.File

class NeopixelBoard() {
    private lateinit var name: String
    var width: Byte = 0
    var height: Byte = 0
    var stride: Byte = 0

    constructor(name: String, width: Byte, height: Byte, stride: Byte): this() {
        this.name = name
        this.width = width
        this.height = height
        this.stride = stride
    }

    constructor(context: Context, standardIndex: Int) : this() {
        val boardsJsonString = FileUtils.readAssetsFile("neopixel" + File.separator + "NeopixelBoards.json", context.assets)

        try {
            val boardsArray = JSONArray(boardsJsonString)
            val boardJson = boardsArray.getJSONObject(standardIndex)

            name = boardJson.getString("name")
            width = boardJson.getInt("width").toByte()
            height = boardJson.getInt("height").toByte()
            stride = boardJson.getInt("stride").toByte()
        }
        catch (e: JSONException) {
            Log.e(NeopixelBoard::class.toString(), "Invalid board parameters")
        }
    }
}