package com.example.restaurant_review.local_database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

/**
 * A data class holding the contents of a social media post.
 * @param id: The unique id of the post
 * @param timeStamp: The time (UNIX epoch time in seconds) when this was posted
 * @param locationLat: The latitude of the location.
 * @param locationLong: The latitude of the location.
 * @param textContent: The text in the post
 * @param imgList: The list of the URIs of attached images in the post.
 */
@Entity(tableName = "social_media_post_table")
@TypeConverters(Converters::class)
data class SocialMediaPostModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    val userId: Int = 0,
    val timeStamp: Long = 0,
    val locationLat: Double = 0.0,
    val locationLong: Double = 0.0,
    val likeCount: Int = 0,
    val title: String = "",
    val textContent: String = "",
    val imgList: List<String>,
    ) {


    override fun hashCode(): Int {
        var result = id
        result = 31 * result + userId
        result = 31 * result + timeStamp.hashCode()
        result = 31 * result + locationLat.hashCode()
        result = 31 * result + locationLong.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + textContent.hashCode()
        result = 31 * result + imgList.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SocialMediaPostModel

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (timeStamp != other.timeStamp) return false
        if (locationLat != other.locationLat) return false
        if (locationLong != other.locationLong) return false
        if (title != other.title) return false
        if (textContent != other.textContent) return false
        if (imgList != other.imgList) return false

        return true
    }


}