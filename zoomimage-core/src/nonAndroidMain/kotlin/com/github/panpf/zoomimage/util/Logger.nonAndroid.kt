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

actual fun createLogPipeline(): Logger.Pipeline = PrintlnLogPipeline()

/**
 * The pipeline of the log, which prints the log to the println
 */
class PrintlnLogPipeline : Logger.Pipeline {

    override fun log(level: Logger.Level, tag: String, msg: String, tr: Throwable?) {
        if (tr != null) {
            val trString = tr.stackTraceToString()
            println("${level}. $tag. $msg. \n$trString")
        } else {
            println("${level}. $tag. $msg")
        }
    }

    override fun flush() {

    }

    @Suppress("RedundantOverride")
    override fun equals(other: Any?): Boolean {
        // If you add construction parameters to this class, you need to change it here
        return super.equals(other)
    }

    @Suppress("RedundantOverride")
    override fun hashCode(): Int {
        // If you add construction parameters to this class, you need to change it here
        return super.hashCode()
    }

    override fun toString(): String {
        return "PrintlnLogPipeline"
    }
}