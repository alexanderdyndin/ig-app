package com.intergroupapplication.presentation.feature.web

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.intergroupapplication.R
import kotlinx.android.synthetic.main.fragment_web.*

class WebActivity : Fragment() {

    companion object {
        private const val KEY_PATH = "PATH"
        private const val KEY_TITLE = "TITLE"
    }

    private lateinit var path: String
    private lateinit var name: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        path = arguments?.getString(KEY_PATH)!!
        return inflater.inflate(R.layout.fragment_web, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //val name = arguments?.getString(KEY_TITLE, R.string.inter_group)
        //        supportActionBar?.apply {
//            setHomeButtonEnabled(true)
//            setDisplayHomeAsUpEnabled(true)
//            setTitle(name)
//        }
        webView.loadUrl(path)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            android.R.id.home -> findNavController().popBackStack()
        }
        return super.onOptionsItemSelected(item)
    }

}