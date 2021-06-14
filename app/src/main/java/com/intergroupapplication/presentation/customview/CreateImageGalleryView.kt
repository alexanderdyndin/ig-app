package com.intergroupapplication.presentation.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.FileEntity

class CreateImageGalleryView  @JvmOverloads constructor(context: Context,
                    private val attrs: AttributeSet? = null, private val defStyleAttr: Int = 0):
    LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        private var pxWidth = 1080
    }

    val listContainer = mutableListOf<LinearLayout>()

    init {
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        orientation = VERTICAL
        val displayMetrics = resources.displayMetrics
        pxWidth = displayMetrics.widthPixels - 30
    }

    val downloadImageView = mutableListOf<View>()
    private var isExpanded: Boolean = false

    fun addImage(view: View){
        downloadImageView.add(view)
        createContainer(downloadImageView, isExpanded)
    }

    private fun createContainer(list: List<View>, isExpanded: Boolean) {
        listContainer.run {
            forEach { container ->
                container.removeAllViews()
            }
            clear()
        }
        this.removeAllViews()
        if (isExpanded && list.size > 3) {
            for (i in 0 until list.size / 3) {
                setupContainer(list.subList(i * 3, i * 3 + 3))
            }
            when (list.size % 3){
                2 -> setupContainer(list.subList(list.size - 2, list.size))
                1 -> setupContainer(list.subList(list.size - 1, list.size))
            }
            val hider = LayoutInflater.from(context).inflate(R.layout.layout_hide, this,
                false)
            val btnHide = hider.findViewById<FrameLayout>(R.id.btnHide)
            btnHide.setOnClickListener {
                this.isExpanded = false
                createContainer(list, this.isExpanded)
            }
            this.addView(hider)
        } else if (!isExpanded && list.size > 3) {
            setupContainer(list.subList(0, 3))
            val expander = LayoutInflater.from(context).inflate(R.layout.layout_expand, this,
                false)
            val btnExpand = expander.findViewById<FrameLayout>(R.id.btnExpand)
            btnExpand.setOnClickListener {
                this.isExpanded = true
                createContainer(list, this.isExpanded)
            }
            this.addView(expander)
        } else if (list.isNotEmpty()) {
            setupContainer(list)
        }
    }

    private fun setupContainer(urls: List<View>) {
        val container = LinearLayout(context, attrs, defStyleAttr)
        container.orientation = HORIZONTAL
        container.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        urls.forEach { view ->
            view.layoutParams = LayoutParams(pxWidth / urls.size, LayoutParams.WRAP_CONTENT)
            container.addView(view)
        }
        listContainer.add(container)
        this.addView(container)
    }

    fun removeImageView(view: View?){
        this.removeView(view)
        downloadImageView.remove(view)
        createContainer(downloadImageView, isExpanded)
    }

}