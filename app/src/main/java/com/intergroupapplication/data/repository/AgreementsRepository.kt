package com.intergroupapplication.data.repository

import com.intergroupapplication.data.network.AgreementsApi
import com.intergroupapplication.domain.gateway.AgreementsGateway
import io.reactivex.Single
import org.jsoup.Jsoup
import javax.inject.Inject

class AgreementsRepository @Inject constructor(private val api: AgreementsApi): AgreementsGateway {

    override fun getPrivacy(): Single<String> {
        return  Single.create<String> {
            try {
                val html = api.privacyPolicy()
                it.onSuccess(html)
            } catch (exception: Exception) {
                it.tryOnError(exception)
            }

        }
    }

    override fun getTerms(): Single<String> {
        return  Single.create<String> {
            try {
                val html = api.termsOfUse()
                it.onSuccess(html)
            } catch (exception: Exception) {
                it.tryOnError(exception)
            }

        }
    }

    override fun getRightHolders(): Single<String> {
        return  Single.create<String> {
            try {
                val html = api.rightHolders()
                it.onSuccess(html)
            } catch (exception: Exception) {
                it.tryOnError(exception)
            }
        }
    }


}