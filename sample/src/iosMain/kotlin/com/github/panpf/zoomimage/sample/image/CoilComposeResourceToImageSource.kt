package com.github.panpf.zoomimage.sample.image

import com.github.panpf.sketch.fetch.ComposeResourceUriFetcher
import com.github.panpf.sketch.util.toUri
import com.github.panpf.zoomimage.coil.CoilModelToImageSource
import com.github.panpf.zoomimage.subsampling.ComposeResourceImageSource
import com.github.panpf.zoomimage.subsampling.ImageSource
import platform.Foundation.NSURL

actual class CoilComposeResourceToImageSource : CoilModelToImageSource {

    actual override fun dataToImageSource(model: Any): ImageSource.Factory? {
        if (model is String && model.startsWith("${ComposeResourceUriFetcher.SCHEME}://")) {
            val uri = model.toUri()
            val resourcePath = "${uri.authority.orEmpty()}${uri.path.orEmpty()}"
            return ComposeResourceImageSource.Factory(resourcePath)
        } else if (model is coil3.Uri
            && model.scheme.equals(ComposeResourceUriFetcher.SCHEME, ignoreCase = true)
        ) {
            val resourcePath = "${model.authority.orEmpty()}${model.path.orEmpty()}"
            return ComposeResourceImageSource.Factory(resourcePath)
        } else if (model is NSURL
            && model.scheme.equals(ComposeResourceUriFetcher.SCHEME, ignoreCase = true)
        ) {
            val resourcePath = "${model.authority.orEmpty()}${model.path.orEmpty()}"
            return ComposeResourceImageSource.Factory(resourcePath)
        }
        return null
    }

    val NSURL.authority: String?
        get() {
            val port = port
            return if (port != null) this.host + ":" + port() else this.host
        }
}