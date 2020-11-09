package com.intergroupapplication.presentation.feature.web

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.intergroupapplication.R
import kotlinx.android.synthetic.main.activity_web.*

class WebActivity : AppCompatActivity() {

    companion object {
        private const val KEY_PATH = "PATH"
        private const val KEY_TITLE = "TITLE"

        fun getIntent(context: Context?, path: String, title: Int): Intent {
            return Intent(context, WebActivity::class.java)
                    .apply {
                        putExtra(KEY_PATH, path)
                        putExtra(KEY_TITLE, title)
                    }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        val path = intent.getStringExtra(KEY_PATH)!!
        val name = intent.getIntExtra(KEY_TITLE, R.string.inter_group)


        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setTitle(name)
        }
        webView.loadUrl(path)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

}