package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

data class GroupsDto(@SerializedName("results") val groups: List<GroupModel>)