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

import android.os.Looper
import com.spazedog.lib.rootfw.ShellStream.Interfaces.ConnectionListener
import com.spazedog.lib.rootfw.ShellStream.Interfaces.StreamListener
import com.spazedog.lib.rootfw.utils.DEBUG
import com.spazedog.lib.rootfw.utils.Debug

/**
 * Wrapper class for [ShellStreamer] for synchronous tasks
 *
 * The [ShellStreamer] class works asynchronous, constantly reading stdout
 * and uses listeners to capture the output. This class can be used for synchronous
 * tasks on a more per command manner. It uses identifiers to figure out when output
 * from a specific command is done printing, meanwhile blocking calls and collecting
 * the data. Ones all output for a specific command has been captured, it is returned
 * along with result code and other useful information. For the most parts, this is
 * the class that you would want to work with.
 *
 * Note that this class was build to handle multiple commands.
 * Unlike other similar libraries, this does not need to exit/re-connect
 * between each command.
 *
 * @constructor
 *      Create a new [Shell] instance using an existing [ShellStream]
 *
 * @param stream
 *      An intance of [ShellStream]
 */
class Shell(stream: ShellStream) : ConnectionListener, StreamListener {

    /** * */
    private val mStream: ShellStream = stream

    /** * */
    private val mEOL: String = "[STREAM:-ID(${stream.streamId()})-:EOL]"

    /** * */
    private var mCallback: ((String) -> Unit)? = null

    /** * */
    private val mLock = Any()

    /** * */
    @Volatile private var mActive = false;

    /** * */
    private val mDebug = Debug("RootFW:Shell(${stream.streamId()})")

    /**
     * @suppress
     */
    init {
        mActive = mStream.connect(false, true)

        mStream.addConnectionListener(this)
        mStream.addStreamListener(this)
    }

    /**
     * Create a new [Shell] instance using a new default [ShellStream]
     *
     * @param requestRoot
     *      Request root for the new [ShellStream] instance
     */
    @JvmOverloads
    constructor(requestRoot: Boolean = false) : this({ val stream = ShellStream(); stream.connect(requestRoot, true); stream }())

    /**
     * Get the [ShellStream] used by this class
     */
    fun getStream(): ShellStream = mStream

    /**
     * Destroy the [ShellStream] used by this class
     */
    fun destroy() = mStream.destroy()

    /**
     * This will detach from [ShellStream] without destroying it
     *
     * This class adds listeners to [ShellStream] to handle output and monitor connection state.
     * This method will detach itself from the stream instance but will not destroy it, meaning
     * that this instance will no longer work, but the [ShellStream] instance will.
     */
    fun close() {
        if (mActive) {
            mActive = false
            mStream.removeConnectionListener(this)
            mStream.removeStreamListener(this)
        }
    }

    /**
     * Check whether or not the [ShellStream] used by this class has root privileges
     */
    fun isRootShell(): Boolean = mActive && mStream.isRootStream()

    /**
     * Check whether or not the [ShellStream] used by this class is active/connected
     */
    fun isActive(): Boolean = mActive

    /**
     * Execute a command
     *
     * @param command
     *      The command to execute
     *
     * @param resultCode
     *      The result code that should be produced upon a successful call
     *
     * @param autopopulate
     *      Whether or not to auto populate with all registered all-in-one binaries
     *
     * @param timeout
     *      Timeout in milliseconds after which to force quit
     */
    @JvmOverloads
    fun execute(command: String, resultCode: Int = 0, autopopulate: Boolean = false, timeout: Long = 0L): Command = execute(Command(command, resultCode, autopopulate), timeout)

    /**
     * Execute a command
     *
     * @param command
     *      [Command] to execute
     *
     * @param timeout
     *      Timeout in milliseconds after which to force quit
     */
    @JvmOverloads
    fun execute(command: Command, timeout: Long = 0L): Command {
        synchronized(mLock) {
            if (!mActive) {
                throw RuntimeException("Cannot execute on a closed shell (ID: ${mStream.streamId()})")

            } else if (DEBUG && Looper.myLooper() != null && Looper.myLooper() == Looper.getMainLooper()) {
                mDebug.log('w', "Executing synchronous on the main Thread could potentially block your apps UI")
            }

            var running = false
            val lines = mutableListOf<String>()
            var resultCode = 1
            var resultCall = -1

            val interupt = if (timeout > 0L)
                System.currentTimeMillis() + timeout
            else
                0L

            mCallback = { line ->
                if (running) {
                    if (line.contains(mEOL)) {
                        if (line.startsWith(mEOL)) {
                            resultCode = Integer.parseInt(line.substring(mEOL.length + 1))
                        }

                        running = false

                        synchronized(mLock) {
                            (mLock as java.lang.Object).notifyAll()
                        }

                    } else {
                        lines.add(line)
                    }
                }
            }

            for (call in command.getCalls()) {
                running = true
                resultCode = 1
                lines.clear()
                resultCall++

                mStream.writeLines(call.command, "echo '$mEOL' \$?")

                while (running && mActive) {
                    val wakeup = if (timeout > 0L) {
                        val out = System.currentTimeMillis() - interupt

                        if (out > 0L) {
                            out

                        } else {
                            1L
                        }

                    } else {
                        1000L
                    }

                    try {
                        (mLock as java.lang.Object).wait(wakeup)

                    } catch (e: Throwable) {
                    } finally {
                        if (interupt > 0L && interupt <= System.currentTimeMillis()) {
                            throw RuntimeException("The request timedout (Time: ${timeout}ms)")
                        }
                    }
                }

                if (!mActive) {
                    throw RuntimeException("Shell was unexpectedly disconnected from the stream (ID: ${mStream.streamId()})")
                }

                if (call.hasResult(resultCode)) {
                    break
                }
            }

            mCallback = null

            return command.updateResultInternal(lines.toTypedArray(), resultCode, resultCall)
        }
    }

    /**
     * Get value of an environment variable on the current connection
     *
     * @param name
     *      Name of the variable
     */
    fun getEnv(name: String): String? {
        val command = execute("echo $" + name.replace(Regex.fromLiteral("[^a-zA-Z0-9_\\-]+"), ""))

        return if (command.getResultSuccess()) {
            command.getLine()

        } else {
            null
        }
    }

    /**
     * Set/Change the value of an environment variable on the current connection
     *
     * @param name
     *      Name of the variable
     *
     * @param value
     *      The value to set
     */
    fun setEnv(name: String, value: Any): Boolean {
        return execute("export " + name.replace(Regex.fromLiteral("[^a-zA-Z0-9_\\-]+"), "") + "='" + value.toString().replace("'", "\\'") + "'").getResultSuccess()
    }

    /**
     * @suppress
     */
    override fun onStdOut(stream: ShellStream, line: String) {
        mCallback?.invoke(line)
    }

    /**
     * @suppress
     */
    override fun onConnect(stream: ShellStream) {

    }

    /**
     * @suppress
     */
    override fun onDisconnect(stream: ShellStream) {
        if (!mStream.isConnected()) {
            close()

            synchronized(mLock) {
                (mLock as java.lang.Object).notifyAll()
            }
        }
    }
}