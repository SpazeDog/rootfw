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

package com.spazedog.lib.rootfw

import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader

import com.spazedog.lib.rootfw.ShellStream.Interfaces.ConnectionListener
import com.spazedog.lib.rootfw.ShellStream.Interfaces.StreamListener


/**
 * I/O stream for a terminal process
 *
 * This stream is used to write to a terminal process and uses
 * listeners to retrieve any output from stdout asynchronously.
 */
class ShellStream() {

    /**
     * Object used to store internal static information
     */
    private companion object {
        var STREAMS = 0;
    }

    /**
     * Object containing interfaces that can be used with [ShellStream]
     */
    object Interfaces {
        /**
         * Used to keep track of the terminal process connection
         *
         * This can be added to a constructor or using [ShellStream.addConnectionListener].
         *
         * If you use `Kotlin` then you can use lambda functions instead of this interface.
         * You can read more in the docs for [ShellStream.addConnectionListener].
         */
        interface ConnectionListener {
            /**
             * Called when a connection has been established
             *
             * @param stream
             *      The [ShellStream] that invoked this method
             */
            fun onConnect(stream: ShellStream)

            /**
             * Called when a connection has been closed
             *
             * @param stream
             *      The [ShellStream] that invoked this method
             */
            fun onDisconnect(stream: ShellStream)
        }

        /**
         * Used to retrieve output from stdout of the terminal process
         *
         * This can be added using [ShellStream.addStreamListener].
         *
         * If you use `Kotlin` then you can use lambda functions instead of this interface.
         * You can read more in the docs for [ShellStream.addStreamListener].
         */
        interface StreamListener {
            /**
             * Called whenever the stream receives a new output line from the terminal process
             *
             * @param stream
             *      The [ShellStream] that invoked this method
             *
             * @param line
             *      The line received from stdout
             */
            fun onStdOut(stream: ShellStream, line: String)
        }
    }

    /**
     * Create a new [ShellStream]
     *
     * @param listener
     *      A kotlin lambda used a [ConnectionListener]
     */
    constructor(listener: (ShellStream, Boolean) -> Unit) : this(InternalConnectionListener(listener))

    /**
     * Create a new [ShellStream]
     *
     * @param listener
     *      A [ConnectionListener]
     */
    constructor(listener: Interfaces.ConnectionListener) : this() {
        addConnectionListener(listener)
    }

    /**
     * Wrapper class to allow the use of Java interfaces and Kotlin Lambdas as connection listeners
     */
    private class InternalConnectionListener(val listener: (ShellStream, Boolean) -> Unit) : Interfaces.ConnectionListener {
        override fun onConnect(stream: ShellStream) = listener(stream, true)
        override fun onDisconnect(stream: ShellStream) = listener(stream, false)

        override fun equals(other: Any?): Boolean {
            if (!super.equals(other)) {
                return listener.equals(other)
            }

            return true
        }
    }

    /**
     * Wrapper class to allow the use of Java interfaces and Kotlin Lambdas as stream listeners
     */
    private class InternalStreamListener(val listener: (ShellStream, String) -> Unit) : Interfaces.StreamListener {
        override fun onStdOut(stream: ShellStream, line: String) = listener(stream, line)

        override fun equals(other: Any?): Boolean {
            if (!super.equals(other)) {
                return listener.equals(other)
            }

            return true
        }
    }

    private enum class WaitState {CONNECTING, DISCONNECTING, NOWAIT}

    /** * */
    private val mStreamId = ++STREAMS;

    /** * */
    @Volatile
    private var mWait = WaitState.NOWAIT;

    /** * */
    private var mRootStream = false;

    /** * */
    private var mIgnoreErrorStream = false;

    /** * */
    private var mConnection: Process? = null

    /** * */
    private var mStdInput: DataOutputStream? = null

    /** * */
    private var mStdOutput: BufferedReader? = null

    /** * */
    private var mStdError: BufferedReader? = null

    /** * */
    private var mStdOutReader: Thread? = null

    /** * */
    private var mStdErrReader: Thread? = null

    /** * */
    private val mLock = Any()

