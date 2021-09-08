package com.intergroupapplication.domain.crypto

import cc.duduhuo.util.android.digest.Digest
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 08/08/2018 at project InterGroupApplication.
 */
class EncryptionDelegate @Inject constructor() : Encryptor {

    override fun encryptSha224Hex(value: String): String =
        Digest.sha224Hex(value)
}
