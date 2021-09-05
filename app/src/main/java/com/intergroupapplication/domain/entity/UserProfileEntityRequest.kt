package com.intergroupapplication.domain.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserProfileEntityRequest(val birthday: String?,
                                    val gender: String?,
                                    val firstName: String,
                                    val surName: String,
                                    val avatar: String?
                        ): Parcelable