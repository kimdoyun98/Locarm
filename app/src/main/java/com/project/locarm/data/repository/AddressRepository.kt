package com.project.locarm.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.project.locarm.data.model.Juso
import com.project.locarm.data.remote.AddressRemoteMediator
import com.project.locarm.data.remote.ApiService
import com.project.locarm.data.room.DataBase
import com.project.locarm.data.room.dao.AddressEntityDao
import com.project.locarm.data.room.dao.AddressRemoteKeyDao
import kotlinx.coroutines.flow.Flow

class AddressRepository(
    private val service: ApiService,
    private val database: DataBase,
    private val addressEntityDao: AddressEntityDao,
    private val addressRemoteKeyDao: AddressRemoteKeyDao
) {
    fun getSearchResultStream(query: String): Flow<PagingData<Juso>> {
        val pagingSourceFactory = { addressEntityDao.getAddress(query) }

        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(pageSize = NETWORK_PAGE_SIZE, enablePlaceholders = false),
            remoteMediator = AddressRemoteMediator(
                query,
                database,
                addressEntityDao,
                addressRemoteKeyDao,
                service
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow
    }

    companion object {
        const val NETWORK_PAGE_SIZE = 10
    }
}
