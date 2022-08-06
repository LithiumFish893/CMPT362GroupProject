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