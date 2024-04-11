package de.amplimind.codingchallenge.extensions

/**
 * Collection of Extensions for [Enum]
 */
object EnumExtensions {
    /**
     * Checks if the enum is in the list of enums
     * @param toMatch the enums to match
     * @return true if the enum is in the list of enums
     */
    inline fun <reified T : Enum<T>> T.matchesAny(vararg toMatch: T): Boolean {
        toMatch.forEach {
            if (this == it) {
                return true
            }
        }
        return false
    }
}
