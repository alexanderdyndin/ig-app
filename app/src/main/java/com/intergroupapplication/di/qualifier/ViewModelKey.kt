package com.intergroupapplication.di.qualifier

import androidx.lifecycle.ViewModel
import dagger.MapKey
import kotlin.reflect.KClass

@kotlin.annotation.MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class ViewModelKey(val value: KClass<out ViewModel>)