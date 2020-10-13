package com.github.marlonlom.utilities.timeago

inline fun <T> doOrIfNull(value: T?, ifNotNullFunction: (value: T) -> T,
                          ifNullFunction: () -> T): T {
    return if (value != null) {
        ifNotNullFunction(value)
    } else {
        ifNullFunction()
    }
}

inline fun <T> doOrIfConditional(conditional: () -> Boolean, ifConditionalFunction: () -> T,
                                 ifNotConditionalFunction: () -> T): T {
    return if (conditional.invoke()) {
        ifConditionalFunction()
    } else {
        ifNotConditionalFunction()
    }
}

inline infix fun <T> T?.onNull(action: () -> Unit): T? {
    if (this == null) {
        action()
    }
    return this
}

inline infix fun <T> T?.onNotNull(action: (value: T) -> Unit): T? {
    val t = this
    if (t != null) {
        action(t)
    }

    return this
}