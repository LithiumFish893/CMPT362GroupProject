package com.example.restaurant_review.local_database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comment_table")
class CommentModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    val parentPostId: Int = 0,
    val userId: Int = 0,
    val timeStamp: Long = 0,
    val textContent: String = "",
) {
}