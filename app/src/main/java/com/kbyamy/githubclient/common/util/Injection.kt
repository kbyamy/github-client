package com.kbyamy.githubclient.common.util

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner
import com.kbyamy.githubclient.api.GithubApiService
import com.kbyamy.githubclient.data.GithubRepository
import com.kbyamy.githubclient.ui.ViewModelFactory

object Injection {

    private fun provideGithubRepository(): GithubRepository {
        return GithubRepository(GithubApiService.create())
    }

    fun provideViewModelFactory(owner: SavedStateRegistryOwner): ViewModelProvider.Factory {
        return ViewModelFactory(owner, null, provideGithubRepository())
    }

    fun provideViewModelFactory(
        owner: SavedStateRegistryOwner,
        defaultArgs: Bundle
    ): ViewModelProvider.Factory {
        return ViewModelFactory(owner, defaultArgs, provideGithubRepository())
    }

}