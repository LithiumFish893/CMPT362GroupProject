package com.example.restaurant_review.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.restaurant_review.Activities.VRViewActivity
import com.example.restaurant_review.R

class NearbyFeedFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        val pView = inflater.inflate(R.layout.sm_fragment_nearby_feed, container)
        return pView
    }
}