package com.intergroupapplication.data.realmmodel

import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

/**
 * Created by abakarmagomedov on 21/09/2018 at project InterGroupApplication.
 */
@RealmClass
open class GroupInPostRealmModel : RealmModel {
    @PrimaryKey
    var id: String = ""
    var name: String = ""
}
