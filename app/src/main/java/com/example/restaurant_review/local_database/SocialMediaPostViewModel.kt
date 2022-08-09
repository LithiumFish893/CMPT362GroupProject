package com.example.restaurant_review.local_database

import androidx.lifecycle.*
import kotlinx.coroutines.flow.Flow

/**
 * View model to access social media posts and comments from the repository
 */
class SocialMediaPostViewModel (private val repository: SocialMediaPostRepository) : ViewModel() {

    val allLikedPosts = repository.allLikedPosts2.asLiveData()
    val allLikedPostsLiveData = repository.allLikedPosts.asLiveData()
    val allDislikedPostsLiveData = repository.allDislikedPosts.asLiveData()

    fun insert (post: SocialMediaPostModel){
        repository.insert(post)
    }

    fun deleteAllEntries (){
        repository.deleteAllEntries()
    }

    fun deleteEntry (id: Int){
        repository.delete(id)
    }

    fun getAllPosts(): Flow<List<SocialMediaPostModel>> {

        return repository.allPosts
    }

    fun getAllComments(): Flow<List<CommentModel>>  {
        return repository.allComments
    }

    fun insertLikedPost (likedPostsModel: LikedPostsModel){
        repository.insertLikedPost(likedPostsModel)
    }

    fun deleteLikedPost (userId: Int, postId: Int){
        repository.deleteLikedPost(userId, postId)
    }

    fun deleteAllLikedPosts (){
        repository.deleteAllLikedPosts()
    }

    fun getLikeCountLiveData (postId: Int): LiveData<Int>{
        return repository.getLikeCount(postId).asLiveData()
    }

    fun insertComment(commentModel: CommentModel){
        repository.insertComment(commentModel)
    }

    fun getAllCommentsWithId(id: Int): LiveData<List<CommentModel>>{
        return repository.getAllCommentsWithId(id).asLiveData()
    }

    fun deleteAllComments() {
        repository.deleteAllComments()
    }
}

class SocialMediaPostViewModelFactory(private val repository: SocialMediaPostRepository): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SocialMediaPostViewModel::class.java)) {
            return SocialMediaPostViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown viewmodel class")
    }
}