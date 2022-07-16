package com.example.restaurant_review.Fragments

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ListView
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import com.arlib.floatingsearchview.FloatingSearchView
import com.example.restaurant_review.Activities.RestaurantDetailActivity
import com.example.restaurant_review.Model.Restaurant
import com.example.restaurant_review.Model.RestaurantManager
import com.example.restaurant_review.R
import com.example.restaurant_review.Views.RestaurantListAdapter
import java.util.ArrayList

/**
 * HomeFragment Class Implementation
 *
 * To populate the restaurants ListView in main screen.
 */
class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_home, container, false)
        // setup menu icon on toolbar
        setHasOptionsMenu(true)
        // init the ListView
        restaurantListView = rootView.findViewById(R.id.restaurant_listView)
        // populate the ListView
        populateListView()
        // Initialize the floating search bar
        initializeSearchBar()
        return rootView
    }

    private fun initializeSearchBar() {
        floatingSearchView =
            requireActivity().findViewById<View>(R.id.floating_search_bar) as FloatingSearchView
        // when switching views
        restaurantListAdapter?.getFilter()?.filter(floatingSearchView!!.query)
        // set text change listener
        floatingSearchView!!.setOnQueryChangeListener { _, newQuery ->
            restaurantListAdapter?.getFilter()?.filter(newQuery)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        requireActivity().menuInflater.inflate(R.menu.menu_home_fragment, menu)
        menu.getItem(0).isChecked = restaurantListAdapter?.favesOnly == true
    }

    private fun populateListView() {
        // setup ListView
        val restaurantList: ArrayList<Restaurant> ?=
            RestaurantManager.instance?.allRestaurants
        restaurantListAdapter =
            restaurantList?.let {
                RestaurantListAdapter(
                    activity,
                    R.layout.list_item_restaurant,
                    it
                )
            }
        restaurantListView!!.adapter = restaurantListAdapter

        // click the item to launch the Restaurant Detail Activity
        restaurantListView!!.setOnItemClickListener { parent, view, position, id ->
            val intent: Intent = RestaurantDetailActivity.makeLaunchIntent(
                activity,
                restaurantList?.get(position)?.id,
                position
            )
            startActivityForResult(intent, 0)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.map_view -> {

                // for switching to map view from listView
                val navController: NavController? =
                    activity?.let { findNavController(it, R.id.nav_host_fragment) }
                navController?.navigate(R.id.nav_maps)
                true
            }
            R.id.menu_favorite_only -> {
                if (!item.isChecked) {
                    restaurantListAdapter?.filter?.setFavoriteOnly(true)
                    item.isChecked = true
                    MapsFragment.setFaveOnly(true)
                } else {
                    restaurantListAdapter?.filter?.setFavoriteOnly(false)
                    item.isChecked = false
                    MapsFragment.setFaveOnly(false)
                }
                // set the filter
                floatingSearchView =
                    requireActivity().findViewById<View>(R.id.floating_search_bar) as FloatingSearchView
                restaurantListAdapter?.getFilter()
                    ?.filter(floatingSearchView!!.query)
                true
            }
            R.id.menu_filter_by_safety -> {
                showFilterBySafetyDialog()
                true
            }
            R.id.menu_filter_by_violation -> {
                showFilterByViolationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showFilterByViolationDialog() {
        // Build an AlertDialog
        val builder = AlertDialog.Builder(
            requireContext()
        )

        // Create a the with custom layout
        val dialogView: View = LayoutInflater.from(activity)
            .inflate(R.layout.dialog_filter_by_violations, null)

        // Define the variables
        val num: TextView = dialogView.findViewById<View>(R.id.editTextNumberDecimal) as TextView
        val less: RadioButton = dialogView.findViewById<View>(R.id.radioButton_less) as RadioButton
        val great: RadioButton =
            dialogView.findViewById<View>(R.id.radioButton_great) as RadioButton

        // Set values for weighs
        num.text = (restaurantListAdapter?.numOfViolation?.let { Integer.toString(it) })
        less.isChecked = restaurantListAdapter?.lessEuqalThan == true
        great.isChecked = restaurantListAdapter?.greatEuqalThan == true

        // Specify the dialog is cancelable
        builder.setCancelable(true)

        // Set a title for alert dialog
        builder.setTitle(getString(R.string.menu_filter_by_violations))

        // Set the positive/yes button click listener
        builder.setPositiveButton(
            getString(R.string.filter_button)
        ) { _, _ ->
            if (num.text.toString() == "") {
                restaurantListAdapter?.filter?.setNumOfViolation(0)
                restaurantListAdapter?.filter?.setGreatEqualThan(true)
                restaurantListAdapter?.filter?.setLessEqualThan(false)
            } else {
                restaurantListAdapter?.filter?.setLessEqualThan(less.isChecked)
                restaurantListAdapter?.filter?.setGreatEqualThan(great.isChecked)
                restaurantListAdapter?.filter?.setNumOfViolation(num.text.toString().toInt())
            }
            if (floatingSearchView == null) {
                floatingSearchView =
                    requireActivity().findViewById<View>(R.id.floating_search_bar) as FloatingSearchView
            }
            restaurantListAdapter?.getFilter()?.filter(floatingSearchView!!.query)
        }

        // Set the neutral/cancel button click listener
        builder.setNegativeButton(
            getString(R.string.cancel_button),
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    // Do something when click the neutral button
                }
            })
        val dialog = builder.create()
        // Display the alert dialog on interface
        dialog.setView(dialogView)
        dialog.show()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            if (restaurantListAdapter != null) {
                initializeSearchBar()
            }
        }
    }

    // To build up the multiple-choice filter dialog when "Filter by safety" menu is clicked.
    private fun showFilterBySafetyDialog() {
        // String array for alert dialog multi choice items
        val safetyRatings = arrayOf(
            getString(R.string.safetyLevel_safe),
            getString(R.string.safetyLevel_moderate),
            getString(R.string.safetyLevel_unsafe),
            getString(R.string.safetyLevel_unknown)
        )

        // Boolean array for initial selected items
        val checkedSafetyRatings = restaurantListAdapter?.includeSafe?.let {
            restaurantListAdapter?.includeModerate?.let { it1 ->
                restaurantListAdapter?.includeUnsafe?.let { it2 ->
                    restaurantListAdapter?.includeUnknown?.let { it3 ->
                        booleanArrayOf(
                            it,  // Safe
                            it1,  // Moderate
                            it2,  // Unsafe
                            it3 // Unknown
                        )
                    }
                }
            }
        }

        // Build an AlertDialog
        val builder = AlertDialog.Builder(
            requireContext()
        )

        // Set multiple choice items for alert dialog
        builder.setMultiChoiceItems(
            safetyRatings,
            checkedSafetyRatings
        ) { _, which, isChecked -> // Update the current focused item's checked status
            checkedSafetyRatings?.set(which, isChecked)
        }

        // Specify the dialog is cancelable
        builder.setCancelable(true)

        // Set a title for alert dialog
        builder.setTitle(getString(R.string.menu_filter_by_safety))

        // Set the positive/yes button click listener
        builder.setPositiveButton(
            getString(R.string.filter_button),
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    // Do something when click positive button
                    checkedSafetyRatings?.get(0)?.let {
                        restaurantListAdapter?.filter?.setIncludeSafe(
                            it
                        )
                    }
                    checkedSafetyRatings?.get(1)?.let {
                        restaurantListAdapter?.filter?.setIncludeModerate(
                            it
                        )
                    }
                    checkedSafetyRatings?.get(2)?.let {
                        restaurantListAdapter?.filter?.setIncludeUnsafe(
                            it
                        )
                    }
                    checkedSafetyRatings?.get(3)?.let {
                        restaurantListAdapter?.filter?.setIncludeUnsafe(
                            it
                        )
                    }
                    restaurantListAdapter?.getFilter()?.filter(floatingSearchView?.query)
                }
            })

        // Set the neutral/cancel button click listener
        builder.setNegativeButton(
            getString(R.string.cancel_button),
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    // Do something when click the neutral button
                }
            })
        val dialog = builder.create()
        // Display the alert dialog on interface
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                val lng: Double = (data?.getDoubleExtra("Longitude", 0.0) ?: 0) as Double
                val lat: Double = (data?.getDoubleExtra("Latitude", 0.0) ?: 0) as Double
                val id: String = (data?.getStringExtra("ID") ?: "")
                // Log.d("TAG","\n" + id + "\n" + lat + "\n" + lng);
                val bundle = Bundle()
                bundle.putDouble("Latitude", lat)
                bundle.putDouble("Longitude", lng)
                bundle.putString("ID", id)
                val navController: NavController =
                    findNavController(requireActivity(), R.id.nav_host_fragment)
                navController.navigate(R.id.nav_maps, bundle)
            }
        }
    }

    companion object {
        var restaurantListView: ListView? = null
        private var restaurantListAdapter: RestaurantListAdapter? = null
        private var floatingSearchView: FloatingSearchView? = null
    }
}