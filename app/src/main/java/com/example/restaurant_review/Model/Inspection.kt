package com.example.restaurant_review.Model

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Inspection Class Implementation
 *
 * To store single inspection data.
 */
class Inspection(// files name in CSV file
    val id: String,
    val date: String,
    hazard: String,
) {
    var simpleDate: Date? = null
    val hazard: String

    override fun toString(): String {
        // for testing the read data.
        return "Restaurant Info: " +
                "ID:" + id + "\t" +
                "Date:" + date + "\t" +
                "Type:" + hazard + "\t"
    }

    init {
        // set date format
        val df = SimpleDateFormat("dd-MMM-yyyy")
        simpleDate = try {
            df.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
        this.hazard = hazard
    }
}