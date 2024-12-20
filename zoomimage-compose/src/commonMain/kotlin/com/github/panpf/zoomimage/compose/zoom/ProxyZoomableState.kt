package com.github.panpf.zoomimage.compose.zoom

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Velocity
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.ContainerWhitespace
import com.github.panpf.zoomimage.zoom.OneFingerScaleSpec
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import com.github.panpf.zoomimage.zoom.ScrollEdge
import kotlinx.coroutines.CoroutineScope

/**
 * Creates and remember a [ZoomableState] that can be used to control the scale, pan, rotation of the content.
 *
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.ZoomableStateTest.testRememberZoomableState
 */
@Composable
fun rememberProxyZoomableState(target: ZoomableState): ZoomableState {
    val zoomableState = remember(target) {
        ProxyZoomableState(target)
    }
    return zoomableState
}

class ProxyZoomableState(
    private val targetZoomableState: ZoomableState
): ZoomableState {
    override val logger: Logger
        get() = targetZoomableState.logger
    override val layoutDirection: LayoutDirection
        get() = targetZoomableState.layoutDirection
    override var coroutineScope: CoroutineScope?
        get() = targetZoomableState.coroutineScope
        set(value) { targetZoomableState.coroutineScope = value }
    override var lastScaleAnimatable: Animatable<*, *>?
        get() = targetZoomableState.lastScaleAnimatable
        set(value) { targetZoomableState.lastScaleAnimatable = value }
    override var lastFlingAnimatable: Animatable<*, *>?
        get() = targetZoomableState.lastFlingAnimatable
        set(value) { targetZoomableState.lastFlingAnimatable = value }
    override var lastInitialUserTransform: Transform
        get() = targetZoomableState.lastInitialUserTransform
        set(value) { targetZoomableState.lastInitialUserTransform = value }
    override var rotation: Int
        get() = targetZoomableState.rotation
        set(value) { targetZoomableState.rotation = value }
    override var rememberedCount: Int
        get() = targetZoomableState.rememberedCount
        set(value) { targetZoomableState.rememberedCount = value }
    override var containerSize: IntSize
        get() = targetZoomableState.containerSize
        set(value) { targetZoomableState.containerSize = value }
    override var contentSize: IntSize
        get() = targetZoomableState.contentSize
        set(value) { targetZoomableState.contentSize = value }
    override var contentOriginSize: IntSize
        get() = targetZoomableState.contentOriginSize
        set(value) { targetZoomableState.contentOriginSize = value }
    override var contentScale: ContentScale
        get() = targetZoomableState.contentScale
        set(value) { targetZoomableState.contentScale = value }
    override var alignment: Alignment
        get() = targetZoomableState.alignment
        set(value) { targetZoomableState.alignment = value }
    override var readMode: ReadMode?
        get() = targetZoomableState.readMode
        set(value) { targetZoomableState.readMode = value }
    override var scalesCalculator: ScalesCalculator
        get() = targetZoomableState.scalesCalculator
        set(value) { targetZoomableState.scalesCalculator = value }
    override var threeStepScale: Boolean
        get() = targetZoomableState.threeStepScale
        set(value) { targetZoomableState.threeStepScale = value }
    override var rubberBandScale: Boolean
        get() = targetZoomableState.rubberBandScale
        set(value) { targetZoomableState.rubberBandScale = value }
    override var oneFingerScaleSpec: OneFingerScaleSpec
        get() = targetZoomableState.oneFingerScaleSpec
        set(value) { targetZoomableState.oneFingerScaleSpec = value }
    override var animationSpec: ZoomAnimationSpec
        get() = targetZoomableState.animationSpec
        set(value) { targetZoomableState.animationSpec = value }
    override var limitOffsetWithinBaseVisibleRect: Boolean
        get() = targetZoomableState.limitOffsetWithinBaseVisibleRect
        set(value) { targetZoomableState.limitOffsetWithinBaseVisibleRect = value }
    override var containerWhitespaceMultiple: Float
        get() = targetZoomableState.containerWhitespaceMultiple
        set(value) { targetZoomableState.containerWhitespaceMultiple = value }
    override var containerWhitespace: ContainerWhitespace
        get() = targetZoomableState.containerWhitespace
        set(value) { targetZoomableState.containerWhitespace = value }
    override var disabledGestureTypes: Int
        get() = targetZoomableState.disabledGestureTypes
        set(value) { targetZoomableState.disabledGestureTypes = value }
    override var reverseMouseWheelScale: Boolean
        get() = targetZoomableState.reverseMouseWheelScale
        set(value) { targetZoomableState.reverseMouseWheelScale = value }
    override var mouseWheelScaleScrollDeltaConverter: (Float) -> Float
        get() = targetZoomableState.mouseWheelScaleScrollDeltaConverter
        set(value) { targetZoomableState.mouseWheelScaleScrollDeltaConverter = value }
    override val baseTransform: Transform
        get() = targetZoomableState.baseTransform
    override val userTransform: Transform
        get() = targetZoomableState.userTransform
    override val transform: Transform
        get() = targetZoomableState.transform.let {
            it.copy(
                scale = it.scale.times(2f)
            )
        }
    override val minScale: Float
        get() = targetZoomableState.minScale
    override val mediumScale: Float
        get() = targetZoomableState.mediumScale
    override val maxScale: Float
        get() = targetZoomableState.maxScale
    override val contentBaseDisplayRect: IntRect
        get() = targetZoomableState.contentBaseDisplayRect
    override val contentBaseVisibleRect: IntRect
        get() = targetZoomableState.contentBaseVisibleRect
    override val contentDisplayRect: IntRect
        get() = targetZoomableState.contentDisplayRect
    override val contentVisibleRect: IntRect
        get() = targetZoomableState.contentVisibleRect
    override val userOffsetBounds: IntRect
        get() = targetZoomableState.userOffsetBounds
    override val scrollEdge: ScrollEdge
        get() = targetZoomableState.scrollEdge
    override var continuousTransformType: Int
        get() = targetZoomableState.continuousTransformType
        set(value) { targetZoomableState.continuousTransformType = value }
    override var lastContainerSize: IntSize
        get() = targetZoomableState.lastContainerSize
        set(value) { targetZoomableState.lastContainerSize = value }
    override var lastContentSize: IntSize
        get() = targetZoomableState.lastContentSize
        set(value) { targetZoomableState.lastContentSize = value }
    override var lastContentOriginSize: IntSize
        get() = targetZoomableState.lastContentOriginSize
        set(value) { targetZoomableState.lastContentOriginSize = value }
    override var lastContentScale: ContentScale
        get() = targetZoomableState.lastContentScale
        set(value) { targetZoomableState.lastContentScale = value }
    override var lastAlignment: Alignment
        get() = targetZoomableState.lastAlignment
        set(value) { targetZoomableState.lastAlignment = value }
    override var lastRotation: Int
        get() = targetZoomableState.lastRotation
        set(value) { targetZoomableState.lastRotation = value }
    override var lastReadMode: ReadMode?
        get() = targetZoomableState.lastReadMode
        set(value) { targetZoomableState.lastReadMode = value }
    override var lastScalesCalculator: ScalesCalculator
        get() = targetZoomableState.lastScalesCalculator
        set(value) { targetZoomableState.lastScalesCalculator = value }
    override var lastLimitOffsetWithinBaseVisibleRect: Boolean
        get() = targetZoomableState.lastLimitOffsetWithinBaseVisibleRect
        set(value) { targetZoomableState.lastLimitOffsetWithinBaseVisibleRect = value }
    override var lastContainerWhitespace: ContainerWhitespace
        get() = targetZoomableState.lastContainerWhitespace
        set(value) { targetZoomableState.lastContainerWhitespace = value }

    override suspend fun reset(caller: String) = targetZoomableState.reset(caller)

    override suspend fun scale(
        targetScale: Float,
        centroidContentPoint: IntOffset,
        animated: Boolean,
        animationSpec: ZoomAnimationSpec?
    ) = targetZoomableState.scale(targetScale, centroidContentPoint, animated, animationSpec)

    override suspend fun switchScale(
        centroidContentPoint: IntOffset,
        animated: Boolean,
        animationSpec: ZoomAnimationSpec?
    ) = targetZoomableState.switchScale(centroidContentPoint, animated, animationSpec)

    override suspend fun offset(
        targetOffset: Offset,
        animated: Boolean,
        animationSpec: ZoomAnimationSpec?
    ) = targetZoomableState.offset(targetOffset, animated, animationSpec)

    override suspend fun locate(
        contentPoint: IntOffset,
        targetScale: Float,
        animated: Boolean,
        animationSpec: ZoomAnimationSpec?
    ) = targetZoomableState.locate(contentPoint, targetScale, animated, animationSpec)

    override suspend fun rotate(targetRotation: Int) = targetZoomableState.rotate(targetRotation)

    override fun getNextStepScale() = targetZoomableState.getNextStepScale()

    override fun touchPointToContentPoint(touchPoint: Offset) =
        targetZoomableState.touchPointToContentPoint(touchPoint)

    override fun canScroll(horizontal: Boolean, direction: Int) =
        targetZoomableState.canScroll(horizontal, direction)

    override fun onRemembered() = targetZoomableState.onRemembered()

    override fun onAbandoned() = targetZoomableState.onAbandoned()

    override fun onForgotten() = targetZoomableState.onForgotten()

    override suspend fun stopAllAnimation(caller: String) =
        targetZoomableState.stopAllAnimation(caller)

    override suspend fun rollbackScale(centroid: Offset?) =
        targetZoomableState.rollbackScale(centroid)

    override suspend fun gestureTransform(
        centroid: Offset,
        panChange: Offset,
        zoomChange: Float,
        rotationChange: Float
    ) = targetZoomableState.gestureTransform(
        centroid, panChange, zoomChange, rotationChange
    )

    override suspend fun fling(velocity: Velocity, density: Density) =
        targetZoomableState.fling(velocity, density)

    override fun checkSupportGestureType(gestureType: Int) =
        targetZoomableState.checkSupportGestureType(gestureType)

    override fun limitUserScale(targetUserScale: Float) =
        targetZoomableState.limitUserScale(targetUserScale)

    override fun limitUserScaleWithRubberBand(
        targetUserScale: Float
    ) = targetZoomableState.limitUserScaleWithRubberBand(targetUserScale)

    override fun limitUserOffset(userOffset: Offset, userScale: Float) =
        targetZoomableState.limitUserOffset(userOffset, userScale)

    override suspend fun animatedUpdateUserTransform(
        targetUserTransform: Transform,
        newContinuousTransformType: Int?,
        animationSpec: ZoomAnimationSpec?,
        caller: String
    ) = targetZoomableState.animatedUpdateUserTransform(targetUserTransform, newContinuousTransformType, animationSpec, caller)

    override fun updateUserTransform(targetUserTransform: Transform) =
        targetZoomableState.updateUserTransform(targetUserTransform)

    override fun updateTransform() = targetZoomableState.updateTransform()

    override fun calculateContainerWhitespace() = targetZoomableState.calculateContainerWhitespace()

    override fun toString() = targetZoomableState.toString()
}