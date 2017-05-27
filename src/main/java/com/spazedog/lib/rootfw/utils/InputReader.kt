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
import com.spazedog.lib.rootfw.utils.InputReader.Signal.Connected
import java.io.Reader

/**
 * Special reader that is build to work with the asynchronous [ShellStream]
 *
 * This class cannot used by itself.
 * Use [ShellStream.getReader] to get the one that works with
 * a specific instance
 */
abstract class InputReader(lock: Any? = null) : Reader() {

    /** * */
    internal enum class Signal { Connected, Disconnected }

    /** * */
    @Volatile private var mBuffered = false

    /** * */
    @Volatile private var mConnected = false

    /** * */
    @Volatile private var mLocks = 0

    /** * */
    @Volatile private var mReceieve = false

    /** * */
    private var mCharBuffer: CharArray? = null

    /** * */
    private var mCurChar = 0

    /** * */
    private var mCharLen = 0

    /** * */
    private val mLock: Any = if (lock != null) lock else this

    /** * */
    internal val receieve: Boolean
        get() = synchronized(mLock) { mReceieve && active }

    /** * */
    internal val active: Boolean
        get() = synchronized(mLock) { mConnected && mLocks > 0 }

    /**
     *
     */
    internal abstract fun getDebug(): Debug

    /**
     *
     */
    internal fun signal(sig: Signal) {
        synchronized(mLock) {
            mConnected = sig == Connected

            threadNotify(mLock)
        }
    }

    /**
     *
     */
    internal fun buffer(buf: CharArray, buffered: Boolean) {
        synchronized(mLock) {
            if (mReceieve) {
                getDebug().log("Reader", "Received buffer from Worker")

                mCharBuffer = buf
                mBuffered = buffered
                mCurChar = 0
                mCharLen = buf.size
                mReceieve = false

                threadNotify(mLock)
            }
        }
    }

    /**
     *
     */
    override fun read(buf: CharArray?, off: Int, len: Int): Int {
        synchronized(mLock) {
            var offset = off

            if (buf == null || offset >= (off + len)) {
                return 0
            }

            if (mConnected) {
                getDebug().log("Reader", "Start reading from local buffer")

                do {
                    if (mCurChar >= mCharLen) {
                        getDebug().log("Reader", "Local buffer is empty, request refill from Worker")

                        mReceieve = true
                        threadWait(mLock, true) { receieve }

                        if (mCurChar >= mCharLen) {
                            getDebug().log("Reader", "Worker has no more data for us, returning to caller")

                            return -1
                        }

                        getDebug().log("Reader", "Local buffer has been filled, continue reading")
                    }

                    while (offset < (off + len) && mCurChar < mCharLen) {
                        buf[offset] = mCharBuffer!![mCurChar]

                        offset++
                        mCurChar++
                    }

                } while (offset < (off + len) && ready())

                getDebug().log("Reader", "Read completed, returning to caller")
            }

            return if ((offset - off) == 0 && !mConnected) -1 else offset - off
        }
    }

    /**
     *
     */
    override fun ready(): Boolean {
        synchronized(mLock) {
            return (mBuffered && mConnected) || mCurChar < mCharLen
        }
    }

    /**
     *
     */
    internal fun open() {
        synchronized(mLock) {
            getDebug().log("Reader", "Open was called, adding lock")

            mLocks++
        }
    }

    /***
     *
     */
    override fun close() {
        synchronized(mLock) {
            getDebug().log("Reader", "Close was called, removing lock")

            mLocks--

            if (mLocks <= 0) {
                mCharBuffer = null
                mBuffered = false
                mCurChar = 0
                mCharLen = 0
            }
        }
    }
}