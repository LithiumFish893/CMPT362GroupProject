package com.example.restaurant_review.Model

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * InspectionManager Class Implementation
 *
 * Singleton supported
 * To store an ArrayList of inspection data in class.
 */
class InspectionManager private constructor() {
    val inspections: ArrayList<Inspection> = ArrayList()
    val safetyLevels = HashMap<String, String?>()
    private val NumOfCritical = HashMap<String?, Int>()
    fun addInspection(i: Inspection) {
        inspections.add(i)
    }

    fun getInspections(ID: String): ArrayList<Inspection> {
        val retArrayList = ArrayList<Inspection>()
        for (i in inspections) {
            if (i.id == ID) {
                retArrayList.add(i)
            }
        }

        // filter by safety
        if (retArrayList.size > 0) {
            val safety = retArrayList[0].hazard
            safetyLevels[ID] = safety
        } else {
            safetyLevels[ID] = "Unknown"
        }
        return retArrayList
    }

    fun getNumOfCritical(): HashMap<String?, Int> {

        // filter by number of critical violation
        val df: DateFormat = SimpleDateFormat("yyyyMMdd")
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -1)
        val currentTime = df.format(cal.time)
        // Log.d("TAG",currentTime);

        // if the inspection within last year
        if (inspections.size > 0) {
            for (i in inspections) {
                if (i.date != "" && i.date.toInt() > currentTime.toInt()) {
                    var numCritical = 0
                    numCritical = if (NumOfCritical.containsKey(i.id)) (
                        // update exist data
                        NumOfCritical[i.id]?.plus(i.numCritical)
                    )!! else {
                        i.numCritical
                    }
                    NumOfCritical[i.id] = numCritical
                }
            }
        }
        return NumOfCritical
    }

    fun clear() {
        if (!isEmpty) {
            inspections.clear()
        }
    }

    val isEmpty: Boolean
        get() = inspections.size == 0

    companion object {
        var instance: InspectionManager? = null
            get() {
                if (field == null) {
                    field = InspectionManager()
                }
                return field
            }
            private set
    }

}