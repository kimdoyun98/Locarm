package com.project.locarm.data.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.project.locarm.data.model.Juso
import com.project.locarm.data.room.entitiy.AddressEntity
import com.project.locarm.data.room.entitiy.AddressRemoteKey

@Dao
interface AddressEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(addresses: List<AddressEntity>)

    @Query("DELETE FROM AddressEntity WHERE `query` = :query")
    suspend fun clearByQuery(query: String)

    @Query(
        """
            SELECT jibunAddr, roadAddr, name 
            FROM AddressEntity 
            WHERE `query` = :query
        """
    )
    fun getAddress(query: String): PagingSource<Int, Juso>
}

@Dao
interface AddressRemoteKeyDao {
    @Query("SELECT * FROM AddressRemoteKey WHERE `query` = :query")
    suspend fun getRemoteKey(query: String): AddressRemoteKey?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(key: AddressRemoteKey)

    @Query("DELETE FROM AddressRemoteKey WHERE `query` = :query")
    suspend fun clearKey(query: String)
}
