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

package com.github.panpf.zoomimage.coil

import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.asCoilImage
import coil3.memory.MemoryCache
import com.github.panpf.zoomimage.subsampling.AndroidTileBitmap
import com.github.panpf.zoomimage.subsampling.CacheTileBitmap
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.subsampling.TileBitmapCache

class CoilTileBitmapCache(private val imageLoader: ImageLoader) : TileBitmapCache {

    override fun get(key: String): CacheTileBitmap? {
        return imageLoader.memoryCache
            ?.get(MemoryCache.Key(key))
            ?.let { CoilTileBitmap(key, it) }
    }

    @OptIn(ExperimentalCoilApi::class)
    override fun put(
        key: String,
        tileBitmap: TileBitmap,
        imageUrl: String,
        imageInfo: ImageInfo,
        disallowReuseBitmap: Boolean
    ): CacheTileBitmap? {
        val bitmap = (tileBitmap as AndroidTileBitmap).bitmap ?: return null
        val newCacheValue = MemoryCache.Value(bitmap.asCoilImage())
        val memoryCache = imageLoader.memoryCache ?: return null
        memoryCache[MemoryCache.Key(key)] = newCacheValue
        return CoilTileBitmap(key, newCacheValue)
    }
}