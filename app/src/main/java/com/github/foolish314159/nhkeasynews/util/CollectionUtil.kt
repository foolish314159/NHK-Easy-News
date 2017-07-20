package com.github.foolish314159.nhkeasynews.util

fun <T> List<T>.contains(predicate: (T) -> Boolean): Boolean {
    this.forEach {
        if (predicate(it))
            return true
    }
    return false
}

/**
 * Add element and sort afterwards
 */
fun <T : Comparable<T>> ArrayList<T>.addSorted(element: T) {
    this.add(element)
    this.sort()
}