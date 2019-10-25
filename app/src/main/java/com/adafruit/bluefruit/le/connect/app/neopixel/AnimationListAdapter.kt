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
import com.adafruit.bluefruit.le.connect.ble.central.UartPacketManager.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val RAINBOW_ANIM_POS = 0
const val THEATRE_ANIM_POS = 1
const val RANDOM_FILL_ANIM_POS = 2
const val METEOR_ANIM_POS = 3

class AnimationListAdapter(private val mContext: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val defaultAnimations = arrayListOf("Rainbow", "Theatre", "Random Position", "Meteor")
    private val mAnimationFlags = mutableListOf(false, false, false, false)
    private val mNeopixelManager: NeopixelManager by lazy { (mContext as NeopixelActivity).mNeopixelManager }

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
                            mNeopixelManager.sendCommand(byteArrayOf(THEATRE_COMMAND))
                        }
                        else {
                            mNeopixelManager.sendCommand(byteArrayOf(STOP_ANIMATION))
                        }
                    }
                    RANDOM_FILL_ANIM_POS -> {
                        if (mAnimationFlags[adapterPosition]) {
                            mNeopixelManager.sendCommand(byteArrayOf(RANDOM_FILL_COMMAND))
                        }
                        else {
                            mNeopixelManager.sendCommand(byteArrayOf(STOP_ANIMATION))
                        }
                    }
                    METEOR_ANIM_POS -> {
                        if (mAnimationFlags[adapterPosition]) {
                            mNeopixelManager.sendCommand(byteArrayOf(METEOR_COMMAND))
                        }
                        else {
                            mNeopixelManager.sendCommand(byteArrayOf(STOP_ANIMATION))
                        }
                    }
                }
            }
        }
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
}