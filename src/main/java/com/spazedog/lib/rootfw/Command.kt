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

import com.spazedog.lib.rootfw.utils.Data
import com.spazedog.lib.rootfw.Command.Containers.Call
import com.spazedog.lib.rootfw.Command.Interfaces.CallCreator
import com.spazedog.lib.rootfw.utils.BINARIES

/**
 * Used to configure a command before executing and to store the result
 *
 * One issue with Android is it's lack of standard shell environments.
 * Different ROM's has different support like different shells and
 * different all-in-one binaries such as `toybox`, `toolbox` and `busybox`.
 * Even each `toybox`, `toolbox` and `busybox` binaries are compiled with
 * different support and output layout. This makes it difficult to create something
 * that is ensured to work on all devices.
 *
 * This class can help take some of the load of. Each command instance can contain
 * multiple `calls`. Each call contains a shell command and one or more acceptable result codes.
 * When the command instance are executed, each call will be executed until one produces an
 * acceptable result code.
 */
open class Command() : Data<Command>(arrayOf<String>()) {

    /**
     *
     */
    companion object {
        /** * */
        protected val mStaticBinaries = mutableListOf<String?>()

        /** * */
        protected val mBinariySupport = mutableMapOf<String, String?>()

        /**
         * Register new all-in-one binary like `busybox` or `toybox`
         *
         * @param bin
         *      The binary to register
         */
        @JvmStatic
        fun addBinary(bin: String?) {
            if (!mStaticBinaries.contains(bin) && !BINARIES.contains(bin)) {
                mStaticBinaries.add(bin)
            }
        }

        /**
         * Find which registered all-in-one binary packs a specific command
         *
         * Transforms something like `cat` into `toybox cat` or `toolbox cat`,
         * depending on whichever supports `cat`. Be careful though, this method
         * does this by invoking the argument `-h` on the specified command.
         * Something like `toolbox reboot -h` will not produce any output,
         * it will simply ignore the argument and reboot the device, if the
         * installed version supports reboot. It will work on most commands though.
         * Just dont use it on commands that will affect the system, like reboot.
         *
         * @param shell
         *      A [Shell] instance to use for the search
         *
         * @param bin
         *      The command to search for like `cat`, `ps` etc
         *
         * @return
         *      This method returns `NULL` if no support could be found
         */
        @JvmStatic
        fun getBinary(shell: Shell, bin: String): String? {
            if (!mBinariySupport.containsKey(bin)) {
                val list = mutableListOf<String?>()
                list.addAll(mStaticBinaries);
                list.addAll(BINARIES);

                if (list.isEmpty()) {
                    list.add(null);
                }

                /* Toolbox = '<cmd>: no such tool'
                 * Toybox = '<cmd>: not found'
                 * Busybox = '<cmd>: applet not found' or '<cmd>: command unknown'
                 */
                val reg = Regex("(not found|such tool|unknown)$")
                val command = Command();

                for (toolbox in list) {
                    val tb = if (toolbox == null || toolbox.length == 0) bin else "$toolbox $bin"

                    command.reset()
                    command.addCall("$tb -h 2>&1")

                    shell.execute(command)

                    val line = command.getLine()

                    if (line != null && !line.toLowerCase().matches(reg)) {
                        mBinariySupport.put(bin, "$tb"); break
                    }
                }
            }

            return mBinariySupport.get(bin)
        }
    }

    /**
     * Contains interfaces that can be used with this class
     */
    object Interfaces {
        /**
         * Can be used with [Command.addCall] to create custom [Call] instances
         * for each registered all-in-one binary
         */
        interface CallCreator {

            /**
             * Called by [Command.addCall] for each all-in-one binaries to create a call for
             *
             * @param bin
             *      The binary to create the call for
             *
             * @return
             *      The [Call] that was created, or `NULL` to skip this binary
             */
            fun onCreateCall(bin: String?): Call?
        }
    }

    /**
     * Contains class containers that can be used with this class
     */
    object Containers {
        /**
         * Used to store a call that can be added to [Command]
         *
         * @param command
         *      The shell command to execute
         *
         * @param resultCodes
         *      One or more acceptable result codes
         */
        open class Call(val command: String, val resultCodes: Array<Int>) {

            /**
             *
             */
            constructor(command: String, resultCode: Int) : this(command, arrayOf<Int>(resultCode))

            /**
             *
             */
            constructor(command: String) : this(command, arrayOf<Int>(0))

            /**
             * Check if a specific result code is acceptable to this call
             *
             * @param resultCode
             *      The result code to check for
             */
            fun hasResult(resultCode: Int): Boolean {
                for (code in resultCodes) {
                    if (resultCode.equals(code)) {
                        return true
                    }
                }

                return false
            }
        }
    }

    /** * */
    protected val mCalls = mutableListOf<Call>()

    /** * */
    protected val mBinaries = mutableListOf<String?>()

    /** * */
    protected var mResultCall = -1

    /** * */
    protected var mResultCode = 0;

    /** * */
    protected var mExecuted = false

    /**
     * @suppress
     */
    init {
        mBinaries.addAll(mStaticBinaries);
        mBinaries.addAll(BINARIES);

        if (mBinaries.isEmpty()) {
            mBinaries.add(null);
        }
    }

    /**
     * Create a new [Command]
     *
     * @see addCall
     */
    @JvmOverloads
    constructor(command: String, resultCode: Int = 0, populate: Boolean = false) : this(command, arrayOf(resultCode), populate)

    /**
     * Create a new [Command]
     *
     * @see addCall
     */
    constructor(command: String, resultCodes: Array<Int>, populate: Boolean) : this() {
        addCall(command, resultCodes, populate)
    }

    /**
     * Create a new [Command]
     *
     * @see addCall
     */
    constructor(callback: CallCreator) : this() {
        addCall(callback)
    }

