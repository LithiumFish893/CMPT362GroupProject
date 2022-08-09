package com.example.restaurant_review.local_database

import androidx.room.TypeConverter

/**
 * Converts a list of strings to a string and vice versa.
 * Used to store/get imageList in SocialMediaPostModel.
 */
class Converters {
    // https://stackoverflow.com/questions/44986626/android-room-database-how-to-handle-arraylist-in-an-entity
    @TypeConverter
    fun fromString(stringListString: String): List<String> {
        val delim = ","
        if (!stringListString.contains(delim) && stringListString.isEmpty()) return listOf()
        return stringListString.split(delim).map { it }
    }

    @TypeConverter
    fun toString(stringList: List<String>): String {
        val res: String = stringList.joinToString(separator = ",")
        return res
    }
}
