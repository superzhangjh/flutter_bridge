package com.galaxy.flutter.bridge.processor.utils

private val camelRegex = Regex("([a-z])([A-Z])")
private val acronymRegex = Regex("([A-Z]+)([A-Z][a-z])")

internal fun String.toSnakeCase(): String {
    return acronymRegex.replace(camelRegex.replace(this, "$1_$2")) {
        "${it.groupValues[1]}_${it.groupValues[2]}"
    }.lowercase()
}