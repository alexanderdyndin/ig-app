package com.intergroupapplication.domain.gateway

import com.intergroupapplication.data.network.dto.CodeDto
import com.intergroupapplication.data.network.dto.EmailDto
import com.intergroupapplication.data.network.dto.NewPasswordDto
import com.intergroupapplication.domain.entity.TokenCodeEntity
import io.reactivex.Completable
import io.reactivex.Single

interface ResetPasswordGateway {

    fun resetPassword(emailDto: EmailDto): Completable

    fun resetPasswordCode(codeDto: CodeDto): Single<TokenCodeEntity>

    fun newPassword(newPasswordDto: NewPasswordDto): Completable
}
