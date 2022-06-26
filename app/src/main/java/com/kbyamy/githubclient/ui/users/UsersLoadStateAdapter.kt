package com.kbyamy.githubclient.ui.users

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter

class UsersLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<UsersLoadStateViewHolder>() {
    override fun onBindViewHolder(holder: UsersLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): UsersLoadStateViewHolder {
        return UsersLoadStateViewHolder.create(parent, retry)
    }
}