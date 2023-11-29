package com.github.panpf.zoomimage.sample.ui.test.view

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView.ScaleType
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.toRect
import androidx.core.view.updateLayoutParams
import com.github.panpf.sketch.displayImage
import com.github.panpf.sketch.fetch.newAssetUri
import com.github.panpf.tools4k.lang.asOrThrow
import com.github.panpf.zoomimage.sample.databinding.ImageMatrixFragmentBinding
import com.github.panpf.zoomimage.sample.ui.base.view.ToolbarBindingFragment
import com.github.panpf.zoomimage.sample.ui.util.computeImageViewSize
import com.github.panpf.zoomimage.sample.ui.util.view.getRotation
import com.github.panpf.zoomimage.sample.ui.util.view.getScale
import com.github.panpf.zoomimage.sample.ui.util.view.getTranslation
import com.github.panpf.zoomimage.sample.util.BitmapScaleTransformation
import com.github.panpf.zoomimage.sample.util.format
import com.github.panpf.zoomimage.sample.util.toVeryShortString
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.round
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.view.internal.toAlignment
import com.github.panpf.zoomimage.view.internal.toContentScale
import com.github.panpf.zoomimage.zoom.calculateBaseTransform
import kotlin.math.min

class ImageMatrixFragment : ToolbarBindingFragment<ImageMatrixFragmentBinding>() {

    //    private val matrix = Matrix()
    private val scaleStep = 0.2f
    private val offsetStep = 50
    private val rotateStep = 90

    private val cacheBaseMatrix = Matrix()
    private val cacheUserMatrix = Matrix()
    private val cacheDisplayMatrix = Matrix()
    private var scaleType = ScaleType.FIT_CENTER
    private var viewSize = IntSizeCompat.Zero
    private var rotation = 0
    private var horImage = true
    private var userTransform = TransformCompat.Origin

    override fun onViewCreated(
        toolbar: Toolbar,
        binding: ImageMatrixFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(toolbar, binding, savedInstanceState)
        toolbar.title = "Image Matrix"

        binding.root.parent.asOrThrow<ViewGroup>().clipChildren = false
        binding.root.parent.asOrThrow<ViewGroup>().parent.asOrThrow<ViewGroup>().clipChildren =
            false

        binding.imageMatrixFragmentImageView.apply {
        }

        binding.imageMatrixFragmentHorizontalButton.apply {
            val setName = {
                text = if (horImage) "Ver" else "Hor"
            }
            setOnClickListener {
                horImage = !horImage
                setName()
                updateImage(binding)
            }
            setName()
        }

        binding.imageMatrixFragmentScaleTypeButton.apply {
            val setName = {
                text = scaleType.name
            }
            setOnClickListener {
                AlertDialog.Builder(requireActivity()).apply {
                    val scaleTypes = ScaleType.values()
                    setItems(scaleTypes.map { it.name }.toTypedArray()) { _, which ->
                        scaleType = scaleTypes[which]
                        setName()
                        updateMatrix(binding)
                    }
                }.show()
            }
            setName()
        }

        binding.imageMatrixFragmentResetButton.setOnClickListener {
            rotation = 0
            userTransform = TransformCompat.Origin
            updateMatrix(binding)
        }

        binding.imageMatrixFragmentScalePlusButton.setOnClickListener {
            val currentScale = userTransform.scaleX
            val targetScale = currentScale + scaleStep
            userTransform = userTransform.copy(scale = ScaleFactorCompat(targetScale))
            updateMatrix(binding)
        }

        binding.imageMatrixFragmentScaleMinusButton.setOnClickListener {
            val currentScale = userTransform.scaleX
            val targetScale = currentScale - scaleStep
            userTransform = userTransform.copy(scale = ScaleFactorCompat(targetScale))
            updateMatrix(binding)
        }

        binding.imageMatrixFragmentOffsetUpButton.setOnClickListener {
            val addOffset = OffsetCompat(0f, -offsetStep.toFloat())
            userTransform = userTransform.copy(offset = userTransform.offset + addOffset)
            updateMatrix(binding)
        }

        binding.imageMatrixFragmentOffsetDownButton.setOnClickListener {
            val addOffset = OffsetCompat(0f, offsetStep.toFloat())
            userTransform = userTransform.copy(offset = userTransform.offset + addOffset)
            updateMatrix(binding)
        }

        binding.imageMatrixFragmentOffsetRightButton.setOnClickListener {
            val addOffset = OffsetCompat(offsetStep.toFloat(), 0f)
            userTransform = userTransform.copy(offset = userTransform.offset + addOffset)
            updateMatrix(binding)
        }

        binding.imageMatrixFragmentOffsetLeftButton.setOnClickListener {
            val addOffset = OffsetCompat(-offsetStep.toFloat(), 0f)
            userTransform = userTransform.copy(offset = userTransform.offset + addOffset)
            updateMatrix(binding)
        }

        binding.imageMatrixFragmentRotatePlusButton.setOnClickListener {
            rotation = (rotation + rotateStep) % 360
            updateMatrix(binding)
        }

        binding.imageMatrixFragmentRotateMinusButton.setOnClickListener {
            rotation = (rotation - rotateStep) % 360
            updateMatrix(binding)
        }

        binding.imageMatrixFragmentImageView.apply {
            addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
                if (viewSize.width != v.width || viewSize.height != v.height) {
                    viewSize = IntSizeCompat(v.width, v.height)
                    updateMatrix(binding)
                }
            }
            updateLayoutParams {
                val viewSize = computeImageViewSize(context)
                width = viewSize.width
                height = viewSize.height
            }
        }

