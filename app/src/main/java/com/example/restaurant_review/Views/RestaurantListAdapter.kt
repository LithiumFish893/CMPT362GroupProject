package com.example.restaurant_review.Views

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.restaurant_review.Model.Inspection
import com.example.restaurant_review.Model.InspectionManager
import com.example.restaurant_review.Model.Restaurant
import com.example.restaurant_review.R
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
        restaurantHazard.text = "5"
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

    // Implementation of Filter
    inner class RestaurantsFilter : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val results = FilterResults()
            println(originalRestaurantsList.size)
            if (constraint === "" && !favesOnly) {
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
                        // Log.d("TAG",r.getName());
                        // Add to the filtered list
                        if (favesOnly) {
                            if (filteredByFaves.contains(r.id)){
                                filteredRestaurants.add(r)
                            }
                        } else {
                            // match the key words
                            filteredRestaurants.add(r)
                        }
                    }
                }
                results.values = filteredRestaurants
                results.count = filteredRestaurants.size
            }
            println("2: ${originalRestaurantsList.size}")
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            // To notice the adapter about the new filtered list

            if (results.count == 0) {println("emptied")
                // Update ListView
                mRestaurantsList.clear()
                notifyDataSetChanged()
            } else {
                println("published")
                // Update ListView
                mRestaurantsList.clear()
                mRestaurantsList.addAll((results.values as ArrayList<Restaurant>))
                notifyDataSetChanged()
            }
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