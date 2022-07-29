package com.example.restaurant_review.Data

data class Review (
    var author: String = "",
    var title: String = "",
    var dateTime: String = "",
    var comment: String = "",
    var rating: Float = 1f,
    var restaurantTrackingNumber: String = ""
)
{

}