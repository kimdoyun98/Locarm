package com.project.locarm.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.project.locarm.data.model.Juso
import com.project.locarm.data.room.DataBase
import com.project.locarm.data.room.dao.AddressEntityDao
import com.project.locarm.data.room.dao.AddressRemoteKeyDao
import com.project.locarm.data.room.entitiy.AddressEntity
import com.project.locarm.data.room.entitiy.AddressRemoteKey
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class AddressRemoteMediator(
    private val query: String,
    private val database: DataBase,
    private val addressEntityDao: AddressEntityDao,
    private val remoteKeyDao: AddressRemoteKeyDao,
    private val networkService: ApiService
) : RemoteMediator<Int, Juso>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Juso>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> {
                    ADDRESS_STARTING_PAGE
                }

                LoadType.PREPEND -> {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }

                LoadType.APPEND -> {
                    val key = remoteKeyDao.getRemoteKey(query)

                    val nextKey = key?.nextKey
                        ?: return MediatorResult.Success(endOfPaginationReached = true)

                    nextKey
                }
            }

            val response = networkService.getAddress(keyword = query, page = page)
            val juso = response.body()?.result?.juso ?: emptyList()

            val endOfPaginationReached = juso.size < state.config.pageSize

            database.withTransaction {

                if (loadType == LoadType.REFRESH) {
                    addressEntityDao.clearByQuery(query)
                    remoteKeyDao.clearKey(query)
                }

                val entities = juso.map {
                    AddressEntity(
                        query = query,
                        jibunAddr = it.jibunAddr,
                        roadAddr = it.roadAddr,
                        name = it.name
                    )
                }

                addressEntityDao.insertAll(entities)

                remoteKeyDao.insertKey(
                    AddressRemoteKey(
                        query = query,
                        nextKey = if (endOfPaginationReached) null else page + 1
                    )
                )
            }

            MediatorResult.Success(endOfPaginationReached)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }

    override suspend fun initialize(): InitializeAction {
        val key = remoteKeyDao.getRemoteKey(query)

        return if (key != null) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    companion object {
        private const val ADDRESS_STARTING_PAGE = 1
    }
}
