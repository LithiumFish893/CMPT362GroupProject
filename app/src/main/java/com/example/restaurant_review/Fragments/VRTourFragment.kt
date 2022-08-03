package com.example.restaurant_review.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.restaurant_review.Activities.CreateVRTourActivity
import com.example.restaurant_review.Activities.VRViewActivity
import com.example.restaurant_review.R

class VRTourFragment : Fragment() {
    private lateinit var viewVrButton: Button
    private lateinit var submitVrButton: Button
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val pView = inflater.inflate(R.layout.fragment_vr_tour, container, false)
        viewVrButton = pView.findViewById(R.id.view_vr_tour_button)
        submitVrButton = pView.findViewById(R.id.submit_vr_tour_button)
        viewVrButton.setOnClickListener {
            val intent = Intent(requireContext(), VRViewActivity::class.java)
            startActivity(intent)
        }
        submitVrButton.setOnClickListener {
            val intent = Intent(requireContext(), CreateVRTourActivity::class.java)
            startActivity(intent)
        }
        return pView
    }
}