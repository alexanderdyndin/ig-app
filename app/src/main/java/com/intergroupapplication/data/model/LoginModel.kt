package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */
data class LoginModel(//@SerializedName("login")
                      val email: String,
                      val password: String)
