package com.example.restaurant_review.Model

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.json.JSONObject
import java.util.*

/**
 * Read restaurant data from file and store in the restaurant manager.
 */
class YelpAPI (val context: Context, var onReadApiCompleteListener: OnReadApiCompleteListener? = null) {

    private var offset = 0

    fun readMoreRestaurantData (size: Int){
        readRestaurantData(offset, size)
        offset += size
    }

    fun readRestaurantData(start: Int, size: Int) {
        if (start == 0){
            offset = size
            RestaurantManager.instance?.clear()
        }
        /**
         * Gets the user's location and finds an approximate one close to the restaurant
         */
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(context, "Please enable location tracking first!", Toast.LENGTH_SHORT).show()
        }
        else {
            // Create the Restaurant Manager
            val manager: RestaurantManager? = RestaurantManager.instance
            val locationClient = LocationServices.getFusedLocationProviderClient(context)
            locationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener {
                    val queue = Volley.newRequestQueue(MyApplication.context)
                    val latitude = it.latitude
                    val longitude = it.longitude
                    val url = "https://api.yelp.com/v3/businesses/search?term=res&latitude=$latitude&longitude=$longitude&limit=$size&offset=$start"
                    val restaurantsRequest: StringRequest = object : StringRequest(
                        Method.GET, url,
                        Response.Listener { response ->
                            if (response != null) {
                                val responseJSON = JSONObject(response)
                                val businessArray = responseJSON.getJSONArray("businesses")
                                if (businessArray.length() == 0){
                                    return@Listener
                                }
                                for (i in 0 until businessArray.length()){
                                    val business = businessArray.getJSONObject(i)
                                    val coords = business.getJSONObject("coordinates")
                                    val id = business.getString("id")
                                    val name = business.getString("name")
                                    val address = business.getJSONObject("location").getJSONArray("display_address").getString(0).removeSurrounding("\"")
                                    val city = business.getJSONObject("location").getString("city")
                                    var type = ""
                                    if (business.getJSONArray("categories").length() > 0) type = business.getJSONArray("categories").getJSONObject(0).getString("title")
                                    val imageUrl = business.getString("image_url")
                                    val lat = coords.getDouble("latitude")
                                    val long = coords.getDouble("longitude")
                                    // Create the new Restaurant object to store data
                                    val mRestaurant = Restaurant(
                                        id, name, address, city, type, imageUrl, lat, long
                                    )
                                    manager!!.addRestaurant(mRestaurant)

                                    // wipe all prev inspection data
                                    val inspections = ArrayList(InspectionManager.instance?.inspections!!)
                                    for (inspection in inspections){
                                        if (inspection.id == id){
                                            InspectionManager.instance?.inspections!!.remove(inspection)
                                        }
                                    }
                                    HealthInspectionHtmlScraper(object: OnReadApiCompleteListener{
                                        override fun onReadApiComplete() {
                                            // refresh the safety levels
                                            InspectionManager.instance?.getInspections(id)
                                        }

                                    }).scrape(mRestaurant!!.name.lowercase(Locale.getDefault()).removePrefix("the ").removeSuffix(" restaurant"),
                                        id)

                                }
                                onReadApiCompleteListener?.onReadApiComplete()
                            }
                        },
                        Response.ErrorListener { }) {
                        //This is for Headers If You Needed
                        override fun getHeaders(): Map<String, String> {
                            val params: MutableMap<String, String> = HashMap()
                            params["Content-Type"] = "application/json; charset=UTF-8"
                            params["Authorization"] = "Bearer 5DID7gCf8IVCfpHugs-cdm6My39YfL3nvQHIu9XMgKphdbVsXRaM4SNZ740uPQ_dwRUcgh3KHqi7P-Bla1OiK2FuuhKpSaewxI0t_KuPmHzhHPWselIlD2co62zrYnYx"
                            return params
                        }
                    }
                    queue.add(restaurantsRequest)
                }
        }
    }

}

interface OnReadApiCompleteListener {
    fun onReadApiComplete()
}