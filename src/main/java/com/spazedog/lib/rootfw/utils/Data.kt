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

/**
 * Abstract data container that stores data as lines in an array.
 *
 * The class provides basic tools to manipulate the data
 * being stored in different ways.
 */
open abstract class Data<T : Data<T>>(lines: Array<String>) {

    /** * */
    protected var mLines = lines

    /**
     * Replace sequence in each line using RegExp
     *
     * @param regex
     *     RegExp to match
     *
     * @param replace
     *     Sequence to replace with
     *
     * @return
     *     This instance
     */
    fun replace(regex: Regex, replace: String): T {
        for (i in 0 until mLines.size) {
            mLines[i] = mLines[i].replace(regex, replace)
        }

        return this as T
    }

    /**
     * Replace sequence in each line by matching a sequence
     *
     * @param regex
     *     Sequence to match to
     *
     * @param replace
     *     Sequence to replace with
     *
     * @return
     *     This instance
     */
    fun replace(find: String, replace: String): T {
        for (i in 0 until mLines.size) {
            mLines[i] = mLines[i].replace(find, replace)
        }

        return this as T
    }

    /**
     * Sort lines by invalidating a RegExp
     *
     * @param regex
     *     RegExp to invalidate
     *
     * @return
     *     This instance
     */
    fun assort(regex: Regex): T {
        if (mLines.size > 0) {
            val list = mutableListOf<String>()

            for (line in mLines) {
                if (!line.contains(regex)) {
                    list.add(line)
                }
            }

            mLines = list.toTypedArray()
        }

        return this as T
    }

    /**
     * Sort lines by validating a RegExp
     *
     * @param regex
     *     RegExp to validate
     *
     * @return
     *     This instance
     */
    fun sort(regex: Regex): T {
        if (mLines.size > 0) {
            val list = mutableListOf<String>()

            for (line in mLines) {
                if (line.contains(regex)) {
                    list.add(line)
                }
            }

            mLines = list.toTypedArray()
        }

        return this as T
    }

    /**
     * Sort lines by checking the absence of a sequence within each line
     *
     * @param contains
     *     Sequence that each line must not contain
     *
     * @param ignoreCase
     *     Ignore case when comparing
     *
     * @return
     *     This instance
     */
    @JvmOverloads
    fun assort(contains: String, ignoreCase: Boolean = false): T {
        if (mLines.size > 0) {
            val list = mutableListOf<String>()

            for (line in mLines) {
                if (!line.contains(contains, ignoreCase)) {
                    list.add(line)
                }
            }

            mLines = list.toTypedArray()
        }

        return this as T
    }

    /**
     * Sort lines by checking the existence of a sequence within each line
     *
     * @param contains
     *     Sequence that each line must contain
     *
     * @param ignoreCase
     *     Ignore case when comparing
     *
     * @return
     *     This instance
     */
    @JvmOverloads
    fun sort(contains: String, ignoreCase: Boolean = false): T {
        if (mLines.size > 0) {
            val list = mutableListOf<String>()

            for (line in mLines) {
                if (line.contains(contains, ignoreCase)) {
                    list.add(line)
                }
            }

            mLines = list.toTypedArray()
        }

        return this as T
    }

    /**
     * Sort lines using a range between `start` and the end of the data array and keep the elements within the range
     *
     * @param start
     *     Where to start
     *
     * @return
     *     This instance
     */
    fun sort(start: Int): T {
        val begin = if (start < 0) mLines.size + start else start
        val end = mLines.size - begin

        return sort(begin, end)
    }

