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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class NeopixelAnimationFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val rootView: View = inflater.inflate(R.layout.fragment_neopixel_animation, container, false)

        val animListAdapter = ArrayAdapter<String>(activity, R.layout.animation_list_item, arrayOf("test"))
        val animList: ListView = rootView.findViewById(R.id.animation_list)
        animList.adapter = animListAdapter

        return rootView
    }
}
