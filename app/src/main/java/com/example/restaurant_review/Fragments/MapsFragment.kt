package com.example.restaurant_review.Fragments

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import com.arlib.floatingsearchview.FloatingSearchView
import com.example.restaurant_review.Activities.RestaurantDetailActivity
import com.example.restaurant_review.Cluster.CustomClusterItem
import com.example.restaurant_review.Cluster.CustomClusterRenderer
import com.example.restaurant_review.Model.*
import com.example.restaurant_review.R
import com.example.restaurant_review.Views.CustomInfoWindowAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.clustering.ClusterManager
import java.util.*

/**
 * MapsFragment Class Implementation
 *
 * To load the Google Map v2 fragment and set the listener for user events.
 */
open class MapsFragment : Fragment(), OnMapReadyCallback {
    private lateinit var rootView: View
    private lateinit var myLocationMarker: Marker
    private lateinit var mRenderer: CustomClusterRenderer
    private lateinit var mPrefs: SharedPreferences
    protected var includeSafe = true
    protected var includeModerate = true
    protected var includeUnsafe = true
    protected var includeUnknown = true
    protected var lessEqualThan = false
    protected var greatEqualThan = true
    protected var numOfViolation = 0
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            // 1.Check location permission
            if (isLocationPermissionGranted && isLocationEnabled(context)) {
                // 2.Load searching result if not null
                initializeSearchBar()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_maps, container, false)
        // setup menu
        setHasOptionsMenu(true)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // location permission check
        if (!isLocationPermissionGranted) {
            Toast.makeText(context, R.string.location_permission, Toast.LENGTH_SHORT).show()
        }
        if (isWriteFilePermissionGranted) {
            Toast.makeText(context, R.string.file_permission, Toast.LENGTH_LONG).show()
        }

        // location service check
        if (!isLocationEnabled(context)) {
            Toast.makeText(context, R.string.location_service, Toast.LENGTH_SHORT).show()
        }

