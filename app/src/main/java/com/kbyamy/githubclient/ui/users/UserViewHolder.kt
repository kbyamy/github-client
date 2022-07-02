package com.kbyamy.githubclient.ui.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kbyamy.githubclient.R
import com.kbyamy.githubclient.data.model.User
import com.squareup.picasso.Picasso

class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val iconImageView: ImageView = view.findViewById(R.id.iconImageView)
    private val nameTextView: TextView = view.findViewById(R.id.nameTextView)

    init {}

    fun bind(user: User, clickListener: (User) -> Unit) {
        Picasso.get().load(user.avatar_url).into(iconImageView)
        nameTextView.text = user.login
        itemView.setOnClickListener { clickListener(user) }
    }

    companion object {
        fun create(parent: ViewGroup): UserViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_view_user, parent, false)
            return UserViewHolder(view)
        }
    }

    data class OnItemClickEvent(val clickListener: (user: User) -> Unit)
}