package com.android.composedemo.GitHub.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.android.composedemo.GitHub.model.Repo

@Dao
interface RepoDao {
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    fun insertAll(repos: List<Repo>)

    @Query(
        "SELECT * FROM repos WHERE " +
                "name LIKE :name OR description LIKE :name " +
                "ORDER BY stars DESC, name ASC"
    )
    fun getReposByName(name: String): PagingSource<Int, Repo>

    @Query("DELETE FROM repos")
    fun clearRepos()
}