package com.example.restaurant_review.Activities


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.restaurant_review.Fragments.HomeFragment
import com.example.restaurant_review.Model.Inspection
import com.example.restaurant_review.Model.InspectionManager
import com.example.restaurant_review.Model.Restaurant
import com.example.restaurant_review.Model.RestaurantManager
import com.example.restaurant_review.R
import com.example.restaurant_review.Views.InspectionListAdapter

/**
 * RestaurantDetailActivity Class Implementation
 *
 * To display the basic info of restaurant.
 * To populate the inspections ListView for selected restaurant.
 */
class RestaurantDetailActivity : AppCompatActivity() {
    var inspectionListView: ListView? = null
    private var mRestaurant: Restaurant? = null
    private lateinit var mPrefs: SharedPreferences
    private lateinit var tv: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_detail)

        ID = intent.getStringExtra(java.lang.String.valueOf(R.string.intent_extra_id))
        mRestaurant = RestaurantManager.instance?.getRestaurant(ID)

        // setup the textViews to display basic info of restaurant.
        setupUI()

        // setup ListView to display the inspections
        inspectionListView = findViewById(R.id.inspection_history_listView)
        tv = this.findViewById<TextView>(R.id.no_inspections_in_db_tv)

        /*HealthInspectionHtmlScraper(object: OnReadApiCompleteListener{
            override fun onReadApiComplete() {
                populateListListView()
            }
        }).scrape(mRestaurant!!.name.lowercase(Locale.getDefault()).removePrefix("the ").removeSuffix(" restaurant"),
            ID!!)*/
        populateListListView()
    }


    // load the menu to the activity
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_restaurant_detail, menu)
        // set favourites icon
        mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val faveRestaurants: String? = mPrefs.getString("fave_restaurants", "")
        if (faveRestaurants != null) {
            if (faveRestaurants.contains(ID!!)) {
                menu.getItem(0).setIcon(R.drawable.ic_menu_mark_favorite)
            } else {
                menu.getItem(0).setIcon(R.drawable.ic_menu_unmark_favorite)
            }
        }
        return true
    }

    // menu item selected event
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            android.R.id.home-> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setViewListIcon(res: Int) {
        val pos = intent.getIntExtra("position", -1)
        if (pos != -1) {
            val view = HomeFragment.restaurantListView?.let { getViewByPosition(pos, it) }
            val icon = view?.findViewById<ImageView>(R.id.list_restaurant_fave)
            icon?.setImageResource(res)
        }
    }


    private fun setupUI() {

        // setup toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitle(R.string.toolbar_restaurant_detail)
        setSupportActionBar(toolbar)
        if(supportActionBar == null){
            Log.e("TAG", "setupUI: toolbar support action is null", )
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
//        Objects.requireNonNull(supportActionBar)?.setDisplayHomeAsUpEnabled(true)

        // setup UI Views
        val mInspections: ArrayList<Inspection> = InspectionManager.instance?.getInspections(ID!!)!!
        val name: TextView = findViewById<TextView>(R.id.restaurant_name)
        val address: TextView = findViewById<TextView>(R.id.restaurant_address)
        val gps: TextView = findViewById<TextView>(R.id.restaurant_gps)
        name.text = mRestaurant?.name
        address.text = getString(R.string.restaurant_address, mRestaurant?.address, mRestaurant?.city)

        gps.text = getString(R.string.restaurant_gps, mRestaurant?.latitude?.let { DDtoDMS(it) }, mRestaurant?.longitude?.let { DDtoDMS(it) })

        // GPS info click event
        val lat: Double = (mRestaurant?.latitude ?: 0) as Double
        val lng: Double = (mRestaurant?.longitude ?: 0) as Double
        val id: String = mRestaurant?.id ?: ""
        gps.setOnClickListener {
            val i = Intent()
            i.putExtra("Latitude", lat)
            i.putExtra("Longitude", lng)
            i.putExtra("ID", id)
            setResult(Activity.RESULT_OK, i)
            finish()
        }
    }


    private fun populateListListView() {
        // setup InspectionListList
        val InspectionList: ArrayList<Inspection>? =
            ID?.let { InspectionManager.instance?.getInspections(it) }
        if (InspectionList == null || InspectionList.isEmpty()){
            tv.visibility = View.VISIBLE
        }
        // setup ListView
        val inspectionListAdapter =
            InspectionList?.let { InspectionListAdapter(this, R.layout.list_item_inspection, it) }
        inspectionListView!!.adapter = inspectionListAdapter
    }

    fun DDtoDMS(d: Double): String {
        val array = d.toString().split(".").toTypedArray()
        val degrees = array[0]
        val m = ("0." + array[1]).toDouble() * 60
        val array1 = java.lang.Double.toString(m).split(".").toTypedArray()
        val minutes = array1[0]
        val s = ("0." + array1[1]).toDouble() * 60
        val array2 = java.lang.Double.toString(s).split(".").toTypedArray()
        val seconds = array2[0]
        return "$degrees°$minutes\'$seconds\""
    }

    fun getViewByPosition(pos: Int, listView: ListView): View {
        val firstListItemPosition = listView.firstVisiblePosition
        +listView.childCount - 1
        return if (pos < firstListItemPosition || pos > firstListItemPosition) {
            listView.adapter.getView(pos, null, listView)
        } else {
            val childIndex = pos - firstListItemPosition
            listView.getChildAt(childIndex)
        }
    }

    companion object {
        var ID: String? = null
        var PREFS_NAME = "mPrefs"
        fun makeLaunchIntent(context: Context?, ID: String?, position: Int): Intent {
            val intent = Intent(context, RestaurantDetailActivity::class.java)
            intent.putExtra(java.lang.String.valueOf(R.string.intent_extra_id), ID)
            intent.putExtra("position", position)
            return intent
        }
    }
}