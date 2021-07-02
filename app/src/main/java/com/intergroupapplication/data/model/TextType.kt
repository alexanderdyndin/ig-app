package com.intergroupapplication.data.model

enum class TextType(
    var color:Int = -1
) {
    BOLD,
    ITALIC,
    SUBSCRIPT,
    SUPERSCRIPT,
    STRIKETHROUGH,
    UNDERLINE,
    H1,
    H2,
    H3,
    H4,
    H5,
    H6,
    ORDEREDLIST,
    UNORDEREDLIST,
    JUSTIFYCENTER,
    JUSTIFYLEFT,
    JUSTIFYRIGHT,
    JUSTIFYFULL,
    FONT_COLOR(0)
}