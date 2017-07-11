package com.github.foolish314159.nhkeasynews

/**
 * Basic interface for translation, classes implementing this can use any strategy to translate
 */
interface Dictionary {

    /** Translate the word however you like **/
    fun translate(word: String)

}