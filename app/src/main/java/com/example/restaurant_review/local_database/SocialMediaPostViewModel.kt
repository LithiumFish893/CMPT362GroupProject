package com.example.restaurant_review.local_database

import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow

@ExperimentalPagingApi
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

    fun getAllPosts(): Flow<PagingData<SocialMediaPostModel>> {

        return repository.letSocialMediaFlowDb().cachedIn(viewModelScope)
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

@ExperimentalPagingApi
class SocialMediaPostViewModelFactory(private val repository: SocialMediaPostRepository): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SocialMediaPostViewModel::class.java)) {
            return SocialMediaPostViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown viewmodel class")
    }
}