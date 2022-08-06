package com.example.restaurant_review.Model

/**
 * Restaurant Class Implementation
 *
 * To store single restaurant data.
 */
class Restaurant     // constructor
constructor(// files name in CSV file
    val id: String,
    val name: String,
    val address: String,
    val city: String,
    val type: String,
    val imgUrl: String,
    val latitude: Double,
    val longitude: Double
) {
    override fun toString(): String {
        // for testing the read data.
        return ("Restaurant Info: " +
                "ID:" + id + "\t" +
                "Name:" + name + "\t" +
                "Address:" + address + "\t" +
                "City:" + city + "\t" +
                "Type:" + type + "\t" +
                "Latitude:" + latitude + "\t" +
                "Longitude:" + longitude)
    }
}