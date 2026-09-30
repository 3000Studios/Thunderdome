package com.example.game.analytics

import android.content.Context
import android.util.Log

object CrashReporter {
    private const val TAG = "CrashReporter"

    fun init(context: Context) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e(TAG, "FATAL CRASH on thread ${thread.name}: ${throwable.message}", throwable)
                // In production, forward sanitized crash log to reporting backend
            } catch (_: Exception) {
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    fun recordException(throwable: Throwable, tag: String = "NON_FATAL") {
        Log.e(TAG, "[$tag] Handled Exception: ${throwable.message}", throwable)
    }
}
