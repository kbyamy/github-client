package com.kbyamy.githubclient.ui

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.kbyamy.githubclient.data.GithubApiRepository
import com.kbyamy.githubclient.ui.searchusers.SearchUsersViewModel
import com.kbyamy.githubclient.ui.searchusers.UserRepositoriesViewModel
import java.lang.IllegalArgumentException

class ViewModelFactory(
    owner: SavedStateRegistryOwner,
    private val defaultArgs: Bundle? = null,
    private val repository: GithubApiRepository,
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(SearchUsersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchUsersViewModel(repository, handle) as T
        }
        if (modelClass.isAssignableFrom(UserRepositoriesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserRepositoriesViewModel(repository, handle) as T
        }
        throw IllegalArgumentException("Unexpected ViewModel class")
    }

}