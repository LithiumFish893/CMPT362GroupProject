package com.example.restaurant_review.Cluster

import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterItem
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptor

/**
 * CustomClusterItem.java
 *
 * Customize the cluster item which show on map
 */
class CustomClusterItem(markerOptions: MarkerOptions) : ClusterItem {
    private val title: String?
    private val snippet: String?
    private var latLng: LatLng
    var icon: BitmapDescriptor?
    override fun getPosition(): LatLng {
        return latLng
    }

    override fun getTitle(): String? {
        return title
    }

    override fun getSnippet(): String? {
        return snippet
    }

    fun setLatLng(latLng: LatLng) {
        this.latLng = latLng
    }

    init {
        latLng = markerOptions.position
        title = markerOptions.title
        snippet = markerOptions.snippet
        icon = markerOptions.icon
    }
}