package com.github.panpf.zoomimage.core.test.util

import com.github.panpf.tools4j.test.ktx.assertNoThrow
import com.github.panpf.tools4j.test.ktx.assertThrow
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.TransformOriginCompat
import com.github.panpf.zoomimage.util.div
import com.github.panpf.zoomimage.util.lerp
import com.github.panpf.zoomimage.util.minus
import com.github.panpf.zoomimage.util.plus
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.util.toShortString
import org.junit.Assert
import org.junit.Test

class TransformCompatTest {

    @Test
    fun testConstructor() {
        assertNoThrow {
            TransformCompat(
                ScaleFactorCompat(1.3f, 4.7f),
                OffsetCompat(156f, 97f),
            )
        }
        assertThrow(IllegalArgumentException::class) {
            TransformCompat(
                ScaleFactorCompat.Unspecified,
                OffsetCompat(156f, 97f),
            )
        }
        assertThrow(IllegalArgumentException::class) {
            TransformCompat(
                ScaleFactorCompat(1.3f, 4.7f),
                OffsetCompat.Unspecified,
            )
        }
    }

    @Test
    fun testProperties() {
        TransformCompat(
            ScaleFactorCompat(1.3f, 4.7f),
            OffsetCompat(156f, 97f),
        ).apply {
            Assert.assertEquals(1.3f, scaleX)
            Assert.assertEquals(4.7f, scaleY)
            Assert.assertEquals(156f, offsetX)
            Assert.assertEquals(97f, offsetY)
            Assert.assertEquals(0f, rotation)
            Assert.assertEquals(0.0f, scaleOriginX)
            Assert.assertEquals(0.0f, scaleOriginY)
            Assert.assertEquals(0.0f, rotationOriginX)
            Assert.assertEquals(0.0f, rotationOriginY)
        }

        TransformCompat(
            ScaleFactorCompat(4.7f, 1.3f),
            OffsetCompat(97f, 156f),
            rotation = 90f,
            scaleOrigin = TransformOriginCompat(0.3f, 0.7f),
            rotationOrigin = TransformOriginCompat(0.6f, 0.2f),
        ).apply {
            Assert.assertEquals(4.7f, scaleX)
            Assert.assertEquals(1.3f, scaleY)
            Assert.assertEquals(97f, offsetX)
            Assert.assertEquals(156f, offsetY)
            Assert.assertEquals(90f, rotation)
            Assert.assertEquals(0.3f, scaleOriginX)
            Assert.assertEquals(0.7f, scaleOriginY)
            Assert.assertEquals(0.6f, rotationOriginX)
            Assert.assertEquals(0.2f, rotationOriginY)
        }

        TransformCompat(
            ScaleFactorCompat(4.7f, 1.3f),
            OffsetCompat(97f, 156f),
            rotation = 270f,
            scaleOrigin = TransformOriginCompat(0.7f, 0.3f),
            rotationOrigin = TransformOriginCompat(0.2f, 0.6f),
        ).apply {
            Assert.assertEquals(4.7f, scaleX)
            Assert.assertEquals(1.3f, scaleY)
            Assert.assertEquals(97f, offsetX)
            Assert.assertEquals(156f, offsetY)
            Assert.assertEquals(270f, rotation)
            Assert.assertEquals(0.7f, scaleOriginX)
            Assert.assertEquals(0.3f, scaleOriginY)
            Assert.assertEquals(0.2f, rotationOriginX)
            Assert.assertEquals(0.6f, rotationOriginY)
        }
    }

    @Test
    fun testOrigin() {
        TransformCompat.Origin.apply {
            Assert.assertEquals(1f, scaleX)
            Assert.assertEquals(1f, scaleY)
            Assert.assertEquals(0f, offsetX)
            Assert.assertEquals(0f, offsetY)
            Assert.assertEquals(0f, rotation)
            Assert.assertEquals(0.0f, scaleOriginX)
            Assert.assertEquals(0.0f, scaleOriginY)
            Assert.assertEquals(0.0f, rotationOriginX)
            Assert.assertEquals(0.0f, rotationOriginY)
        }

        Assert.assertEquals(TransformCompat.Origin, TransformCompat.Origin)
    }

    @Test
    fun testToString() {
        Assert.assertEquals(
            "(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)",
            TransformCompat.Origin.toShortString()
        )

        Assert.assertEquals(
            "(4.7x1.3,97.0x156.0,270.0,0.7x0.3,0.2x0.6)",
            TransformCompat(
                ScaleFactorCompat(4.7f, 1.3f),
                OffsetCompat(97f, 156f),
                rotation = 270f,
                scaleOrigin = TransformOriginCompat(0.7f, 0.3f),
                rotationOrigin = TransformOriginCompat(0.2f, 0.6f),
            ).toShortString()
        )
    }

