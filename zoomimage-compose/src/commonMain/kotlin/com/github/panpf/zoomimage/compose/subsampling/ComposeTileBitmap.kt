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

package com.github.panpf.zoomimage.compose.subsampling

import androidx.compose.ui.graphics.ImageBitmap
import com.github.panpf.zoomimage.subsampling.TileBitmap

// todo Improved implementation of TileBitmap, it's a bit messy now
class ComposeTileBitmap constructor(
    val imageBitmap: ImageBitmap,
    bitmapByteCount: Int,
) : TileBitmap {

    override val width: Int = imageBitmap.width

    override val height: Int = imageBitmap.height

    override val byteCount: Int = bitmapByteCount

    override fun recycle() {}

    override val isRecycled: Boolean = false
}