    /**
     * Create a new [Command]
     *
     * @see addCall
     */
    constructor(callback: (it: String?) -> Call?) : this() {
        addCall(callback)
    }

    /**
     * Add a new [Call] that is auto build from a shell command,
     * and optionally auto populate with all registered all-in-one binaries
     *
     * Auto populate means that multiple [Call] instances will be auto generated, each
     * using one of the registered all-in-one binaries that can be located at [BINARIES].
     *
     * __Example__
     * ```
     * ins.addCall("ls /", 0, true)
     *
     *     -> ls /
     *     -> busybox ls /
     *     -> toolbox ls /
     *     -> toybox ls /
     * ```
     *
     * @param command
     *      Shell command
     *
     * @param resultCode
     *      An acceptible result code for the shell command
     *
     * @param populate
     *      Populate with all registered all-in-one binaries
     */
    @JvmOverloads
    open fun addCall(command: String, resultCode: Int = 0, populate: Boolean = false) = addCall(command, arrayOf(resultCode), populate)

    /**
     * Add a new [Call] that is auto build from a shell command with multiple acceptible result codes
     * and optionally auto populate with all registered all-in-one binaries
     *
     * Auto populate means that multiple [Call] instances will be auto generated, each
     * using one of the registered all-in-one binaries that can be located at [BINARIES].
     *
     * __Example__
     * ```
     * ins.addCall("ls /", 0, true)
     *
     *     -> ls /
     *     -> busybox ls /
     *     -> toolbox ls /
     *     -> toybox ls /
     * ```
     *
     * @param command
     *      Shell command
     *
     * @param resultCode
     *      An acceptible result code for the shell command
     *
     * @param populate
     *      Populate with all registered all-in-one binaries
     */
    open fun addCall(command: String, resultCodes: Array<Int>, populate: Boolean) {
        if (populate) {
            for (bin in mBinaries) {
                if (bin != null) {
                    if (command.contains("%{bin}")) {
                        mCalls.add(Call(command.replace("%{bin}", bin), resultCodes))

                    } else {
                        mCalls.add(Call("$bin $command", resultCodes))
                    }

                } else {
                    if (command.contains("%{bin}")) {
                        mCalls.add(Call(command.replace("%{bin}", ""), resultCodes))

                    } else {
                        mCalls.add(Call(command, resultCodes))
                    }
                }
            }

        } else {
            mCalls.add(Call(command, resultCodes))
        }
    }

    /**
     * Add a new [Call] instances that is build from a callback interface.
     *
     * This method will run through all the all-in-one binaries in [BINARIES]
     * and call the callback each time, allowing you to create custom [Call] instances
     * for each binary.
     *
     * @param callback
     *      The [CallCreator] callback to use
     *
     * @return
     *      This callback should return the [Call] instances to use or `NULL` to skip the current one
     */
    open fun addCall(callback: CallCreator) {
        for (bin in mBinaries) {
            val call = callback.onCreateCall(bin)

            if (call != null) {
                mCalls.add(call)
            }
        }
    }

    /**
     * Add a new [Call] instances that is build from a Kotlin lambda callback.
     *
     * This method will run through all the all-in-one binaries in [BINARIES]
     * and call the callback each time, allowing you to create custom [Call] instances
     * for each binary.
     *
     * @param callback
     *      The lambda callback to use
     *
     * @return
     *      This callback should return the [Call] instances to use or `NULL` to skip the current one
     */
    open fun addCall(callback: (bin: String?) -> Call?) {
        for (bin in mBinaries) {
            val call = callback(bin)

            if (call != null) {
                mCalls.add(call)
            }
        }
    }

    /**
     * Get all [Call] instances currently added to this class
     */
    open fun getCalls(): Array<Call> = mCalls.toTypedArray()

    /**
     * Get the number of [Call] instances added to this class
     */
    open fun getCallSize(): Int = mCalls.size

    /**
     * Get the [Call] instance at the specified array position
     *
     * @param pos
     *      Positition of the [Call] to return
     */
    open fun getCallAt(pos: Int): Call? {
        val loc = if (pos < 0)
            mCalls.size + pos
        else
            pos

        if (loc < mCalls.size) {
            return mCalls[loc]
        }

        return null
    }

    /**
     * Array position of the last executed call
     */
    open fun getResultCall(): Int = mResultCall

    /**
     * The result code of the last executed call
     */
    open fun getResultCode(): Int = mResultCode

    /**
     * Returns `TRUE` if one of the calls returned with an acceptible result code
     */
    open fun getResultSuccess(): Boolean {
        if (mExecuted) {
            val call = getCallAt(getResultCall())

            if (call != null) {
                return call.hasResult(getResultCode())
            }
        }

        return false
    }

    /**
     * Reset this instance and clear all [Call]'s
     */
    open fun reset() {
        resetInternal()
        mCalls.clear()
    }

    /**
     * @suppress
     */
    internal fun updateResultInternal(lines: Array<String>, resultCode: Int, resultCall: Int): Command {
        mLines = lines;
        mResultCode = resultCode
        mResultCall = resultCall
        mExecuted = true

        onUpdateResult(lines, resultCode, resultCall)

        return this
    }

    /**
     * @suppress
     */
    internal fun resetInternal(): Command {
        if (mLines.size > 0) {
            mLines = arrayOf<String>();
        }

        mResultCode = 0
        mResultCall = -1
        mExecuted = false

        resetInternal()

        return this
    }

    /**
     * Allow interaction with updateResultInternal() without exposing it
     */
    open protected fun onUpdateResult(lines: Array<String>, resultCode: Int, resultCall: Int) {}

    /**
     * Allow interaction with resetInternal() without exposing it
     */
    open protected fun onReset() {}
}