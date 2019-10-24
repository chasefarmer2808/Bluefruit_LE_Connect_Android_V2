package com.adafruit.bluefruit.le.connect.app.neopixel


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.adafruit.bluefruit.le.connect.R

class NeopixelAnimationFragment : Fragment() {

    private lateinit var mAnimationRecyclerView: RecyclerView
    private val mAnimationAdapter: AnimationListAdapter by lazy { AnimationListAdapter(activity as Context) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val rootView: View = inflater.inflate(R.layout.fragment_neopixel_animation, container, false)

        mAnimationRecyclerView = rootView.findViewById<RecyclerView>(R.id.animation_recycler_view).apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = mAnimationAdapter
        }

        return rootView
    }
}
