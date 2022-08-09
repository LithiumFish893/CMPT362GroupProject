package com.example.restaurant_review.Model

import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.lang.Exception
import java.util.*


class HealthInspectionHtmlScraper (var onReadApiCompleteListener: OnReadApiCompleteListener? = null) {
    private val inspectionManager = InspectionManager.instance
    fun scrape(query: String, restaurantId: String){
        try {
            scrapeFraserHealth(query, restaurantId)
        } catch (e: Exception){
            onReadApiCompleteListener?.onReadApiComplete()
        }
    }
    fun scrapeFraserHealth (query: String, restaurantId: String){
        val queue = Volley.newRequestQueue(MyApplication.context)
        val url = "https://www.healthspace.ca/Clients/FHA/FHA_Website.nsf/Food-List-ByName?SearchView"
        val restaurantsRequest: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                if (response != null) {
                    val searchString = "/Clients/FHA/FHA_Website.nsf/Food-FacilityHistory?OpenView&RestrictToCategory="
                    val start = response.indexOf(searchString)
                    val end = response.indexOf('>', start)
                    if (start == -1) {
                        onReadApiCompleteListener?.onReadApiComplete()
                        return@Listener
                    }
                    val getQuery = response.slice(start until end)
                    val url2 = "https://www.healthspace.ca$getQuery"
                    val restaurantRequest: StringRequest = object : StringRequest (
                        Method.GET, url2,
                        Response.Listener {  response ->
                            val startFlag = "<!-- content -->"
                            var searchIndex = response.indexOf(startFlag)

                            // get all links
                            while (searchIndex != -1){
                                val toFind = "<tr valign=\"top\">"
                                // find <tr valign="top">
                                val trStart = response.indexOf(toFind, searchIndex)
                                val trEnd = response.indexOf("</tr>", trStart)
                                if (trStart == -1) break
                                val tr = response.slice(trStart until trEnd)
                                val date = getDateFromTr(tr)
                                val hazard = getHazardLevelFromTr(tr)

                                val inspection = Inspection (
                                    id = restaurantId,
                                    date = date,
                                    hazard = hazard
                                )
                                inspectionManager?.addInspection(inspection)
                                searchIndex = response.indexOf(toFind, trEnd)
                            }
                            onReadApiCompleteListener?.onReadApiComplete()
                        },
                        Response.ErrorListener { } ) { }
                    queue.add(restaurantRequest)
                }
            },
            Response.ErrorListener { }) {
            //This is for Headers If You Needed
            override fun getHeaders(): Map<String, String> {
                return HashMap()
            }

            override fun getBody(): ByteArray {
                return "Query=$query".toByteArray()
            }
        }
        queue.add(restaurantsRequest)
    }

    fun getInspectionLinkFromTr (trString: String) : String {
        val preInspectionLinkString = "href="
        val postInspectionLinkString = ">"
        val preInspectionLinkIndex = trString.indexOf(preInspectionLinkString)
        val linkStringStart = preInspectionLinkIndex + preInspectionLinkString.length
        val postInspectionLinkIndex = trString.indexOf(postInspectionLinkString, preInspectionLinkIndex)
        val linkStringEnd = postInspectionLinkIndex
        val linkString = trString.slice(linkStringStart until linkStringEnd)
        return linkString
    }

    fun getDateFromTr (trString: String): String {
        val preDateString = "&nbsp;&nbsp;"
        val postDateString = "</td>"
        val preDateIndex = trString.indexOf(preDateString)
        val dateStringStart = preDateIndex + preDateString.length
        val postDateIndex = trString.indexOf(postDateString, preDateIndex)
        val dateStringEnd = postDateIndex
        val dateString = trString.slice(dateStringStart until dateStringEnd)
        return dateString
    }

    fun getHazardLevelFromTr (trString: String): String {
        val preLevelString = "<font color=#"
        val postLevelString = "</font"
        val preLevelIndex = trString.indexOf(preLevelString)
        // 6 for font color hex, 1 for end bracket >
        val levelStart = preLevelIndex + preLevelString.length + 6 + 1
        val postLevelIndex = trString.indexOf(postLevelString)
        val levelEnd = postLevelIndex
        var hazardLevel = trString.slice(levelStart until levelEnd)
        // high has bold
        hazardLevel = hazardLevel.removePrefix("<B>").removeSuffix("</B>")
        return hazardLevel
    }
}