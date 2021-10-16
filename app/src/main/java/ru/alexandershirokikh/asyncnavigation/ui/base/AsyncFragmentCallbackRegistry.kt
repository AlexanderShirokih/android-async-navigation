package ru.alexandershirokikh.asyncnavigation.ui.base

import kotlinx.coroutines.CompletableDeferred
import java.util.concurrent.ConcurrentHashMap

/**
 * Global registry that keeps asynchronous results for fragment communication
 */
object AsyncFragmentCallbackRegistry {

    private data class CompositeKey(
        val sourceTag: String,
        val destinationTag: String,
    )

    /**
     * Class, that keeps destination tag, and current result
     */
    class AsyncResultWrapper<T>(val destinationTag: String) {

        var currentResult: T? = null
        val deferred = CompletableDeferred<T?>()

        /**
         * Commits deferred with the current result
         */
        fun completeWithCurrentResult() {
            deferred.complete(currentResult)
        }
    }

    private val callbacks = ConcurrentHashMap<CompositeKey, AsyncResultWrapper<*>>()

    /**
     * Creates async connection between [sourceTag] and [destinationTag]
     * @param sourceTag connection initiator that will receive result later
     * @param destinationTag destination that will pass result to the source
     * @return new [AsyncResultWrapper] that keeps current result
     */
    fun <T : Any> registerForAsyncResult(
        sourceTag: String,
        destinationTag: String
    ): AsyncResultWrapper<T> {

        val key = CompositeKey(
            sourceTag = sourceTag,
            destinationTag = destinationTag
        )

        return AsyncResultWrapper<T>(destinationTag).apply {
            callbacks[key] = this

            deferred.invokeOnCompletion {
                callbacks.remove(key)
            }
        }
    }

    /**
     * Returns collection of asynchronous result created by [sourceTag]
     * @return collection of results or an empty collection, if there is no results
     */
    fun findAsyncResults(sourceTag: String): Collection<AsyncResultWrapper<*>> {
        return callbacks.filterKeys { it.sourceTag == sourceTag }.values
    }

    /**
     * Removes asynchronous result from the [sourceTag] to the [destinationTag].
     * If [destinationTag] is `null` then all [sourceTag] results will be removed.
     */
    fun removeAsyncResult(sourceTag: String, destinationTag: String? = null) {
        if (destinationTag == null) {
            callbacks
                .filterKeys { it.sourceTag == sourceTag }
                .keys
                .forEach {
                    callbacks.remove(it)
                }
        } else {
            callbacks.remove(
                CompositeKey(
                    sourceTag = sourceTag,
                    destinationTag = destinationTag,
                )
            )
        }
    }

    /**
     * Updates the latest result addressed from [destinationTag] to [sourceTag]
     * @return `true` if result was updated successfully, `false` if connection with the current
     * tags was not found or result type doesn't matches original contract type
     */
    fun <T> setResult(sourceTag: String, destinationTag: String, result: T): Boolean {
        val key = CompositeKey(
            sourceTag = sourceTag,
            destinationTag = destinationTag
        )

        val resultWrapper = callbacks[key] as? AsyncResultWrapper<T>

        return if (resultWrapper == null) {
            false
        } else {
            resultWrapper.currentResult = result
            true
        }
    }
}
