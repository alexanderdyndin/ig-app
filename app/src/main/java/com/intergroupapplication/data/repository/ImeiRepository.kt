package com.intergroupapplication.data.repository

import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.crypto.Encryptor
import com.intergroupapplication.domain.entity.DeviceInfoEntity
import com.intergroupapplication.domain.gateway.ImeiGateway
import com.intergroupapplication.presentation.manager.PhoneCharacteristicManager
import io.reactivex.Completable
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */
class ImeiRepository @Inject constructor(private val infoManager: PhoneCharacteristicManager,
                                         private val session: UserSession,
                                         private val encryptor: Encryptor) : ImeiGateway {

    override fun extractDeviceInfo(): Completable = Completable.fromAction {
        val imei = infoManager.getImei()
        val serialNumber = infoManager.getSerialNumber()
        session.deviceInfoEntity = DeviceInfoEntity(encryptor.encryptSha224Hex(imei),
                encryptor.encryptSha224Hex(serialNumber))
    }
}
