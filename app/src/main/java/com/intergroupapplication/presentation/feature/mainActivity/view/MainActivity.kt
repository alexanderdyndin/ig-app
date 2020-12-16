package com.intergroupapplication.presentation.feature.mainActivity.view

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.intergroupapplication.R
import com.intergroupapplication.data.session.UserSession
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable


import javax.inject.Inject

class MainActivity : FragmentActivity() {

    @Inject
    lateinit var userSession: UserSession

    lateinit var compositeDisposable: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        setContentView(R.layout.activity_main)
        compositeDisposable = CompositeDisposable()
    }

}