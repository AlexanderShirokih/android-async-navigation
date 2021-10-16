package ru.alexandershirokikh.asyncnavigation.ui.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

abstract class BaseFragment : Fragment {

    constructor() : super()

    constructor(layout: Int) : super(layout)

    private val requestTag = "$REQUEST_KEY_PREFIX${javaClass.name}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setAsyncResultListener()
    }

    private fun setAsyncResultListener() {
        val registry = AsyncFragmentCallbackRegistry

        val observer = object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_START) {

                    val callbacks = registry
                        .findAsyncResults(sourceTag = requestTag)

                    // Complete callbacks delivered to this fragment
                    callbacks.forEach {
                        it.completeWithCurrentResult()
                        registry.removeAsyncResult(
                            sourceTag = requestTag,
                            destinationTag = it.destinationTag,
                        )
                    }
                }

                if (event == Lifecycle.Event.ON_DESTROY) {
                    lifecycle.removeObserver(this)

                    // Remove hanging callbacks
                    registry
                        .findAsyncResults(sourceTag = requestTag)
                        .forEach { it.completeWithCurrentResult() }

                    registry.removeAsyncResult(sourceTag = requestTag)
                }
            }
        }

        lifecycle.addObserver(observer)
    }

    /**
     * Starts destination fragment and suspends until it completes. Destination fragment should send
     * result by calling [setAsyncResult]. Subsequent calls will replace current result.
     * When fragment destroys, the latest result will be available when
     * the source fragment moves to started state. If destination fragment doesn't made calls to
     * [setAsyncResult], then result will be `null`.
     * Note that ifo process will be restarted, result will not be received!
     */
    suspend fun <T : Any> startFragmentAsync(
        destination: BaseFragment,
        transactionBuilder: FragmentTransaction.(Fragment) -> Unit
    ): T? {
        val registry = AsyncFragmentCallbackRegistry

        // Add source fragment key to fragment arguments
        destination.arguments = (destination.arguments ?: Bundle()).apply {
            putString(KEY_SOURCE_FRAGMENT_TAG, requestTag)
        }

        val completable = registry.registerForAsyncResult<T>(
            sourceTag = requestTag,
            destinationTag = destination.requestTag,
        )

        parentFragmentManager.beginTransaction().apply {
            transactionBuilder(this, destination)
            commitAllowingStateLoss()
        }

        return completable.deferred.await()
    }

    /**
     * Sets the current result. To make it works fragment should be started by [startFragmentAsync].
     * Note that if process will be restarted, result will not be received!
     * @param result fragment result to be set.
     * @return `true` is result was successfully assigned, `false` is result cannot be delivered due
     * to argument type mismatch or the process death.
     * @throws AsyncNavigationException if fragment was not started by [startFragmentAsync] call
     */
    fun <T> setAsyncResult(
        result: T,
    ): Boolean {
        val registry = AsyncFragmentCallbackRegistry

        val sourceTag = arguments?.getString(KEY_SOURCE_FRAGMENT_TAG)
            ?: throw AsyncNavigationException("Source fragment argument is missing. It means that this fragment was not started via startFragmentAsync()")

        return registry.setResult(
            sourceTag = sourceTag,
            destinationTag = requestTag,
            result = result
        )
    }

    companion object {
        private const val KEY_SOURCE_FRAGMENT_TAG = "async:key_source_fragment_tag"
        private const val REQUEST_KEY_PREFIX = "requestKey:"
    }
}
