package dev.kdrag0n.app.debug

import android.util.Log
import dev.kdrag0n.app.log.LogBackend
import io.sentry.Breadcrumb
import io.sentry.IHub
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.protocol.Message

// Based on Sentry's official Timber integration
class SentryLogBackend(
    private val hub: IHub,
    private val minEventLevel: SentryLevel,
    private val minBreadcrumbLevel: SentryLevel
) : LogBackend {
    override fun print(tag: String, priority: Int, message: String, exception: Throwable?) {
        if (message.isEmpty() && exception == null) {
            return
        }

        val level = getSentryLevel(priority)
        val sentryMessage = Message().apply {
            this.message = message
        }

        captureEvent(level, tag, sentryMessage, exception)
        addBreadcrumb(level, sentryMessage, exception)
    }

    /**
     * do not log if it's lower than min. required level.
     */
    private fun isLoggable(
        level: SentryLevel,
        minLevel: SentryLevel
    ): Boolean = level.ordinal >= minLevel.ordinal

    /**
     * Captures an event with the given attributes
     */
    private fun captureEvent(
        sentryLevel: SentryLevel,
        tag: String?,
        msg: Message,
        throwable: Throwable?
    ) {
        if (isLoggable(sentryLevel, minEventLevel)) {
            val sentryEvent = SentryEvent().apply {
                level = sentryLevel
                throwable?.let { setThrowable(it) }
                tag?.let {
                    setTag("UlogTag", it)
                }
                message = msg
                logger = "Ulog"
            }

            hub.captureEvent(sentryEvent)
        }
    }

    /**
     * Adds a breadcrumb
     */
    private fun addBreadcrumb(
        sentryLevel: SentryLevel,
        msg: Message,
        throwable: Throwable?
    ) {
        // checks the breadcrumb level
        if (isLoggable(sentryLevel, minBreadcrumbLevel)) {
            val throwableMsg = throwable?.message
            val breadCrumb = when {
                msg.message != null -> Breadcrumb().apply {
                    level = sentryLevel
                    category = "Ulog"
                    message = msg.formatted ?: msg.message
                }
                throwableMsg != null -> Breadcrumb.error(throwableMsg).apply {
                    category = "exception"
                }
                else -> null
            }

            breadCrumb?.let { hub.addBreadcrumb(it) }
        }
    }
}

private fun getSentryLevel(priority: Int): SentryLevel {
    return when (priority) {
        Log.ASSERT -> SentryLevel.FATAL
        Log.ERROR -> SentryLevel.ERROR
        Log.WARN -> SentryLevel.WARNING
        Log.INFO -> SentryLevel.INFO
        Log.DEBUG -> SentryLevel.DEBUG
        Log.VERBOSE -> SentryLevel.DEBUG
        else -> SentryLevel.DEBUG
    }
}