    /** * */
    private val mConnListeners = mutableListOf<Interfaces.ConnectionListener>()

    /** * */
    private val mStreamListeners = mutableListOf<Interfaces.StreamListener>()

    /**
     * Add a new Kotlin lambda as [ConnectionListener]
     *
     * The second argument of the lambda represents the new state of the terminal process.
     * `TRUE` means connected and `FALSE` means disconnected.
     *
     * @param listener
     *      The listener to add
     *
     * @return
     *      An [ConnectionListener] wrapper that can be used with [ShellStream.removeConnectionListener]
     */
    fun addConnectionListener(listener: (ShellStream, Boolean) -> Unit): ConnectionListener? {
        for (stored in mConnListeners) {
            if (stored.equals(listener)) {
                return null;
            }
        }

        val out = InternalConnectionListener(listener)
        mConnListeners.add(out)

        return out
    }

    /**
     * Add a new [ConnectionListener]
     *
     * @param listener
     *      The listener to add
     */
    fun addConnectionListener(listener: ConnectionListener) {
        for (stored in mConnListeners) {
            if (stored.equals(listener)) {
                return
            }
        }

        mConnListeners.add(listener)
    }

    /**
     * Remove a connection listener
     *
     * This removes a Kotlin lambda that was added as a [ConnectionListener].
     *
     * @param listener
     *      The listener used in [ShellStream.addConnectionListener]
     */
    fun removeConnectionListener(listener: (ShellStream, Boolean) -> Unit) {
        for (stored in mConnListeners) {
            if (stored.equals(listener)) {
                mConnListeners.remove(stored); return
            }
        }
    }

    /**
     * Remove a connection listener
     *
     * @param listener
     *      The listener used in [ShellStream.addConnectionListener]
     */
    fun removeConnectionListener(listener: ConnectionListener) = mConnListeners.remove(listener)

    /**
     * Add a new Kotlin lambda as [StreamListener]
     *
     * The second argument of the lambda contains the new line retrieved from stdout
     *
     * @param listener
     *      The listener to add
     */
    fun addStreamListener(listener: (ShellStream, String) -> Unit): StreamListener? {
        for (stored in mStreamListeners) {
            if (stored.equals(listener)) {
                return null;
            }
        }

        val out = InternalStreamListener(listener)
        mStreamListeners.add(out)

        return out
    }

    /**
     * Add a new [StreamListener]
     *
     * @param listener
     *      The listener to add
     */
    fun addStreamListener(listener: StreamListener) {
        for (stored in mStreamListeners) {
            if (stored.equals(listener)) {
                return
            }
        }

        mStreamListeners.add(listener)
    }

    /**
     * Remove a stream listener
     *
     * This removes a Kotlin lambda that was added as a [ShellStream].
     *
     * @param listener
     *      The listener used in [ShellStream.addStreamListener]
     */
    fun removeStreamListener(listener: (ShellStream, String) -> Unit) {
        for (stored in mStreamListeners) {
            if (stored.equals(listener)) {
                mStreamListeners.remove(stored); return
            }
        }
    }

    /**
     * Remove a stream listener
     *
     * @param listener
     *      The listener used in [ShellStream.addStreamListener]
     */
    fun removeStreamListener(listener: StreamListener) = mStreamListeners.remove(listener)

    /**
     * Return this stream's id
     *
     * Each new [ShellStream] get's an id. This method will return the id of this stream
     */
    fun streamId(): Int = mStreamId

    /**
     * Check whether or not this stream is running with root privileges
     */
    fun isRootStream(): Boolean = mRootStream && isConnected()

    /**
     * Check whether or not this stream has been configured to ignore stderr
     */
    fun ignoreErrorStream(): Boolean = mIgnoreErrorStream

    /**
     * Connect to the terminal process using default setup
     *
     * Default setup means no root privileges and stderr will not be ignored
     */
    fun connect(): Boolean = connect(false, false, false)

    /**
     * Connect to the terminal process
     *
     * @param requestRoot
     *      Request root privileges. If not possible, the connection will use normal privileges
     */
    fun connect(requestRoot: Boolean): Boolean = connect(requestRoot, false, false)

