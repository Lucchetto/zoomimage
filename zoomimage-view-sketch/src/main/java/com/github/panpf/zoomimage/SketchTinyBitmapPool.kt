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
package com.github.panpf.zoomimage

import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import com.github.panpf.sketch.Sketch

class SketchTinyBitmapPool(private val sketch: Sketch) : TinyBitmapPool {

    override fun put(bitmap: Bitmap): Boolean {
        return sketch.bitmapPool.put(bitmap, "SubsamplingImageView")
    }

    override fun get(width: Int, height: Int, config: Config): Bitmap? {
        return sketch.bitmapPool.get(width, height, config)
    }
}