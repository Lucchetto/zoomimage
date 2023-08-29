package com.github.panpf.zoomimage.zoom

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.internal.format
import com.github.panpf.zoomimage.util.isNotEmpty
import com.github.panpf.zoomimage.zoom.ScalesCalculator.Companion.Multiple
import kotlin.math.max

/**
 * Used to calculate mediumScale and maxScale
 */
interface ScalesCalculator {

    fun calculate(
        containerSize: IntSizeCompat,
        contentSize: IntSizeCompat,
        contentOriginSize: IntSizeCompat,
        contentScale: ContentScaleCompat,
        minScale: Float,
    ): Result

    companion object {
        /**
         * The default multiplier between the scales, because by default `mediumScale = minScale * multiple`, `maxScale = mediumScale * multiple`
         */
        const val Multiple = 3f

        /**
         * Dynamic scales calculator based on content size, content raw size, and container size
         */
        val Dynamic = DynamicScalesCalculator()

        /**
         * Fixed scales calculator, always 'mediumScale = minScale * multiple', 'maxScale = mediumScale * multiple'
         */
        val Fixed = FixedScalesCalculator()

        /**
         * Creates a [DynamicScalesCalculator] and specified [multiple]
         */
        fun dynamic(multiple: Float = Multiple): ScalesCalculator =
            DynamicScalesCalculator(multiple)

        /**
         * Creates a [FixedScalesCalculator] and specified [multiple]
         */
        fun fixed(multiple: Float = Multiple): ScalesCalculator =
            FixedScalesCalculator(multiple)
    }

    data class Result(val mediumScale: Float, val maxScale: Float)
}

/**
 * Dynamic scales calculator based on content size, content raw size, and container size
 */
data class DynamicScalesCalculator(
    private val multiple: Float = Multiple
) : ScalesCalculator {

    override fun calculate(
        containerSize: IntSizeCompat,
        contentSize: IntSizeCompat,
        contentOriginSize: IntSizeCompat,
        contentScale: ContentScaleCompat,
        minScale: Float
    ): ScalesCalculator.Result {
        val minMediumScale = minScale * multiple
        val mediumScale = if (contentScale != ContentScaleCompat.FillBounds) {
            // The width and height of content fill the container at the same time
            val fillContainerScale = max(
                containerSize.width / contentSize.width.toFloat(),
                containerSize.height / contentSize.height.toFloat()
            )
            // Enlarge content to the same size as its original
            val contentOriginScale = if (contentOriginSize.isNotEmpty()) {
                // Sometimes there will be a slight difference in the original scaling ratio of width and height, so take the larger one
                val widthScale = contentOriginSize.width / contentSize.width.toFloat()
                val heightScale = contentOriginSize.height / contentSize.height.toFloat()
                max(widthScale, heightScale)
            } else {
                1.0f
            }
            floatArrayOf(minMediumScale, fillContainerScale, contentOriginScale).maxOrNull()!!
        } else {
            minMediumScale
        }
        val maxScale = mediumScale * multiple
        return ScalesCalculator.Result(mediumScale = mediumScale, maxScale = maxScale)
    }

    override fun toString(): String {
        return "DynamicScalesCalculator(${multiple.format(2)})"
    }
}


/**
 * Fixed scales calculator, always 'mediumScale = minScale * multiple', 'maxScale = mediumScale * multiple'
 */
data class FixedScalesCalculator(
    private val multiple: Float = Multiple
) : ScalesCalculator {

    override fun calculate(
        containerSize: IntSizeCompat,
        contentSize: IntSizeCompat,
        contentOriginSize: IntSizeCompat,
        contentScale: ContentScaleCompat,
        minScale: Float
    ): ScalesCalculator.Result {
        val mediumScale = minScale * multiple
        val maxScale = mediumScale * multiple
        return ScalesCalculator.Result(mediumScale = mediumScale, maxScale = maxScale)
    }

    override fun toString(): String {
        return "FixedScalesCalculator(${multiple.format(2)})"
    }
}