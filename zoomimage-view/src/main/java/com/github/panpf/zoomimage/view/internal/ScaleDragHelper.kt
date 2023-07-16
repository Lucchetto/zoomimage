/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
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
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.view.MotionEvent
import android.widget.ImageView.ScaleType
import com.github.panpf.zoomimage.Edge
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.ScrollEdge
import com.github.panpf.zoomimage.core.IntSizeCompat
import com.github.panpf.zoomimage.core.OffsetCompat
import com.github.panpf.zoomimage.core.ScaleFactorCompat
import com.github.panpf.zoomimage.core.internal.canScroll
import com.github.panpf.zoomimage.core.internal.limitScaleWithRubberBand
import com.github.panpf.zoomimage.core.isEmpty
import com.github.panpf.zoomimage.core.toShortString
import com.github.panpf.zoomimage.view.internal.ScaleDragGestureDetector.OnActionListener
import com.github.panpf.zoomimage.view.internal.ScaleDragGestureDetector.OnGestureListener
import kotlin.math.abs
import kotlin.math.roundToInt

internal class ScaleDragHelper constructor(
    private val context: Context,
    private val logger: Logger,
    private val engine: ZoomEngine,
    val onUpdateMatrix: () -> Unit,
    val onViewDrag: (dx: Float, dy: Float) -> Unit,
    val onDragFling: (velocityX: Float, velocityY: Float) -> Unit,
    val onScaleChanged: (scaleFactor: Float, focusX: Float, focusY: Float) -> Unit,
) {

    private val view = engine.view

    /* Stores default scale and translate information */
    private val baseMatrix = Matrix()

    /* Stores zoom, translate and externally set rotation information generated by the user through touch events */
    private val supportMatrix = Matrix()

    /* Store the fused information of baseMatrix and supportMatrix for drawing */
    private val displayMatrix = Matrix()
    private val displayRectF = RectF()

    /* Cache the coordinates of the last zoom gesture, used when restoring zoom */
    private var lastScaleFocusX: Float = 0f
    private var lastScaleFocusY: Float = 0f

    private var flingRunnable: FlingRunnable? = null
    private var locationRunnable: LocationRunnable? = null
    private var animatedScaleRunnable: AnimatedScaleRunnable? = null
    private val scaleDragGestureDetector: ScaleDragGestureDetector
    private var _scrollEdge: ScrollEdge = ScrollEdge(horizontal = Edge.BOTH, vertical = Edge.BOTH)
    private var blockParentIntercept: Boolean = false
    private var dragging = false
    private var manualScaling = false

    val scrollEdge: ScrollEdge
        get() = _scrollEdge

    val isScaling: Boolean
        get() = animatedScaleRunnable?.isRunning == true || manualScaling

    val scale: Float
        get() = supportMatrix.getScale().scaleX
    val offset: OffsetCompat
        get() = supportMatrix.getTranslation()

    val baseScale: ScaleFactorCompat
        get() = baseMatrix.getScale()
    val baseOffset: OffsetCompat
        get() = baseMatrix.getTranslation()

    val displayScale: ScaleFactorCompat
        get() = displayMatrix.apply { getDisplayMatrix(this) }.getScale()
    val displayOffset: OffsetCompat
        get() = displayMatrix.apply { getDisplayMatrix(this) }.getTranslation()

    init {
        scaleDragGestureDetector = ScaleDragGestureDetector(context, object : OnGestureListener {
            override fun onDrag(dx: Float, dy: Float) = doDrag(dx, dy)

            override fun onFling(velocityX: Float, velocityY: Float) = doFling(velocityX, velocityY)

            override fun onScaleBegin(): Boolean = doScaleBegin()

            override fun onScale(
                scaleFactor: Float, focusX: Float, focusY: Float, dx: Float, dy: Float
            ) = doScale(scaleFactor, focusX, focusY, dx, dy)

            override fun onScaleEnd() = doScaleEnd()
        }).apply {
            onActionListener = object : OnActionListener {
                override fun onActionDown(ev: MotionEvent) = actionDown()
                override fun onActionUp(ev: MotionEvent) = actionUp()
                override fun onActionCancel(ev: MotionEvent) = actionUp()
            }
        }
    }

    fun reset() {
        resetBaseMatrix()
        resetSupportMatrix()
        checkAndApplyMatrix()
    }

    fun clean() {
        animatedScaleRunnable?.cancel()
        animatedScaleRunnable = null
        locationRunnable?.cancel()
        locationRunnable = null
        flingRunnable?.cancel()
        flingRunnable = null
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        /* Location operations cannot be interrupted */
        if (this.locationRunnable?.isRunning == true) {
            logger.d {
                "onTouchEvent. requestDisallowInterceptTouchEvent true. locating"
            }
            requestDisallowInterceptTouchEvent(true)
            return true
        }
        return scaleDragGestureDetector.onTouchEvent(event)
    }

    private fun resetBaseMatrix() {
        baseMatrix.apply {
            reset()
            val transform = engine.baseInitialTransform
            postScale(transform.scale.scaleX, transform.scale.scaleY)
            postTranslate(transform.offset.x, transform.offset.y)
            postRotate(engine.rotateDegrees.toFloat())
        }
    }

    private fun resetSupportMatrix() {
        supportMatrix.apply {
            reset()
            val transform = engine.supportInitialTransform
            postScale(transform.scale.scaleX, transform.scale.scaleY)
            postTranslate(transform.offset.x, transform.offset.y)
        }
    }

    private fun checkAndApplyMatrix() {
        if (checkMatrixBounds()) {
            onUpdateMatrix()
        }
    }

    private fun checkMatrixBounds(): Boolean {
        val displayRectF = displayRectF.apply { getDisplayRect(this) }
        if (displayRectF.isEmpty) {
            _scrollEdge = ScrollEdge(horizontal = Edge.BOTH, vertical = Edge.BOTH)
            return false
        }

        var deltaX = 0f
        val viewWidth = engine.viewSize.width
        val displayWidth = displayRectF.width()
        when {
            displayWidth.toInt() <= viewWidth -> {
                deltaX = when (engine.scaleType) {
                    ScaleType.FIT_START -> -displayRectF.left
                    ScaleType.FIT_END -> viewWidth - displayWidth - displayRectF.left
                    else -> (viewWidth - displayWidth) / 2 - displayRectF.left
                }
            }

            displayRectF.left.toInt() > 0 -> {
                deltaX = -displayRectF.left
            }

            displayRectF.right.toInt() < viewWidth -> {
                deltaX = viewWidth - displayRectF.right
            }
        }

        var deltaY = 0f
        val viewHeight = engine.viewSize.height
        val displayHeight = displayRectF.height()
        when {
            displayHeight.toInt() <= viewHeight -> {
                deltaY = when (engine.scaleType) {
                    ScaleType.FIT_START -> -displayRectF.top
                    ScaleType.FIT_END -> viewHeight - displayHeight - displayRectF.top
                    else -> (viewHeight - displayHeight) / 2 - displayRectF.top
                }
            }

            displayRectF.top.toInt() > 0 -> {
                deltaY = -displayRectF.top
            }

            displayRectF.bottom.toInt() < viewHeight -> {
                deltaY = viewHeight - displayRectF.bottom
            }
        }

        // Finally actually translate the matrix
        supportMatrix.postTranslate(deltaX, deltaY)

        _scrollEdge = ScrollEdge(
            horizontal = when {
                displayWidth.toInt() <= viewWidth -> Edge.BOTH
                displayRectF.left.toInt() >= 0 -> Edge.START
                displayRectF.right.toInt() <= viewWidth -> Edge.END
                else -> Edge.NONE
            },
            vertical = when {
                displayHeight.toInt() <= viewHeight -> Edge.BOTH
                displayRectF.top.toInt() >= 0 -> Edge.START
                displayRectF.bottom.toInt() <= viewHeight -> Edge.END
                else -> Edge.NONE
            },
        )
        return true
    }

    fun offsetBy(dx: Float, dy: Float) {
        supportMatrix.postTranslate(dx, dy)
        checkAndApplyMatrix()
    }

    fun offsetTo(dx: Float, dy: Float) {
        val offset = offset
        supportMatrix.postTranslate(dx - offset.x, dy - offset.y)
        checkAndApplyMatrix()
    }

    fun location(xInDrawable: Float, yInDrawable: Float, animate: Boolean) {
        locationRunnable?.cancel()
        cancelFling()

        val (viewWidth, viewHeight) = engine.viewSize.takeIf { !it.isEmpty() } ?: return
        val pointF = PointF(xInDrawable, yInDrawable).apply {
            rotatePoint(this, engine.rotateDegrees, engine.drawableSize)
        }
        val newX = pointF.x
        val newY = pointF.y
        val nowScale = scale
        if (nowScale.format(2) == engine.minScale.format(2)) {
            scale(
                newScale = engine.getNextStepScale(),
                focalX = engine.viewSize.width / 2f,
                focalY = engine.viewSize.height / 2f,
                animate = false
            )
        }

        val displayRectF = getDisplayRect()
        val currentScale = displayScale
        val scaleLocationX = (newX * currentScale.scaleX).toInt()
        val scaleLocationY = (newY * currentScale.scaleY).toInt()
        val scaledLocationX =
            scaleLocationX.coerceIn(0, displayRectF.width().toInt())
        val scaledLocationY =
            scaleLocationY.coerceIn(0, displayRectF.height().toInt())
        val centerLocationX = (scaledLocationX - viewWidth / 2).coerceAtLeast(0)
        val centerLocationY = (scaledLocationY - viewHeight / 2).coerceAtLeast(0)
        val startX = abs(displayRectF.left.toInt())
        val startY = abs(displayRectF.top.toInt())
        logger.d {
            "location. inDrawable=${xInDrawable}x${yInDrawable}, start=${startX}x${startY}, end=${centerLocationX}x${centerLocationY}"
        }
        if (animate) {
            locationRunnable?.cancel()
            locationRunnable = LocationRunnable(
                context = context,
                engine = engine,
                scaleDragHelper = this@ScaleDragHelper,
                startX = startX,
                startY = startY,
                endX = centerLocationX,
                endY = centerLocationY
            )
            locationRunnable?.start()
        } else {
            val dx = -(centerLocationX - startX).toFloat()
            val dy = -(centerLocationY - startY).toFloat()
            offsetBy(dx, dy)
        }
    }

    fun scale(newScale: Float, focalX: Float, focalY: Float, animate: Boolean) {
        cancelFling()
        animatedScaleRunnable?.cancel()
        val currentScale = scale
        if (animate) {
            animatedScaleRunnable = AnimatedScaleRunnable(
                engine = engine,
                scaleDragHelper = this@ScaleDragHelper,
                startScale = currentScale,
                endScale = newScale,
                scaleFocalX = focalX,
                scaleFocalY = focalY
            )
            animatedScaleRunnable?.start()
        } else {
            scaleBy(addScale = newScale / currentScale, focalX = focalX, focalY = focalY)
        }
    }

    fun getDisplayMatrix(matrix: Matrix) {
        matrix.set(baseMatrix)
        matrix.postConcat(supportMatrix)
    }

    fun getDisplayRect(rectF: RectF) {
        val drawableSize = engine.drawableSize
        val displayMatrix = displayMatrix.apply { getDisplayMatrix(this) }
        rectF.set(0f, 0f, drawableSize.width.toFloat(), drawableSize.height.toFloat())
        displayMatrix.mapRect(rectF)
    }

    fun getDisplayRect(): RectF {
        return RectF().apply { getDisplayRect(this) }
    }

    /**
     * Gets the area that the user can see on the drawable (not affected by rotation)
     */
    fun getVisibleRect(rect: Rect) {
        rect.setEmpty()
        val displayRectF =
            displayRectF.apply { getDisplayRect(this) }.takeIf { !it.isEmpty } ?: return
        val viewSize = engine.viewSize.takeIf { !it.isEmpty() } ?: return
        val drawableSize = engine.drawableSize.takeIf { !it.isEmpty() } ?: return
        val (drawableWidth, drawableHeight) = drawableSize.let {
            if (engine.rotateDegrees % 180 == 0) it else IntSizeCompat(it.height, it.width)
        }
        val displayWidth = displayRectF.width()
        val displayHeight = displayRectF.height()
        val widthScale = displayWidth / drawableWidth
        val heightScale = displayHeight / drawableHeight
        var left: Float = if (displayRectF.left >= 0)
            0f else abs(displayRectF.left)
        var right: Float = if (displayWidth >= viewSize.width)
            viewSize.width + left else displayRectF.right - displayRectF.left
        var top: Float = if (displayRectF.top >= 0)
            0f else abs(displayRectF.top)
        var bottom: Float = if (displayHeight >= viewSize.height)
            viewSize.height + top else displayRectF.bottom - displayRectF.top
        left /= widthScale
        right /= widthScale
        top /= heightScale
        bottom /= heightScale
        rect.set(left.roundToInt(), top.roundToInt(), right.roundToInt(), bottom.roundToInt())
        reverseRotateRect(rect, engine.rotateDegrees, drawableSize)
    }

    /**
     * Gets the area that the user can see on the drawable (not affected by rotation)
     */
    fun getVisibleRect(): Rect {
        return Rect().apply { getVisibleRect(this) }
    }

    fun touchPointToDrawablePoint(touchPoint: PointF): Point? {
        val drawableSize = engine.drawableSize.takeIf { !it.isEmpty() } ?: return null
        val displayRect = getDisplayRect()
        if (!displayRect.contains(touchPoint.x, touchPoint.y)) {
            return null
        }

        val zoomScale = displayScale
        val drawableX =
            ((touchPoint.x - displayRect.left) / zoomScale.scaleX).roundToInt()
                .coerceIn(0, drawableSize.width)
        val drawableY =
            ((touchPoint.y - displayRect.top) / zoomScale.scaleY).roundToInt()
                .coerceIn(0, drawableSize.height)
        return Point(drawableX, drawableY)
    }

    /**
     * Whether you can scroll horizontally or vertical in the specified direction
     *
     * @param direction Negative to check scrolling left, positive to check scrolling right.
     */
    fun canScroll(horizontal: Boolean, direction: Int): Boolean {
        return canScroll(horizontal, direction, scrollEdge)
    }

    private fun doDrag(dx: Float, dy: Float) {
        logger.d { "onDrag. dx: $dx, dy: $dy" }

        if (scaleDragGestureDetector.isScaling) {
            logger.d { "onDrag. isScaling" }
            return
        }

        supportMatrix.postTranslate(dx, dy)
        checkAndApplyMatrix()

        onViewDrag(dx, dy)

        val scaling = scaleDragGestureDetector.isScaling
        val disallowParentInterceptOnEdge = !engine.allowParentInterceptOnEdge
        val blockParent = blockParentIntercept
        val disallow = if (dragging || scaling || blockParent || disallowParentInterceptOnEdge) {
            logger.d {
                "onDrag. DisallowParentIntercept. dragging=$dragging, scaling=$scaling, blockParent=$blockParent, disallowParentInterceptOnEdge=$disallowParentInterceptOnEdge"
            }
            true
        } else {
            val slop = engine.view.resources.displayMetrics.density * 3
            val result = (scrollEdge.horizontal == Edge.NONE && (dx >= slop || dx <= -slop))
                    || (scrollEdge.horizontal == Edge.START && dx <= -slop)
                    || (scrollEdge.horizontal == Edge.END && dx >= slop)
                    || (scrollEdge.vertical == Edge.NONE && (dy >= slop || dy <= -slop))
                    || (scrollEdge.vertical == Edge.START && dy <= -slop)
                    || (scrollEdge.vertical == Edge.END && dy >= slop)
            val type = if (result) "DisallowParentIntercept" else "AllowParentIntercept"
            logger.d {
                "onDrag. $type. scrollEdge=${scrollEdge.horizontal}-${scrollEdge.vertical}, d=${dx}x${dy}"
            }
            dragging = result
            result
        }
        requestDisallowInterceptTouchEvent(disallow)
    }

    private fun doFling(velocityX: Float, velocityY: Float) {
        logger.d {
            "fling. velocity=($velocityX, $velocityY), offset=${offset.toShortString()}"
        }

        flingRunnable?.cancel()
        flingRunnable = FlingRunnable(
            context = context,
            engine = engine,
            scaleDragHelper = this@ScaleDragHelper,
            velocityX = velocityX.toInt(),
            velocityY = velocityY.toInt()
        )
        flingRunnable?.start()

        onDragFling(velocityX, velocityY)
    }

    private fun cancelFling() {
        flingRunnable?.cancel()
    }

    private fun doScaleBegin(): Boolean {
        logger.d { "onScaleBegin" }
        manualScaling = true
        return true
    }

    private fun scaleBy(addScale: Float, focalX: Float, focalY: Float) {
        supportMatrix.postScale(addScale, addScale, focalX, focalY)
        checkAndApplyMatrix()
    }

    internal fun doScale(scaleFactor: Float, focusX: Float, focusY: Float, dx: Float, dy: Float) {
        logger.d {
            "onScale. scaleFactor: $scaleFactor, focusX: $focusX, focusY: $focusY, dx: $dx, dy: $dy"
        }

        /* Simulate a rubber band effect when zoomed to max or min */
        var newScaleFactor = scaleFactor
        lastScaleFocusX = focusX
        lastScaleFocusY = focusY
        val currentSupportScale = scale
        val newSupportScale = currentSupportScale * newScaleFactor
        val limitedNewSupportScale = if (engine.rubberBandScale) {
            limitScaleWithRubberBand(
                currentScale = currentSupportScale,
                targetScale = newSupportScale,
                minScale = engine.minScale,
                maxScale = engine.maxScale
            )
        } else {
            newSupportScale.coerceIn(minimumValue = engine.minScale, maximumValue = engine.maxScale)
        }
        newScaleFactor = limitedNewSupportScale / currentSupportScale

        supportMatrix.postScale(newScaleFactor, newScaleFactor, focusX, focusY)
        supportMatrix.postTranslate(dx, dy)
        checkAndApplyMatrix()

        onScaleChanged(newScaleFactor, focusX, focusY)
    }

    private fun doScaleEnd() {
        logger.d { "onScaleEnd" }
        manualScaling = false
        onUpdateMatrix()
    }

    private fun actionDown() {
        logger.d {
            "onActionDown. disallow parent intercept touch event"
        }

        lastScaleFocusX = 0f
        lastScaleFocusY = 0f
        dragging = false

        requestDisallowInterceptTouchEvent(true)

        cancelFling()
    }

    private fun actionUp() {
        /* Roll back to minimum or maximum scaling */
        val currentScale = scale.format(2)
        val minZoomScale = engine.minScale.format(2)
        val maxZoomScale = engine.maxScale.format(2)
        if (currentScale < minZoomScale) {
            val displayRectF = displayRectF.apply { getDisplayRect(this) }
            if (!displayRectF.isEmpty) {
                scale(minZoomScale, displayRectF.centerX(), displayRectF.centerY(), true)
            }
        } else if (currentScale > maxZoomScale) {
            val lastScaleFocusX = lastScaleFocusX
            val lastScaleFocusY = lastScaleFocusY
            if (lastScaleFocusX != 0f && lastScaleFocusY != 0f) {
                scale(maxZoomScale, lastScaleFocusX, lastScaleFocusY, true)
            }
        }
    }

    private fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        view.parent?.requestDisallowInterceptTouchEvent(disallowIntercept)
    }
}