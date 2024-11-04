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

package com.github.panpf.zoomimage.subsampling.internal

import com.github.panpf.zoomimage.subsampling.BitmapTileImage
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.toSkiaRect
import okio.buffer
import okio.use
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Data
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.impl.use
import kotlin.math.ceil

/**
 * Use [Image] to decode the image
 *
 * *Not thread safe*
 *
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.internal.SkiaDecoderHelperTest
 */
class SkiaRegionDecoder(
    override val subsamplingImage: SubsamplingImage,
    val imageSource: ImageSource,
    imageInfo: ImageInfo? = subsamplingImage.imageInfo,
    bytes: ByteArray? = null,
) : RegionDecoder {

    private val bytes: ByteArray by lazy {
        bytes ?: imageSource.openSource().buffer().use { it.readByteArray() }
    }

    private val image: Image by lazy { Image.makeFromEncoded(this.bytes) }

    override val imageInfo: ImageInfo by lazy { imageInfo ?: decodeImageInfo() }

    private fun decodeImageInfo(): ImageInfo {
        val data = Data.makeFromBytes(bytes)
        val encodedImageFormat = Codec.makeFromData(data).use { it.encodedImageFormat }
        val mimeType = "image/${encodedImageFormat.name.lowercase()}"
        return ImageInfo(
            width = image.width,
            height = image.height,
            mimeType = mimeType
        )
    }

    override fun ready() {

    }

    override fun decodeRegion(
        key: String,
        region: IntRectCompat,
        sampleSize: Int
    ): BitmapTileImage {
        // Image will parse exif orientation and does not support closing
        val widthValue = region.width / sampleSize.toDouble()
        val heightValue = region.height / sampleSize.toDouble()
        val bitmapWidth: Int = ceil(widthValue).toInt()
        val bitmapHeight: Int = ceil(heightValue).toInt()
        val bitmap = Bitmap().apply {
            allocN32Pixels(bitmapWidth, bitmapHeight)
        }
        val canvas = Canvas(bitmap)
        canvas.drawImageRect(
            image = image,
            src = region.toSkiaRect(),
            dst = Rect.makeWH(bitmapWidth.toFloat(), bitmapHeight.toFloat())
        )
        return BitmapTileImage(bitmap, key, fromCache = false)
    }

    override fun close() {
        image.close()
    }

    override fun copy(): RegionDecoder {
        return SkiaRegionDecoder(
            subsamplingImage = subsamplingImage,
            imageSource = imageSource,
            imageInfo = imageInfo,
            bytes = bytes,
        )
    }

    override fun toString(): String {
        return "SkiaDecodeHelper(subsamplingImage=$subsamplingImage, imageInfo=$imageInfo)"
    }

    class Factory : RegionDecoder.Factory {

        override suspend fun accept(subsamplingImage: SubsamplingImage): Boolean = true

        override fun checkSupport(mimeType: String): Boolean? = when (mimeType) {
            "image/jpeg", "image/png", "image/webp", "image/bmp" -> true
            "image/svg+xml" -> false
            // TODO Get the skiko version and return false directly.
            //  "image/heic", "image/heif", "image/avif" -> false
            else -> null
        }

        override fun create(
            subsamplingImage: SubsamplingImage,
            imageSource: ImageSource,
        ): SkiaRegionDecoder = SkiaRegionDecoder(
            subsamplingImage = subsamplingImage,
            imageSource = imageSource,
        )
    }
}