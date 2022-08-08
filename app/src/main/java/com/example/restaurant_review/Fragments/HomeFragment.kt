package com.example.restaurant_review.Fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.*
import android.widget.Toast.LENGTH_SHORT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import com.arlib.floatingsearchview.FloatingSearchView
import com.example.restaurant_review.Activities.RestaurantDetailActivity
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
        const val PAGE_SIZE=20
        @SuppressLint("StaticFieldLeak")
        var restaurantListView: ListView? = null
        private var restaurantListAdapter: RestaurantListAdapter? = null
        private var floatingSearchView: FloatingSearchView? = null
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        println("init1")
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
        //CoroutineScope(Dispatchers.IO).launch {  FraserHealthHtmlScraper().scrape("cafe") }
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
            println("query changed")
            restaurantListAdapter?.filter!!.filter(newQuery)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //Toast.makeText(requireActivity(),"List Activity", LENGTH_SHORT).show()
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_home_fragment, menu);

       inflater.inflate(R.menu.menu_main_activity, menu)

        menu.getItem(0).isChecked = restaurantListAdapter?.favesOnly == true
//        return super.onCreateOptionsMenu(menu, inflater)
    }

    private fun populateListView() {
        // setup ListView
        val restaurantList: ArrayList<Restaurant> =
            RestaurantManager.instance!!.allRestaurants
        restaurantListAdapter =
                RestaurantListAdapter(
                    activity,
                    R.layout.list_item_restaurant,
                    restaurantList
                )

        restaurantListView!!.adapter = restaurantListAdapter

        // read more data from api
        restaurantListView!!.setOnScrollListener (object: AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                // only update scroll of no query and no filter
                if (floatingSearchView.query == "" && !MapsFragment.favesOnly &&
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
                restaurantList.get(position).id,
                position
            )
            intent.putExtra(java.lang.String.valueOf(R.string.intent_extra_id), restaurantList.get(position).id)
//            startActivity(intent)
          startActivityForResult(intent, 0)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        //Toast.makeText(requireActivity(),"item selected", LENGTH_SHORT).show()
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
            else -> super.onOptionsItemSelected(item)
        }
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
}
