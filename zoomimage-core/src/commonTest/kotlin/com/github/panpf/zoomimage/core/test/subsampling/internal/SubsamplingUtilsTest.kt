package com.github.panpf.zoomimage.core.test.subsampling.internal

import com.github.panpf.zoomimage.subsampling.internal.calculatePreferredTileSize
import com.github.panpf.zoomimage.subsampling.internal.calculateTileGridMap
import com.github.panpf.zoomimage.subsampling.internal.canUseSubsamplingByAspectRatio
import com.github.panpf.zoomimage.subsampling.internal.toIntroString
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.div
import org.junit.Assert
import org.junit.Test

class SubsamplingUtilsTest {

    @Test
    fun testCanUseSubsamplingByAspectRatio() {
        val imageSize = IntSizeCompat(1000, 2000)

        Assert.assertTrue(
            canUseSubsamplingByAspectRatio(imageSize, imageSize / ScaleFactorCompat(17f, 17f))
        )
        Assert.assertTrue(
            canUseSubsamplingByAspectRatio(imageSize, imageSize / ScaleFactorCompat(17f, 16.5f))
        )
        Assert.assertTrue(
            canUseSubsamplingByAspectRatio(imageSize, imageSize / ScaleFactorCompat(17.3f, 17f))
        )
        Assert.assertFalse(
            canUseSubsamplingByAspectRatio(imageSize, imageSize / ScaleFactorCompat(17f, 16.4f))
        )
        Assert.assertFalse(
            canUseSubsamplingByAspectRatio(imageSize, imageSize / ScaleFactorCompat(17.6f, 17f))
        )
        Assert.assertTrue(
            canUseSubsamplingByAspectRatio(
                imageSize,
                imageSize / ScaleFactorCompat(17f, 16.4f),
                minDifference = 0.8f
            )
        )
        Assert.assertTrue(
            canUseSubsamplingByAspectRatio(
                imageSize,
                imageSize / ScaleFactorCompat(17.6f, 17f),
                minDifference = 0.8f
            )
        )
    }

    @Test
    fun testToIntroString() {
        val imageSize = IntSizeCompat(8000, 8000)
        val containerSize = IntSizeCompat(1080, 1920)
        val preferredTileSize = containerSize / 2
        calculateTileGridMap(
            imageSize = imageSize,
            preferredTileSize = preferredTileSize,
        ).apply {
            Assert.assertEquals("[16:1:1x1,8:4:2x2,4:12:4x3,2:40:8x5,1:135:15x9]", toIntroString())
        }
    }

    @Test
    fun testCalculatePreferredTileSize() {
        Assert.assertEquals(
            /* expected = */ IntSizeCompat(1080, 1920) / 2,
            /* actual = */ calculatePreferredTileSize(IntSizeCompat(1080, 1920))
        )

        Assert.assertEquals(
            /* expected = */ IntSizeCompat(1000, 2000) / 2,
            /* actual = */ calculatePreferredTileSize(IntSizeCompat(1000, 2000))
        )
    }
}