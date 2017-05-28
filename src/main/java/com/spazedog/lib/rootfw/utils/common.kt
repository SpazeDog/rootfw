/*
 * This file is part of the RootFW Project: https://github.com/spazedog/rootfw
 *
 * Copyright (c) 2017 Daniel Bergløv, License: MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

@file:JvmName("Common")

package com.spazedog.lib.rootfw.utils

import android.util.Log
import com.spazedog.lib.rootfw.BuildConfig

/** * */
internal val BINARIES: List<String?> = listOf(null, "toybox", "busybox", "toolbox")

/** * */
internal var FORCED_DEBUG = false

/**
 * Check if we have a debug build or debug has been forced
 */
val DEBUG: Boolean
    get() = FORCED_DEBUG || BuildConfig.BUILD_TYPE == "debug"

/**
 * Debug class that can be used to send logcat entries with pre-defined tags
 *
 * Note that this class will only send logcat messages if and only if `DEBUG` returns `true`,
 * also note that the methods are inline, meaning that they will be copied to the location where
 * they are called. There is no need to manually do a debug check each time to save resources on
 * calling the methods, the conditions are handled automatically.
 *
 * @param tag
 *      A tag that will automatically be pre-pended to all entries sent by this instance
 */
class Debug(val tag: String) {

    /**
     * Send message to logcat
     *
     * @param msg
     *      Message to send
     */
    inline fun log(msg: String) = logDebug('d', tag, msg)

    /**
     * Send message to logcat
     *
     * @param msg
     *      Message to send
     *
     * @param e
     *      Throwable to parse to logcat
     */
    inline fun log(msg: String, e: Throwable) = logDebug('d', tag, msg, e)

    /**
     * Send message to logcat
     *
     * @param identifier
     *      Additional tag that will be apended to one parsed in the constructor, separated by `:`
     *
     * @param msg
     *      Message to send
     */
    inline fun log(identifier: String, msg: String) = logDebug('d', "$tag:$identifier", msg)

    /**
     * Send message to logcat
     *
     * @param identifier
     *      Additional tag that will be apended to one parsed in the constructor, separated by `:`
     *
     * @param msg
     *      Message to send
     *
     * @param e
     *      Throwable to parse to logcat
     */
    inline fun log(identifier: String, msg: String, e: Throwable) = logDebug('d', "$tag:$identifier", msg, e)

    /**
     * Send message to logcat
     *
     * @param type
     *      Type of log `d`,`i`,`w`, or `e` for `debug`, `info`, `warning` or `error`
     *
     * @param msg
     *      Message to send
     */
    inline fun log(type: Char, msg: String) = logDebug(type, tag, msg)

    /**
     * Send message to logcat
     *
     * @param type
     *      Type of log `d`,`i`,`w`, or `e` for `debug`, `info`, `warning` or `error`
     *
     * @param msg
     *      Message to send
     *
     * @param e
     *      Throwable to parse to logcat
     */
    inline fun log(type: Char, msg: String, e: Throwable) = logDebug(type, tag, msg, e)

    /**
     * Send message to logcat
     *
     * @param type
     *      Type of log `d`,`i`,`w`, or `e` for `debug`, `info`, `warning` or `error`
     *
     * @param identifier
     *      Additional tag that will be apended to one parsed in the constructor, separated by `:`
     *
     * @param msg
     *      Message to send
     */
    inline fun log(type: Char, identifier: String, msg: String) = logDebug(type, "$tag:$identifier", msg)

    /**
     * Send message to logcat
     *
     * @param type
     *      Type of log `d`,`i`,`w`, or `e` for `debug`, `info`, `warning` or `error`
     *
     * @param identifier
     *      Additional tag that will be apended to one parsed in the constructor, separated by `:`
     *
     * @param msg
     *      Message to send
     *
     * @param e
     *      Throwable to parse to logcat
     */
    inline fun log(type: Char, identifier: String, msg: String, e: Throwable) = logDebug(type, "$tag:$identifier", msg, e)
}

/**
 * Send mesage to logcat
 *
 * Note that this function will only send logcat messages if and only if `DEBUG` returns `true`
 * also note that the function is inline, meaning that it will be copied to the location where
 * it is called. There is no need to manually do a debug check each time to save resources on
 * calling the function, the conditions are handled automatically.
 *
 * @param type
 *      Type of log `d`,`i`,`w`, or `e` for `debug`, `info`, `warning` or `error`
 *
 * @param tag
 *      Tag to be used on the entry
 *
 * @param msg
 *      Message to send
 *
 * @param e
 *      Throwable to parse to logcat
 */
inline fun logDebug(type: Char, tag: String, msg: String, e: Throwable) {
    if (DEBUG) {
        when (type) {
            'd' -> Log.d(tag, msg, e)
            'w' -> Log.w(tag, msg, e)
            'i' -> Log.i(tag, msg, e)
            'e' -> Log.e(tag, msg, e)
        }
    }
}

/**
 * Send mesage to logcat
 *
 * Note that this function will only send logcat messages if and only if `DEBUG` returns `true`
 * also note that the function is inline, meaning that it will be copied to the location where
 * it is called. There is no need to manually do a debug check each time to save resources on
 * calling the function, the conditions are handled automatically.
 *
 * @param type
 *      Type of log `d`,`i`,`w`, or `e` for `debug`, `info`, `warning` or `error`
 *
 * @param tag
 *      Tag to be used on the entry
 *
 * @param msg
 *      Message to send
 */
inline fun logDebug(type: Char, tag: String, msg: String) {
    if (DEBUG) {
        when (type) {
            'd' -> Log.d(tag, msg)
            'w' -> Log.w(tag, msg)
            'i' -> Log.i(tag, msg)
            'e' -> Log.e(tag, msg)
        }
    }
}

/**
 * Debug is set based on build type, but you can force it on releases
 */
fun setForcedDebug(flag: Boolean) {
    FORCED_DEBUG = flag
}

/**
 *
 */
internal inline fun threadSuspend(obj: Any, noinline timeout: () -> Long) = threadSuspend(obj, false, timeout)

/**
 *
 */
internal inline fun threadSuspend(obj: Any, notify: Boolean, noinline timeout: () -> Long) {
    if (notify) {
        (obj as java.lang.Object).notifyAll()
    }

    while (true) {
        val timeout = timeout()

        if (timeout > 0L) {
            try {
                (obj as java.lang.Object).wait(timeout)

            } catch (e: InterruptedException) {
            }

        } else {
            break
        }
    }
}

/**
 *
 */
internal inline fun threadWait(obj: Any) = threadWait(obj, false)

/**
 *
 */
internal inline fun threadWait(obj: Any, notify: Boolean) {
    if (notify) {
        (obj as java.lang.Object).notifyAll()
    }

    try {
        (obj as java.lang.Object).wait()

    } catch (e: InterruptedException) {
    }
}

/**
 *
 */
internal inline fun threadWait(obj: Any, noinline condition: () -> Boolean) = threadWait(obj, false, condition)

/**
 *
 */
internal inline fun threadWait(obj: Any, notify: Boolean, noinline condition: () -> Boolean) {
    if (notify) {
        (obj as java.lang.Object).notifyAll()
    }

    while (condition()) {
        try {
            (obj as java.lang.Object).wait()

        } catch (e: InterruptedException) {
        }
    }
}

/**
 *
 */
internal inline fun threadNotify(obj: Any) {
    (obj as java.lang.Object).notifyAll()
}
