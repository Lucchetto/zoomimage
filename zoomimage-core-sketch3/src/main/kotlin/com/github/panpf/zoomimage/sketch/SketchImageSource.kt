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

package com.github.panpf.zoomimage.sketch

import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.cache.CachePolicy
import com.github.panpf.sketch.datasource.BasedStreamDataSource
import com.github.panpf.sketch.request.Depth
import com.github.panpf.sketch.request.LoadRequest
import com.github.panpf.zoomimage.subsampling.ImageSource
import okio.Source
import okio.source

/**
 * [ImageSource] implementation that uses Sketch to load images
 *
 * @see com.github.panpf.zoomimage.core.sketch3.test.SketchImageSourceTest
 */
class SketchImageSource(
    val imageUri: String,
    val dataSource: BasedStreamDataSource,
) : ImageSource {

    override val key: String = imageUri

    override fun openSource(): Source {
        return dataSource.newInputStream().source()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as SketchImageSource
        return imageUri == other.imageUri
    }

    override fun hashCode(): Int {
        var result = imageUri.hashCode()
        result = 31 * result + dataSource.hashCode()
        return result
    }

    override fun toString(): String {
        return "SketchImageSource('$imageUri')"
    }

    class Factory(
        val sketch: Sketch,
        val imageUri: String,
    ) : ImageSource.Factory {

        override val key: String = imageUri

        override suspend fun create(): SketchImageSource {
            val request = LoadRequest(sketch.context, imageUri) {
                downloadCachePolicy(CachePolicy.ENABLED)
                depth(Depth.NETWORK)
            }
            val fetcher = sketch.components.newFetcherOrThrow(request)
            val fetchResult = fetcher.fetch().getOrThrow()
            val dataSource = fetchResult.dataSource
            if (dataSource !is BasedStreamDataSource) {
                throw IllegalStateException("DataSource is not BasedStreamDataSource. imageUri='$imageUri'")
            }
            return SketchImageSource(imageUri, dataSource)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as Factory
            if (sketch != other.sketch) return false
            if (imageUri != other.imageUri) return false
            return true
        }

        override fun hashCode(): Int {
            var result = sketch.hashCode()
            result = 31 * result + imageUri.hashCode()
            return result
        }

        override fun toString(): String {
            return "SketchImageSource.Factory('$imageUri')"
        }
    }
}