package com.intergroupapplication.presentation.manager

import android.annotation.SuppressLint
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */
class PhoneCharacteristicManager @Inject constructor() {


    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission", "HardwareIds")
    fun getImei(): String = "sanya_hui_sosi"

    fun getSerialNumber(): String = "jopa"
}
