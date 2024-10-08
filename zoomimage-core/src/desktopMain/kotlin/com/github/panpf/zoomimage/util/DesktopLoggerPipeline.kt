/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.util

import java.io.ByteArrayOutputStream
import java.io.PrintStream

actual fun createLogPipeline(): Logger.Pipeline = JvmLogPipeline()

/**
 * The pipeline of the log, which prints the log to the println
 */
class JvmLogPipeline : Logger.Pipeline {

    override fun log(level: Int, tag: String, msg: String, tr: Throwable?) {
        if (tr != null) {
            val trString = stackTraceToString(tr)
            println("${Logger.levelName(level)}. $tag. $msg. $trString")
        } else {
            println("${Logger.levelName(level)}. $tag. $msg")
        }
    }

    private fun stackTraceToString(throwable: Throwable): String {
        val arrayOutputStream = ByteArrayOutputStream()
        val printWriter = PrintStream(arrayOutputStream)
        throwable.printStackTrace(printWriter)
        return String(arrayOutputStream.toByteArray())
    }

    override fun flush() {

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun toString(): String {
        return "JvmLogPipeline"
    }
}