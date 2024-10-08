package com.github.panpf.zoomimage.core.test.subsampling.internal

import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.zoomimage.core.test.internal.useApply
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.subsampling.TileBitmapCacheSpec
import com.github.panpf.zoomimage.subsampling.TileBitmapReuseSpec
import com.github.panpf.zoomimage.subsampling.TileSnapshot
import com.github.panpf.zoomimage.subsampling.TileState
import com.github.panpf.zoomimage.subsampling.fromAsset
import com.github.panpf.zoomimage.subsampling.internal.AndroidTileBitmapReuseHelper
import com.github.panpf.zoomimage.subsampling.internal.AndroidTileDecoder
import com.github.panpf.zoomimage.subsampling.internal.TileBitmapCacheHelper
import com.github.panpf.zoomimage.subsampling.internal.TileManager
import com.github.panpf.zoomimage.subsampling.internal.calculateImageLoadRect
import com.github.panpf.zoomimage.subsampling.internal.calculatePreferredTileSize
import com.github.panpf.zoomimage.subsampling.internal.decodeImageInfo
import com.github.panpf.zoomimage.subsampling.internal.toIntroString
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import java.io.Closeable

class TileManagerTest {

    @Test
    fun testCompanion() {
        Assert.assertEquals(
            ContinuousTransformType.SCALE or ContinuousTransformType.OFFSET or ContinuousTransformType.LOCATE,
            TileManager.DefaultPausedContinuousTransformType
        )
    }

    @Test
    fun testPausedContinuousTransformType() {
        TileManagerHolder().useApply {
            Assert.assertEquals(
                TileManager.DefaultPausedContinuousTransformType,
                tileManager.pausedContinuousTransformType
            )
            listOf(
                ContinuousTransformType.SCALE to -2,
                ContinuousTransformType.OFFSET to -2,
                ContinuousTransformType.LOCATE to -2,
                ContinuousTransformType.GESTURE to 0,
                ContinuousTransformType.FLING to 0,
            ).forEach {
                Assert.assertEquals(
                    "continuousTransformType=${it.first}",
                    it.second,
                    refreshTiles(continuousTransformType = it.first)
                )
            }

            tileManager.pausedContinuousTransformType = 0
            listOf(
                ContinuousTransformType.SCALE to 0,
                ContinuousTransformType.OFFSET to 0,
                ContinuousTransformType.LOCATE to 0,
                ContinuousTransformType.GESTURE to 0,
                ContinuousTransformType.FLING to 0,
            ).forEach {
                Assert.assertEquals(
                    "continuousTransformType=${it.first}",
                    it.second,
                    refreshTiles(continuousTransformType = it.first)
                )
            }

            tileManager.pausedContinuousTransformType = ContinuousTransformType.SCALE
            listOf(
                ContinuousTransformType.SCALE to -2,
                ContinuousTransformType.OFFSET to 0,
                ContinuousTransformType.LOCATE to 0,
                ContinuousTransformType.GESTURE to 0,
                ContinuousTransformType.FLING to 0,
            ).forEach {
                Assert.assertEquals(
                    "continuousTransformType=${it.first}",
                    it.second,
                    refreshTiles(continuousTransformType = it.first)
                )
            }

            tileManager.pausedContinuousTransformType = ContinuousTransformType.SCALE or
                    ContinuousTransformType.OFFSET
            listOf(
                ContinuousTransformType.SCALE to -2,
                ContinuousTransformType.OFFSET to -2,
                ContinuousTransformType.LOCATE to 0,
                ContinuousTransformType.GESTURE to 0,
                ContinuousTransformType.FLING to 0,
            ).forEach {
                Assert.assertEquals(
                    "continuousTransformType=${it.first}",
                    it.second,
                    refreshTiles(continuousTransformType = it.first)
                )
            }

            tileManager.pausedContinuousTransformType = ContinuousTransformType.SCALE or
                    ContinuousTransformType.OFFSET or
                    ContinuousTransformType.LOCATE
            listOf(
                ContinuousTransformType.SCALE to -2,
                ContinuousTransformType.OFFSET to -2,
                ContinuousTransformType.LOCATE to -2,
                ContinuousTransformType.GESTURE to 0,
                ContinuousTransformType.FLING to 0,
            ).forEach {
                Assert.assertEquals(
                    "continuousTransformType=${it.first}",
                    it.second,
                    refreshTiles(continuousTransformType = it.first)
                )
            }

            tileManager.pausedContinuousTransformType = ContinuousTransformType.SCALE or
                    ContinuousTransformType.OFFSET or
                    ContinuousTransformType.LOCATE or
                    ContinuousTransformType.GESTURE
            listOf(
                ContinuousTransformType.SCALE to -2,
                ContinuousTransformType.OFFSET to -2,
                ContinuousTransformType.LOCATE to -2,
                ContinuousTransformType.GESTURE to -2,
                ContinuousTransformType.FLING to 0,
            ).forEach {
                Assert.assertEquals(
                    "continuousTransformType=${it.first}",
                    it.second,
                    refreshTiles(continuousTransformType = it.first)
                )
            }

            tileManager.pausedContinuousTransformType = ContinuousTransformType.SCALE or
                    ContinuousTransformType.OFFSET or
                    ContinuousTransformType.LOCATE or
                    ContinuousTransformType.GESTURE or
                    ContinuousTransformType.FLING
            listOf(
                ContinuousTransformType.SCALE to -2,
                ContinuousTransformType.OFFSET to -2,
                ContinuousTransformType.LOCATE to -2,
                ContinuousTransformType.GESTURE to -2,
                ContinuousTransformType.FLING to -2,
            ).forEach {
                Assert.assertEquals(
                    "continuousTransformType=${it.first}",
                    it.second,
                    refreshTiles(continuousTransformType = it.first)
                )
            }
        }
    }

