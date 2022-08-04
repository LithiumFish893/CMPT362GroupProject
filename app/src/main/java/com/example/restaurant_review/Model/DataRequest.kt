package com.example.restaurant_review.Model

import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject
import android.util.Log
import com.android.volley.Request
import java.lang.Exception

/**
 * DataRequest Class Implementation
 *
 * To request the url of download file and state of server connection
 */
class DataRequest {
    var isRestaurantsConnected = false
    var isInspectionsConnected = false
    var restaurantsLastModified: String? = null
    var inspectionsLastModified: String? = null
    var restaurantsUrl: String? = null
    var inspectionsUrl: String? = null

    companion object {
        var instance: DataRequest? = null
            get() {
                if (field == null) {
                    field = DataRequest()
                }
                return field
            }
            private set
        const val URL_RESTAURANT =
            "https://data.surrey.ca/api/3/action/package_show?id=restaurants"
        private const val URL_INSPECTION =
            "https://data.surrey.ca/api/3/action/package_show?id=fraser-health-restaurant-inspection-reports"
    }

    init {
        // init the volley request queue
        val queue = Volley.newRequestQueue(MyApplication.context)
        // Request the info for restaurants
        val restaurantsRequest = StringRequest(Request.Method.GET, URL_RESTAURANT, { response ->
            try {
                val overall = JSONObject(response)
                isRestaurantsConnected = overall.getString("success") == "true"
                // successfully connect to API
                if (isRestaurantsConnected) {
                    val result = overall.getJSONObject("result")
                    val resources = result.getJSONArray("resources")
                    val csv = resources.getJSONObject(0)
                    restaurantsLastModified = csv.getString("last_modified")
                    restaurantsUrl = csv.getString("url")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("TAG", "Failed to Parse Json")
            }
        }) { Log.d("TAG", "Data : Response Failed") }

        // Request data for inspection reports.
        val inspectionsRequest = StringRequest(Request.Method.GET, URL_INSPECTION, { response ->
            try {
                val overall = JSONObject(response)
                isInspectionsConnected = overall.getString("success") == "true"
                // successfully connect to API
                if (isInspectionsConnected) {
                    val result = overall.getJSONObject("result")
                    val resources = result.getJSONArray("resources")
                    val csv = resources.getJSONObject(0)
                    inspectionsLastModified = csv.getString("last_modified")
                    inspectionsUrl = csv.getString("url")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("TAG", "Failed to Parse Json")
            }
        }) { Log.d("TAG", "Data : Response Failed") }
        queue.add(restaurantsRequest)
        queue.add(inspectionsRequest)
    }
}