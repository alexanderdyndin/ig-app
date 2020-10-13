package com.intergroupapplication.domain.crypto

/**
 * Created by abakarmagomedov on 08/08/2018 at project InterGroupApplication.
 */
interface Encryptor {
    fun encryptSha224Hex(value: String): String
}
