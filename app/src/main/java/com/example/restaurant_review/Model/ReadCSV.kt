package com.example.restaurant_review.Model

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.restaurant_review.R
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.ArrayList

/**
 * ReadCSV Class Implementation
 *
 * Read inspection data from file and store in the objects.
 */
class ReadCSV (private val onReadCSVCompleteListener: onReadCSVCompleteListener? = null) {
    private fun readRestaurantData(`is`: InputStream?) {
        // Read CSV Resource File: Android Programming - Brian Fraser
        // Reference: https://www.youtube.com/watch?v=i-TqNzUryn8&ab_channel=BrianFraser

        // InputStream is = getResources().openRawResource(R.raw.restaurants_itr1);
        // pass the input stream by parameter
        val reader = BufferedReader(
            InputStreamReader(`is`, StandardCharsets.UTF_8)
        )
        var line:String ?= ""
        try {
            // To skip over headline
            reader.readLine()
            line = reader.readLine()
            while (line != null) {
                // Split by ","
                // Don's split the content with quotation mark
                val tokens = parseString(line)
                // Create the Restaurant Manager
                val manager: RestaurantManager? = RestaurantManager.Companion.instance
                // Create the new Restaurant object to store data
                val mRestaurant = Restaurant(
                    tokens[0].replace("\"".toRegex(), ""),
                    tokens[1].replace("\"".toRegex(), ""),
                    tokens[2].replace("\"".toRegex(), ""),
                    tokens[3].replace("\"".toRegex(), ""),
                    tokens[4].replace("\"".toRegex(), ""),
                    tokens[5].toDouble(),
                    tokens[6].toDouble()
                )
                manager?.addRestaurant(mRestaurant)
                line = reader.readLine()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // sort the arrayList before populate
        val allRestaurants: ArrayList<Restaurant> = RestaurantManager.Companion.instance?.allRestaurants ?: ArrayList()
        Collections.sort(allRestaurants, Comparator { item, t1 ->
            val s1 = item.name
            val s2 = t1.name
            s1.compareTo((s2), ignoreCase = true)
        })
    }
//reference : https://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes

    fun parseString(input: String): List<String> {
        val result = mutableListOf<String>()
        var inQuotes = false
        var inEscape = false
        val current = StringBuilder()
        for (i in input.indices) {
            // If this character is escaped, add it without looking
            if (inEscape) {
                inEscape = false
                current.append(input[i])
                continue
            }
            when (val c = input[i]) {
                '\\' -> inEscape = true // escape the next character, \ isn't added to result
                ',' -> if (inQuotes) {
                    current.append(c)
                } else {
                    result += current.toString()
                    current.clear()
                }
                '"' -> inQuotes = !inQuotes
                else -> current.append(c)
            }
        }
        if (current.isNotEmpty()) {
            result += current.toString()
        }
        if(result.size == 6)
            result += ""
        return result
    }
    private fun readInspectionData(`is`: InputStream?) {
        // Read CSV Resource File: Android Programming - Brian Fraser
        // Reference: https://www.youtube.com/watch?v=i-TqNzUryn8&ab_channel=BrianFraser
        val reader = BufferedReader(
            InputStreamReader(`is`, StandardCharsets.UTF_8)
        )
        var line:String?
        try {
            // To skip over headline
            reader.readLine()
            line = reader.readLine()
            while (line != null) {
                // Split by ","
                // Don's split the content with quotation mark

                val tokens = parseString(line)
                    // Create the Inspection Manager
                val manager: InspectionManager ?= InspectionManager.Companion.instance
                // Create the new Inspection object to store data

                val mInspection = Inspection(
                    tokens[0].replace("\"".toRegex(), ""),
                    tokens[1].replace("\"".toRegex(), ""),
                    tokens[2].replace("\"".toRegex(), ""),
                    tokens[3].replace("", "0").toInt(),
                    tokens[4].replace("", "0").toInt(),  // handle empty column
                    tokens[5].replace("\"".toRegex(), ""),
                    tokens[6].replace("\"".toRegex(), "")
                )
                manager?.addInspection(mInspection)
                line = reader.readLine()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val inspectionList: ArrayList<Inspection> =
            (InspectionManager.Companion.instance?.inspections) ?: ArrayList()
        inspectionList.sortWith(Comparator { item, t1 ->
            val s1 = item.date
            val s2 = t1.date
            s2!!.compareTo((s1)!!, ignoreCase = true)
        })
    }

    fun LoadLocalData() {
        val mPrefs: SharedPreferences ?= MyApplication.context
            ?.getSharedPreferences("mPrefs", Context.MODE_PRIVATE)
        // init with the iteration 1 data
//        mPrefs?.edit()?.clear()?.apply()
        var restaurantInputStream: InputStream? =
            MyApplication.context?.resources
                ?.openRawResource(R.raw.restaurants_itr1)
        var inspectionInputStream: InputStream? =
            MyApplication.context?.resources
                ?.openRawResource(R.raw.inspectionreports_itr1)

        // check if local data exist
        val restaurantsFilePath: String? = mPrefs?.getString("RestaurantsFilePath", null)
        val inspectionsFilePath: String? = mPrefs?.getString("InspectionsFilePath", null)

        // load the local data
        if (restaurantsFilePath != null) {
            // set the restaurantInputStream
            val file = File(restaurantsFilePath)
            try {
                restaurantInputStream = FileInputStream(file)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
        if (inspectionsFilePath != null) {
            // set the restaurantInputStream
            val file = File(inspectionsFilePath)
            try {
                inspectionInputStream = FileInputStream(file)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }

        // load data form the CSV files
        if (!RestaurantManager.Companion.instance?.isEmpty!!) {
            // clear the array list before adding new
            RestaurantManager.Companion.instance?.clear()
        }
        if (!InspectionManager.Companion.instance?.isEmpty!!) {
            // clear the array list before adding new
            InspectionManager.Companion.instance?.clear()
        }
        readRestaurantData(restaurantInputStream)
        readInspectionData(inspectionInputStream)

    }
}

interface onReadCSVCompleteListener {
    fun onReadCSVComplete()
}