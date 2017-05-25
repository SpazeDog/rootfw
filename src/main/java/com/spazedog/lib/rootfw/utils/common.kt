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

/** * */
internal val BINARIES: List<String?> = listOf(null, "toybox", "busybox", "toolbox")

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
