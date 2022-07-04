package com.kbyamy.githubclient.ui.userrepositories

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kbyamy.githubclient.R
import com.kbyamy.githubclient.data.model.Repository

class RepositoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val nameTextView: TextView = view.findViewById(R.id.repositoryNameTextView)
    private val langTextView: TextView = view.findViewById(R.id.repositoryLangTextView)
    private val starTextView: TextView = view.findViewById(R.id.repositoryStarTextView)
    private val descTextView: TextView = view.findViewById(R.id.repositoryDescTextView)

    fun bind(repository: Repository, clickListener: (Repository) -> Unit) {
        nameTextView.text = repository.name
        langTextView.text = repository.language
        starTextView.text = repository.stargazers_count.toString()
        descTextView.text = repository.description
        itemView.setOnClickListener { clickListener(repository) }
    }

    companion object {
        fun create(parent: ViewGroup): RepositoryViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_view_repository, parent, false)
            return RepositoryViewHolder(view)
        }
    }

    data class OnItemClickEvent(val clickListener: (repository: Repository) -> Unit)
}