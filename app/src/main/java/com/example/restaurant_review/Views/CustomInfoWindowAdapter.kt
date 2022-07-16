package com.example.restaurant_review.Views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.restaurant_review.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import java.util.*

/**
 * Custom Info Windows Class Implementation
 *
 * To display the custom info windows when user clicked the marker on map
 */
// Learned from Youtube: Custom Marker Info Window by CodingWithMitch
// Reference: https://www.youtube.com/watch?v=DhYofrJPzlI&list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt&index=11&ab_channel=CodingWithMitch
class CustomInfoWindowAdapter(private val mContext: Context) : GoogleMap.InfoWindowAdapter {
    private val mWindow: View
    private fun rendowWindowText(marker: Marker, view: View) {
        val name: TextView = view.findViewById<View>(R.id.info_name) as TextView
        val address: TextView = view.findViewById<View>(R.id.info_address) as TextView
        val hazard: TextView = view.findViewById<View>(R.id.info_hazard) as TextView
        name.text = marker.title
        if (marker.snippet!!.isNotEmpty()) {
            // Log.d("TAG",marker.getSnippet());
            val snippets = marker.snippet!!.split("\\|").toTypedArray()
            address.text = snippets[1]
            hazard.text= snippets[2]
            // setup hazard level color
            if (snippets[2].lowercase(Locale.getDefault()).contains("low")) {
                hazard.setTextColor(ContextCompat.getColor(mContext, R.color.colorLowHazard))
            } else if (snippets[2].lowercase(Locale.getDefault()).contains("moderate")) {
                hazard.setTextColor(ContextCompat.getColor(mContext, R.color.colorModerateHazard))
            } else if (snippets[2].lowercase(Locale.getDefault()).contains("high")) {
                hazard.setTextColor(ContextCompat.getColor(mContext, R.color.colorHighHazard))
            } else {
                hazard.setTextColor(ContextCompat.getColor(mContext, R.color.colorUnknownHazard))
            }
        }
    }

    override fun getInfoWindow(marker: Marker): View? {
        if (marker.snippet != null) {
            rendowWindowText(marker, mWindow)
            return mWindow
        }
        marker.hideInfoWindow()
        return null
    }

    override fun getInfoContents(marker: Marker): View? {
        if (marker.snippet != null) {
            rendowWindowText(marker, mWindow)
            return mWindow
        }
        marker.hideInfoWindow()
        return null
    }

    init {
        mWindow = LayoutInflater.from(mContext).inflate(R.layout.custom_info_window, null)
    }
}