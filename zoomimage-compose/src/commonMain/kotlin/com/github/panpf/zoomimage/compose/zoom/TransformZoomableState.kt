package com.github.panpf.zoomimage.compose.zoom

import androidx.compose.animation.core.Animatable
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Velocity
import com.github.panpf.zoomimage.compose.util.format
import com.github.panpf.zoomimage.compose.util.name
import com.github.panpf.zoomimage.compose.util.toShortString
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.ContainerWhitespace
import com.github.panpf.zoomimage.zoom.OneFingerScaleSpec
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import com.github.panpf.zoomimage.zoom.ScrollEdge
import kotlinx.coroutines.CoroutineScope

/**
 * Abstract base class implementing [ZoomableState] that transforms the behavior of another
 * [ZoomableState] instance (referred to as the "source").
 *
 * This class acts as a proxy, delegating most operations to the underlying [source],
 * while allowing derived classes to intercept, modify, or extend the behavior as needed.
 *
 * @param source The [ZoomableState] instance whose behavior will be transformed.
 *
 * Subclasses can override methods or properties to apply custom transformations, such as
 * modifying the zoom scale, adjusting offsets, or intercepting gesture inputs.
 *
 * Example Usage:
 * ```
 * class CustomZoomableState(source: ZoomableState) : TransformZoomableState(source) {
 *     override var rotation: Int
 *         get() = super.rotation + 90 // Apply custom rotation logic
 *         set(value) { super.rotation = value - 90 }
 * }
 * ```
 */