    @Test
    fun testTimes() {
        val transformCompat = TransformCompat(
            ScaleFactorCompat(4.7f, 1.3f),
            OffsetCompat(97f, 156f),
            rotation = 270f,
            scaleOrigin = TransformOriginCompat(0.7f, 0.3f),
            rotationOrigin = TransformOriginCompat(0.2f, 0.6f),
        )

        Assert.assertEquals(
            "(4.7x1.3,97.0x156.0,270.0,0.7x0.3,0.2x0.6)",
            transformCompat.toShortString()
        )

        Assert.assertEquals(
            "(9.4x1.95,194.0x234.0,270.0,0.7x0.3,0.2x0.6)",
            (transformCompat * ScaleFactorCompat(2.0f, 1.5f)).toShortString()
        )

        Assert.assertEquals(
            "(7.05x2.6,145.5x312.0,270.0,0.7x0.3,0.2x0.6)",
            (transformCompat * ScaleFactorCompat(1.5f, 2.0f)).toShortString()
        )
    }

    @Test
    fun testDiv() {
        val transformCompat = TransformCompat(
            ScaleFactorCompat(4.7f, 1.3f),
            OffsetCompat(97f, 156f),
            rotation = 270f,
            scaleOrigin = TransformOriginCompat(0.7f, 0.3f),
            rotationOrigin = TransformOriginCompat(0.2f, 0.6f),
        )

        Assert.assertEquals(
            "(4.7x1.3,97.0x156.0,270.0,0.7x0.3,0.2x0.6)",
            transformCompat.toShortString()
        )

        Assert.assertEquals(
            "(2.35x0.87,48.5x104.0,270.0,0.7x0.3,0.2x0.6)",
            (transformCompat / ScaleFactorCompat(2.0f, 1.5f)).toShortString()
        )

        Assert.assertEquals(
            "(3.13x0.65,64.67x78.0,270.0,0.7x0.3,0.2x0.6)",
            (transformCompat / ScaleFactorCompat(1.5f, 2.0f)).toShortString()
        )
    }

