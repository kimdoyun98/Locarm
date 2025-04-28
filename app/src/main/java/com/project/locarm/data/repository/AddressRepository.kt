package com.project.locarm.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.project.locarm.data.AddressDTO
import com.project.locarm.data.datasource.AddressDataSource
import com.project.locarm.data.remote.ApiService
import kotlinx.coroutines.flow.Flow

class AddressRepository(
    private val service: ApiService
) {
    fun getSearchResultStream(query: String): Flow<PagingData<AddressDTO.Result.Juso>> {
        return Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { AddressDataSource(service, query) }
        ).flow
    }

    companion object {
        const val NETWORK_PAGE_SIZE = 10
    }
}
