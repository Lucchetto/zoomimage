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
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.ContainerWhitespace
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import com.github.panpf.zoomimage.zoom.GestureType
import com.github.panpf.zoomimage.zoom.OneFingerScaleSpec
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import com.github.panpf.zoomimage.zoom.ScrollEdge
import kotlinx.coroutines.CoroutineScope

interface ZoomableState {
    val logger: Logger
    val layoutDirection: LayoutDirection
    var coroutineScope: CoroutineScope?
    var lastScaleAnimatable: Animatable<*, *>?
    var lastFlingAnimatable: Animatable<*, *>?
    var lastInitialUserTransform: Transform
    var rotation: Int
    var rememberedCount: Int

    /**
     * The size of the container that holds the content, this is usually the size of the ZoomImage component
     */
    var containerSize: IntSize

    /**
     * The size of the content, usually Painter.intrinsicSize.round(), setup by the ZoomImage component
     */
    var contentSize: IntSize

    /**
     * The original size of the content, it is usually set by [SubsamplingState] after parsing the original size of the image
     */
    var contentOriginSize: IntSize

    /**
     * The scale of the content, usually set by ZoomImage component
     */
    var contentScale: ContentScale

    /**
     * The alignment of the content, usually set by ZoomImage component
     */
    var alignment: Alignment

    /**
     * Setup whether to enable read mode and configure read mode
     */
    var readMode: ReadMode?

    /**
     * Set up [ScalesCalculator] for custom calculations mediumScale and maxScale
     */
    var scalesCalculator: ScalesCalculator

    /**
     * If true, the switchScale() method will cycle between minScale, mediumScale, maxScale,
     * otherwise only cycle between minScale and mediumScale
     */
    var threeStepScale: Boolean

    /**
     * If true, when the user zooms to the minimum or maximum zoom factor through a gesture,
     * continuing to zoom will have a rubber band effect, and when the hand is released,
     * it will spring back to the minimum or maximum zoom factor
     */
    var rubberBandScale: Boolean

    /**
     * One finger double-click and hold the screen and slide up and down to scale the configuration
     */
    var oneFingerScaleSpec: OneFingerScaleSpec

    /**
     * The animation configuration for the zoom animation
     */
    var animationSpec: ZoomAnimationSpec

    /**
     * Whether to limit the offset of the user's pan to within the base visible rect
     */
    var limitOffsetWithinBaseVisibleRect: Boolean

    /**
     * Add whitespace around containers based on container size
     */
    var containerWhitespaceMultiple: Float

    /**
     * Add whitespace around containers, has higher priority than [containerWhitespaceMultiple]
     */
    var containerWhitespace: ContainerWhitespace

    /**
     * Disabled gesture types. Allow multiple types to be combined through the 'and' operator
     *
     * @see com.github.panpf.zoomimage.zoom.GestureType
     */
    var disabledGestureTypes: Int

    /**
     * Whether to reverse the scale of the mouse wheel, the default is false
     */
    var reverseMouseWheelScale: Boolean

    /**
     * Zoom increment converter when zooming with mouse wheel
     */
    var mouseWheelScaleScrollDeltaConverter: (Float) -> Float

    /**
     * Base transformation, include the base scale, offset, rotation,
     * which is affected by [contentScale], [alignment] properties and [rotate] method
     */
    val baseTransform: Transform

    /**
     * User transformation, include the user scale, offset, rotation,
     * which is affected by the user's gesture, [readMode] properties and [scale], [offset], [locate] method
     */
    val userTransform: Transform

    /**
     * Final transformation, include the final scale, offset, rotation,
     * which is the sum of [baseTransform] and [userTransform]
     */
    val transform: Transform

    /**
     * Minimum scale factor, for limits the final scale factor, and as a target value for one of when switch scale
     */
    val minScale: Float

    /**
     * Medium scale factor, only as a target value for one of when switch scale
     */
    val mediumScale: Float

    /**
     * Maximum scale factor, for limits the final scale factor, and as a target value for one of when switch scale
     */
    val maxScale: Float

    /**
     * The content region in the container after the baseTransform transformation
     */
    val contentBaseDisplayRect: IntRect

    /**
     * The content is visible region to the user after the baseTransform transformation
     */
    val contentBaseVisibleRect: IntRect

    /**
     * The content region in the container after the final transform transformation
     */
    val contentDisplayRect: IntRect

    /**
     * The content is visible region to the user after the final transform transformation
     */
    val contentVisibleRect: IntRect

