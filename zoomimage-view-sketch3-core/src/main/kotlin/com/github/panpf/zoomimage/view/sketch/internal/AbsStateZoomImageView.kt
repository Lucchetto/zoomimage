/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
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

package com.github.panpf.zoomimage.view.sketch.internal

import android.content.Context
import android.util.AttributeSet
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.DisplayRequestState
import com.github.panpf.sketch.request.DisplayResult
import com.github.panpf.sketch.request.ImageOptions
import com.github.panpf.sketch.request.ImageOptionsProvider
import com.github.panpf.sketch.request.Listener
import com.github.panpf.sketch.request.ProgressListener
import com.github.panpf.sketch.request.internal.Listeners
import com.github.panpf.sketch.request.internal.ProgressListeners

/**
 * Convert Sketch's Request Listener to StateFlow's ZoomImageView
 *
 * Copy from Sketch
 */
open class AbsStateZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AbsAbilityZoomImageView(context, attrs, defStyle), ImageOptionsProvider {

    override var displayImageOptions: ImageOptions? = null
    private var displayListenerList: MutableList<Listener<DisplayRequest, DisplayResult.Success, DisplayResult.Error>>? =
        null
    private var displayProgressListenerList: MutableList<ProgressListener<DisplayRequest>>? = null

    val requestState = DisplayRequestState()

    init {
        registerDisplayListener(requestState)
    }

    override fun getDisplayListener(): Listener<DisplayRequest, DisplayResult.Success, DisplayResult.Error>? {
        val myListeners = displayListenerList?.takeIf { it.isNotEmpty() }
        val superListener = super.getDisplayListener()
        if (myListeners == null && superListener == null) {
            return null
        }

        val listenerList = (myListeners?.toMutableList() ?: mutableListOf()).apply {
            if (superListener != null) add(superListener)
        }.toList()
        return Listeners(listenerList)
    }

    override fun getDisplayProgressListener(): ProgressListener<DisplayRequest>? {
        val myProgressListeners = displayProgressListenerList?.takeIf { it.isNotEmpty() }
        val superProgressListener = super.getDisplayProgressListener()
        if (myProgressListeners == null && superProgressListener == null) {
            return null
        }

        val progressListenerList = (myProgressListeners?.toMutableList() ?: mutableListOf()).apply {
            if (superProgressListener != null) add(superProgressListener)
        }.toList()
        return ProgressListeners(progressListenerList)
    }

    fun registerDisplayListener(listener: Listener<DisplayRequest, DisplayResult.Success, DisplayResult.Error>) {
        this.displayListenerList = (this.displayListenerList ?: mutableListOf()).apply {
            add(listener)
        }
    }

    fun unregisterDisplayListener(listener: Listener<DisplayRequest, DisplayResult.Success, DisplayResult.Error>) {
        this.displayListenerList?.remove(listener)
    }

    fun registerDisplayProgressListener(listener: ProgressListener<DisplayRequest>) {
        this.displayProgressListenerList =
            (this.displayProgressListenerList ?: mutableListOf()).apply {
                add(listener)
            }
    }

    fun unregisterDisplayProgressListener(listener: ProgressListener<DisplayRequest>) {
        this.displayProgressListenerList?.remove(listener)
    }
}