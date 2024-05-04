package de.amplimind.codingchallenge.aspects

import de.amplimind.codingchallenge.annotations.WithRateLimit
import de.amplimind.codingchallenge.exceptions.RateLimitException
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

/**
 * Aspect for handling rate limiting.
 */
@Aspect
@Component
class RateLimitAspect {
    // A concurrent map to hold request counts per IP address
    private val requestCounts = ConcurrentHashMap<String, MutableList<Long>>()

    // The maximum number of requests allowed within the rate duration
    @Value("\${APP_RATE_LIMIT:#{5}}")
    private val rateLimit = 0

    // The duration (in milliseconds) within which the rate limit applies
    @Value("\${APP_RATE_DURATIONINMS:#{60000}}")
    private val rateDuration: Long = 0

    /**
     * This method is executed before each call of a method annotated with [WithRateLimit] which should be an HTTP endpoint.
     * It counts calls per remote address. Calls older than [.rateDuration] milliseconds will be forgotten. If there have
     * been more than [.rateLimit] calls within [.rateDuration] milliseconds from a remote address, a [RateLimitException]
     * will be thrown.
     * @throws RateLimitException if rate limit for a given remote address has been exceeded
     */
    @Before("@annotation(de.amplimind.codingchallenge.annotations.WithRateLimit)")
    fun rateLimit() {
        val requestAttributes = RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes
        val key = requestAttributes.request.remoteAddr
        val currentTime = System.currentTimeMillis()
        requestCounts.putIfAbsent(key, ArrayList())
        requestCounts[key]!!.add(currentTime)
        cleanUpRequestCounts(currentTime)
        if (requestCounts[key]!!.size > rateLimit) {
            throw RateLimitException(
                String.format(
                    ERROR_MESSAGE,
                    requestAttributes.request.requestURI,
                    key,
                ),
            )
        }
    }

    /**
     * This method resets the request count for a given IP address.
     */
    fun resetCount() {
        val requestAttributes = RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes
        val key = requestAttributes.request.remoteAddr
        requestCounts.computeIfPresent(key) { _, list ->
            list.clear()
            list
        }
    }

    /**
     * This method cleans up the request counts map by removing entries older than the rate duration.
     * @param currentTime The current time in milliseconds.
     */
    private fun cleanUpRequestCounts(currentTime: Long) {
        requestCounts.values.forEach(
            Consumer { l: MutableList<Long> ->
                l.removeIf { t: Long -> timeIsTooOld(currentTime, t) }
            },
        )
    }

    /**
     * This method checks if a given time is older than the rate duration.
     * @param currentTime The current time in milliseconds.
     * @param timeToCheck The time to check.
     * @return True if the time to check is older than the rate duration, false otherwise.
     */
    private fun timeIsTooOld(
        currentTime: Long,
        timeToCheck: Long,
    ): Boolean {
        return currentTime - timeToCheck > rateDuration
    }

    companion object {
        // Error message template for rate limit exceptions
        const val ERROR_MESSAGE: String = "To many request at endpoint %s from IP %s!"
    }
}
