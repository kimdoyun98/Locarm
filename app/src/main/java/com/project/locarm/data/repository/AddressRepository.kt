package com.project.locarm.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
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

    suspend fun deleteOldAddressCache(){
        val expireTime = System.currentTimeMillis() - CACHE_TIMEOUT

        database.withTransaction {
            val oldQueryList = addressRemoteKeyDao.getExpiredQueries(expireTime)
            addressRemoteKeyDao.deleteOldRemoteKeys(expireTime)

            oldQueryList.map { addressEntityDao.clearByQuery(it) }
        }
    }

    companion object {
        private const val CACHE_TIMEOUT = 1000L * 60 * 60 * 24 * 7 //7일
        const val NETWORK_PAGE_SIZE = 10
    }
}
