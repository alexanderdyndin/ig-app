package com.intergroupapplication.presentation.exstension

import com.intergroupapplication.data.model.ChooseMedia

fun MutableSet<ChooseMedia>.addChooseMedia(chooseMedia: ChooseMedia) {
    this.add(chooseMedia)
}

fun MutableSet<ChooseMedia>.removeChooseMedia(url: String) {
    this.forEach {
        if (it.url == url) {
            this.remove(it)
            return
        }
    }
}

fun MutableSet<ChooseMedia>.containsMedia(url: String): Boolean {
    this.forEach {
        if (it.url == url) {
            return true
        }
    }
    return false
}

fun MutableList<ChooseMedia>.addMediaIfNotContains(newChooseMedia: ChooseMedia) {
    this.forEach { chooseMedia ->
        if (chooseMedia.name == newChooseMedia.name) return@addMediaIfNotContains
    }
    this.add(newChooseMedia)
}

fun MutableList<ChooseMedia>.removeMedia(url: String?): Boolean {
    this.forEach {
        if (it.url == url) {
            remove(it)
            return true
        }
    }
    return false
}
