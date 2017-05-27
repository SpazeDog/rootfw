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

package com.spazedog.lib.rootfw.utils

import com.spazedog.lib.rootfw.ShellStream
import java.io.Writer


/**
 * Special writer that is build to work with [ShellStream]
 *
 * This class cannot used by itself.
 * Use [ShellStream.getWriter] to get the one that works with
 * a specific instance
 */
abstract class OutputWriter(lock: Any? = null) : Writer() {

    /** * */
    private val mLock: Any = if (lock != null) lock else this

    /**
     *
     */
    abstract internal fun getOutputStream(): Writer?

    /**
     *
     */
    override fun write(cbuf: CharArray?, off: Int, len: Int) {
        synchronized(mLock) {
            getOutputStream()?.write(cbuf, off, len)
        }
    }

    /**
     *
     */
    override fun flush() {
        synchronized(mLock) {
            getOutputStream()?.flush()
        }
    }

    /**
     *
     */
    override fun close() {
    }
}