abstract class TransformZoomableState(
    protected val source: ZoomableState
) : ZoomableState {
    override val logger: Logger
        get() = source.logger
    override val layoutDirection: LayoutDirection
        get() = source.layoutDirection
    override var coroutineScope: CoroutineScope?
        get() = source.coroutineScope
        set(value) {
            source.coroutineScope = value
        }
    override var lastScaleAnimatable: Animatable<*, *>?
        get() = source.lastScaleAnimatable
        set(value) {
            source.lastScaleAnimatable = value
        }
    override var lastFlingAnimatable: Animatable<*, *>?
        get() = source.lastFlingAnimatable
        set(value) {
            source.lastFlingAnimatable = value
        }
    override var lastInitialUserTransform: Transform
        get() = source.lastInitialUserTransform
        set(value) {
            source.lastInitialUserTransform = value
        }
    override var rotation: Int
        get() = source.rotation
        set(value) {
            source.rotation = value
        }
    override var rememberedCount: Int
        get() = source.rememberedCount
        set(value) {
            source.rememberedCount = value
        }
    override var containerSize: IntSize
        get() = source.containerSize
        set(value) {
            source.containerSize = value
        }
    override var contentSize: IntSize
        get() = source.contentSize
        set(value) {
            source.contentSize = value
        }
    override var contentOriginSize: IntSize
        get() = source.contentOriginSize
        set(value) {
            source.contentOriginSize = value
        }
    override var contentScale: ContentScale
        get() = source.contentScale
        set(value) {
            source.contentScale = value
        }
    override var alignment: Alignment
        get() = source.alignment
        set(value) {
            source.alignment = value
        }
    override var readMode: ReadMode?
        get() = source.readMode
        set(value) {
            source.readMode = value
        }
    override var scalesCalculator: ScalesCalculator
        get() = source.scalesCalculator
        set(value) {
            source.scalesCalculator = value
        }
    override var threeStepScale: Boolean
        get() = source.threeStepScale
        set(value) {
            source.threeStepScale = value
        }
    override var rubberBandScale: Boolean
        get() = source.rubberBandScale
        set(value) {
            source.rubberBandScale = value
        }
    override var oneFingerScaleSpec: OneFingerScaleSpec
        get() = source.oneFingerScaleSpec
        set(value) {
            source.oneFingerScaleSpec = value
        }
    override var animationSpec: ZoomAnimationSpec
        get() = source.animationSpec
        set(value) {
            source.animationSpec = value
        }
    override var limitOffsetWithinBaseVisibleRect: Boolean
        get() = source.limitOffsetWithinBaseVisibleRect
        set(value) {
            source.limitOffsetWithinBaseVisibleRect = value
        }
    override var containerWhitespaceMultiple: Float
        get() = source.containerWhitespaceMultiple
        set(value) {
            source.containerWhitespaceMultiple = value
        }
    override var containerWhitespace: ContainerWhitespace
        get() = source.containerWhitespace
        set(value) {
            source.containerWhitespace = value
        }
    override var disabledGestureTypes: Int
        get() = source.disabledGestureTypes
        set(value) {
            source.disabledGestureTypes = value
        }
    override var reverseMouseWheelScale: Boolean
        get() = source.reverseMouseWheelScale
        set(value) {
            source.reverseMouseWheelScale = value
        }
    override var mouseWheelScaleScrollDeltaConverter: (Float) -> Float
        get() = source.mouseWheelScaleScrollDeltaConverter
        set(value) {
            source.mouseWheelScaleScrollDeltaConverter = value
        }
    override val baseTransform: Transform
        get() = source.baseTransform
    override val userTransform: Transform
        get() = source.userTransform
    override val transform: Transform
        get() = source.transform
    override val minScale: Float
        get() = source.minScale
    override val mediumScale: Float
        get() = source.mediumScale
    override val maxScale: Float
        get() = source.maxScale
    override val contentBaseDisplayRect: IntRect
        get() = source.contentBaseDisplayRect
    override val contentBaseVisibleRect: IntRect
        get() = source.contentBaseVisibleRect
    override val contentDisplayRect: IntRect
        get() = source.contentDisplayRect
    override val contentVisibleRect: IntRect
        get() = source.contentVisibleRect
    override val userOffsetBounds: IntRect
        get() = source.userOffsetBounds
    override val scrollEdge: ScrollEdge
        get() = source.scrollEdge
    override var continuousTransformType: Int
        get() = source.continuousTransformType
        set(value) {
            source.continuousTransformType = value
        }
    override var lastContainerSize: IntSize
        get() = source.lastContainerSize
        set(value) {
            source.lastContainerSize = value
        }
    override var lastContentSize: IntSize
        get() = source.lastContentSize
        set(value) {
            source.lastContentSize = value
        }
    override var lastContentOriginSize: IntSize
        get() = source.lastContentOriginSize
        set(value) {
            source.lastContentOriginSize = value
        }
    override var lastContentScale: ContentScale
        get() = source.lastContentScale
        set(value) {
            source.lastContentScale = value
        }
    override var lastAlignment: Alignment
        get() = source.lastAlignment
        set(value) {
            source.lastAlignment = value
        }
    override var lastRotation: Int
        get() = source.lastRotation
        set(value) {
            source.lastRotation = value
        }
    override var lastReadMode: ReadMode?
        get() = source.lastReadMode
        set(value) {
            source.lastReadMode = value
        }
    override var lastScalesCalculator: ScalesCalculator
        get() = source.lastScalesCalculator
        set(value) {
            source.lastScalesCalculator = value
        }
    override var lastLimitOffsetWithinBaseVisibleRect: Boolean
        get() = source.lastLimitOffsetWithinBaseVisibleRect
        set(value) {
            source.lastLimitOffsetWithinBaseVisibleRect = value
        }
    override var lastContainerWhitespace: ContainerWhitespace
        get() = source.lastContainerWhitespace
        set(value) {
            source.lastContainerWhitespace = value
        }

    override suspend fun reset(caller: String) = source.reset(caller)

    override suspend fun scale(
        targetScale: Float,
        centroidContentPoint: IntOffset,
        animated: Boolean,
        animationSpec: ZoomAnimationSpec?
    ) = source.scale(targetScale, centroidContentPoint, animated, animationSpec)

    override suspend fun switchScale(
        centroidContentPoint: IntOffset,
        animated: Boolean,
        animationSpec: ZoomAnimationSpec?
    ) = source.switchScale(centroidContentPoint, animated, animationSpec)

    override suspend fun offset(
        targetOffset: Offset,
        animated: Boolean,
        animationSpec: ZoomAnimationSpec?
    ) = source.offset(targetOffset, animated, animationSpec)

    override suspend fun locate(
        contentPoint: IntOffset,
        targetScale: Float,
        animated: Boolean,
        animationSpec: ZoomAnimationSpec?
    ) = source.locate(contentPoint, targetScale, animated, animationSpec)

    override suspend fun rotate(targetRotation: Int) = source.rotate(targetRotation)

    override fun getNextStepScale() = source.getNextStepScale()

    override fun touchPointToContentPoint(touchPoint: Offset) =
        source.touchPointToContentPoint(touchPoint)

    override fun canScroll(horizontal: Boolean, direction: Int) =
        source.canScroll(horizontal, direction)

    override fun onRemembered() = source.onRemembered()

    override fun onAbandoned() = source.onAbandoned()

    override fun onForgotten() = source.onForgotten()

    override suspend fun stopAllAnimation(caller: String) =
        source.stopAllAnimation(caller)

    override suspend fun rollbackScale(centroid: Offset?) =
        source.rollbackScale(centroid)

    override suspend fun gestureTransform(
        centroid: Offset,
        panChange: Offset,
        zoomChange: Float,
        rotationChange: Float
    ) = source.gestureTransform(
        centroid, panChange, zoomChange, rotationChange
    )

    override suspend fun fling(velocity: Velocity, density: Density) =
        source.fling(velocity, density)

    override fun checkSupportGestureType(gestureType: Int) =
        source.checkSupportGestureType(gestureType)

    override fun limitUserScale(targetUserScale: Float) =
        source.limitUserScale(targetUserScale)

    override fun limitUserScaleWithRubberBand(
        targetUserScale: Float
    ) = source.limitUserScaleWithRubberBand(targetUserScale)

    override fun limitUserOffset(userOffset: Offset, userScale: Float) =
        source.limitUserOffset(userOffset, userScale)

    override suspend fun animatedUpdateUserTransform(
        targetUserTransform: Transform,
        newContinuousTransformType: Int?,
        animationSpec: ZoomAnimationSpec?,
        caller: String
    ) = source.animatedUpdateUserTransform(
        targetUserTransform,
        newContinuousTransformType,
        animationSpec,
        caller
    )

    override fun updateUserTransform(targetUserTransform: Transform) =
        source.updateUserTransform(targetUserTransform)

    override fun updateTransform() = source.updateTransform()

    override fun calculateContainerWhitespace() = source.calculateContainerWhitespace()

    override fun toString() = "TransformZoomableState(" +
            "source=${source}" +
            "minScale=${minScale.format(4)}, " +
            ")"
}