    @Test
    fun testPlus() {
        assertThrow(IllegalArgumentException::class) {
            // scale origin is different
            TransformCompat.Origin.copy(
                scale = ScaleFactorCompat(2f),
                scaleOrigin = TransformOriginCompat(0.5f, 0.5f),
            ).plus(
                TransformCompat.Origin.copy(
                    scale = ScaleFactorCompat(3f),
                    scaleOrigin = TransformOriginCompat(0.2f, 0.2f),
                )
            )
        }
        assertNoThrow {
            // start scale default
            TransformCompat.Origin.copy(
                scale = ScaleFactorCompat(1f),
                scaleOrigin = TransformOriginCompat(0.5f, 0.5f),
            ).plus(
                TransformCompat.Origin.copy(
                    scale = ScaleFactorCompat(3f),
                    scaleOrigin = TransformOriginCompat(0.2f, 0.2f),
                )
            ).apply {
                Assert.assertEquals(TransformOriginCompat(0.2f, 0.2f), scaleOrigin)
            }

            // end scale default
            TransformCompat.Origin.copy(
                scale = ScaleFactorCompat(2f),
                scaleOrigin = TransformOriginCompat(0.5f, 0.5f),
            ).plus(
                TransformCompat.Origin.copy(
                    scale = ScaleFactorCompat(1f),
                    scaleOrigin = TransformOriginCompat(0.2f, 0.2f),
                )
            ).apply {
                Assert.assertEquals(TransformOriginCompat(0.5f, 0.5f), scaleOrigin)
            }

            // start and end scale default
            TransformCompat.Origin.copy(
                scale = ScaleFactorCompat(1f),
                scaleOrigin = TransformOriginCompat(0.5f, 0.5f),
            ).plus(
                TransformCompat.Origin.copy(
                    scale = ScaleFactorCompat(1f),
                    scaleOrigin = TransformOriginCompat(0.2f, 0.2f),
                )
            ).apply {
                Assert.assertEquals(TransformOriginCompat(0.5f, 0.5f), scaleOrigin)
            }
        }

        assertThrow(IllegalArgumentException::class) {
            // rotation origin is different
            TransformCompat.Origin.copy(
                rotation = 90f,
                rotationOrigin = TransformOriginCompat(0.5f, 0.5f),
            ).plus(
                TransformCompat.Origin.copy(
                    rotation = 180f,
                    rotationOrigin = TransformOriginCompat(0.2f, 0.2f),
                )
            )
        }
        assertNoThrow {
            // start rotation default
            TransformCompat.Origin.copy(
                rotation = 0f,
                rotationOrigin = TransformOriginCompat(0.5f, 0.5f),
            ).plus(
                TransformCompat.Origin.copy(
                    rotation = 180f,
                    rotationOrigin = TransformOriginCompat(0.2f, 0.2f),
                )
            ).apply {
                Assert.assertEquals(TransformOriginCompat(0.2f, 0.2f), rotationOrigin)
            }

            // end rotation default
            TransformCompat.Origin.copy(
                rotation = 90f,
                rotationOrigin = TransformOriginCompat(0.5f, 0.5f),
            ).plus(
                TransformCompat.Origin.copy(
                    rotation = 0f,
                    rotationOrigin = TransformOriginCompat(0.2f, 0.2f),
                )
            ).apply {
                Assert.assertEquals(TransformOriginCompat(0.5f, 0.5f), rotationOrigin)
            }

            // start and end rotation default
            TransformCompat.Origin.copy(
                rotation = 0f,
                rotationOrigin = TransformOriginCompat(0.5f, 0.5f),
            ).plus(
                TransformCompat.Origin.copy(
                    rotation = 0f,
                    rotationOrigin = TransformOriginCompat(0.2f, 0.2f),
                )
            ).apply {
                Assert.assertEquals(TransformOriginCompat(0.5f, 0.5f), rotationOrigin)
            }
        }

        val transform1 = TransformCompat(
            scale = ScaleFactorCompat(2f),
            scaleOrigin = TransformOriginCompat(0.5f, 0.5f),
            offset = OffsetCompat(199f, 563f),
            rotation = 90f,
            rotationOrigin = TransformOriginCompat(0.2f, 0.2f),
        )
        Assert.assertEquals(
            "(2.0x2.0,199.0x563.0,90.0,0.5x0.5,0.2x0.2)",
            transform1.toShortString()
        )

        val transform2 = TransformCompat(
            scale = ScaleFactorCompat(3f),
            scaleOrigin = TransformOriginCompat(0.5f, 0.5f),
            offset = OffsetCompat(-50f, 1006f),
            rotation = 180f,
            rotationOrigin = TransformOriginCompat(0.2f, 0.2f),
        )
        Assert.assertEquals(
            "(3.0x3.0,-50.0x1006.0,180.0,0.5x0.5,0.2x0.2)",
            transform2.toShortString()
        )

        val transform3 = transform1.plus(transform2)
        Assert.assertEquals(
            "(6.0x6.0,547.0x2695.0,270.0,0.5x0.5,0.2x0.2)",
            transform3.toShortString()
        )
    }

