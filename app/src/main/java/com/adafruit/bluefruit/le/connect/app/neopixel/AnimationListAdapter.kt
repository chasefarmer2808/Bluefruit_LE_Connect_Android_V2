package com.adafruit.bluefruit.le.connect.app.neopixel

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.adafruit.bluefruit.le.connect.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val RAINBOW_ANIM_POS = 0

class AnimationListAdapter(private val mContext: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val defaultAnimations = arrayListOf("Rainbow")
    private val mAnimationFlags = mutableListOf(false)
    private val mNeopixelManager: NeopixelManager by lazy { (mContext as NeopixelActivity).mNeopixelManager }

    inner class AnimationViewHolder(animView: View) :
            RecyclerView.ViewHolder(animView) {
        val playBtn: Button = animView.findViewById(R.id.playPauseBtn)

        init {
            playBtn.setOnClickListener {
                mAnimationFlags.replaceAll { false }
                when (adapterPosition) {
                    RAINBOW_ANIM_POS -> {
                        mAnimationFlags[adapterPosition] = true
                        GlobalScope.launch {
                            rainbow(adapterPosition)
                        }
                    }
                }
            }
        }
    }

    fun cancelAllAnimations() {
        mAnimationFlags.replaceAll { false }
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

    private suspend fun rainbow(flagPos: Int) {
        while (mAnimationFlags[flagPos]) {
            mNeopixelManager.setAllPixelColor(Color.RED)
            delay(1000)
            mNeopixelManager.setAllPixelColor(Color.BLACK)
            delay(1000)
        }
    }
}