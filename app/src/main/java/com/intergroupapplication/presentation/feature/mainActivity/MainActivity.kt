package com.intergroupapplication.presentation.feature.mainActivity

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.intergroupapplication.R

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }
}