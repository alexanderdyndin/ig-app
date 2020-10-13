package com.intergroupapplication.domain.entity

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */
data class RegistrationEntity(
        val email: String,
        val password: String,
        val emailConfirm: String,
        val passwordConfirm: String):Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(email)
        parcel.writeString(password)
        parcel.writeString(emailConfirm)
        parcel.writeString(passwordConfirm)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RegistrationEntity> {
        override fun createFromParcel(parcel: Parcel): RegistrationEntity {
            return RegistrationEntity(parcel)
        }

        override fun newArray(size: Int): Array<RegistrationEntity?> {
            return arrayOfNulls(size)
        }
    }
}
