package com.github.panpf.zoomimage.sample.ui.examples.view

import com.github.panpf.assemblyadapter.ItemFactory
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.zoomimage.sample.ui.photoalbum.Photo
import com.github.panpf.zoomimage.sample.ui.photoalbum.view.CoilPhotoGridItemFactory
import com.github.panpf.zoomimage.sample.ui.photoalbum.view.GlidePhotoGridItemFactory
import com.github.panpf.zoomimage.sample.ui.photoalbum.view.PicassoPhotoGridItemFactory
import com.github.panpf.zoomimage.sample.ui.photoalbum.view.SketchPhotoGridItemFactory

enum class ZoomViewType(
    val title: String,
    val createListItemFactory: () -> ItemFactory<Photo>,
    val createPageItemFactory: () -> FragmentItemFactory<String>,
    val my: Boolean,
    val supportIgnoreExifOrientation: Boolean,
) {
    ZoomImageView(
        title = "ZoomImageView",
        createListItemFactory = { SketchPhotoGridItemFactory() },
        createPageItemFactory = { ZoomImageViewFragment.ItemFactory() },
        my = true,
        supportIgnoreExifOrientation = true,
    ),

    SketchZoomImageView(
        title = "SketchZoomImageView",
        createListItemFactory = { SketchPhotoGridItemFactory() },
        createPageItemFactory = { SketchZoomImageViewFragment.ItemFactory() },
        my = true,
        supportIgnoreExifOrientation = true,
    ),

    CoilZoomImageView(
        title = "CoilZoomImageView",
        createListItemFactory = { CoilPhotoGridItemFactory() },
        createPageItemFactory = { CoilZoomImageViewFragment.ItemFactory() },
        my = true,
        supportIgnoreExifOrientation = false,
    ),

    GlideZoomImageView(
        title = "GlideZoomImageView",
        createListItemFactory = { GlidePhotoGridItemFactory() },
        createPageItemFactory = { GlideZoomImageViewFragment.ItemFactory() },
        my = true,
        supportIgnoreExifOrientation = false,
    ),

    PicassoZoomImageView(
        title = "PicassoZoomImageView",
        createListItemFactory = { PicassoPhotoGridItemFactory() },
        createPageItemFactory = { PicassoZoomImageViewFragment.ItemFactory() },
        my = true,
        supportIgnoreExifOrientation = false,
    ),

    PhotoView(
        title = "PhotoView",
        createListItemFactory = { SketchPhotoGridItemFactory() },
        createPageItemFactory = { PhotoViewFragment.ItemFactory() },
        my = false,
        supportIgnoreExifOrientation = false,
    ),

    SubsamplingScaleImageView(
        title = "SubsamplingScaleImageView",
        createListItemFactory = { SketchPhotoGridItemFactory() },
        createPageItemFactory = { SubsamplingViewFragment.ItemFactory() },
        my = false,
        supportIgnoreExifOrientation = false,
    ),
}