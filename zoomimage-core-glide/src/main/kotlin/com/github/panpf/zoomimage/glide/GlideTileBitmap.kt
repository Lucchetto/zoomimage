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

package com.github.panpf.zoomimage.glide

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.EngineResourceWrapper
import com.github.panpf.zoomimage.glide.internal.toHexString
import com.github.panpf.zoomimage.subsampling.AndroidCacheTileBitmap

internal class GlideTileBitmap(
    override val key: String,
    private val resource: EngineResourceWrapper
) : AndroidCacheTileBitmap {

    override val bitmap: Bitmap
        get() = resource.bitmap

    override val width: Int = bitmap.width

    override val height: Int = bitmap.height

    override val byteCount: Int = bitmap.byteCount

    override val isRecycled: Boolean
        get() = bitmap.isRecycled

    override fun recycle() {
        bitmap.recycle()
    }

    override fun setIsDisplayed(displayed: Boolean) {
        resource.setIsDisplayed(displayed)
    }

    override fun toString(): String {
        return "GlideTileBitmap(size=${width}x${height},config=${bitmap.config},@${bitmap.toHexString()})"
    }
}