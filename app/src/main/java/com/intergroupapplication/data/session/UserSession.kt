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

    companion object {
        private const val IS_AD_ENABLED = "is_AD_enabled"
    }

    var user: UserEntity? by SharedPrefDelegate(UserEntity::class.java, gson, sharedPreferences)
    var token: TokenEntity? by SharedPrefDelegate(TokenEntity::class.java, gson, sharedPreferences)
    var deviceInfoEntity: DeviceInfoEntity? by SharedPrefDelegate(DeviceInfoEntity::class.java, gson, sharedPreferences)
    var lastErrorMessage: ErrorMessageEntity? by SharedPrefDelegate(ErrorMessageEntity::class.java, gson, sharedPreferences)
    var firebaseToken: FirebaseTokenEntity? by SharedPrefDelegate(FirebaseTokenEntity::class.java, gson, sharedPreferences)
    var acceptTerms: TermsEntity? by SharedPrefDelegate(TermsEntity::class.java, gson, sharedPreferences)
    var email: EmailEntity? by SharedPrefDelegate(EmailEntity::class.java, gson, sharedPreferences)
    var countAd: AdEntity? by SharedPrefDelegate(AdEntity::class.java, gson, sharedPreferences)

    var isAdEnabled: Boolean
        get() = sharedPreferences.getBoolean(IS_AD_ENABLED, true)
        set(isEnabled) {
            sharedPreferences.edit().putBoolean(IS_AD_ENABLED, isEnabled).apply()
            if (!isEnabled) {
                countAd = AdEntity(0,999,999,
                        0, 999, 999,
                        0, 999, 999)
            }
        }


    fun clearAllData() {
        user = null
        token = null
        lastErrorMessage = null
        sharedPreferences.edit().clear().apply()
    }

    fun logout() {
        user = null
        token = null
        sharedPreferences.edit()
                .remove(UserEntity::class.java.name)
                .remove(TokenEntity::class.java.name)
                .apply()
    }

    fun isLoggedIn(): Boolean = token != null

    fun isAcceptTerms(): Boolean = acceptTerms?.isAcceptTerms ?: false

}
