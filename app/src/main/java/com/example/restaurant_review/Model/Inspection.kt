package com.example.restaurant_review.Model

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Inspection Class Implementation
 *
 * To store single inspection data.
 */
class Inspection(// files name in CVS file
    val id: String,
    val date: String,
    type: String,
    numCritical: Int,
    numNonCritical: Int,
    violLump: String,
    hazard: String
) {
    var simpleDate: Date? = null
    val type: String
    val numCritical: Int
    val numNonCritical: Int
    val hazard: String
    var violLump: String
    private var violationsList: ArrayList<Violation>?
    fun getViolationsList(): ArrayList<Violation>? {
        return if (violationsList == null) {
            getViolationsList(violLump)
        } else violationsList
    }

    fun getViolationsList(violationsLump: String): ArrayList<Violation>? {
        violationsList = ArrayList<Violation>()
        try {
            if (violationsLump.contains("|")) {
                val violations = violationsLump.split("\\|").toTypedArray()
                for (violation in violations) {
                    val details = violation.split(",").toTypedArray()
                    val code = details[0].replace("\"".toRegex(), "")
                    val critical = details[1].replace("\"".toRegex(), "")
                    val description = details[2].replace("\"".toRegex(), "")
                    violationsList!!.add(Violation(code, critical, description))
                }
            } else {
                val details = violationsLump.split(",").toTypedArray()
                val code = details[0].replace("\"".toRegex(), "")
                val critical = details[1].replace("\"".toRegex(), "")
                val description = details[2].replace("\"".toRegex(), "")
                violationsList!!.add(Violation(code, critical, description))
            }
        } catch (e: ArrayIndexOutOfBoundsException) {
            violationsList = null
        }
        return violationsList
    }

    override fun toString(): String {
        // for testing the read data.
        return "Restaurant Info: " +
                "ID:" + id + "\t" +
                "Date:" + date + "\t" +
                "Type:" + type + "\t" +
                "Critical:" + numCritical + "\t" +
                "NON-Critical:" + numNonCritical + "\t" +
                "Hazard:" + hazard + "\t" +
                "VioLump:" + violLump
    }

    init {
        // set date format
        val df: DateFormat = SimpleDateFormat("yyyyMMdd")
        simpleDate = try {
            df.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
        this.type = type
        this.numCritical = numCritical
        this.numNonCritical = numNonCritical
        this.hazard = hazard
        this.violLump = violLump
        violationsList = null
    }
}