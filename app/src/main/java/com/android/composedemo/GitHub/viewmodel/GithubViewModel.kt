package com.android.composedemo.GitHub.viewmodel

import androidx.activity.ComponentActivity
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.savedstate.SavedStateRegistryOwner
import com.android.composedemo.GitHub.GithubRepository
import com.android.composedemo.GitHub.api.GithubService
import com.android.composedemo.GitHub.db.RepoDatabase
import com.android.composedemo.GitHub.model.Repo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GithubViewModel(
    private val repository: GithubRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val mUiState: StateFlow<UiState>
    val userAction: (UiAction) -> Unit
    val pagingDataFlow: Flow<PagingData<UiModel>>

    init {
        val initialQuery: String = savedStateHandle[LAST_SEARCH_QUERY] ?: DEFAULT_QUERY
//        val lastQueryScrolled: String = savedStateHandle[LAST_QUERY_SCROLLED] ?: DEFAULT_QUERY
        savedStateHandle[LAST_SEARCH_QUERY] = initialQuery
        val actionStateFlow = MutableSharedFlow<UiAction>()
        val searchAction = actionStateFlow
            .filterIsInstance<UiAction.Search>()
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
            .onStart { emit(UiAction.Search(query = initialQuery)) }
        val scrollAction = actionStateFlow
            .filterIsInstance<UiAction.Scroll>()
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), 1)
            .onStart { emit(UiAction.Scroll(query = initialQuery)) }
        mUiState = combine(searchAction, scrollAction, ::Pair)
            .flowOn(Dispatchers.IO)
            .map { (search, scroll) ->
                UiState(query = search.query, currentQuery = scroll.query)
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                UiState(query = initialQuery, currentQuery = initialQuery)
            )
        pagingDataFlow =
            searchAction.flatMapLatest { requestSearch(it.query) }
                .flowOn(Dispatchers.IO)
                .cachedIn(viewModelScope)
        userAction = {
            viewModelScope.launch { actionStateFlow.emit(it) }
        }
    }

    fun requestSearch(query: String): Flow<PagingData<UiModel>> {
        return repository.requestRepository(query)
            .map { pagingData -> pagingData.map { UiModel.RepoItem(it) } }
            .map {
                it.insertSeparators { before, after ->
                    if (after == null) {
                        // we're at the end of the list
                        return@insertSeparators null
                    }

                    if (before == null) {
                        // we're at the beginning of the list
                        return@insertSeparators UiModel.SeparatorItem("${after.roundedStarCount}0.000+ stars")
                    }
                    // check between 2 items
                    if (before.roundedStarCount > after.roundedStarCount) {
                        if (after.roundedStarCount >= 1) {
                            UiModel.SeparatorItem("${after.roundedStarCount}0.000+ stars")
                        } else {
                            UiModel.SeparatorItem("< 10.000+ stars")
                        }
                    } else {
                        // no separator
                        null
                    }
                }
            }

    }

    companion object {
        fun Factory(owner: ComponentActivity): ViewModelProvider.Factory {
            return ViewModelFactory(
                owner,
                GithubRepository(GithubService.create(), RepoDatabase.getInstance(owner))
            )
        }
    }
}

sealed class UiAction {
    data class Search(val query: String) : UiAction()
    data class Scroll(val query: String) : UiAction()
}

data class UiState(
    val query: String,
    val currentQuery: String
)

sealed class UiModel {
    data class RepoItem(val repo: Repo) : UiModel()
    data class SeparatorItem(val description: String) : UiModel()
}

private val UiModel.RepoItem.roundedStarCount: Int
    get() = this.repo.stars / 10_000

private const val LAST_QUERY_SCROLLED: String = "last_query_scrolled"
private const val LAST_SEARCH_QUERY: String = "last_search_query"
private const val DEFAULT_QUERY = "iOS"
/**
 * Factory for ViewModels
 */
class ViewModelFactory(
    owner: SavedStateRegistryOwner,
    private val repository: GithubRepository
) : AbstractSavedStateViewModelFactory(owner, null) {

    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(GithubViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GithubViewModel(repository, handle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}