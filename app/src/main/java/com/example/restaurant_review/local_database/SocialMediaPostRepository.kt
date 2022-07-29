package com.example.restaurant_review.local_database

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.restaurant_review.Util.Util
import com.example.restaurant_review.local_database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow

@ExperimentalPagingApi
class SocialMediaPostRepository(private val socialMediaPostDatabase: SocialMediaPostDatabase) {
    var socialMediaPostDao = socialMediaPostDatabase.socialMediaPostDao
    val allComments: Flow<List<CommentModel>> =  socialMediaPostDao.getAllComments()
    val allLikedPosts2 = socialMediaPostDao.getAllLikedPosts()
    var allLikedPosts = socialMediaPostDao.getLikedPosts(Util.getUserId())
    var allDislikedPosts = socialMediaPostDao.getDislikedPosts(Util.getUserId())

    companion object {
        const val DEFAULT_PAGE_INDEX = 1
        const val DEFAULT_PAGE_SIZE = 20
    }

    fun letSocialMediaFlowDb (pagingConfig: PagingConfig = getDefaultPageConfig()): Flow<PagingData<SocialMediaPostModel>> {
        val pagingSourceFactory = {socialMediaPostDao.getAllPosts()}
        return Pager (
            config = pagingConfig,
            pagingSourceFactory = pagingSourceFactory,
            remoteMediator = SocialMediaPostMediator(socialMediaPostDatabase)
        ).flow
    }

    private fun getDefaultPageConfig(): PagingConfig {
        return PagingConfig(pageSize = DEFAULT_PAGE_SIZE, enablePlaceholders = false)
    }

    fun insert (post: SocialMediaPostModel){
        CoroutineScope(Dispatchers.IO).launch {
            socialMediaPostDao.insertPost(post)
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
            socialMediaPostDao.insertComment(comment)
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