package com.example.restaurant_review.Views

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.example.restaurant_review.Model.Inspection
import com.example.restaurant_review.Model.InspectionManager
import com.example.restaurant_review.Model.Restaurant
import com.example.restaurant_review.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * RestaurantListAdapter Class Implementation
 *
 * To populate the data to ListView.
 * To support the search/filter functions.
 */
class RestaurantListAdapter(
    context: Context?,
    listViewResId: Int,
    restaurantList: ArrayList<Restaurant>
) : ArrayAdapter<Restaurant?>(context!!, listViewResId, restaurantList as List<Restaurant?>) {
    private var mRestaurantsList: ArrayList<Restaurant>
    private var originalRestaurantsList: ArrayList<Restaurant>
    private var mPrefs: SharedPreferences
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val viewRestaurant: View =
            view ?: LayoutInflater.from(context).inflate(R.layout.list_item_restaurant, null)
        println("getting view")
        // Get the restaurant and inspection objects.
        val mRestaurant: Restaurant = mRestaurantsList[position]
        val inspectionList: ArrayList<Inspection> ?=
            InspectionManager.instance?.getInspections(mRestaurant.id)

        // Get each view from the list_item_restaurant
        val restaurantIcon = viewRestaurant.findViewById<ImageView>(R.id.list_restaurant_icon)
        val restaurantName: TextView =
            viewRestaurant.findViewById<TextView>(R.id.list_restaurant_name)
        val restaurantIssues: TextView =
            viewRestaurant.findViewById<TextView>(R.id.list_restaurant_issues)
        val restaurantHazardIcon =
            viewRestaurant.findViewById<ImageView>(R.id.list_restaurant_hazard_icon)
        val restaurantHazard: TextView =
            viewRestaurant.findViewById<TextView>(R.id.list_restaurant_hazard)
        val restaurantDate: TextView =
            viewRestaurant.findViewById<TextView>(R.id.list_restaurant_date)
        val restaurantFaveIcon = viewRestaurant.findViewById<ImageView>(R.id.list_restaurant_fave)

        // set the fave icon for restaurant
        val faveRestaurants: String ?= mPrefs.getString("fave_restaurants", "")
        if (faveRestaurants?.contains(mRestaurant.id) == true) {
            restaurantFaveIcon.setImageResource(R.drawable.ic_menu_mark_favorite)
        } else {
            restaurantFaveIcon.setImageResource(R.drawable.ic_menu_unmark_favorite)
        }
        // see which icon is needed and set value for each view
        Glide.with(context).load(mRestaurant.imgUrl).placeholder(R.drawable.ic_restaurant).into(restaurantIcon)

        restaurantName.text = mRestaurant.name
        restaurantHazardIcon.setImageResource(R.drawable.ic_baseline_star_rate_24)
        val database = Firebase.database
        database.reference.child("restaurants")
            .child(mRestaurant.id).child("avgRating").addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null)
                        restaurantHazard.text = "0"
                    else
                        restaurantHazard.text = snapshot.value.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        restaurantIssues.text = mRestaurant.address
        restaurantDate.text = mRestaurant.type
        return viewRestaurant
    }

    fun updateList (newList: ArrayList<Restaurant>){
        mRestaurantsList = newList
        originalRestaurantsList = ArrayList(newList)
        notifyDataSetChanged()
    }


    // TODO: To Jason and Dylan
    // For setup filter/favorite functions
    override fun getFilter(): RestaurantsFilter {
        return RestaurantsFilter()
    }

    var favesOnly = false
        protected set
    var includeSafe = true
        protected set
    var includeModerate = true
        protected set
    var includeUnsafe = true
        protected set
    var includeUnknown = true
        protected set

    // Implementation of Filter
    inner class RestaurantsFilter : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val results = FilterResults()
            if (constraint === "" && !favesOnly && includeSafe && includeModerate && includeUnsafe && includeUnknown ) {
                // No constraint made, apply the original array list.
                results.values = originalRestaurantsList
                results.count = originalRestaurantsList.size
            } else {
                val filteredRestaurants: ArrayList<Restaurant> = ArrayList<Restaurant>()
                val filteredBySafety = ArrayList<String>()
                var filteredByFaves = ArrayList<String?>()
                // Only Show Faves Part
                if (favesOnly) {
                    filteredByFaves = favorites
                }
                // Filter by Safety Part
                if (!includeSafe || !includeModerate || !includeUnsafe|| !includeUnknown) {
                    val safeties: HashMap<String, String?>? =
                        InspectionManager.instance?.safetyLevels
                    //If low selected
                    if (includeSafe) {
                        for (s in safeties!!.keys) {
                            if (safeties[s] == "Low") {
                                filteredBySafety.add(s)
                            }
                        }
                    }
                    //If mid selected
                    if (includeModerate) {
                        for (s in safeties?.keys!!) {
                            if (safeties[s] == "Moderate") {
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

                // Looking at single restaurant one by one
                for (r in originalRestaurantsList) {
                    // Matching text in the search box to the restaurants name
                    if (r.name.trim().replace(" ", "").toLowerCase()
                            .contains(
                                constraint.toString().trim { it <= ' ' }.replace(" ", "").lowercase(
                                    Locale.getDefault()
                                )
                            )
                    ) {
                        // Add to the filtered list
                        if (favesOnly || !includeSafe || !includeModerate || !includeUnsafe || !includeUnknown) {
                            if (favesOnly && filteredByFaves.contains(r.id)) {
                                // combine with faves and keywords and certain hazard level
                                if (!includeSafe || !includeModerate || !includeUnsafe || !includeUnknown) {
                                    if (filteredBySafety.contains(r.id)) {
                                        filteredRestaurants.add(r)
                                    }
                                } else {
                                    filteredRestaurants.add(r)
                                }
                            } else if (!favesOnly && filteredBySafety.contains(r.id)) {
                                filteredRestaurants.add(r)
                            }
                        } else {
                            // match the key words
                            filteredRestaurants.add(r)
                        }
                    }
                }

                // Apply the filtered ArrayList
                results.values = filteredRestaurants
                results.count = filteredRestaurants.size
            }
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            // To notice the adapter about the new filtered list
            if (results.count == 0) {
                // Update ListView
                mRestaurantsList.clear()
                notifyDataSetChanged()
            } else {
                // Update ListView
                mRestaurantsList.clear()
                mRestaurantsList.addAll((results.values as ArrayList<Restaurant>))
                notifyDataSetChanged()
            }
        }

        fun setIncludeSafe(includeSafe: Boolean) {
            this@RestaurantListAdapter.includeSafe = includeSafe
        }

        fun setIncludeModerate(includeModerate: Boolean) {
            this@RestaurantListAdapter.includeModerate = includeModerate
        }

        fun setIncludeUnsafe(includeUnsafe: Boolean) {
            this@RestaurantListAdapter.includeUnsafe = includeUnsafe
        }

        fun setIncludeUnknown(includeUnknown: Boolean) {
            this@RestaurantListAdapter.includeUnknown = includeUnknown
        }

        fun setFavoriteOnly(favoriteOnly: Boolean) {
            favesOnly = favoriteOnly
        }

    }

    private val favorites: ArrayList<String?>
        get() {
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

    companion object {
        private const val PREFS_NAME = "mPrefs"
    }

    init {
        mRestaurantsList = restaurantList
        originalRestaurantsList = ArrayList<Restaurant>(mRestaurantsList)
        mPrefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}