        updateImage(binding)
        updateMatrix(binding)
    }

    @SuppressLint("SetTextI18n")
    private fun updateMatrix(binding: ImageMatrixFragmentBinding) {
        val drawable = binding.imageMatrixFragmentImageView.drawable
        val drawableSize = drawable?.let { IntSizeCompat(it.intrinsicWidth, it.intrinsicHeight) }
            ?: IntSizeCompat.Zero

        val rotation = rotation
        val scaleType = scaleType
        val viewSize = viewSize
        val baseMatrix = cacheBaseMatrix.apply {
            reset()
            val transform = calculateBaseTransform(
                containerSize = viewSize,
                contentSize = drawableSize,
                contentScale = scaleType.toContentScale(),
                alignment = scaleType.toAlignment(),
                rotation = rotation,
            )
            require(transform.scale.scaleX > 0f && transform.scale.scaleY > 0f) {
                "resetBaseMatrix transform scale=$transform is invalid"
            }
            postRotate(rotation.toFloat(), drawableSize.width / 2f, drawableSize.height / 2f)
            postScale(transform.scale.scaleX, transform.scale.scaleY)
            postTranslate(transform.offset.x, transform.offset.y)
        }

        val userMatrix = cacheUserMatrix.apply {
            reset()
            postScale(userTransform.scaleX, userTransform.scaleY)
            postTranslate(userTransform.offset.x, userTransform.offset.y)
        }
        val displayMatrix = cacheDisplayMatrix.apply {
            set(baseMatrix)
            postConcat(userMatrix)
        }

        binding.imageMatrixFragmentImageView.imageMatrix = displayMatrix
        updateValues(binding)
    }

    @SuppressLint("SetTextI18n")
    private fun updateValues(binding: ImageMatrixFragmentBinding) {
        val matrix = binding.imageMatrixFragmentImageView.imageMatrix
        val scaleString = matrix.getScale().scaleX.format(2)
        val translationString = matrix.getTranslation().round().toShortString()
        val rotationString = matrix.getRotation().toString()
        binding.imageMatrixFragmentTransformValueText.text =
            "scale: ${scaleString}, offset: ${translationString}, rotation: $rotationString"

        val displayRect = RectF()
        val drawable = binding.imageMatrixFragmentImageView.drawable
        if (drawable != null) {
            displayRect.set(
                /* left = */ 0f,
                /* top = */ 0f,
                /* right = */ drawable.intrinsicWidth.toFloat(),
                /* bottom = */ drawable.intrinsicHeight.toFloat()
            )
        }
        matrix.mapRect(displayRect)
        binding.imageMatrixFragmentDisplayValueText.text =
            "display: ${displayRect.toRect().toVeryShortString()}"
        val contentSize = drawable?.let { IntSizeCompat(it.intrinsicWidth, it.intrinsicHeight) }
            ?: IntSizeCompat.Zero
        val containerSize = viewSize
        binding.imageMatrixFragmentSizeValueText.text =
            "container: ${containerSize.toShortString()}, content: ${contentSize.toShortString()}"
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateImage(binding: ImageMatrixFragmentBinding) {
        val imageUri = if (horImage) {
            newAssetUri("sample_elephant.jpg")
        } else {
            newAssetUri("sample_cat.jpg")
        }
        binding.imageMatrixFragmentImageView.displayImage(imageUri) {
            val resources = requireContext().resources
            val maxSize =
                min(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels) / 4
            addTransformations(BitmapScaleTransformation(maxSize))
            listener(
                onSuccess = { _, _ ->
                    updateMatrix(binding)
                },
                onError = { _, _ ->
                    updateMatrix(binding)
                }
            )
        }
    }
}