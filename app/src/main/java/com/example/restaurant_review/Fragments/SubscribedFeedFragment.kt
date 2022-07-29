package com.example.restaurant_review.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.restaurant_review.R

class SubscribedFeedFragment : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        val pView = inflater.inflate(R.layout.sm_fragment_subscribed_feed, container)

        return pView
    }
}