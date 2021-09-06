package com.intergroupapplication.data.network

import android.annotation.SuppressLint
import org.jsoup.Jsoup
import java.security.KeyManagementException
import javax.net.ssl.*
import javax.security.cert.CertificateException
import javax.security.cert.X509Certificate

class AgreementsApi {


    companion object {
        private const val URL_PRIVACY_POLICY = "https://igsn.net/agreement/1.html"
        private const val URL_TERMS_OF_USE = "https://igsn.net/agreement/3.html"
        private const val URL_RIGHTHOLDERS = "https://igsn.net/agreement/2.html"
    }

    fun privacyPolicy(): String {
        val allHostsValid = HostnameVerifier { _, _ -> true }
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid)
        val doc = Jsoup.connect(URL_PRIVACY_POLICY)
            .get()
        return doc.select("body").html()
    }

    fun termsOfUse(): String {
        val doc = Jsoup.connect(URL_TERMS_OF_USE)
            .get()
        return doc.select("body").html()
    }

    fun rightHolders(): String {
        val doc = Jsoup.connect(URL_RIGHTHOLDERS)
            .get()
        return doc.select("body").html()
    }

}