    /**
     * The offset boundary of userTransform, affected by scale and limitOffsetWithinBaseVisibleRect
     */
    val userOffsetBounds: IntRect

    /**
     * Edge state for the current offset
     */
    val scrollEdge: ScrollEdge

    /**
     * The type of transformation currently in progress
     *
     * @see ContinuousTransformType
     */
    var continuousTransformType: Int
    var lastContainerSize: IntSize
    var lastContentSize: IntSize
    var lastContentOriginSize: IntSize
    var lastContentScale: ContentScale
    var lastAlignment: Alignment
    var lastRotation: Int
    var lastReadMode: ReadMode?
    var lastScalesCalculator: ScalesCalculator
    var lastLimitOffsetWithinBaseVisibleRect: Boolean
    var lastContainerWhitespace: ContainerWhitespace

    /**
     * Reset [transform] and [minScale], [mediumScale], [maxScale], automatically called when [containerSize],
     * [contentSize], [contentOriginSize], [contentScale], [alignment], [rotate], [scalesCalculator], [readMode] changes
     */
    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun reset(caller: String): Unit

    /**
     * Scale to the [targetScale] and move the focus around [centroidContentPoint], and animation occurs when [animated] is true.
     *
     * @param centroidContentPoint The focus point of the scale, the default is the center of the visible area of the content
     */
    suspend fun scale(
        targetScale: Float,
        centroidContentPoint: IntOffset = contentVisibleRect.center,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
    ): Boolean

    /**
     * Scale to the next step scale and move the focus around [centroidContentPoint], and animation occurs when [animated] is true.
     *
     * If [threeStepScale] is true, it will cycle between [minScale], [mediumScale], [maxScale],
     * otherwise it will only cycle between [minScale] and [mediumScale]
     *
     * @param centroidContentPoint The focus point of the scale, the default is the center of the visible area of the content
     */
    suspend fun switchScale(
        centroidContentPoint: IntOffset = contentVisibleRect.center,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
    ): Float?

    /**
     * Pan the image to the [targetOffset] position, and animation occurs when [animated] is true
     */
    suspend fun offset(
        targetOffset: Offset,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
    ): Boolean

    /**
     * Pan the [contentPoint] on content to the center of the screen while zooming to [targetScale], and there will be an animation when [animated] is true
     *
     * @param targetScale The target scale, the default is the current scale
     */
    suspend fun locate(
        contentPoint: IntOffset,
        targetScale: Float = transform.scaleX,
        animated: Boolean = false,
        animationSpec: ZoomAnimationSpec? = null,
    ): Boolean

    /**
     * Rotate the content to [targetRotation]
     */
    suspend fun rotate(targetRotation: Int): Unit

    /**
     * Gets the next step scale factor,
     * and if [threeStepScale] is true, it will cycle between [minScale], [mediumScale], [maxScale],
     * otherwise it will only loop between [minScale], [mediumScale].
     */
    fun getNextStepScale(): Float

    /**
     * Converts touch points on the screen to points on content
     */
    fun touchPointToContentPoint(touchPoint: Offset): IntOffset

    /**
     * If true is returned, scrolling can continue on the specified axis and direction
     *
     * @param horizontal Whether to scroll horizontally
     * @param direction positive means scroll to the right or scroll down, negative means scroll to the left or scroll up
     */
    fun canScroll(horizontal: Boolean, direction: Int): Boolean
    fun onRemembered()
    fun onAbandoned()
    fun onForgotten()

    /**
     * Stop all animations immediately
     */
    suspend fun stopAllAnimation(caller: String)

    suspend fun rollbackScale(centroid: Offset? = null): Boolean

    suspend fun gestureTransform(
        centroid: Offset,
        panChange: Offset,
        zoomChange: Float,
        rotationChange: Float
    ): Unit

    suspend fun fling(velocity: Velocity, density: Density): Boolean
    fun checkSupportGestureType(@GestureType gestureType: Int): Boolean
    fun limitUserScale(targetUserScale: Float): Float
    fun limitUserScaleWithRubberBand(targetUserScale: Float): Float
    fun limitUserOffset(userOffset: Offset, userScale: Float): Offset

    suspend fun animatedUpdateUserTransform(
        targetUserTransform: Transform,
        @ContinuousTransformType newContinuousTransformType: Int?,
        animationSpec: ZoomAnimationSpec?,
        caller: String
    )

    fun updateUserTransform(targetUserTransform: Transform)
    fun updateTransform()
    fun calculateContainerWhitespace(): ContainerWhitespace

    override fun toString(): String
}