        // onMapReadyCallback
        if (isLocationPermissionGranted && isLocationEnabled(context)) {
            val mapFragment: SupportMapFragment? =
                childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync(this@MapsFragment)
        }
    }

    private fun initializeSearchBar() {
        floatingSearchView =
            requireActivity().findViewById<View>(R.id.floating_search_bar) as FloatingSearchView
        // when switching views
        mMap?.clear()
        mClusterManager?.clearItems()
        // create a list that contains the query
        var mFilteredList: ArrayList<MarkerOptions?> = ArrayList<MarkerOptions?>()
        var filteredByFaves = ArrayList<String?>()
        if (favesOnly) {
            filteredByFaves = favorites
        }
        val filteredBySafety = ArrayList<String>()
        if (!includeSafe || !includeModerate || !includeUnknown || !includeUnknown) {
            val safeties: HashMap<String, String?>? =
                InspectionManager.instance?.safetyLevels
            //If low selected
            if (includeSafe) {
                for (s in safeties?.keys!!) {
                    if (safeties.get(s) == "Low") {
                        filteredBySafety.add(s)
                    }
                }
            }
            //If mid selected
            if (includeModerate) {
                for (s in safeties?.keys!!) {
                    if (safeties.get(s) == "Moderate") {
                        filteredBySafety.add(s)
                    }
                }
            }
            //If high selected
            if (includeUnsafe) {
                for (s in safeties?.keys!!) {
                    if (safeties[s] == "High") {
                        filteredBySafety.add(s)
                    }
                }
            }
            //If unknown selected
            if (includeUnknown) {
                for (s in safeties?.keys!!) {
                    if (safeties[s] == "Unknown") {
                        filteredBySafety.add(s)
                    }
                }
            }
        }
        val filteredByViolation = ArrayList<String>()
        if (numOfViolation != 0) {
            val criticalViolations: HashMap<String?, Int>? =
                InspectionManager.instance?.getNumOfCritical()
            if (lessEqualThan) {
                for (s in criticalViolations?.keys!!) {
                    if (criticalViolations[s]!! <= numOfViolation) {
                        if (s != null) {
                            filteredByViolation.add(s)
                        }
                    }
                }
            } else {
                for (s in criticalViolations?.keys!!) {
                    if (criticalViolations[s]!! >= numOfViolation) {
                        if (s != null) {
                            filteredByViolation.add(s)
                        }
                    }
                }
            }
        }
        for (i in mSearchList) {
            if (i.title?.trim { it <= ' ' }?.replace(" ", "")?.lowercase(Locale.getDefault())
                    ?.contains(floatingSearchView!!.query.trim { it <= ' ' }
                        .replace(" ", "").lowercase(Locale.getDefault())) == true
            ) {
                val id: String ?= i.snippet?.split("\\|")?.toTypedArray()?.get(0)
                if (favesOnly || !includeSafe || !includeModerate || !includeUnsafe || !includeUnknown) {
                    if (favesOnly && filteredByFaves.contains(id)) {
                        // combine with faves and keywords and certain hazard level
                        if (!includeSafe || !includeModerate || !includeUnsafe || !includeUnknown) {
                            if (filteredBySafety.contains(id)) {
                                mFilteredList.add(i)
                            }
                        } else {
                            mFilteredList.add(i)
                        }
                    } else if (!favesOnly && filteredBySafety.contains(id)) {
                        mFilteredList.add(i)
                    }
                } else {
                    // match the key words
                    mFilteredList.add(i)
                }
            }
        }

        // Apply the filter by number of critical violations
        if (numOfViolation != 0) {
            val finalFilteredList: ArrayList<MarkerOptions?> = ArrayList<MarkerOptions?>()
            for (i in mFilteredList) {
                val id: String? = i?.snippet?.split("\\|")?.toTypedArray()?.get(0)
                if (filteredByViolation.contains(id)) {
                    finalFilteredList.add(i)
                }
            }
            mFilteredList.clear()
            mFilteredList = finalFilteredList
        }
        for (i in mFilteredList) {
            val marker = i?.let { CustomClusterItem(it) }
            mClusterManager?.addItem(marker)
        }
        // auto refresh map
        mClusterManager?.cluster()
        // now, we can click the zoom-in and zoom-out button to refresh the markers
        // set text change listener
        floatingSearchView!!.setOnQueryChangeListener { _, newQuery -> // clear the markers on map
            mMap?.clear()
            mClusterManager?.clearItems()
            // create a list that contains the query
            val mFilteredList: ArrayList<MarkerOptions> = ArrayList<MarkerOptions>()
            for (i in mSearchList) {
                if (i.title?.trim { it <= ' ' }?.replace(" ", "")
                        ?.lowercase(Locale.getDefault())
                        ?.contains(
                            newQuery.trim { it <= ' ' }.replace(" ", "")
                                .lowercase(Locale.getDefault())
                        ) == true
                ) {
                    mFilteredList.add(i)
                }
            }
            for (i in mFilteredList) {
                val marker = CustomClusterItem(i)
                mClusterManager?.addItem(marker)
            }
            // auto refresh map
            mClusterManager?.cluster()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        requireActivity().menuInflater.inflate(R.menu.menu_maps_fragment, menu)
        menu.getItem(0).isChecked = favesOnly
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.list_view -> {
                val navController: NavController =
                    findNavController(requireActivity(), R.id.nav_host_fragment)
                navController.navigate(R.id.nav_home)
                return true
            }
            R.id.menu_favorite_only -> {
                if (!item.isChecked) {
                    item.isChecked = true
                    favesOnly = true
                } else {
                    item.isChecked = false
                    favesOnly = false
                }
                // set the filter
                initializeSearchBar()
                return true
            }
            R.id.menu_filter_by_safety -> {
                showFilterBySafetyDialog()
                return true
            }
            R.id.menu_filter_by_violation -> {
                showFilterByViolationDialog()
                return true
            }
        }
        return false
    }

    private val favorites: ArrayList<String?>
        private get() {
            val faveRestaurants: String? = mPrefs.getString("fave_restaurants", "")
            return if (faveRestaurants?.isNotEmpty() == true) {
                if (faveRestaurants.contains(",")) {
                    ArrayList(
                        Arrays.asList(
                            *faveRestaurants.split(
                                ","
                            ).toTypedArray()
                        )
                    )
                } else {
                    ArrayList(
                        Arrays.asList(
                            faveRestaurants
                        )
                    )
                }
            } else {
                ArrayList()
            }
        }

    override fun onMapReady(googleMap: GoogleMap) {
        // init mMaps
        if (mMap == null) {
            mMap = googleMap
          // setup map toolbar
            mMap?.uiSettings?.isMapToolbarEnabled = true
            // setup zoom-in and zoom-out controls
            mMap?.uiSettings?.isZoomControlsEnabled = true
            // permissions check
            // setup my location button available
            val permissions =
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(requireActivity(), permissions, REQUEST_CODE)
                return
            }
            mMap?.isMyLocationEnabled = true
            // setup custom info windows
            mMap?.setInfoWindowAdapter(context?.let { CustomInfoWindowAdapter(it) })

            // Set map move Listener
            mMap?.setOnCameraMoveListener(object : GoogleMap.OnCameraMoveListener {
                override fun onCameraMove() {
                    val centerOfMap: LatLng ?= mMap?.getCameraPosition()?.target
                    if (myLocationMarker != null) {
                        if (centerOfMap != null) {
                            myLocationMarker!!.position = centerOfMap
                        }
                    }
                }
            })
            // setup marker cluster manager
            initClusterManager()
            // add restaurants marker
            updateMarkersOnMaps()
            // initialize the floating search bar
            initializeSearchBar()
        }

        // I have GPS info, I need move to there.
        if (arguments != null) {
            val id = requireArguments().getString("ID")
            val lat = requireArguments().getDouble("Latitude")
            val lng = requireArguments().getDouble("Longitude")
            // set gps info
            val latLng = LatLng(lat, lng)
            // move camera
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEEP_ZOOM))
            // pop-up info windows
            // TODO: BUG: marker always null
            val clusterItem: CustomClusterItem? = mClusterItemList[id]
            val marker: Marker = mRenderer.getMarker(clusterItem)
            marker.showInfoWindow()
        } else {
            // I don't have the coordinates, move to my location.
            addCurrentLocationMarker()
        }
    }

    private fun initClusterManager() {
        //init
        mClusterManager = ClusterManager<CustomClusterItem>(context, mMap)
        mRenderer = CustomClusterRenderer(context, mMap,  ClusterManager<CustomClusterItem?>(context, mMap))

        // cluster clicked event
        mClusterManager!!.setOnClusterClickListener {
            Toast.makeText(context, R.string.cluster_click_message, Toast.LENGTH_SHORT).show()
            false
        }

        // show info windows
        mClusterManager!!.markerCollection.setInfoWindowAdapter(
            context?.let {
                CustomInfoWindowAdapter(
                    it
                )
            }
        )
        mClusterManager!!.setOnClusterItemInfoWindowClickListener { clusterItem ->
            if (clusterItem?.snippet != null) {
                val id: String = clusterItem.snippet!!.split("\\|")[0]
                val intent: Intent = RestaurantDetailActivity.makeLaunchIntent(activity, id, -1)
                startActivity(intent)
            }
        }

        // Put map event into ClusterManager
        mMap?.setOnCameraIdleListener(mClusterManager)
        mMap?.setOnMarkerClickListener(mClusterManager)
        mMap?.setInfoWindowAdapter(mClusterManager!!.markerManager)
        mMap?.setOnInfoWindowClickListener(mClusterManager)
    }

    private fun addCurrentLocationMarker() {
        // init the mFusedLocationProviderClient
        val mFusedLocationProviderClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(
                requireActivity()
            )
        // permission check
        val permissions =
            arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request Permissions
            ActivityCompat.requestPermissions(requireActivity(), permissions, REQUEST_CODE)
            // addCurrentLocationMarker();
            return
        }
        // get last location
        val task: Task<Location> = mFusedLocationProviderClient.getLastLocation()
        task.addOnSuccessListener(object : OnSuccessListener<Location?> {
            override fun onSuccess(location: Location?) {
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    mMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                    mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM))
                    val options: MarkerOptions = MarkerOptions()
                        .title(getString(R.string.you_are_here))
                        .draggable(true)
                        .position(latLng)
                        .icon(
                            getBitmapDescriptor(
                                R.drawable.ic_maps_my_location,
                                Color.parseColor("#FF0000")
                            )
                        )
                    myLocationMarker = mMap?.addMarker(options)!!
                } else {
                    Snackbar.make(
                        rootView,
                        R.string.unable_find_your_location,
                        Snackbar.LENGTH_SHORT
                    )
                }
            }
        })
    }// if permission denied, then request the permissions.

    // permissions OK, initialize the map.
    private val isLocationPermissionGranted: Boolean
        private get() {
            val permissions =
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            return if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                // permissions OK, initialize the map.
                true
            } else {
                // if permission denied, then request the permissions.
                ActivityCompat.requestPermissions(requireActivity(), permissions, REQUEST_CODE)
                false
            }
        }

    // if permission denied, then request the permissions.
    private val isWriteFilePermissionGranted: Boolean
        private get() = if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else {
            // if permission denied, then request the permissions.
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE
            )
            false
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty()) {
                for (grantResult in grantResults) if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Log.d("TAG", "onRequestPermissionsResult: permission failed")
                    return
                }
                Log.d("TAG", "onRequestPermissionsResult: permission granted")
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
        val checkedSafetyRatings = booleanArrayOf(
            includeSafe,
            includeModerate,
            includeUnsafe,
            includeUnknown
        )

        // Build an AlertDialog
        val builder = AlertDialog.Builder(
            requireContext()
        )

        // Set multiple choice items for alert dialog
        builder.setMultiChoiceItems(
            safetyRatings,
            checkedSafetyRatings
        ) { _, which, isChecked -> // Update the current focused item's checked status
            checkedSafetyRatings[which] = isChecked
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
                    includeSafe = checkedSafetyRatings[0]
                    includeModerate = checkedSafetyRatings[1]
                    includeUnsafe = checkedSafetyRatings[2]
                    includeUnknown = checkedSafetyRatings[3]
                    initializeSearchBar()
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
        num.text = numOfViolation.toString()
        less.isChecked= lessEqualThan
        great.isChecked = greatEqualThan

        // Specify the dialog is cancelable
        builder.setCancelable(true)

        // Set a title for alert dialog
        builder.setTitle(getString(R.string.menu_filter_by_violations))

        // Set the positive/yes button click listener
        builder.setPositiveButton(
            getString(R.string.filter_button)
        ) { _, _ ->
            if (num.text.toString() == "") {
                numOfViolation = 0
                greatEqualThan = true
                lessEqualThan = false
            } else {
                lessEqualThan = less.isChecked
                greatEqualThan = great.isChecked
                numOfViolation = num.text.toString().toInt()
            }
            initializeSearchBar()
        }

        // Set the neutral/cancel button click listener
        builder.setNegativeButton(
            getString(R.string.cancel_button)
        ) { dialog, which ->
            // Do something when click the neutral button
        }
        val dialog = builder.create()
        // Display the alert dialog on interface
        dialog.setView(dialogView)
        dialog.show()
    }

    companion object {
        private const val DEFAULT_ZOOM = 15f
        private const val DEEP_ZOOM = 19f
        private const val REQUEST_CODE = 1234
        private const val PREFS_NAME = "mPrefs"
        private var mMap: GoogleMap? = null
        private var floatingSearchView: FloatingSearchView? = null
        private var mClusterManager: ClusterManager<CustomClusterItem>? = null
        private val mClusterItemList: HashMap<String?, CustomClusterItem> =
            HashMap<String?, CustomClusterItem>()
        private val mSearchList: ArrayList<MarkerOptions> = ArrayList<MarkerOptions>()
        var favesOnly = false
        fun setFaveOnly(b: Boolean) {
            favesOnly = b
        }

        fun updateMarkersOnMaps() {
            // update Markers On Maps
            var mHazard: String
            var mHazardIcon: BitmapDescriptor? = null
            var mLatitude: Double
            var mLongitude: Double
            // init the RestaurantList
            val mRestaurantList: ArrayList<Restaurant> ?=
                RestaurantManager.instance?.allRestaurants
            // clean the markers before adding new
            mMap?.clear()
            mClusterItemList.clear()
            mClusterManager?.clearItems()
            // Add the marker
            if (mRestaurantList != null) {
                for (r in mRestaurantList) {
                    // set default value for empty inspection
                    mHazard = MyApplication.context?.getString(R.string.hazard_unknown).toString()
                    val context = MyApplication.context
                    if(context != null)
                        mHazardIcon = getBitmapDescriptor(
                            R.drawable.ic_hazard_unknown,
                            ContextCompat.getColor(context, R.color.colorUnknownHazard)
                        )
                    // init latitude and longitude variables
                    mLatitude = r.latitude
                    mLongitude = r.longitude
                    // get the most recent inspection
                    val mInspectionList: ArrayList<Inspection>? =
                        InspectionManager.instance?.getInspections(r.id)
                    if (mInspectionList?.size!! > 0) {
                        val mInspection: Inspection = mInspectionList[0]
                        // set the hazard level
                        mHazard = mInspection.hazard
                        // set the hazard color
                        if(context != null)
                            mHazardIcon = if (mInspection.hazard.toLowerCase().contains("low")) {
                                getBitmapDescriptor(
                                    R.drawable.ic_hazard_low,
                                    ContextCompat.getColor(
                                        context,
                                        R.color.colorLowHazard
                                    )
                                )

                            } else if (mInspection.hazard.toLowerCase().contains("moderate")) {
                                getBitmapDescriptor(
                                    R.drawable.ic_hazard_moderate,
                                    ContextCompat.getColor(
                                        context,
                                        R.color.colorModerateHazard
                                    )
                                )
                            } else {
                                getBitmapDescriptor(
                                    R.drawable.ic_hazard_high,
                                    ContextCompat.getColor(
                                        context,
                                        R.color.colorHighHazard
                                    )
                                )
                            }
                    }
                    // set the snippet value pass to the info windows
                    val snippet: String = r.id + "|" +
                            MyApplication.context
                                ?.getString(R.string.snippet_address) + " " + r.address + ", " + r.city + "|" +
                            MyApplication.context
                                ?.getString(R.string.snippet_hazard) + " " + mHazard
                    var options: MarkerOptions = MarkerOptions()
                    if(mHazardIcon != null)
                     options = MarkerOptions()
                        .position(LatLng(mLatitude, mLongitude))
                        .title(r.name)
                        .snippet(snippet)
                        .icon(mHazardIcon)
                    // Add the marker info to ArrayList
                    mSearchList.add(options)
                    // Add marker to cluster manager
                    val markerItem = CustomClusterItem(options)
                    mClusterManager?.addItem(markerItem)
                    mClusterItemList[r.id] = markerItem
                }
            }
        }

        private fun getBitmapDescriptor(id: Int, color: Int): BitmapDescriptor {
            // Learn from: lbarbosa's answer under VectorDrawable with GoogleMap BitmapDescriptor
            // https://stackoverflow.com/questions/33548447/vectordrawable-with-googlemap-bitmapdescriptor
            val vectorDrawable =
                MyApplication.context?.getResources()
                    ?.let { ResourcesCompat.getDrawable(it, id, null) }
            val bitmap: Bitmap = Bitmap.createBitmap(
                vectorDrawable!!.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
            DrawableCompat.setTint(vectorDrawable, color)
            vectorDrawable.draw(canvas)
            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }

        fun isLocationEnabled(context: Context?): Boolean {
            val lm: LocationManager =
                context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return lm.isLocationEnabled
        }


    }
}
