package com.intergroupapplication.presentation.exstension

inline fun <T> doOrIfNull(
    value: T?, ifNotNullFunction: (value: T) -> Unit,
    ifNullFunction: () -> Unit
) {
    if (value != null) {
        ifNotNullFunction(value)
    } else {
        ifNullFunction()
    }
}

fun <T> T?.on(predicate: (T) -> Boolean): ExpressionValue<T> {
    return ExpressionValue(this) { it != null && predicate(it) }
}

data class ExpressionValue<T>(
    private val value: T?,
    private val predicate: (T) -> Boolean
) {

    infix fun then(action: (T) -> Unit) {
        if (value != null && predicate(value)) {
            action(value)
        }
    }
}
