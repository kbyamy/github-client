package com.kbyamy.githubclient.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kbyamy.githubclient.R
import com.kbyamy.githubclient.ui.users.SearchUsersFragment
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("call")
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, SearchUsersFragment.newInstance())
                .commitNow()
        }
    }
}