    @Test
    fun testMinus() {
        assertThrow(IllegalArgumentException::class) {
            // scale origin is different
            TransformCompat.Origin.copy(
                scale = ScaleFactorCompat(2f),
                scaleOrigin = TransformOriginCompat(0.5f, 0.5f),
            ).minus(
                TransformCompat.Origin.copy(
                    scale = ScaleFactorCompat(3f),
                    scaleOrigin = TransformOriginCompat(0.2f, 0.2f),
                )
            )
        }
        assertNoThrow {
            // start scale default
            TransformCompat.Origin.copy(
                scale = ScaleFactorCompat(1f),
                scaleOrigin = TransformOriginCompat(0.5f, 0.5f),
            ).minus(
                TransformCompat.Origin.copy(
                    scale = ScaleFactorCompat(3f),
                    scaleOrigin = TransformOriginCompat(0.2f, 0.2f),
                )
            ).apply {
                Assert.assertEquals(TransformOriginCompat(0.2f, 0.2f), scaleOrigin)
            }

            // end scale default
            TransformCompat.Origin.copy(
                scale = ScaleFactorCompat(2f),
                scaleOrigin = TransformOriginCompat(0.5f, 0.5f),
            ).minus(
                TransformCompat.Origin.copy(
                    scale = ScaleFactorCompat(1f),
                    scaleOrigin = TransformOriginCompat(0.2f, 0.2f),
                )
            ).apply {
                Assert.assertEquals(TransformOriginCompat(0.5f, 0.5f), scaleOrigin)
            }

            // start and end scale default
            TransformCompat.Origin.copy(
                scale = ScaleFactorCompat(1f),
                scaleOrigin = TransformOriginCompat(0.5f, 0.5f),
            ).plus(
                TransformCompat.Origin.copy(
                    scale = ScaleFactorCompat(1f),
                    scaleOrigin = TransformOriginCompat(0.2f, 0.2f),
                )
            ).apply {
                Assert.assertEquals(TransformOriginCompat(0.5f, 0.5f), scaleOrigin)
            }
        }

        assertThrow(IllegalArgumentException::class) {
            // rotation origin is different
            TransformCompat.Origin.copy(
                rotation = 90f,
                rotationOrigin = TransformOriginCompat(0.5f, 0.5f),
            ).minus(
                TransformCompat.Origin.copy(
                    rotation = 180f,
                    rotationOrigin = TransformOriginCompat(0.2f, 0.2f),
                )
            )
        }
        assertNoThrow {
            // start rotation default
            TransformCompat.Origin.copy(
                rotation = 0f,
                rotationOrigin = TransformOriginCompat(0.5f, 0.5f),
            ).minus(
                TransformCompat.Origin.copy(
                    rotation = 180f,
                    rotationOrigin = TransformOriginCompat(0.2f, 0.2f),
                )
            ).apply {
                Assert.assertEquals(TransformOriginCompat(0.2f, 0.2f), rotationOrigin)
            }

            // end rotation default
            TransformCompat.Origin.copy(
                rotation = 90f,
                rotationOrigin = TransformOriginCompat(0.5f, 0.5f),
            ).minus(
                TransformCompat.Origin.copy(
                    rotation = 0f,
                    rotationOrigin = TransformOriginCompat(0.2f, 0.2f),
                )
            ).apply {
                Assert.assertEquals(TransformOriginCompat(0.5f, 0.5f), rotationOrigin)
            }

            // start and end rotation default
            TransformCompat.Origin.copy(
                rotation = 0f,
                rotationOrigin = TransformOriginCompat(0.5f, 0.5f),
            ).plus(
                TransformCompat.Origin.copy(
                    rotation = 0f,
                    rotationOrigin = TransformOriginCompat(0.2f, 0.2f),
                )
            ).apply {
                Assert.assertEquals(TransformOriginCompat(0.5f, 0.5f), rotationOrigin)
            }
        }

        val transform1 = TransformCompat(
            scale = ScaleFactorCompat(2f),
            scaleOrigin = TransformOriginCompat(0.5f, 0.5f),
            offset = OffsetCompat(199f, 563f),
            rotation = 90f,
            rotationOrigin = TransformOriginCompat(0.2f, 0.2f),
        )
        Assert.assertEquals(
            "(2.0x2.0,199.0x563.0,90.0,0.5x0.5,0.2x0.2)",
            transform1.toShortString()
        )

        val transform2 = TransformCompat(
            scale = ScaleFactorCompat(3f),
            scaleOrigin = TransformOriginCompat(0.5f, 0.5f),
            offset = OffsetCompat(-50f, 1006f),
            rotation = 180f,
            rotationOrigin = TransformOriginCompat(0.2f, 0.2f),
        )
        Assert.assertEquals(
            "(3.0x3.0,-50.0x1006.0,180.0,0.5x0.5,0.2x0.2)",
            transform2.toShortString()
        )

        val transform3 = transform1.plus(transform2)
        Assert.assertEquals(
            "(6.0x6.0,547.0x2695.0,270.0,0.5x0.5,0.2x0.2)",
            transform3.toShortString()
        )

        val transform4 = transform3.minus(transform1)
        Assert.assertEquals(
            transform2.toShortString(),
            transform4.toShortString()
        )
    }

