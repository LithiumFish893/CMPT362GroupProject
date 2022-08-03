package com.example.restaurant_review.local_database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SocialMediaPostModel::class, LikedPostsModel::class, CommentModel::class], version = 12)
abstract class SocialMediaPostDatabase : RoomDatabase() {
    abstract val socialMediaPostDao: SocialMediaPostDao

    companion object {
        @Volatile
        private var INSTANCE: SocialMediaPostDatabase? = null
        fun getInstance(context: Context): SocialMediaPostDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    // difference between app context and context:
                    //    -context is activity context
                    //    -application context remains until the user kills the app
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SocialMediaPostDatabase::class.java, "social_media_post_db"
                    )
                        .fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}