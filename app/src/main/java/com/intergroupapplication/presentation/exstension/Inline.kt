package com.intergroupapplication.presentation.exstension

inline fun <T> doOrIfNull(value: T?, ifNotNullFunction: (value: T) -> Unit,
                          ifNullFunction: () -> Unit) {
    if (value != null) {
        ifNotNullFunction(value)
    } else {
        ifNullFunction()
    }
}

inline infix fun <T> T?.ifNull(action: () -> Unit): T? {
    if (this == null) {
        action()
    }
    return this
}

inline infix fun <T> T?.ifNotNull(action: (value: T) -> Unit): T? {
    val t = this
    if (t != null) {
        action(t)
    }

    return this
}

fun <T> T?.on(predicate: (T) -> Boolean): ExpressionValue<T> {
    return ExpressionValue(this) { it != null && predicate(it) }
}

fun <T> T?.onNull(predicate: (T?) -> Boolean): ExpressionValue<T> {
    return ExpressionValue(this, predicate)
}

data class ExpressionValue<T>(
        private val value: T?,
        private val predicate: (T) -> Boolean) {

    infix fun then(action: (T) -> Unit) {
        if (value != null && predicate(value)) {
            action(value)
        }
    }
}
