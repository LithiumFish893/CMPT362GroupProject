package com.example.restaurant_review.Model

/**
 * Created by Pin Wen on 4th Nov, 2020.
 *
 * To store single violation data.
 */
class Violation constructor(
    val violationCode: String,
    val violationCritical: String,
    detail: String
) {
    val violationDetail: String
    override fun toString(): String {
        // for testing the read data.
        return ("Code: " + violationCode + "\t" +
                "Critical:" + violationCritical + "\t" +
                "Detail:" + violationDetail)
    }



    init {
        // To replace the weired code with symbol in the column.
        violationDetail = detail.replace("掳", "°")
    }
}