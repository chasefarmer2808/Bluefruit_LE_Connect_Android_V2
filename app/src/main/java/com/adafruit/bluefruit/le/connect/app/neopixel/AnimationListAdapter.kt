package com.adafruit.bluefruit.le.connect.app.neopixel

import android.content.Context
import android.graphics.Color
import android.graphics.ColorSpace
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.adafruit.bluefruit.le.connect.R
import com.adafruit.bluefruit.le.connect.ble.central.UartPacketManager.RAINBOW_COMMAND
import com.adafruit.bluefruit.le.connect.ble.central.UartPacketManager.STOP_ANIMATION
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val RAINBOW_ANIM_POS = 0
const val THEATRE_ANIM_POS = 1
const val RAINBOW_DELAY_MS = 40.toLong()
const val MAX_HUE = 360

class AnimationListAdapter(private val mContext: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val defaultAnimations = arrayListOf("Rainbow", "Theatre")
    private val mAnimationFlags = mutableListOf(false, false)
    private val mNeopixelManager: NeopixelManager by lazy { (mContext as NeopixelActivity).mNeopixelManager }
    private var mAnimJob: Job? = null

    inner class AnimationViewHolder(animView: View) :
            RecyclerView.ViewHolder(animView) {
        val playBtn: Button = animView.findViewById(R.id.playPauseBtn)

        init {
            playBtn.setOnClickListener {
                // Pause everything except current pos and toggle current pos.
                mAnimationFlags.forEachIndexed { index, flag ->
                    if (index == adapterPosition) {
                        mAnimationFlags[index] = !flag
                    }
                    else {
                        mAnimationFlags[index] = false
                    }
                }

                when (adapterPosition) {
                    RAINBOW_ANIM_POS -> {
                        if (mAnimationFlags[adapterPosition]) {
                            mNeopixelManager.sendCommand(byteArrayOf(RAINBOW_COMMAND))
                        }
                        else {
                            mNeopixelManager.sendCommand(byteArrayOf(STOP_ANIMATION))
                        }
                    }
                    THEATRE_ANIM_POS -> {
                        if (mAnimationFlags[adapterPosition]) {
                            mNeopixelManager.sendCommand(byteArrayOf(0x48))
                        }
                        else {
                            mNeopixelManager.sendCommand(byteArrayOf(STOP_ANIMATION))
                        }
                    }
                }
            }
        }
    }

    fun cancelAllAnimations() {
        mAnimJob?.cancel()
    }

    override fun getItemCount(): Int {
        return defaultAnimations.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.animation_list_item, parent, false)
        return AnimationViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as AnimationViewHolder).playBtn.text = defaultAnimations[position]
    }

    private suspend fun rainbow() {
        var currHue = 0

        while (true) {
            for (i in 0 until mNeopixelManager.neopixelBoard.width) {
                val color = Color.HSVToColor(floatArrayOf(currHue.toFloat(), 1.toFloat(), 1.toFloat()))
                mNeopixelManager.setPixelColor(color, row = i.toByte())
                currHue++
                currHue = currHue.rem(MAX_HUE)
                delay(RAINBOW_DELAY_MS)
            }
        }
    }

    private suspend fun theatre() {
        while (true) {
            for (i in 0 until mNeopixelManager.neopixelBoard.width) {
                mNeopixelManager.setPixelColor(Color.CYAN, row = i.toByte())

                val prevIndex = if (i == 0) {
                    mNeopixelManager.neopixelBoard.width - 1
                }
                else {
                    i - 1
                }

                mNeopixelManager.setPixelColor(Color.BLACK, row = prevIndex.toByte())
                delay(2000)
            }
        }
    }
}