package com.intergroupapplication.presentation.feature.commentsdetails.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.intergroupapplication.R

class CommentDividerItemDecorator(private val context: Context) : RecyclerView.ItemDecoration() {

    companion object {
        private const val ITEM_SIDE_OFFSET = 0.20
    }

    private val divider: Drawable? = ContextCompat.getDrawable(context, R.drawable.comment_divider)

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft + (parent.width * ITEM_SIDE_OFFSET).toInt()
        val right = parent.width - parent.paddingRight

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            val viewType = parent.adapter?.getItemViewType(position)
            if (viewType != CommentDetailsAdapter.loadingViewType && viewType != CommentDetailsAdapter.errorViewType) {
                val params = child.layoutParams as RecyclerView.LayoutParams

                val top = child.bottom + params.bottomMargin
                divider?.let {
                    val bottom = top + it.intrinsicHeight
                    it.setBounds(left, top, right, bottom)
                    it.draw(c)
                }
            }
        }

    }
}