package com.example.restaurant_review.Data

/**
 * Data class that holds the information for a restaurant review.
 */
data class Review (
    var id: String = "",
    var author: String = "",
    var title: String = "",
    var dateTime: String = "",
    var comment: String = "",
    var rating: Float = 1f,
    var restaurantTrackingNumber: String = ""
)
{

}