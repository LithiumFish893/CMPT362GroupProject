package com.example.restaurant_review.Cluster

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import com.example.restaurant_review.Model.MyApplication
import com.example.restaurant_review.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator

/**
 * CustomClusterRenderer.kt
 *
 * The collection of cluster item
 * Override the default cluster renderer to support the customize event
 */
class CustomClusterRenderer(
    context: Context?,
    map: GoogleMap?,
    clusterManager: ClusterManager<CustomClusterItem?>?
) : DefaultClusterRenderer<CustomClusterItem?>(context, map, clusterManager) {
    private val mClusterIconGenerator: IconGenerator

    override fun onBeforeClusterItemRendered(
        markerItem: CustomClusterItem,
        markerOptions: MarkerOptions
    ) {
        if (markerItem.icon != null) {
            markerOptions.icon(markerItem.icon)
        }
        markerOptions.visible(true)
    }

    override fun onBeforeClusterRendered(
        cluster: Cluster<CustomClusterItem?>,
        markerOptions: MarkerOptions
    ) {
        /// set the cluster appearance
        if (cluster.size > 6) {
            mClusterIconGenerator.setBackground(
                MyApplication.context?.let {
                    ContextCompat.getDrawable(
                        it,
                        R.drawable.cluster_circle_01
                    )
                }
            )
        } else {
            mClusterIconGenerator.setBackground(
                MyApplication.context?.let {
                    ContextCompat.getDrawable(
                        it,
                        R.drawable.cluster_circle_02
                    )
                }
            )
        }
        // set text style
        mClusterIconGenerator.setTextAppearance(R.style.AppTheme_WhiteTextAppearance)
        // set icon
        val clusterTitle: String = cluster.getSize().toString()
        val icon: Bitmap = mClusterIconGenerator.makeIcon(clusterTitle)
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))
    }

    init {
        clusterManager?.renderer = this
        mClusterIconGenerator = IconGenerator(MyApplication.context)
    }
}