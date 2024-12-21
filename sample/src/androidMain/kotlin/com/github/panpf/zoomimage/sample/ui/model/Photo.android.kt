/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
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

package com.github.panpf.zoomimage.sample.ui.model

import android.os.Parcelable
import com.github.panpf.sketch.util.Size
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
actual data class Photo actual constructor(
    @SerialName("originalUrl") actual val originalUrl: String,
    @SerialName("mediumUrl") actual val mediumUrl: String?,
    @SerialName("thumbnailUrl") actual val thumbnailUrl: String?,
    @SerialName("width") actual val width: Int?,
    @SerialName("height") actual val height: Int?,
    @SerialName("index") actual val index: Int?,
) : Parcelable, java.io.Serializable {

    @IgnoredOnParcel
    actual val listThumbnailUrl: String = thumbnailUrl ?: mediumUrl ?: originalUrl

    @IgnoredOnParcel
    actual val detailPreviewUrl: String = mediumUrl ?: originalUrl
}

val Photo.photoSize: Size?
    get() = if (width != null && height != null)
        Size(width, height) else null