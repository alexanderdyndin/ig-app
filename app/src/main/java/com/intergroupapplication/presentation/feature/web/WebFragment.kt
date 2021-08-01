package com.intergroupapplication.presentation.feature.web

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.intergroupapplication.R
import com.intergroupapplication.databinding.FragmentWebBinding

class WebFragment : Fragment() {

    private companion object {
        const val KEY_PATH = "PATH"
    }

    private val viewBinding by viewBinding(FragmentWebBinding::bind)

    private var path: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        path = arguments?.getString(KEY_PATH)
        return inflater.inflate(R.layout.fragment_web, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.webView.clearCache(true)
        path?.let { url ->
            viewBinding.webView.run{
                loadUrl(url)
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> findNavController().popBackStack()
        }
        return super.onOptionsItemSelected(item)
    }

}