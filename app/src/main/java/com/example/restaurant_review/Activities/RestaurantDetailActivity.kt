package com.example.restaurant_review.Activities


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.restaurant_review.Fragments.HomeFragment
import com.example.restaurant_review.Model.Inspection
import com.example.restaurant_review.Model.InspectionManager
import com.example.restaurant_review.Model.Restaurant
import com.example.restaurant_review.Model.RestaurantManager
import com.example.restaurant_review.R
import com.example.restaurant_review.Views.InspectionListAdapter
import java.util.*

/**
 * RestaurantDetailActivity Class Implementation
 *
 * To display the basic info of restaurant.
 * To populate the inspections ListView for selected restaurant.
 */
class RestaurantDetailActivity : AppCompatActivity() {
    var inspectionListView: ListView? = null
    private lateinit var mPrefs: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_detail)

        // setup the textViews to display basic info of restaurant.
        setupUI()

        // setup ListView to display the inspections
        inspectionListView = findViewById(R.id.inspection_history_listView)
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
            android.R.id.home -> {
                finish()
                if (item.icon.constantState == getDrawable(R.drawable.ic_menu_unmark_favorite)!!.constantState) {
                    addFave(ID)
                    item.setIcon(R.drawable.ic_menu_mark_favorite)
                    Toast.makeText(this, "Added to Favorites", Toast.LENGTH_LONG).show()
                    setViewListIcon(R.drawable.ic_menu_mark_favorite)
                } else {
                    removeFave(ID)
                    item.setIcon(R.drawable.ic_menu_unmark_favorite)
                    Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_LONG).show()
                    setViewListIcon(R.drawable.ic_menu_unmark_favorite)
                }
                true
            }
            R.id.favourite -> {
                if (item.icon.constantState == getDrawable(R.drawable.ic_menu_unmark_favorite)!!.constantState) {
                    addFave(ID)
                    item.setIcon(R.drawable.ic_menu_mark_favorite)
                    Toast.makeText(this, "Added to Favorites", Toast.LENGTH_LONG).show()
                    setViewListIcon(R.drawable.ic_menu_mark_favorite)
                } else {
                    removeFave(ID)
                    item.setIcon(R.drawable.ic_menu_unmark_favorite)
                    Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_LONG).show()
                    setViewListIcon(R.drawable.ic_menu_unmark_favorite)
                }
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

    private fun removeFave(id: String?) {
        var faveRestaurants: String ?= mPrefs.getString("fave_restaurants", "")
        // Log.d("TAG","before remove: " + faveRestaurants);
        if (faveRestaurants != null) {
            if (faveRestaurants.contains("$id,")) {
                faveRestaurants = faveRestaurants.replace("$id,", "")
            } else if (faveRestaurants.contains(",$id")) {
                faveRestaurants = faveRestaurants.replace(",$id", "")
            } else if (faveRestaurants.contains(id!!)) {
                faveRestaurants = faveRestaurants.replace(id, "")
            }
        }
        mPrefs.edit().putString("fave_restaurants", faveRestaurants).apply()
        //Log.d("TAG","after remove: " + faveRestaurants);
    }

    private fun addFave(id: String?) {
        val faveRestaurants: String ?= mPrefs.getString("fave_restaurants", "")
        if (faveRestaurants != null) {
            if (faveRestaurants.length == 0) {
                mPrefs.edit().putString("fave_restaurants", id).apply()
            } else {
                mPrefs.edit().putString("fave_restaurants", "$faveRestaurants,$id").apply()
            }
        }
    }

    private fun setupUI() {

        // setup toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitle(R.string.toolbar_restaurant_detail)
        setSupportActionBar(toolbar)
        Objects.requireNonNull(supportActionBar)?.setDisplayHomeAsUpEnabled(true)

        // setup UI Views
        ID = intent.getStringExtra(java.lang.String.valueOf(R.string.intent_extra_id))
        val mRestaurant: Restaurant? = RestaurantManager.instance?.getRestaurant(ID)
        val name: TextView = findViewById<TextView>(R.id.restaurant_name)
        val address: TextView = findViewById<TextView>(R.id.restaurant_address)
        val gps: TextView = findViewById<TextView>(R.id.restaurant_gps)
        val writeReview = findViewById<Button>(R.id.button6)
        name.text = mRestaurant?.name
        address.text = getString(R.string.restaurant_address, mRestaurant?.address, mRestaurant?.city)

        gps.text = getString(R.string.restaurant_gps, mRestaurant?.latitude?.let { DDtoDMS(it) }, mRestaurant?.longitude?.let { DDtoDMS(it) })

        // GPS info click event
        val lat: Double = (mRestaurant?.latitude ?: 0) as Double
        val lng: Double = (mRestaurant?.longitude ?: 0) as Double
        val id: String = mRestaurant?.id ?: ""
        gps.setOnClickListener(View.OnClickListener {
            val i = Intent()
            i.putExtra("Latitude", lat)
            i.putExtra("Longitude", lng)
            i.putExtra("ID", id)
            setResult(Activity.RESULT_OK, i)
            finish()
        })
        writeReview.setOnClickListener(){
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }


    private fun populateListListView() {
        // setup InspectionListList
        val InspectionList: ArrayList<Inspection>? =
            ID?.let { InspectionManager.instance?.getInspections(it) }
        // setup ListView
        val inspectionListAdapter =
            InspectionList?.let { InspectionListAdapter(this, R.layout.list_item_inspection, it) }
        inspectionListView!!.adapter = inspectionListAdapter

        //TODO: set up onclick for item in adapter
        // click the item to launch the Restaurant Detail Activity
        inspectionListView!!.setOnItemClickListener { _, view, position, _ ->
            val intent: Intent? =
                InspectionDetailActivity().makeLaunchIntent(applicationContext, ID, position)
            startActivity(intent)
        }
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