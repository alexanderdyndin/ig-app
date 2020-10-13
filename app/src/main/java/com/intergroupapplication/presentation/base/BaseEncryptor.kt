package com.intergroupapplication.presentation.base

import com.intergroupapplication.domain.crypto.EncryptionDelegate
import com.intergroupapplication.domain.crypto.Encryptor

/**
 * Created by abakarmagomedov on 08/08/2018 at project InterGroupApplication.
 */
class BaseEncryptor(encryptionManager: EncryptionDelegate) : Encryptor by encryptionManager

