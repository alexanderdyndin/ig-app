package com.intergroupapplication.data.repository

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.intergroupapplication.R
import com.intergroupapplication.domain.gateway.ColorDrawableGateway
import javax.inject.Inject

class ColorDrawableRepository @Inject constructor(private val context: Context) :
    ColorDrawableGateway {

    override fun getDrawableByColor(color: Int): Drawable? {
        return when (color) {
            context.getColor(R.color.blue_1) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_blue1)
            context.getColor(R.color.blue_2) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_blue2)
            context.getColor(R.color.blue_3) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_blue3)
            context.getColor(R.color.blue_4) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_blue4)
            context.getColor(R.color.blue_5) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_blue5)
            context.getColor(R.color.blue_6) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_blue6)
            context.getColor(R.color.blue_7) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_blue7)
            context.getColor(R.color.blue_8) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_blue8)
            context.getColor(R.color.dark_blue1) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_dark_blue1)
            context.getColor(R.color.dark_blue2) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_dark_blue2)
            context.getColor(R.color.dark_blue3) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_dark_blue3)
            context.getColor(R.color.dark_blue4) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_dark_blue4)
            context.getColor(R.color.dark_blue5) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_dark_blue5)
            context.getColor(R.color.dark_blue6) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_dark_blue6)
            context.getColor(R.color.dark_blue7) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_dark_blue7)
            context.getColor(R.color.dark_blue8) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_dark_blue8)
            context.getColor(R.color.purple_1) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_purple1)
            context.getColor(R.color.purple_2) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_purple2)
            context.getColor(R.color.purple_3) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_purple3)
            context.getColor(R.color.purple_4) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_purple4)
            context.getColor(R.color.purple_5) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_purple5)
            context.getColor(R.color.purple_6) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_purple6)
            context.getColor(R.color.purple_7) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_purple7)
            context.getColor(R.color.purple_8) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_purple8)
            context.getColor(R.color.red_1) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_red1)
            context.getColor(R.color.red_2) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_red2)
            context.getColor(R.color.red_3) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_red3)
            context.getColor(R.color.red_4) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_red4)
            context.getColor(R.color.red_5) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_red5)
            context.getColor(R.color.red_6) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_red6)
            context.getColor(R.color.red_7) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_red7)
            context.getColor(R.color.red_8) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_red8)
            context.getColor(R.color.orange_1) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_orange1)
            context.getColor(R.color.orange_2) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_orange2)
            context.getColor(R.color.orange_3) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_orange3)
            context.getColor(R.color.orange_4) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_orange4)
            context.getColor(R.color.orange_5) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_orange5)
            context.getColor(R.color.orange_6) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_orange6)
            context.getColor(R.color.orange_7) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_orange7)
            context.getColor(R.color.orange_8) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_orange8)
            context.getColor(R.color.green_1) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_green1)
            context.getColor(R.color.green_2) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_green2)
            context.getColor(R.color.green_3) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_green3)
            context.getColor(R.color.green_4) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_green4)
            context.getColor(R.color.green_5) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_green5)
            context.getColor(R.color.green_6) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_green6)
            context.getColor(R.color.green_7) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_green7)
            context.getColor(R.color.green_8) ->
                ContextCompat.getDrawable(context, R.drawable.ic_edit_color_green8)
            else -> ContextCompat.getDrawable(context, R.drawable.ic_edit_color)
        }
    }
}
