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

package com.github.panpf.zoomimage.subsampling.internal

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.WorkerThread
import com.github.panpf.zoomimage.subsampling.AndroidTileBitmap
import com.github.panpf.zoomimage.subsampling.AndroidTileBitmapPool
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.subsampling.TileBitmapPool
import com.github.panpf.zoomimage.subsampling.TileBitmapReuseSpec
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.isEmpty
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex

/**
 * Assist [TileDecoder] to obtain Bitmap from [TileBitmapPool] and set it to BitmapFactory and release Bitmap
 *
 * @see [com.github.panpf.zoomimage.core.test.subsampling.internal.AndroidTileBitmapReuseHelperTest]
 */
class AndroidTileBitmapReuseHelper(
    private val logger: Logger,
    override val spec: TileBitmapReuseSpec
): TileBitmapReuseHelper {

    companion object {
        private val bitmapPoolLock = Mutex()
    }

    private val coroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main + CoroutineExceptionHandler { _, throwable ->
            logger.e(throwable) { "BitmapReuse. CoroutineExceptionHandler: ${throwable.message}" }
        })

    private suspend fun <R> withBitmapPoolLock(block: () -> R): R {
        bitmapPoolLock.lock()
        try {
            return block()
        } finally {
            bitmapPoolLock.unlock()
        }
    }

    @WorkerThread
    fun setInBitmapForRegion(
        options: BitmapFactory.Options,
        regionSize: IntSizeCompat,
        imageMimeType: String?,
        imageSize: IntSizeCompat,
        caller: String,
    ): Boolean {
        val disallowReuseBitmap = spec.disabled
        val tileBitmapPool = spec.tileBitmapPool as AndroidTileBitmapPool?
        if (disallowReuseBitmap || tileBitmapPool == null) {
            return false
        }
        if (regionSize.isEmpty()) {
            logger.e("BitmapReuse. setInBitmapForRegion:$caller. error. regionSize is empty: $regionSize")
            return false
        }
        val config = options.inPreferredConfig
        if (config?.isAndSupportHardware() == true) {
            logger.d { "BitmapReuse. setInBitmapForRegion:$caller. error. inPreferredConfig is HARDWARE does not support inBitmap" }
            return false
        }
        if (!isSupportInBitmapForRegion(imageMimeType)) {
            logger.d {
                "BitmapReuse. setInBitmapForRegion:$caller. error. " +
                        "The current configuration does not support the use of inBitmap in BitmapFactory. " +
                        "imageMimeType=$imageMimeType. " +
                        "For details, please refer to 'DecodeUtils.isSupportInBitmapForRegion()'"
            }
            return false
        }
        val inSampleSize = options.inSampleSize.coerceAtLeast(1)
        val sampledBitmapSize = calculateSampledBitmapSizeForRegion(
            regionSize = regionSize,
            sampleSize = inSampleSize,
            mimeType = imageMimeType,
            imageSize = imageSize
        )
        // BitmapRegionDecoder does not support inMutable, so creates Bitmap
        val width = sampledBitmapSize.width
        val height = sampledBitmapSize.height
        var newCreate = false
        val inBitmap = runBlocking {
            withBitmapPoolLock {
                tileBitmapPool.get(width = width, height = height, config = config)
            }
        } ?: Bitmap.createBitmap(width, height, config).apply { newCreate = true }

        // IllegalArgumentException("Problem decoding into existing bitmap") is thrown when inSampleSize is 0 but inBitmap is not null
        options.inSampleSize = inSampleSize
        options.inBitmap = inBitmap

        val from = if (newCreate) "newCreate" else "fromPool"
        logger.d {
            "BitmapReuse. setInBitmapForRegion:$caller. successful, $from. " +
                    "regionSize=$regionSize, " +
                    "imageMimeType=$imageMimeType, " +
                    "imageSize=$imageSize. " +
                    "inSampleSize=$inSampleSize, " +
                    "inBitmap=${inBitmap.toHexShortString()}"
        }
        return true
    }

    @WorkerThread
    fun getOrCreate(width: Int, height: Int, config: Bitmap.Config, caller: String): Bitmap {
        val disallowReuseBitmap = spec.disabled
        val tileBitmapPool = spec.tileBitmapPool as AndroidTileBitmapPool?
        if (disallowReuseBitmap || tileBitmapPool == null) {
            return Bitmap.createBitmap(width, height, config)
        }

        var newCreate = false
        val bitmap = runBlocking {
            withBitmapPoolLock {
                tileBitmapPool.get(width, height, config)
            }
        } ?: Bitmap.createBitmap(width, height, config).apply { newCreate = true }

        val from = if (newCreate) "newCreate" else "fromPool"
        logger.d {
            "BitmapReuse. getOrCreate:$caller. $from. width=$width, height=$height, config=$config. bitmap=${bitmap.toHexShortString()}"
        }
        return bitmap
    }

    override fun freeTileBitmap(tileBitmap: TileBitmap?, caller: String) {
        val bitmap = (tileBitmap as AndroidTileBitmap?)?.bitmap
        freeBitmap(bitmap, caller)
    }

    fun freeBitmap(bitmap: Bitmap?, caller: String) {
        if (bitmap == null || bitmap.isRecycled) {
            logger.e("BitmapReuse. freeBitmap:$caller. error, bitmap null or recycled. bitmap=${bitmap?.toHexShortString()}")
            return
        }

        /*
         * During the asynchronous execution of freeBitmap, if the waiting time is too long and a large number of new bitmaps are generated,
         * a large number of bitmaps will be accumulated in a short period of time, resulting in out of memory
         *
         * The solution is to synchronize the released Bitmap with the produced Bitmap with a lock,
         * so that a large number of new bitmaps are not generated before the old Bitmap is free
         *
         * This bug is triggered when swiping quickly in a SketchZoomImageView
         *
         * Why was this lock put here? Since this problem is caused by asynchronous freeBitmap,
         * it makes more sense to address it here and avoid contaminating other code
         *
         * Execute freeBitmap asynchronously.
         * The BitmapPoolHelper operation was placed in the worker thread，
         * because the main thread would stall if it had to compete with the worker thread for the synchronization lock
         */
        val disallowReuseBitmap = spec.disabled
        val tileBitmapPool = spec.tileBitmapPool as AndroidTileBitmapPool?
        coroutineScope.launch(Dispatchers.IO) {
            val success = if (!disallowReuseBitmap && tileBitmapPool != null) {
                withBitmapPoolLock {
                    tileBitmapPool.put(bitmap)
                }
            } else {
                false
            }
            if (success) {
                logger.d { "BitmapReuse. freeBitmap$$caller. successful. bitmap=${bitmap.toHexShortString()}" }
            } else {
                bitmap.recycle()
                logger.d { "BitmapReuse. freeBitmap$$caller. failed, execute recycle. bitmap=${bitmap.toHexShortString()}" }
            }
        }
    }
}