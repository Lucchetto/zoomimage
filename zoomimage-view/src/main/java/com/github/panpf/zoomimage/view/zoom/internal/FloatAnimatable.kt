/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
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
package com.github.panpf.zoomimage.view.zoom.internal

import android.view.View
import android.view.animation.Interpolator
import androidx.core.view.ViewCompat

internal class FloatAnimatable(
    private val view: View,
    private val startValue: Float,
    private val endValue: Float,
    private val durationMillis: Int,
    private val interpolator: Interpolator,
    private val onUpdateValue: (value: Float) -> Unit,
    private val onEnd: () -> Unit = {}
) {

    private val runnable = Runnable { frame() }
    private var startTime = 0L
    var value = startValue
        private set

    var running = false
        private set

    fun start() {
        if (running) return
        value = startValue
        startTime = System.currentTimeMillis()
        running = true
        view.post(runnable)
    }

    fun stop() {
        if (!running) return
        running = false
        view.removeCallbacks(runnable)
        onEnd()
    }

    private fun frame() {
        val progress = computeProgress()
        val currentValue = startValue + (progress * (endValue - startValue))
        value = currentValue
        onUpdateValue(currentValue)
        running = progress < 1f
        if (running) {
            ViewCompat.postOnAnimation(view, runnable)
        } else {
            onEnd()
        }
    }

    @Suppress("UnnecessaryVariable")
    private fun computeProgress(): Float {
        val elapsedTime = System.currentTimeMillis() - startTime
        val progress = (elapsedTime.toFloat() / durationMillis).coerceAtMost(1f)
        val changedProgress = interpolator.getInterpolation(progress)
        return changedProgress
    }
}