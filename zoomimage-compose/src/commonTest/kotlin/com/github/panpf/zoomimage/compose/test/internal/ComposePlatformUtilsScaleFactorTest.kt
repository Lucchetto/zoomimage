package com.github.panpf.zoomimage.compose.test.internal

import androidx.compose.ui.layout.ScaleFactor
import com.github.panpf.zoomimage.compose.internal.Origin
import com.github.panpf.zoomimage.compose.internal.ScaleFactor
import com.github.panpf.zoomimage.compose.internal.div
import com.github.panpf.zoomimage.compose.internal.times
import com.github.panpf.zoomimage.compose.internal.toShortString
import org.junit.Assert
import org.junit.Test

class ComposePlatformUtilsScaleFactorTest {

    @Test
    fun testToShortString() {
        Assert.assertEquals(
            "10.34x9.77",
            ScaleFactor(10.342f, 9.765f).toShortString()
        )
        Assert.assertEquals(
            "9.77x10.34",
            ScaleFactor(9.765f, 10.342f).toShortString()
        )
    }

    @Test
    fun testCreate() {
        Assert.assertEquals(
            "3.3x3.3",
            ScaleFactor(3.3f).toShortString()
        )
        Assert.assertEquals(
            "5.3x5.3",
            ScaleFactor(5.3f).toShortString()
        )
    }

    @Test
    fun testOrigin() {
        Assert.assertEquals(
            "1.0x1.0",
            ScaleFactor.Origin.toShortString()
        )
        Assert.assertEquals(
            ScaleFactor.Origin,
            ScaleFactor.Origin
        )
    }

    @Test
    fun testTimes() {
        Assert.assertEquals(
            "35.97x53.35",
            (ScaleFactor(10.9f, 9.7f) * ScaleFactor(3.3f, 5.5f)).toShortString()
        )
        Assert.assertEquals(
            "59.95x32.01",
            (ScaleFactor(10.9f, 9.7f) * ScaleFactor(5.5f, 3.3f)).toShortString()
        )
    }

    @Test
    fun testDiv() {
        Assert.assertEquals(
            "10.9x9.7",
            (ScaleFactor(35.97f, 53.35f) / ScaleFactor(3.3f, 5.5f)).toShortString()
        )
        Assert.assertEquals(
            "6.54x16.17",
            (ScaleFactor(35.97f, 53.35f) / ScaleFactor(5.5f, 3.3f)).toShortString()
        )
    }
}