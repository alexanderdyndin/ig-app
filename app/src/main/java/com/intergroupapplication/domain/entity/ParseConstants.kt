package com.intergroupapplication.domain.entity

object ParseConstants {
    const val PARSE_SYMBOL = "~~"
    const val START_MEDIA_PREFIX = "~~{"
    const val END_MEDIA_PREFIX = "}~~"
    const val START_CONTAINER = "<div>"
    const val END_CONTAINER = "</div>"
    const val MEDIA_PREFIX = "(MEDIA)"
    const val START_AUDIO = "<audio src=\""
    const val START_VIDEO = "<video src=\""
    const val START_IMAGE = "<img src=\""
    const val END_AUDIO = "\" controls=\"\"></audio>"
    const val END_VIDEO = "controls=\"\"></video>"
    const val END_IMAGE = "\" alt=\"alt\">"
}