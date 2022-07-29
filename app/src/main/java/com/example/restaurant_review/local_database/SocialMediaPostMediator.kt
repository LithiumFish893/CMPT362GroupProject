package com.example.restaurant_review.local_database

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator

@OptIn(ExperimentalPagingApi::class)
class SocialMediaPostMediator (database: SocialMediaPostDatabase):
    RemoteMediator<Int, SocialMediaPostModel>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, SocialMediaPostModel>
    ): MediatorResult {
        var isEndOfDb: Boolean = true
        return MediatorResult.Success(endOfPaginationReached = isEndOfDb)
    }
}