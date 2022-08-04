package com.example.restaurant_review.local_database

import com.example.restaurant_review.Util.Util
import com.example.restaurant_review.local_database.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow

class SocialMediaPostRepository(private val socialMediaPostDatabase: SocialMediaPostDatabase) {
    var socialMediaPostDao = socialMediaPostDatabase.socialMediaPostDao
    var allPosts = socialMediaPostDao.getAllPosts()
    val allComments: Flow<List<CommentModel>> =  socialMediaPostDao.getAllComments()
    val allLikedPosts2 = socialMediaPostDao.getAllLikedPosts()
    var allLikedPosts = socialMediaPostDao.getLikedPosts(Util.getUserId())
    var allDislikedPosts = socialMediaPostDao.getDislikedPosts(Util.getUserId())
    var firebaseDatabase = Firebase.database
    var firebaseMediaRef = firebaseDatabase.reference.child("socialMediaPost")
    var auth = Firebase.auth

    companion object {
        const val DEFAULT_PAGE_INDEX = 1
        const val DEFAULT_PAGE_SIZE = 20
    }

    fun insert (post: SocialMediaPostModel){
        CoroutineScope(Dispatchers.IO).launch {
            val id = socialMediaPostDao.insertPost(post)
            val userRef = firebaseMediaRef.child("users").child(post.userId)
            //val postId = userRef.push().key
            println("debug: id $id.")

            val postRef = userRef.child("posts").child(id.toString())
            postRef.child("title").setValue(post.title)
            postRef.child("content").setValue(post.textContent)
            postRef.child("timeStamp").setValue(post.timeStamp)
            postRef.child("imgList").setValue(post.imgList)
            postRef.child("likeCount").setValue(post.likeCount)
            postRef.child("locationLat").setValue(post.locationLat)
            postRef.child("locationLong").setValue(post.locationLong)


        }
    }

    fun delete (id: Int){
        CoroutineScope(Dispatchers.IO).launch {
            socialMediaPostDao.deletePost(id)
        }
    }

    fun deleteAllEntries (){
        CoroutineScope(Dispatchers.IO).launch {
            socialMediaPostDao.deleteAllPosts()
            firebaseMediaRef.child("users").child(auth.currentUser!!.uid).removeValue()
        }
    }

    fun insertLikedPost (likedPostsModel: LikedPostsModel) {
        CoroutineScope(Dispatchers.IO).launch {
            socialMediaPostDao.insertLikedPost(likedPostsModel)
        }
    }

    fun deleteLikedPost (userId: Int, postId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            socialMediaPostDao.deleteLikedPost(userId, postId)
        }
    }

    fun deleteAllLikedPosts (){
        CoroutineScope(Dispatchers.IO).launch {
            socialMediaPostDao.deleteAllLikedPosts()
        }
    }

    fun getLikeCount (postId: Int) : Flow<Int> {
        return socialMediaPostDao.getTotalLikes(postId)
    }

    fun insertComment (comment: CommentModel){
        CoroutineScope(Dispatchers.IO).launch {
            val id = socialMediaPostDao.insertComment(comment)
            println("debug: ${comment.parentPostUserId}, ${comment.parentPostId}")

            val commentRef = firebaseMediaRef.child("users").child(comment.parentPostUserId)
                .child("posts").child(comment.parentPostId.toString())
                .child("comment").child(id.toString())
            commentRef.child("parentPostId").setValue(comment.parentPostId)
            commentRef.child("userId").setValue(comment.userId)
            commentRef.child("textContent").setValue(comment.textContent)
            commentRef.child("timeStamp").setValue(comment.timeStamp)
            //val postId = userRef.push().key
            //println("debug: id $id.")
        }
    }

    fun getAllCommentsWithId (id: Int): Flow<List<CommentModel>>{
        return socialMediaPostDao.getAllCommentsWithId(id)
    }

    fun deleteAllComments() {
        CoroutineScope(Dispatchers.IO).launch {
            socialMediaPostDao.deleteAllComments()
        }
    }

}