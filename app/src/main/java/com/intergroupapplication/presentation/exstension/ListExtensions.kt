package com.intergroupapplication.presentation.exstension

import com.intergroupapplication.data.model.ChooseClass

fun MutableList<out ChooseClass>.cancelChoose(){
    forEach {
        it.isChoose = false
    }
}

fun MutableList<out ChooseClass>.countChoose():Int{
    return this.filter { it.isChoose }.size
}