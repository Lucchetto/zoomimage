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

package com.github.panpf.zoomimage.view.internal

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView.ScaleType
import androidx.core.view.ViewCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.times
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt


internal fun requiredMainThread() {
    check(Looper.myLooper() == Looper.getMainLooper()) {
        "This method must be executed in the UI thread"
    }
}

internal fun requiredWorkThread() {
    check(Looper.myLooper() != Looper.getMainLooper()) {
        "This method must be executed in the work thread"
    }
}

internal val View.isAttachedToWindowCompat: Boolean
    get() = ViewCompat.isAttachedToWindow(this)

internal fun getPointerIndex(action: Int): Int {
    return action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
}

val ZeroRect = Rect(0, 0, 0, 0)

internal fun IntSizeCompat.times(scale: Float): IntSizeCompat =
    IntSizeCompat(
        (this.width * scale).roundToInt(),
        (this.height * scale).roundToInt()
    )

internal fun Rect.scale(scale: Float): Rect {
    return Rect(
        left = (left * scale).roundToInt(),
        top = (top * scale).roundToInt(),
        right = (right * scale).roundToInt(),
        bottom = (bottom * scale).roundToInt()
    )
}

fun Rect(left: Int, top: Int, right: Int, bottom: Int): Rect {
    return Rect(left, top, right, bottom)
}

internal fun Rect.toIntRectCompat(): IntRectCompat {
    return IntRectCompat(left, top, right, bottom)
}


internal fun ScaleType.computeScaleFactor(
    srcSize: IntSizeCompat,
    dstSize: IntSizeCompat
): ScaleFactorCompat {
    val widthScale = dstSize.width / srcSize.width.toFloat()
    val heightScale = dstSize.height / srcSize.height.toFloat()
    val fillMaxDimension = max(widthScale, heightScale)
    val fillMinDimension = min(widthScale, heightScale)
    return when (this) {
        ScaleType.CENTER -> ScaleFactorCompat(scaleX = 1.0f, scaleY = 1.0f)

        ScaleType.CENTER_CROP -> {
            ScaleFactorCompat(scaleX = fillMaxDimension, scaleY = fillMaxDimension)
        }

        ScaleType.CENTER_INSIDE -> {
            if (srcSize.width <= dstSize.width && srcSize.height <= dstSize.height) {
                ScaleFactorCompat(scaleX = 1.0f, scaleY = 1.0f)
            } else {
                ScaleFactorCompat(scaleX = fillMinDimension, scaleY = fillMinDimension)
            }
        }

        ScaleType.FIT_START,
        ScaleType.FIT_CENTER,
        ScaleType.FIT_END -> {
            ScaleFactorCompat(scaleX = fillMinDimension, scaleY = fillMinDimension)
        }

        ScaleType.FIT_XY -> {
            ScaleFactorCompat(scaleX = widthScale, scaleY = heightScale)
        }

        ScaleType.MATRIX -> ScaleFactorCompat(1.0f, 1.0f)
        else -> ScaleFactorCompat(scaleX = 1.0f, scaleY = 1.0f)
    }
}

internal fun ScaleType.isStart(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
    return this == ScaleType.MATRIX
            || this == ScaleType.FIT_XY
            || (this == ScaleType.FIT_START && scaledSrcSize.width < dstSize.width)
}

internal fun ScaleType.isHorizontalCenter(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
    return this == ScaleType.CENTER
            || this == ScaleType.CENTER_CROP
            || this == ScaleType.CENTER_INSIDE
            || this == ScaleType.FIT_CENTER
            || (this == ScaleType.FIT_START && scaledSrcSize.width >= dstSize.width)
            || (this == ScaleType.FIT_END && scaledSrcSize.width >= dstSize.width)
}

internal fun ScaleType.isCenter(): Boolean =
    this == ScaleType.CENTER
            || this == ScaleType.CENTER_CROP
            || this == ScaleType.CENTER_INSIDE
            || this == ScaleType.FIT_CENTER

internal fun ScaleType.isEnd(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
    return this == ScaleType.FIT_END && scaledSrcSize.width < dstSize.width
}

internal fun ScaleType.isTop(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
    return this == ScaleType.MATRIX
            || this == ScaleType.FIT_XY
            || (this == ScaleType.FIT_START && scaledSrcSize.height < dstSize.height)
}

internal fun ScaleType.isVerticalCenter(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
    return this == ScaleType.CENTER
            || this == ScaleType.CENTER_CROP
            || this == ScaleType.CENTER_INSIDE
            || this == ScaleType.FIT_CENTER
            || (this == ScaleType.FIT_START && scaledSrcSize.height >= dstSize.height)
            || (this == ScaleType.FIT_END && scaledSrcSize.height >= dstSize.height)
}

internal fun ScaleType.isBottom(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
    return this == ScaleType.FIT_END && scaledSrcSize.height < dstSize.height
}

private val matrixValuesLocal = ThreadLocal<FloatArray>()
private val Matrix.localValues: FloatArray
    get() {
        val values = matrixValuesLocal.get()
            ?: FloatArray(9).apply { matrixValuesLocal.set(this) }
        getValues(values)
        return values
    }

internal fun Matrix.getScale(): ScaleFactorCompat {
    val values = localValues

    val scaleX: Float = values[Matrix.MSCALE_X]
    val skewY: Float = values[Matrix.MSKEW_Y]
    val scaleX1 = sqrt(scaleX.toDouble().pow(2.0) + skewY.toDouble().pow(2.0)).toFloat()
    val scaleY: Float = values[Matrix.MSCALE_Y]
    val skewX: Float = values[Matrix.MSKEW_X]
    val scaleY1 = sqrt(scaleY.toDouble().pow(2.0) + skewX.toDouble().pow(2.0)).toFloat()
    @Suppress("UnnecessaryVariable") val scaleFactorCompat =
        ScaleFactorCompat(scaleX = scaleX1, scaleY = scaleY1)
    return scaleFactorCompat
}

internal fun Matrix.getTranslation(): OffsetCompat {
    val values = localValues
    @Suppress("UnnecessaryVariable") val offsetCompat = OffsetCompat(
        x = values[Matrix.MTRANS_X],
        y = values[Matrix.MTRANS_Y]
    )
    return offsetCompat
}

internal fun Matrix.getRotation(): Int {
    val values = localValues
    val skewX: Float = values[Matrix.MSKEW_X]
    val scaleX: Float = values[Matrix.MSCALE_X]
    val degrees = (atan2(skewX.toDouble(), scaleX.toDouble()) * (180 / Math.PI)).roundToInt()
    val rotation = when {
        degrees < 0 -> abs(degrees)
        degrees > 0 -> 360 - degrees
        else -> 0
    }
    return rotation
}

internal fun Context?.findLifecycle(): Lifecycle? {
    var context: Context? = this
    while (true) {
        when (context) {
            is LifecycleOwner -> return context.lifecycle
            is ContextWrapper -> context = context.baseContext
            else -> return null
        }
    }
}

internal fun Drawable.intrinsicSize(): IntSizeCompat? {
    if (intrinsicWidth < 0 || intrinsicHeight < 0) return null
    return IntSizeCompat(intrinsicWidth, intrinsicHeight)
}