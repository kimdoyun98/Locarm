package com.project.locarm.data.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.project.locarm.data.AddressDTO
import com.project.locarm.data.remote.ApiService
import retrofit2.HttpException
import java.io.IOException

class AddressDataSource(
    private val service: ApiService,
    private val query: String
) : PagingSource<Int, AddressDTO.Result.Juso>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, AddressDTO.Result.Juso> {
        val position = params.key ?: ADDRESS_STARTING_PAGE

        return try {
            val response = service.getAddress(query, position)
            val juso = response.body()?.result?.juso ?: emptyList()

            LoadResult.Page(
                data = juso,
                prevKey = if (position == ADDRESS_STARTING_PAGE) null else position - 1,
                nextKey = if (juso.isEmpty() || juso.size < 10) null else position + 1
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, AddressDTO.Result.Juso>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    companion object {
        private const val ADDRESS_STARTING_PAGE = 1
    }
}
