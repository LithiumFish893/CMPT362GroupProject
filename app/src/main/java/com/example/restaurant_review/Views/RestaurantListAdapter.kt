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
import com.example.restaurant_review.Model.Inspection
import com.example.restaurant_review.Model.InspectionManager
import com.example.restaurant_review.Model.Restaurant
import com.example.restaurant_review.R
import java.text.SimpleDateFormat
import java.util.*

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
    private val mRestaurantsList: ArrayList<Restaurant>
    private val originalRestaurantsList: ArrayList<Restaurant>
    private var mPrefs: SharedPreferences
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val viewRestaurant: View =
            view ?: LayoutInflater.from(context).inflate(R.layout.list_item_restaurant, null)

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
        val name: String = mRestaurant.name
        if (name.contains("104 Sushi")) {
            restaurantIcon.setImageResource(R.drawable.sushiandco)
        } else if (name.contains("5 Star Catering")) {
            restaurantIcon.setImageResource(R.drawable.fivestarcatering)
        } else if (name.contains("7-Eleven")) {
            restaurantIcon.setImageResource(R.drawable.seveneleven)
        } else if (name.contains("A&W")) {
            restaurantIcon.setImageResource(R.drawable.awlogo)
        } else if (name.contains("A1 Coffee & Donuts")) {
            restaurantIcon.setImageResource(R.drawable.a1coffeeanddonuts)
        } else if (name.contains("Aaron's Steak & Pizza")) {
            restaurantIcon.setImageResource(R.drawable.aaronssteakandpizza)
        } else if (name.contains("Academics preKindergarten")) {
            restaurantIcon.setImageResource(R.drawable.academicsprekindergarten)
        } else if (name.contains("Aggarwal Sweets")) {
            restaurantIcon.setImageResource(R.drawable.aggarwalsweets)
        } else if (name.contains("Boston Pizza")) {
            restaurantIcon.setImageResource(R.drawable.bostonpizza)
        } else if (name.contains("Blenz")) {
            restaurantIcon.setImageResource(R.drawable.blenzcoffee)
        } else if (name.contains("Boiling Point")) {
            restaurantIcon.setImageResource(R.drawable.boilingpoint)
        } else if (name.contains("Booster Juice")) {
            restaurantIcon.setImageResource(R.drawable.boosterjuice)
        } else if (name.contains("Church's Chicken")) {
            restaurantIcon.setImageResource(R.drawable.churchschicken)
        } else if (name.contains("Chatime")) {
            restaurantIcon.setImageResource(R.drawable.chatime)
        } else if (name.contains("Chipotle Mexican Grill")) {
            restaurantIcon.setImageResource(R.drawable.chipotle)
        } else if (name.contains("Dairy Queen")) {
            restaurantIcon.setImageResource(R.drawable.dairyqueen)
        } else if (name.contains("Freshslice")) {
            restaurantIcon.setImageResource(R.drawable.freshslice)
        } else {
            restaurantIcon.setImageResource(R.drawable.ic_restaurant)
        }

        //restaurantIcon.setImageResource(R.drawable.ic_restaurant);
        restaurantName.text = mRestaurant.name
        if (inspectionList?.size != 0) {
            val mInspection: Inspection ?= inspectionList?.get(0)
            // setup # issues found
            restaurantIssues.text = context.getString(
                    R.string.restaurant_issues,
                mInspection?.numCritical?.plus(mInspection?.numNonCritical)
                )

            when (mInspection?.hazard) {
                "Low" -> {
                    restaurantHazardIcon.setImageResource(R.drawable.ic_hazard_low)
                    restaurantHazard.text = context.getString(
                            R.string.hazardLevel,
                            mInspection.hazard
                        )
                    restaurantHazard.setTextColor(context.getColor(R.color.colorLowHazard))
                }
                "Moderate" -> {
                    restaurantHazardIcon.setImageResource(R.drawable.ic_hazard_moderate)
                    restaurantHazard.text = context.getString(
                            R.string.hazardLevel,
                            mInspection.hazard
                        )
                    restaurantHazard.setTextColor(context.getColor(R.color.colorModerateHazard))
                }
                "High" -> {
                    restaurantHazardIcon.setImageResource(R.drawable.ic_hazard_high)
                    restaurantHazard.text = context.getString(
                            R.string.hazardLevel,
                            mInspection.hazard
                        )
                    restaurantHazard.setTextColor(context.getColor(R.color.colorHighHazard))
                }
                else -> {}
            }
            // setup last inspection date
            var calendar = Calendar.getInstance()
            val now = calendar.time
            calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -1)
            val oneMonth = calendar.time
            calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -1)
            val oneYear = calendar.time
            if (mInspection?.simpleDate?.before(oneYear) == true) {
                val df = SimpleDateFormat("MMM yyyy")
                val simpleDate = df.format(mInspection?.simpleDate)
                restaurantDate.text = simpleDate
            } else if (mInspection?.simpleDate?.before(oneMonth) == true) {
                val df = SimpleDateFormat("MMM dd")
                val simpleDate = df.format(mInspection?.simpleDate)
                restaurantDate.text =simpleDate
            } else {
                // within one month
                val days: Long =
                    (now.time - mInspection?.simpleDate?.getTime()!!) / (24 * 60 * 60 * 1000)
                restaurantDate.setText(getContext().getString(R.string.inspection_days, days))
            }
        } else {
            restaurantIssues.setText(getContext().getString(R.string.restaurant_no_record))
            restaurantHazardIcon.setImageResource(R.drawable.ic_hazard_unknown)
            restaurantHazard.setText(getContext().getString(R.string.hazard_unknown))
            restaurantHazard.setTextColor(getContext().getColor(R.color.colorUnknownHazard))
            restaurantDate.setText(getContext().getString(R.string.restaurant_no_record))
        }
        return viewRestaurant
    }


    // TODO: To Jason and Dylan
    // For setup filter/favorite functions
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
    var lessEuqalThan = false
        protected set
    var greatEuqalThan = true
        protected set
    var numOfViolation = 0
        protected set
    val filter: RestaurantsFilter
        get() = RestaurantsFilter()

    // Implementation of Filter
    inner class RestaurantsFilter : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val results = FilterResults()
            if (constraint === "" && !favesOnly && includeSafe && includeModerate && includeUnsafe && includeUnknown && numOfViolation == 0) {
                // No constraint made, apply the original array list.
                results.values = originalRestaurantsList
                results.count = originalRestaurantsList.size
            } else {
                val filteredRestaurants: ArrayList<Restaurant> = ArrayList<Restaurant>()
                val filteredBySafety = ArrayList<String>()
                var filteredByFaves = ArrayList<String?>()
                val filteredByViolation = ArrayList<String>()
                // Only Show Faves Part
                if (favesOnly) {
                    filteredByFaves = favorites
                }
                // Filter by Safety Part
                if (!includeSafe || !includeModerate || !includeUnknown || !includeUnknown) {
                    val safeties: HashMap<String, String?>? =
                        InspectionManager.instance?.safetyLevels
                    //If low selected
                    if (includeSafe) {
                        for (s in safeties?.keys!!) {
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

                // set list of filter by number of critical violation
                if (numOfViolation != 0) {
                    val criticalViolations: HashMap<String?, Int>? =
                        InspectionManager.instance?.getNumOfCritical()
                    if (lessEuqalThan) {
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

                // Apply the filter by number of critical violations
                if (numOfViolation != 0) {
                    val finalFilteredRestaurants: ArrayList<Restaurant> = ArrayList<Restaurant>()
                    for (r in filteredRestaurants) {
                        if (filteredByViolation.contains(r.id)) {
                            finalFilteredRestaurants.add(r)
                            Log.d("TAG", Integer.toString(filteredRestaurants.size))
                        }
                    }
                    results.values = finalFilteredRestaurants
                    results.count = finalFilteredRestaurants.size
                }

                // Apply the filtered ArrayList
                if (numOfViolation == 0) {
                    results.values = filteredRestaurants
                    results.count = filteredRestaurants.size
                }
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

        fun setLessEqualThan(b: Boolean) {
            lessEuqalThan = b
        }

        fun setGreatEqualThan(b: Boolean) {
            greatEuqalThan = b
        }

        fun setNumOfViolation(i: Int) {
            numOfViolation = i
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