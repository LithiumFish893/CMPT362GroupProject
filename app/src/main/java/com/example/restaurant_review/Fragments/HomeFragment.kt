package com.example.restaurant_review.Fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import com.arlib.floatingsearchview.FloatingSearchView
import com.example.restaurant_review.Activities.RestaurantReview
import com.example.restaurant_review.Model.*
import com.example.restaurant_review.R
import com.example.restaurant_review.Views.RestaurantListAdapter


/**
 * HomeFragment Class Implementation
 *
 * To populate the restaurants ListView in main screen.
 */
class HomeFragment : Fragment() {
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var yelpAPI: YelpAPI
    private var progressBars = arrayListOf<ProgressBar>()
    private lateinit var floatingSearchView: FloatingSearchView
    companion object {
        const val PAGE_SIZE=15
        @SuppressLint("StaticFieldLeak")
        var restaurantListView: ListView? = null
        private var restaurantListAdapter: RestaurantListAdapter? = null
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView: View = inflater.inflate(R.layout.fragment_home, container, false)
        val progressBar = rootView.findViewById<ProgressBar>(R.id.home_progress_bar)
        constraintLayout = rootView.findViewById(R.id.home_fragment_linear_layout)
        // load from yelp
        yelpAPI = YelpAPI(requireContext(), object: OnReadApiCompleteListener{
            override fun onReadApiComplete() {
                progressBar.visibility = View.GONE
                // populate the ListView
                populateListView()
            }
        })
        yelpAPI.readRestaurantData(0, PAGE_SIZE)
        // setup menu icon on toolbar
        setHasOptionsMenu(true)
        // init the ListView
        restaurantListView = rootView.findViewById(R.id.restaurant_listView)
        floatingSearchView = requireActivity().findViewById(R.id.floating_search_bar)
        // Initialize the floating search bar
        initializeSearchBar()
        return rootView
    }

    private fun initializeSearchBar() {
        // when switching views
        restaurantListAdapter?.getFilter()?.filter(floatingSearchView.query)
        // set text change listener
        floatingSearchView.setOnQueryChangeListener { _, newQuery ->
            restaurantListAdapter?.filter!!.filter(newQuery)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_home_fragment, menu);
       inflater.inflate(R.menu.menu_main_activity, menu)

        menu.getItem(0).isChecked = restaurantListAdapter?.favesOnly == true
    }

    private fun populateListView() {
        // setup ListView
        val restaurantList: ArrayList<Restaurant> =
            RestaurantManager.instance!!.allRestaurants
        restaurantListAdapter =
                RestaurantListAdapter(
                    requireContext(),
                    R.layout.list_item_restaurant,
                    restaurantList
                )

        restaurantListView!!.adapter = restaurantListAdapter

        // read more data from api
        restaurantListView!!.setOnScrollListener (object: AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                // only update scroll of no query and no filter
                if (floatingSearchView.query == "" && !MapsFragment.favesOnly && !restaurantListAdapter!!.filter.anyFiltered() &&
                    scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                    && restaurantListView!!.lastVisiblePosition - restaurantListView!!.headerViewsCount -
                    restaurantListView!!.footerViewsCount >= restaurantListAdapter!!.count - 1
                ) {
                    val progressBar = ProgressBar(requireContext())
                    progressBar.isIndeterminate = true
                    progressBar.background = requireActivity().getDrawable(com.arlib.floatingsearchview.R.color.transparent)
                    constraintLayout.addView(progressBar)
                    progressBars.add(progressBar)
                    progressBar.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        startToStart = constraintLayout.id
                        endToEnd = constraintLayout.id
                        bottomToBottom = constraintLayout.id
                    }
                    yelpAPI.onReadApiCompleteListener = object: OnReadApiCompleteListener{
                        override fun onReadApiComplete() {
                            progressBars.forEach{constraintLayout.removeView(it)}
                            progressBars = arrayListOf()
                            // populate the ListView
                            restaurantListAdapter!!.updateList(restaurantList)
                        }
                    }
                    yelpAPI.readMoreRestaurantData(PAGE_SIZE)
                }
            }

            override fun onScroll(
                view: AbsListView,
                firstVisibleItem: Int,
                visibleItemCount: Int,
                totalItemCount: Int
            ) {
            }
        })

        // click the item to launch the Restaurant Detail Activity
        restaurantListView!!.setOnItemClickListener { parent, view, position, id ->
            val intent: Intent = RestaurantReview.makeLaunchIntent(
                activity,
                restaurantList[position].id,
                position
            )
            intent.putExtra(java.lang.String.valueOf(R.string.intent_extra_id), restaurantList.get(position).id)
          startActivityForResult(intent, 0)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.map_view -> {

                // for switching to map view from listView
                val navController: NavController =
                    requireActivity().let { findNavController(it, R.id.nav_host_fragment) }
                navController.navigate(R.id.nav_maps)
                true
            }
            R.id.menu_favorite_only -> {
                if (!item.isChecked) {
                    restaurantListAdapter!!.filter.setFavoriteOnly(true)
                    item.isChecked = true
                    MapsFragment.setFaveOnly(true)
                } else {
                    restaurantListAdapter!!.filter.setFavoriteOnly(false)
                    item.isChecked = false
                    MapsFragment.setFaveOnly(false)
                }
                // set the filter
                val floatingSearchView : FloatingSearchView=
                    requireActivity().findViewById(R.id.floating_search_bar)
                restaurantListAdapter!!.filter.filter(floatingSearchView.query)
                true
            }
            R.id.menu_filter_by_safety -> {
                showFilterBySafetyDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

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
                        restaurantListAdapter?.filter?.setIncludeUnknown(
                            it
                        )
                    }
                    restaurantListAdapter!!.filter.filter(floatingSearchView.query)
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


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            if (restaurantListAdapter != null) {
                initializeSearchBar()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                val lng: Double = (data?.getDoubleExtra("Longitude", 0.0) ?: 0) as Double
                val lat: Double = (data?.getDoubleExtra("Latitude", 0.0) ?: 0) as Double
                val id: String = (data?.getStringExtra("ID") ?: "")
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
}