    /**
     * Connect to the terminal process
     *
     * @param requestRoot
     *      Request root privileges. If not possible, the connection will use normal privileges
     *
     * @param wait
     *      Block until connection listeners are done
     */
    fun connect(requestRoot: Boolean, wait: Boolean): Boolean = connect(requestRoot, wait, false)

    /**
     * Connect to the terminal process
     *
     * @param requestRoot
     *      Request root privileges. If not possible, the connection will use normal privileges
     *
     * @param wait
     *      Block until connection listeners are done
     *
     * @param ignoreErr
     *      Filter stderr out of the output
     */
    fun connect(requestRoot: Boolean, wait: Boolean, ignoreErr: Boolean): Boolean {
        synchronized(mLock) {
            var status = false;

            if (!isConnected()) {
                mWait = if (wait) WaitState.CONNECTING else WaitState.NOWAIT

                val cmds = if (requestRoot) arrayOf("su", "sh") else arrayOf("sh")

                for (console in cmds) {
                    try {
                        val builder = ProcessBuilder(console)

                        if (!ignoreErr) {
                            builder.redirectErrorStream(true)
                        }

                        mConnection = builder.start();
                        mStdInput = DataOutputStream(mConnection!!.outputStream)
                        mStdOutput = BufferedReader(InputStreamReader(mConnection!!.inputStream))
                        mRootStream = console.equals("su")
                        mIgnoreErrorStream = ignoreErr

                        if (ignoreErr) {
                            mStdError = BufferedReader(InputStreamReader(mConnection!!.errorStream))
                        }

                        if (isConnected()) {
                            mStdOutReader = Thread {
                                var thread = HandlerThread("Stream\$$mStreamId")
                                thread.start()

                                var handler = Handler(thread.looper)

                                if (mConnListeners.size > 0) {
                                    handler.post {
                                        for (listener in mConnListeners) {
                                            listener.onConnect(this)
                                        }
                                    }

                                    if (mWait == WaitState.CONNECTING) {
                                        handler.post {
                                            mWait = WaitState.DISCONNECTING

                                            synchronized(mLock) {
                                                (mLock as java.lang.Object).notifyAll()
                                            }
                                        }
                                    }

                                } else if (mWait == WaitState.CONNECTING) {
                                    mWait = WaitState.DISCONNECTING

                                    synchronized(mLock) {
                                        (mLock as java.lang.Object).notifyAll()
                                    }
                                }

                                try {
                                    var output: String? = null

                                    while ({ output = mStdOutput?.readLine(); output }() != null) {
                                        if (mStreamListeners.size > 0) {
                                            val line = output!!;

                                            handler.post {
                                                for (listener in mStreamListeners) {
                                                    listener.onStdOut(this, line)
                                                }
                                            }
                                        }
                                    }

                                } catch (e: IOException) {
                                } finally {
                                    destroy()

                                    if (mConnListeners.size > 0) {
                                        handler.post {
                                            for (listener in mConnListeners) {
                                                listener.onDisconnect(this)
                                            }
                                        }

                                        if (mWait == WaitState.DISCONNECTING) {
                                            handler.post {
                                                mWait = WaitState.NOWAIT

                                                synchronized(mLock) {
                                                    (mLock as java.lang.Object).notifyAll()
                                                }
                                            }
                                        }

                                    } else if (mWait == WaitState.DISCONNECTING) {
                                        mWait = WaitState.NOWAIT

                                        synchronized(mLock) {
                                            (mLock as java.lang.Object).notifyAll()
                                        }
                                    }

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                        thread.quitSafely()

                                    } else {
                                        /*
                                         * Make sure that the message above has been delivered
                                         */
                                        Thread.sleep(1000)
                                        thread.quit()
                                    }
                                }
                            }

                            if (ignoreErr) {
                                mStdErrReader = Thread {
                                    try {
                                        var output: String? = null

                                        while ({ output = mStdError?.readLine(); output }() != null) {
                                            // Ignore
                                        }

                                    } catch (e: IOException) {}
                                }
                            }

                            mStdErrReader?.start()
                            mStdOutReader!!.start()

                            status = true;
                        }

                    } catch (e: IOException) {
                        mConnection?.destroy()
                        mConnection = null
                    }
                }

            } else {
                status = true
            }

            while (status && mWait == WaitState.CONNECTING) {
                try {
                    (mLock as java.lang.Object).wait()

                } catch (e: Throwable) {}
            }

            return status
        }
    }

    /**
     * Send exit signal to the terminal process
     *
     * Note that if a daemon like process is running in the
     * terminal process, this signal will never be intercepted by the terminal process.
     * To force kill the terminal process, you can use [ShellStream.destroy]
     */
    fun disconnect() {
        synchronized(mLock) {
            if (isConnected()) {
                writeLines("exit $?")

                while (mWait == WaitState.DISCONNECTING) {
                    try {
                        (mLock as java.lang.Object).wait()

                    } catch (e: Throwable) {
                    }
                }
            }
        }
    }

    /**
     * Kill the terminal process
     *
     * Note that this will not wait until some ongoing command finishes.
     * If you need to make sure that something get's processed fully before terminating,
     * consider using [ShellStream.disconnect]
     */
    fun destroy() {
        synchronized(mLock) {
            if (mConnection != null) {
                val wasConnected = isConnected()
                val conn = mConnection
                mConnection = null
                conn?.destroy()

                try {
                    mStdInput?.close()

                } catch (e: IOException) {}

                try {
                    mStdOutput?.close()

                } catch (e: IOException) {}

                try {
                    mStdError?.close()

                } catch (e: IOException) {}

                mStdInput = null;
                mStdOutput = null;
                mStdError = null;

                mRootStream = false;
                mIgnoreErrorStream = false

                mStdOutReader = null
                mStdErrReader = null

                if (wasConnected) {
                    while (mWait == WaitState.DISCONNECTING) {
                        try {
                            (mLock as java.lang.Object).wait()

                        } catch (e: Throwable) {
                        }
                    }
                }
            }
        }
    }

    /**
     * Check whether or not the terminal process is active
     */
    fun isConnected(): Boolean {
        synchronized(mLock) {
            try {
                /*
                 * If mConnection is null, it's not connected.
                 * Also if it returns an exit value, the process has been terminated.
                 * If it's connected, this call will throw an exception.
                 */
                mConnection?.exitValue();

            } catch (e: IllegalThreadStateException) {
                return true
            }

            return false
        }
    }

    /**
     * Write lines to the terminal process
     *
     * This method will append a line feed character to each line,
     * which is equal to hitting ENTER in a terminal. If you don't want any
     * line feeds, you can use [ShellStream.write] instead.
     *
     * @param lines
     *      Lines to write
     */
    fun writeLines(vararg lines: String): Boolean {
        synchronized(mLock) {
            if (isConnected()) {
                try {
                    for (line in lines) {
                        mStdInput!!.write("$line\n".toByteArray())
                    }

                    if (mStdInput != null) {
                        mStdInput!!.flush()
                    }

                    return true

                } catch (e: IOException) {
                }
            }

            return false
        }
    }

    /**
     * Write one or more [String]'s to the terminal process
     *
     * @param out
     *      Data to write
     */
    fun write(vararg out: String): Boolean {
        synchronized(mLock) {
            if (isConnected()) {
                try {
                    for (str in out) {
                        mStdInput!!.write(str.toByteArray())
                    }

                    if (mStdInput != null) {
                        mStdInput!!.flush()
                    }

                    return true

                } catch (e: IOException) {
                }
            }

            return false
        }
    }

    /**
     * Write one or more [Byte]'s to the terminal process
     *
     * @param out
     *      Data to write
     */
    fun write(vararg out: Byte): Boolean {
        synchronized(mLock) {
            if (isConnected()) {
                try {
                    mStdInput!!.write(out)

                    if (mStdInput != null) {
                        mStdInput!!.flush()
                    }

                    return true

                } catch (e: IOException) {
                }
            }

            return false
        }
    }
}