    /**
     * Sort lines using a range between `start` and `stop` and keep the elements within the range
     *
     * @param start
     *     Where to start
     *
     * @param stop
     *     When to stop
     *
     * @return
     *     This instance
     */
    fun sort(start: Int, stop: Int): T {
        if (mLines.size > 0) {
            var begin = if (start < 0) mLines.size + start else start
            var end = if (stop < 0) mLines.size + stop else begin + stop

            if (begin < 0) {
                begin = 0

            } else if (begin > mLines.size) {
                begin = mLines.size
            }

            if (end < 0) {
                end = 0

            } else if (end > mLines.size) {
                end = mLines.size
            }

            if (begin == end) {
                mLines = arrayOf<String>()

            } else {
                val list = mutableListOf<String>()

                val min = if (begin > end) {
                    if (end == 0) arrayOf(begin) else arrayOf(0, begin)

                } else {
                    arrayOf(begin)
                }

                val max = if (begin > end) {
                    if (end == 0) arrayOf(mLines.size) else arrayOf(end, mLines.size)

                } else {
                    arrayOf(end)
                }

                for (i in 0 until min.size) {
                    for (x in min[i] until max[i]) {
                        list.add(mLines[x])
                    }
                }

                mLines = list.toTypedArray()
            }
        }

        return this as T
    }

    /**
     * Sort lines using a range between `start` and the end of the data array and exclude the elements within the range
     *
     * @param start
     *     Where to start
     *
     * @return
     *     This instance
     */
    fun assort(start: Int): T {
        val begin = if (start < 0) mLines.size + start else start
        val end = mLines.size

        return sort(end, begin)
    }

    /**
     * Sort lines using a range between `start` and `stop` and exclude the elements within the range
     *
     * @param start
     *     Where to start
     *
     * @param stop
     *     When to stop
     *
     * @return
     *     This instance
     */
    fun assort(start: Int, stop: Int): T {
        val begin = if (start < 0) mLines.size + start else start
        val end = if (stop < 0) mLines.size + stop else begin + stop

        return sort(end, begin)
    }

    /**
     * Reverses the lines in this collection

     * @return
     * *     This instance
     */
    fun reverse(): T {
        if (mLines.size > 0) {
            val list = mutableListOf<String>()

            for (i in (mLines.size - 1) downTo 0) {
                list.add(mLines[i])
            }

            mLines = list.toTypedArray()
        }

        return this as T
    }

    /**
     * This method will remove all of the empty lines from the data array and trim each line
     *
     * @return
     *     This instance
     */
    fun trim(): T {
        if (mLines.size > 0) {
            val list = mutableListOf<String>()

            for (line in mLines) {
                val trimmed = line.trim()

                if (trimmed.length > 0) {
                    list.add(trimmed)
                }
            }

            mLines = list.toTypedArray()
        }

        return this as T
    }

    /**
     * Count the lines in the data array
     *
     * @return
     *     The number of lines
     */
    fun getSize(): Int = mLines.size

    /**
     * This will return the data array
     *
     * @return
     *     The data array
     */
    fun getArray(): Array<String> = mLines

    /**
     * This will return a string of the data array with custom characters used as line breakers
     *
     * @param separater
     *     A separator character used to separate each line
     *
     * @return
     *     The data array as a string
     */
    @JvmOverloads
    fun getString(separater: String = "\n"): String {
        val builder = StringBuilder()

        for (i in 0 until mLines.size) {
            if (i > 0) {
                builder.append(separater)
            }

            builder.append(mLines[i])
        }

        return builder.toString()
    }

    /**
     * This will return one specified line of the data array.
     *
     * Note that this also takes negative number to get a line from the end and up
     *
     * @param lineNum
     *     The line number to return
     *
     * @param skipEmpty
     *     Whether or not to include empty lines
     *
     * @return
     *     The specified line
     */
    @JvmOverloads
    fun getLine(lineNum: Int = -1, skipEmpty: Boolean = false): String? {
        if (mLines.size > 0) {
            var count: Int = if (lineNum < 0)
                mLines.size + lineNum
            else
                lineNum

            while (count >= 0 && count < mLines.size) {
                if (!skipEmpty || mLines[count].trim().length > 0) {
                    return mLines[count].trim()
                }

                count = if (lineNum < 0)
                    count - 1
                else
                    count + 1
            }
        }

        return null
    }
}
