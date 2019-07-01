package com.adafruit.bluefruit.le.connect.app.neopixel


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.ListFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView

import com.adafruit.bluefruit.le.connect.R

class NeopixelAnimationFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val rootView: View = inflater.inflate(R.layout.fragment_neopixel_animation, container, false)

        val animListAdapter = ArrayAdapter<String>(activity, R.layout.animation_list_item, R.id.animation_item, arrayOf("test"))
        val animList: ListView = rootView.findViewById(R.id.animation_list)
        animList.adapter = animListAdapter

        return rootView
    }
}
