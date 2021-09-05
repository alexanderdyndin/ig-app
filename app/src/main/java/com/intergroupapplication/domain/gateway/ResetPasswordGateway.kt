package com.intergroupapplication.domain.gateway

import com.intergroupapplication.data.model.CodeModel
import com.intergroupapplication.data.model.EmailModel
import com.intergroupapplication.data.model.NewPasswordModel
import com.intergroupapplication.domain.entity.TokenCodeEntity
import io.reactivex.Completable
import io.reactivex.Single

interface ResetPasswordGateway {

    fun resetPassword(emailModel: EmailModel): Completable

    fun resetPasswordCode(codeModel: CodeModel): Single<TokenCodeEntity>

    fun newPassword(newPasswordModel: NewPasswordModel): Completable
}
