package com.example.restaurant_review.local_database

import androidx.room.Entity

@Entity(tableName = "liked_posts_table", primaryKeys = ["userId", "postId"])
data class LikedPostsModel(
    val userId: Int = 0,
    val postId: Int = 0,
    val isLiked: Int = 0
) {
    companion object {
        const val LIKE = 1
        const val DISLIKE = -1
    }
}