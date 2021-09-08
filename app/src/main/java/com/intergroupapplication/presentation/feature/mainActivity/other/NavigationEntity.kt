package com.intergroupapplication.presentation.feature.mainActivity.other

data class NavigationEntity(
    val name: Int,
    val icon: Int,
    val action: () -> Unit = {},
    var checked: Boolean? = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NavigationEntity

        if (name != other.name) return false
        if (icon != other.icon) return false
        if (action != other.action) return false
        if (checked != other.checked) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name
        result = 31 * result + icon
        result = 31 * result + action.hashCode()
        result = 31 * result + checked.hashCode()
        return result
    }
}
