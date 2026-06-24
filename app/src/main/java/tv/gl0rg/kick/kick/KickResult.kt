package tv.gl0rg.kick.kick

sealed interface KickResult<out T> {
    data class Success<T>(val value: T) : KickResult<T>
    data class Failure(val reason: String, val cause: Throwable? = null) : KickResult<Nothing>
}
