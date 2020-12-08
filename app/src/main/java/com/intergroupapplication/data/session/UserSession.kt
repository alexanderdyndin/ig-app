package com.intergroupapplication.data.session

import android.content.SharedPreferences
import com.google.gson.Gson
import com.intergroupapplication.di.scope.PerApplication
import com.intergroupapplication.domain.entity.*
import com.intergroupapplication.presentation.delegate.SharedPrefDelegate
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 06/08/2018 at project InterGroupApplication.
 */
@PerApplication
class UserSession @Inject constructor(private val sharedPreferences: SharedPreferences, gson: Gson) {

    var user: UserEntity? by SharedPrefDelegate(UserEntity::class.java, gson, sharedPreferences)
    var token: TokenEntity? by SharedPrefDelegate(TokenEntity::class.java, gson, sharedPreferences)
    var deviceInfoEntity: DeviceInfoEntity? by SharedPrefDelegate(DeviceInfoEntity::class.java, gson, sharedPreferences)
    var lastErrorMessage: ErrorMessageEntity? by SharedPrefDelegate(ErrorMessageEntity::class.java, gson, sharedPreferences)
    var firebaseToken: FirebaseTokenEntity? by SharedPrefDelegate(FirebaseTokenEntity::class.java, gson, sharedPreferences)
    var acceptTerms: TermsEntity? by SharedPrefDelegate(TermsEntity::class.java, gson, sharedPreferences)
    var email: EmailEntity? by SharedPrefDelegate(EmailEntity::class.java, gson, sharedPreferences)
    var countAd: AdEntity? = null
    //var countAd: AdEntity? by SharedPrefDelegate(AdEntity::class.java, gson, sharedPreferences)


    fun clearAllData() {
        user = null
        token = null
        lastErrorMessage = null
        sharedPreferences.edit().clear().apply()
    }

    fun logout() {
        sharedPreferences.edit()
                .remove(UserEntity::class.java.name)
                .remove(TokenEntity::class.java.name)
                .apply()
    }

    fun isLoggedIn(): Boolean = token != null

    fun isAcceptTerms(): Boolean = acceptTerms?.isAcceptTerms ?: false

}
