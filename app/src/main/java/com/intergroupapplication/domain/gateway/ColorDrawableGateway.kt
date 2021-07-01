package com.intergroupapplication.domain.gateway

import android.graphics.drawable.Drawable

interface ColorDrawableGateway {

    fun getDrawableByColor(color:Int):Drawable?
}