package com.example.restaurant_review.local_database

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SocialMediaPostDao {
    @Insert
    suspend fun insertPost(post: SocialMediaPostModel)

    @Query("SELECT * FROM social_media_post_table")
    // don't need suspend because we're using Flow, Flow makes it act like suspend
    fun getAllPosts(): PagingSource<Int, SocialMediaPostModel>

    @Query("SELECT MAX(id) from social_media_post_table")
    fun getMaxId(): Flow<Int>

    @Query("DELETE FROM social_media_post_table")
    suspend fun deleteAllPosts()

    @Query("DELETE FROM social_media_post_table WHERE id = :id")
    suspend fun deletePost(id: Int)

    @Query("SELECT * FROM liked_posts_table")
    fun getAllLikedPosts() : Flow<List<LikedPostsModel>>

    @Query("SELECT postId FROM liked_posts_table WHERE (userId = :userId AND isLiked = 1)")
    fun getLikedPosts(userId: Int): Flow<List<Int>>

    @Query("SELECT postId FROM liked_posts_table WHERE (userId = :userId AND isLiked = -1)")
    fun getDislikedPosts(userId: Int): Flow<List<Int>>

    @Query("SELECT SUM(isLiked) FROM liked_posts_table WHERE postId = :postId")
    fun getTotalLikes(postId: Int): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLikedPost(likedPost: LikedPostsModel)

    @Query("DELETE FROM liked_posts_table WHERE (userId = :userId AND postId = :postId)")
    suspend fun deleteLikedPost(userId: Int, postId: Int)

    @Query("DELETE FROM liked_posts_table")
    suspend fun deleteAllLikedPosts()

    @Query("SELECT * FROM comment_table")
    fun getAllComments(): Flow<List<CommentModel>>

    @Query("SELECT * FROM comment_table WHERE parentPostId=:id")
    fun getAllCommentsWithId(id: Int): Flow<List<CommentModel>>

    @Insert
    suspend fun insertComment(commentModel: CommentModel)

    @Query("DELETE FROM comment_table")
    suspend fun deleteAllComments()
}