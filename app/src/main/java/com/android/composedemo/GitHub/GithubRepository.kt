package com.android.composedemo.GitHub

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.android.composedemo.GitHub.api.GithubService
import com.android.composedemo.GitHub.api.RepoSearchResponse
import com.android.composedemo.GitHub.db.RepoDatabase
import com.android.composedemo.GitHub.model.RemoteKeys
import com.android.composedemo.GitHub.model.Repo
import com.android.composedemo.utils.Logcat
import com.android.composedemo.utils.dLog
import com.android.composedemo.utils.isWorkThread
import com.fz.gson.GsonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class GithubRepository(
    private val githubService: GithubService,
    private val repoDatabase: RepoDatabase
) {
    val config = PagingConfig(
        pageSize = NETWORK_PAGE_SIZE,
        initialLoadSize = NETWORK_PAGE_SIZE,
        enablePlaceholders = false
    )

    @OptIn(ExperimentalPagingApi::class)
    fun requestRepository(
        query: String,
    ): Flow<PagingData<Repo>> {
        val remotePagingSource = RemotePagingSource(githubService, query, config)
        val pagingSource = { repoDatabase.reposDao().getReposByName(query) }
        return Pager(
            config = config,
            remoteMediator = GithubRemoteMediator(remotePagingSource, repoDatabase, query),
            pagingSourceFactory = pagingSource
//            pagingSourceFactory = { remotePagingSource }
        ).flow
    }

    companion object {
        const val NETWORK_PAGE_SIZE = 30
        const val GITHUB_STARTING_PAGE_INDEX = 1
        const val IN_QUALIFIER = "in:name,description"
    }

    @OptIn(ExperimentalPagingApi::class)
    class GithubRemoteMediator(
        private val pagingSource: RemotePagingSource,
        private val repoDatabase: RepoDatabase,
        private val query: String
    ) : RemoteMediator<Int, Repo>() {
        override suspend fun load(
            loadType: LoadType,
            state: PagingState<Int, Repo>
        ): MediatorResult {
            if (isWorkThread()) {
                Logcat.d("RemotePagingSource", " >>>loadType = $loadType,query = $query")
                val params: LoadParams<Int> = when (loadType) {
                    LoadType.REFRESH -> {
                        val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                        LoadParams.Refresh(
                            remoteKeys?.nextKey?.minus(1),
                            pagingSource.config.pageSize,
                            false
                        )
                    }

                    LoadType.PREPEND -> {
                        val remoteKeys = getRemoteKeyForFirstItem(state)
                        val prevKey = remoteKeys?.prevKey
                            ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                        LoadParams.Prepend(prevKey, pagingSource.config.pageSize, false)
                    }

                    LoadType.APPEND -> {
                        val remoteKeys = getRemoteKeyForLastItem(state)
                        val nextKey = remoteKeys?.nextKey
                            ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                        LoadParams.Append(nextKey, pagingSource.config.pageSize, false)
                    }
                }
                try {
                    val result = pagingSource.load(params = params)
                    val repos = result.data
                    val prevKey = result.prevKey
                    val nextKey = result.nextKey
                    val endOfPaginationReached = nextKey == null
                    repoDatabase.withTransaction {
                        // clear all tables in the database
                        if (loadType == LoadType.REFRESH) {
                            repoDatabase.remoteKeysDao().clearRemoteKeys()
                            repoDatabase.reposDao().clearRepos()
                        }

                        val keys = repos.map {
                            RemoteKeys(repoId = it.id, prevKey = prevKey, nextKey = nextKey)
                        }
                        repoDatabase.remoteKeysDao().insertAll(keys)
                        repoDatabase.reposDao().insertAll(repos)
                    }
                    dLog {  "RemotePagingSource >>>endOfPaginationReached:$endOfPaginationReached" }
                    return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
                } catch (exception: IOException) {
                    exception.printStackTrace()
                    return MediatorResult.Error(exception)
                } catch (exception: HttpException) {
                    exception.printStackTrace()
                    return MediatorResult.Error(exception)
                }
            } else {
                return withContext(Dispatchers.IO) {
                    load(loadType, state)
                }
            }
        }

        private fun getRemoteKeyForLastItem(state: PagingState<Int, Repo>): RemoteKeys? {
            // Get the last page that was retrieved, that contained items.
            // From that last page, get the last item
            return state.pages.lastOrNull() { it.data.isNotEmpty() }?.data?.lastOrNull()
                ?.let { repo ->
                    // Get the remote keys of the last item retrieved
                    repoDatabase.remoteKeysDao().remoteKeysRepoId(repo.id)
                }
        }

        private fun getRemoteKeyForFirstItem(state: PagingState<Int, Repo>): RemoteKeys? {
            // Get the first page that was retrieved, that contained items.
            // From that first page, get the first item
            return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
                ?.let { repo ->
                    // Get the remote keys of the first items retrieved
                    repoDatabase.remoteKeysDao().remoteKeysRepoId(repo.id)
                }
        }

        private fun getRemoteKeyClosestToCurrentPosition(
            state: PagingState<Int, Repo>
        ): RemoteKeys? {
            // The paging library is trying to load data after the anchor position
            // Get the item closest to the anchor position
            return state.anchorPosition?.let { position ->
                state.closestItemToPosition(position)?.id?.let { repoId ->
                    repoDatabase.remoteKeysDao().remoteKeysRepoId(repoId)
                }
            }
        }
    }

    class RemotePagingSource(
        private val service: GithubService,
        private val query: String,
        val config: PagingConfig
    ) : PagingSource<Int, Repo>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult.Page<Int, Repo> {
            if (isWorkThread()) {
                val currentPage = params.key ?: GITHUB_STARTING_PAGE_INDEX
                val loadSize = params.loadSize
                val apiQuery = "$query $IN_QUALIFIER"
                val initialLoadSize = config.initialLoadSize
//                val response = service.requestData(apiQuery, currentPage, loadSize)
                val response = service.requestLocalData()
                Logcat.writeLog("RemotePagingSource", " >>>response.size = ${response?.items?.size?:0}")
                val totalPage = calTotalPage(response?.total ?: 0, loadSize)
                val curTotalSize = currentPage * loadSize
                val maxSize = config.maxSize
                val nextKey =
                    if (curTotalSize >= maxSize || response == null || response.isEmpty() || currentPage >= totalPage) {
                        null
                    } else {
                        currentPage + 1
                    }
                Logcat.d(
                    "RemotePagingSource",
                    " >>>currentPage = $currentPage, loadSize = $loadSize," +
                            " totalPage = $totalPage,initialLoadSize = " +initialLoadSize+
                            " nextKey = $nextKey, maxSize = $maxSize,curTotalSize = $curTotalSize"
                )
                return LoadResult.Page(
                    data = response?.items ?: arrayListOf(),
                    prevKey = if (currentPage == 1) null else currentPage - 1,
                    nextKey = nextKey
                )
            } else {
                return withContext(Dispatchers.IO) {
                    load(params)
                }
            }
        }

        private fun calTotalPage(totalSize: Int, pageSize: Int = NETWORK_PAGE_SIZE): Int {
            val pageNum = totalSize / pageSize
            val leftPage = if (totalSize % pageSize > 0) 1 else 0
            return pageNum + leftPage
        }

        override fun getRefreshKey(state: PagingState<Int, Repo>): Int? {
            return state.anchorPosition?.let { anchorPosition ->
                val page = state.closestPageToPosition(anchorPosition)
                Logcat.d("RemotePagingSource", " >>>currentPage = ${page?.nextKey}, pageSize = $page")
                page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1) // 返回刷新会使用的键
            }
        }
    }
}