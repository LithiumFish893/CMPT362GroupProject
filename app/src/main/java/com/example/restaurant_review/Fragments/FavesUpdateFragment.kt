package com.example.restaurant_review.Fragments

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import com.example.restaurant_review.Model.Restaurant
import com.example.restaurant_review.Model.RestaurantManager
import com.example.restaurant_review.R
import com.example.restaurant_review.Views.RestaurantListAdapter
import java.util.*

/**
 * FavesUpdateFragment Class Implementation
 *
 * To display the what's new update on user favourite restaurants
 */
class FavesUpdateFragment : AppCompatDialogFragment() {
    private lateinit var rootView: View
    private lateinit var mPrefs: SharedPreferences
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        rootView = LayoutInflater.from(activity)
            .inflate(R.layout.dialog_faves_update, null)
        println("getting view3")
        // set up the ListView
        populateListView()
        val listener: DialogInterface.OnClickListener = object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    dialog.cancel()
                }
            }
        }
        return AlertDialog.Builder(requireActivity())
            .setTitle(R.string.faves_update_title)
            .setView(rootView)
            .setPositiveButton(android.R.string.ok, listener)
            .create()
    }

    private fun populateListView() {
        // setup ListView
        val restaurantListView =
            rootView.findViewById<View>(R.id.faves_restaurant_listView) as ListView
        val restaurantList: ArrayList<Restaurant> ?=
            RestaurantManager.instance?.allRestaurants
        val filteredRestaurants: ArrayList<Restaurant> = ArrayList<Restaurant>()
        val favesList = favorites
        if (restaurantList != null) {
            for (r in restaurantList) {
                if (favesList.contains(r.id)) {
                    filteredRestaurants.add(r)
                }
            }
        }
        println("getting view2")
        val restaurantListAdapter =
            RestaurantListAdapter(activity, R.layout.list_item_restaurant, filteredRestaurants)
        restaurantListView.adapter = restaurantListAdapter
    }

    private val favorites: ArrayList<String>
        get() {
            val faveRestaurants: String? = mPrefs.getString("fave_restaurants", "")
            if (faveRestaurants != null) {
                return if (faveRestaurants.isNotEmpty()) {
                    if (faveRestaurants.contains(",")) {
                        ArrayList(
                            listOf(
                                *faveRestaurants.split(
                                    ","
                                ).toTypedArray()
                            )
                        )
                    } else {
                        return ArrayList(
                            listOf(
                                faveRestaurants
                            )
                        )
                    }
                } else {
                    return ArrayList()
                }
            }
            return ArrayList()
        }

    companion object {
        private const val PREFS_NAME = "mPrefs"
    }
}