    @Test
    fun testDisabledBackgroundTiles() {
        TileManagerHolder().useApply {
            Assert.assertEquals(false, tileManager.disabledBackgroundTiles)
            Assert.assertEquals(0, tileManager.sampleSize)
            Assert.assertEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)

            Assert.assertEquals(0, refreshTiles(scale = 3f))
            Assert.assertEquals(8, tileManager.sampleSize)
            Thread.sleep(1000)
            Assert.assertEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)

            Assert.assertEquals(0, refreshTiles(scale = 6f))
            Assert.assertEquals(4, tileManager.sampleSize)
            Thread.sleep(50)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)
            Thread.sleep(100)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)
            Thread.sleep(1000)
            Assert.assertEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)

            Assert.assertEquals(0, refreshTiles(scale = 3f))
            Assert.assertEquals(8, tileManager.sampleSize)
            Thread.sleep(50)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)
            Thread.sleep(100)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)
            Thread.sleep(1000)
            Assert.assertEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)

            tileManager.disabledBackgroundTiles = true
            Assert.assertEquals(true, tileManager.disabledBackgroundTiles)

            Assert.assertEquals(0, refreshTiles(scale = 6f))
            Assert.assertEquals(4, tileManager.sampleSize)
            Thread.sleep(50)
            Assert.assertEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)
            Thread.sleep(100)
            Assert.assertEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)
            Thread.sleep(1000)
            Assert.assertEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)

            Assert.assertEquals(0, refreshTiles(scale = 3f))
            Assert.assertEquals(8, tileManager.sampleSize)
            Thread.sleep(50)
            Assert.assertEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)
            Thread.sleep(100)
            Assert.assertEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)
            Thread.sleep(1000)
            Assert.assertEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)
        }
    }

    @Test
    fun testTileAnimationSpec() {
        TileManagerHolder().useApply {
            Assert.assertEquals(TileAnimationSpec.Default, tileManager.tileAnimationSpec)

            Assert.assertTrue(foregroundTilesChangedList.size == 0)
            Assert.assertEquals(0, refreshTiles(scale = 3f))
            Thread.sleep(2000)
            Assert.assertTrue(foregroundTilesChangedList.any { it.any { tile -> tile.alpha < 255 } })

            foregroundTilesChangedList.clear()
            Assert.assertTrue(foregroundTilesChangedList.size == 0)
            Assert.assertEquals(0, refreshTiles(scale = 6f))
            Thread.sleep(2000)
            Assert.assertTrue(foregroundTilesChangedList.any { it.any { tile -> tile.alpha < 255 } })

            tileManager.tileAnimationSpec = TileAnimationSpec.None
            Assert.assertEquals(TileAnimationSpec.None, tileManager.tileAnimationSpec)

            foregroundTilesChangedList.clear()
            Assert.assertTrue(foregroundTilesChangedList.size == 0)
            Assert.assertEquals(0, refreshTiles(scale = 3f))
            Thread.sleep(2000)
            Assert.assertTrue(foregroundTilesChangedList.all { it.all { tile -> tile.alpha == 255 } })

            foregroundTilesChangedList.clear()
            Assert.assertTrue(foregroundTilesChangedList.size == 0)
            Assert.assertEquals(0, refreshTiles(scale = 6f))
            Thread.sleep(2000)
            Assert.assertTrue(foregroundTilesChangedList.all { it.all { tile -> tile.alpha == 255 } })
        }
    }

    @Test
    fun testSortedTileGridMap() {
        TileManagerHolder().useApply {
            Assert.assertEquals(
                "[16:1:1x1,8:2:1x2,4:4:1x4,2:7:1x7,1:26:2x13]",
                tileManager.sortedTileGridMap.toIntroString()
            )
        }
    }

    @Test
    fun testSampleSize() {
        TileManagerHolder().useApply {
            listOf(
                1f to 0,
                3f to 8,
                6f to 4,
                12f to 2,
                24f to 1,
            ).forEach { (scale, expectedSampleSize) ->
                refreshTiles(scale = scale)
                Assert.assertEquals("scale=$scale", expectedSampleSize, tileManager.sampleSize)
            }
        }
    }

    @Test
    fun testImageLoadRect() {
        TileManagerHolder().useApply {
            val widthSpace = contentSize.width / 4
            val heightSpace = contentSize.height / 4
            val contentVisibleRect1 = IntRectCompat(
                left = widthSpace,
                top = heightSpace,
                right = contentSize.width - widthSpace,
                bottom = contentSize.height - heightSpace,
            )
            val contentVisibleRect2 = IntRectCompat(
                left = 0,
                top = 0,
                right = contentSize.width - widthSpace * 2,
                bottom = contentSize.height - heightSpace * 2,
            )
            val contentVisibleRect3 = IntRectCompat(
                left = contentSize.width - widthSpace * 2,
                top = contentSize.height - heightSpace * 2,
                right = contentSize.width,
                bottom = contentSize.height,
            )
            listOf(
                contentVisibleRect1 to calculateImageLoadRect(
                    imageSize = imageInfo.size,
                    contentSize = contentSize,
                    preferredTileSize = preferredTileSize,
                    contentVisibleRect = contentVisibleRect1
                ),
                contentVisibleRect2 to calculateImageLoadRect(
                    imageSize = imageInfo.size,
                    contentSize = contentSize,
                    preferredTileSize = preferredTileSize,
                    contentVisibleRect = contentVisibleRect2
                ),
                contentVisibleRect3 to calculateImageLoadRect(
                    imageSize = imageInfo.size,
                    contentSize = contentSize,
                    preferredTileSize = preferredTileSize,
                    contentVisibleRect = contentVisibleRect3
                ),
            ).forEach { (contentVisibleRect, expectedImageLoadRect) ->
                refreshTiles(contentVisibleRect = contentVisibleRect)
                Assert.assertEquals(
                    "contentVisibleRect=$contentVisibleRect",
                    expectedImageLoadRect,
                    tileManager.imageLoadRect
                )
            }
        }
    }

    @Test
    fun testRefreshTiles() {
        // rotation
        TileManagerHolder().useApply {
            listOf(-90, 0, 90, 180, 270, 360, 450)
                .forEach { rotation ->
                    Assert.assertEquals(
                        /* message = */ "rotation=$rotation",
                        /* expected = */ 0,
                        /* actual = */ refreshTiles(rotation = rotation)
                    )
                }
            listOf(-89, -91, -1, 1, 89, 91, 179, 181, 269, 271, 359, 361, 449, 451)
                .forEach { rotation ->
                    Assert.assertEquals(
                        /* message = */ "rotation=$rotation",
                        /* expected = */ -1,
                        /* actual = */ refreshTiles(rotation = rotation)
                    )
                }
        }

        // scale and contentVisibleRect
        TileManagerHolder("sample_huge_card.jpg").useApply {
            val tileManager = tileManager
            val contentSize = contentSize
            Assert.assertEquals(0, tileManager.sampleSize)
            Assert.assertEquals(IntRectCompat.Zero, tileManager.imageLoadRect)
            Assert.assertEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)

            val contentVisibleSize = IntSizeCompat(
                width = contentSize.width / 2,
                height = contentSize.height / 3,
            )
            val widthSpace = (contentSize.width - contentVisibleSize.width) / 2
            val heightSpace = (contentSize.height - contentVisibleSize.height) / 2
            val contentVisibleRect1 = IntRectCompat(
                left = 0,
                top = 0,
                right = contentVisibleSize.width,
                bottom = contentVisibleSize.height,
            )
            val contentVisibleRect2 = IntRectCompat(
                left = widthSpace,
                top = heightSpace,
                right = widthSpace + contentVisibleSize.width,
                bottom = heightSpace + contentVisibleSize.height,
            )
            val contentVisibleRect3 = IntRectCompat(
                left = contentVisibleSize.width - widthSpace,
                top = contentVisibleSize.height - heightSpace,
                right = contentVisibleSize.width - widthSpace + contentVisibleSize.width,
                bottom = contentVisibleSize.height - heightSpace + contentVisibleSize.height,
            )

            refreshTiles(scale = 8f, contentVisibleRect = contentVisibleRect1)
            Thread.sleep(2000)
            Assert.assertEquals(4, tileManager.sampleSize)
            Assert.assertEquals(IntRectCompat(0, 0, 4049, 2370), tileManager.imageLoadRect)
            Assert.assertEquals(
                "[0x0, 1x0, 2x0]",
                tileManager.foregroundTiles
                    .filter { it.state == TileState.STATE_LOADED }
                    .map { it.coordinate.toShortString() }
                    .toString()
            )

            refreshTiles(scale = 8f, contentVisibleRect = contentVisibleRect2)
            Thread.sleep(2000)
            Assert.assertEquals(4, tileManager.sampleSize)
            Assert.assertEquals(IntRectCompat(1619, 1409, 5938, 4260), tileManager.imageLoadRect)
            Assert.assertEquals(
                "[0x0, 1x0, 2x0, 3x0, 0x1, 1x1, 2x1, 3x1]",
                tileManager.foregroundTiles
                    .filter { it.state == TileState.STATE_LOADED }
                    .map { it.coordinate.toShortString() }
                    .toString()
            )

            refreshTiles(scale = 8f, contentVisibleRect = contentVisibleRect3)
            Thread.sleep(2000)
            Assert.assertEquals(4, tileManager.sampleSize)
            Assert.assertEquals(IntRectCompat(1619, 0, 5938, 2370), tileManager.imageLoadRect)
            Assert.assertEquals(
                "[0x0, 1x0, 2x0, 3x0]",
                tileManager.foregroundTiles
                    .filter { it.state == TileState.STATE_LOADED }
                    .map { it.coordinate.toShortString() }
                    .toString()
            )

            refreshTiles(scale = 14f, contentVisibleRect = contentVisibleRect1)
            Thread.sleep(2000)
            Assert.assertEquals(2, tileManager.sampleSize)
            Assert.assertEquals(IntRectCompat(0, 0, 4049, 2370), tileManager.imageLoadRect)
            Assert.assertEquals(
                "[0x0, 1x0, 2x0, 3x0, 0x1, 1x1, 2x1, 3x1]",
                tileManager.foregroundTiles
                    .filter { it.state == TileState.STATE_LOADED }
                    .map { it.coordinate.toShortString() }
                    .toString()
            )

            refreshTiles(scale = 14f, contentVisibleRect = contentVisibleRect2)
            Thread.sleep(2000)
            Assert.assertEquals(2, tileManager.sampleSize)
            Assert.assertEquals(IntRectCompat(1619, 1409, 5938, 4260), tileManager.imageLoadRect)
            Assert.assertEquals(
                "[1x0, 2x0, 3x0, 4x0, 5x0, 1x1, 2x1, 3x1, 4x1, 5x1, 1x2, 2x2, 3x2, 4x2, 5x2]",
                tileManager.foregroundTiles
                    .filter { it.state == TileState.STATE_LOADED }
                    .map { it.coordinate.toShortString() }
                    .toString()
            )

            refreshTiles(scale = 14f, contentVisibleRect = contentVisibleRect3)
            Thread.sleep(2000)
            Assert.assertEquals(2, tileManager.sampleSize)
            Assert.assertEquals(IntRectCompat(1619, 0, 5938, 2370), tileManager.imageLoadRect)
            Assert.assertEquals(
                "[1x0, 2x0, 3x0, 4x0, 5x0, 1x1, 2x1, 3x1, 4x1, 5x1]",
                tileManager.foregroundTiles
                    .filter { it.state == TileState.STATE_LOADED }
                    .map { it.coordinate.toShortString() }
                    .toString()
            )
        }
    }

    @Test
    fun testClean() {
        TileManagerHolder().useApply {
            Assert.assertEquals(0, refreshTiles(scale = 3f))
            Thread.sleep(1000)
            Assert.assertTrue(tileManager.foregroundTiles.all { it.state == TileState.STATE_LOADED })

            runBlocking(Dispatchers.Main) {
                tileManager.clean("testClean")
            }
            Thread.sleep(1000)
            Assert.assertTrue(tileManager.foregroundTiles.all { it.state == TileState.STATE_NONE })

            Assert.assertEquals(0, refreshTiles(scale = 6f))
            Thread.sleep(1000)
            Assert.assertTrue(tileManager.foregroundTiles.all { it.state == TileState.STATE_LOADED })

            runBlocking(Dispatchers.Main) {
                tileManager.clean("testClean")
            }
            Thread.sleep(1000)
            Assert.assertTrue(tileManager.foregroundTiles.all { it.state == TileState.STATE_NONE })
        }
    }

    private class TileManagerHolder(assetName: String = "sample_long_comic.jpg") : Closeable {
        private val context = InstrumentationRegistry.getInstrumentation().context
        private val logger = Logger("Test").apply {
            level = Logger.DEBUG
        }
        private val imageSource = ImageSource.fromAsset(context, assetName)
        val imageInfo = imageSource.decodeImageInfo().getOrThrow()
        private val tileBitmapCacheHelper = TileBitmapCacheHelper(logger, TileBitmapCacheSpec())
        private val tileBitmapReuseHelper =
            AndroidTileBitmapReuseHelper(logger, TileBitmapReuseSpec())
        val containerSize = IntSizeCompat(1080, 1920)
        val preferredTileSize = calculatePreferredTileSize(containerSize)
        val contentSize = imageInfo.size / 32
        val tileDecoder =
            AndroidTileDecoder(logger, imageSource, imageInfo, null, tileBitmapReuseHelper)
        val backgroundTilesChangedList = mutableListOf<List<TileSnapshot>>()
        val foregroundTilesChangedList = mutableListOf<List<TileSnapshot>>()
        val sampleSizeChangedList = mutableListOf<Int>()
        val imageLoadChangedList = mutableListOf<IntRectCompat>()
        val tileManager = TileManager(
            logger = logger,
            tileDecoder = tileDecoder,
            tileBitmapConvertor = null,
            tileBitmapCacheHelper = tileBitmapCacheHelper,
            tileBitmapReuseHelper = tileBitmapReuseHelper,
            imageSource = imageSource,
            imageInfo = imageInfo,
            preferredTileSize = preferredTileSize,
            contentSize = contentSize,
            onTileChanged = {
                backgroundTilesChangedList.add(it.backgroundTiles)
                foregroundTilesChangedList.add(it.foregroundTiles)
            },
            onSampleSizeChanged = {
                sampleSizeChangedList.add(it.sampleSize)
            },
            onImageLoadRectChanged = {
                imageLoadChangedList.add(it.imageLoadRect)
            },
        )

        override fun close() {
            runBlocking(Dispatchers.Main) {
                tileDecoder.destroy("close")
            }
        }

        val contentVisibleRect = IntRectCompat(
            left = contentSize.width / 4,
            top = contentSize.height / 4,
            right = contentSize.width - contentSize.width / 4,
            bottom = contentSize.height - contentSize.height / 4,
        )
        val scale = 3f
        val rotation = 0
        val continuousTransformType = ContinuousTransformType.NONE

        fun refreshTiles(
            scale: Float = this.scale,
            contentVisibleRect: IntRectCompat = this.contentVisibleRect,
            rotation: Int = this.rotation,
            continuousTransformType: Int = this.continuousTransformType
        ): Int {
            return runBlocking(Dispatchers.Main) {
                tileManager.refreshTiles(
                    scale = scale,
                    contentVisibleRect = contentVisibleRect,
                    rotation = rotation,
                    continuousTransformType = continuousTransformType,
                    caller = "test"
                )
            }
        }
    }
}