    @Test
    fun testLerp() {
        assertThrow(IllegalArgumentException::class) {
            // scale origin is different
            lerp(
                start = TransformCompat.Origin.copy(
                    scale = ScaleFactorCompat(2f),
                    scaleOrigin = TransformOriginCompat(0.5f, 0.5f),
                ),
                stop = TransformCompat.Origin.copy(
                    scale = ScaleFactorCompat(3f),
                    scaleOrigin = TransformOriginCompat(0.2f, 0.2f),
                ),
                fraction = 0.5f
            )
        }
        assertNoThrow {
            // start scale default
            lerp(
                start = TransformCompat.Origin.copy(
                    scale = ScaleFactorCompat(1f),
                    scaleOrigin = TransformOriginCompat(0.5f, 0.5f),
                ),
                stop = TransformCompat.Origin.copy(
                    scale = ScaleFactorCompat(3f),
                    scaleOrigin = TransformOriginCompat(0.2f, 0.2f),
                ),
                fraction = 0.5f
            ).apply {
                Assert.assertEquals(TransformOriginCompat(0.2f, 0.2f), scaleOrigin)
            }

            // end scale default
            lerp(
                start = TransformCompat.Origin.copy(
                    scale = ScaleFactorCompat(2f),
                    scaleOrigin = TransformOriginCompat(0.5f, 0.5f),
                ),
                stop = TransformCompat.Origin.copy(
                    scale = ScaleFactorCompat(1f),
                    scaleOrigin = TransformOriginCompat(0.2f, 0.2f),
                ),
                fraction = 0.5f
            ).apply {
                Assert.assertEquals(TransformOriginCompat(0.5f, 0.5f), scaleOrigin)
            }

            // start and end scale default
            lerp(
                start = TransformCompat.Origin.copy(
                    scale = ScaleFactorCompat(1f),
                    scaleOrigin = TransformOriginCompat(0.5f, 0.5f),
                ),
                stop = TransformCompat.Origin.copy(
                    scale = ScaleFactorCompat(1f),
                    scaleOrigin = TransformOriginCompat(0.2f, 0.2f),
                ),
                fraction = 0.5f
            ).apply {
                Assert.assertEquals(TransformOriginCompat(0.5f, 0.5f), scaleOrigin)
            }
        }

        assertThrow(IllegalArgumentException::class) {
            // rotation origin is different
            lerp(
                start = TransformCompat.Origin.copy(
                    rotation = 90f,
                    rotationOrigin = TransformOriginCompat(0.5f, 0.5f),
                ),
                stop = TransformCompat.Origin.copy(
                    rotation = 180f,
                    rotationOrigin = TransformOriginCompat(0.2f, 0.2f),
                ),
                fraction = 0.5f
            )
        }
        assertNoThrow {
            // start rotation default
            lerp(
                start = TransformCompat.Origin.copy(
                    rotation = 0f,
                    rotationOrigin = TransformOriginCompat(0.5f, 0.5f),
                ),
                stop = TransformCompat.Origin.copy(
                    rotation = 180f,
                    rotationOrigin = TransformOriginCompat(0.2f, 0.2f),
                ),
                fraction = 0.5f
            ).apply {
                Assert.assertEquals(TransformOriginCompat(0.2f, 0.2f), rotationOrigin)
            }

            // end rotation default
            lerp(
                start = TransformCompat.Origin.copy(
                    rotation = 90f,
                    rotationOrigin = TransformOriginCompat(0.5f, 0.5f),
                ),
                stop = TransformCompat.Origin.copy(
                    rotation = 0f,
                    rotationOrigin = TransformOriginCompat(0.2f, 0.2f),
                ),
                fraction = 0.5f
            ).apply {
                Assert.assertEquals(TransformOriginCompat(0.5f, 0.5f), rotationOrigin)
            }

            // start and end rotation default
            lerp(
                start = TransformCompat.Origin.copy(
                    rotation = 0f,
                    rotationOrigin = TransformOriginCompat(0.5f, 0.5f),
                ),
                stop = TransformCompat.Origin.copy(
                    rotation = 0f,
                    rotationOrigin = TransformOriginCompat(0.2f, 0.2f),
                ),
                fraction = 0.5f
            ).apply {
                Assert.assertEquals(TransformOriginCompat(0.5f, 0.5f), rotationOrigin)
            }
        }

        val transform1 = TransformCompat(
            scale = ScaleFactorCompat(2f),
            scaleOrigin = TransformOriginCompat(0.5f, 0.5f),
            offset = OffsetCompat(199f, 563f),
            rotation = 90f,
            rotationOrigin = TransformOriginCompat(0.2f, 0.2f),
        )
        Assert.assertEquals(
            "(2.0x2.0,199.0x563.0,90.0,0.5x0.5,0.2x0.2)",
            transform1.toShortString()
        )

        val transform2 = TransformCompat(
            scale = ScaleFactorCompat(3f),
            scaleOrigin = TransformOriginCompat(0.5f, 0.5f),
            offset = OffsetCompat(-50f, 1006f),
            rotation = 180f,
            rotationOrigin = TransformOriginCompat(0.2f, 0.2f),
        )
        Assert.assertEquals(
            "(3.0x3.0,-50.0x1006.0,180.0,0.5x0.5,0.2x0.2)",
            transform2.toShortString()
        )

        lerp(transform1, transform2, fraction = 0f).apply {
            Assert.assertEquals(
                transform1.toShortString(),
                this.toShortString()
            )
        }
        lerp(transform1, transform2, fraction = 0.5f).apply {
            Assert.assertEquals(
                "(2.5x2.5,74.5x784.5,135.0,0.5x0.5,0.2x0.2)",
                this.toShortString()
            )
        }
        lerp(transform1, transform2, fraction = 1f).apply {
            Assert.assertEquals(
                transform2.toShortString(),
                this.toShortString()
            )
        }
    }
}