package com.github.panpf.zoomimage.sample.util

import com.github.panpf.sketch.http.HttpHeaders
import com.github.panpf.sketch.http.merged
import com.github.panpf.sketch.request.ImageData
import com.github.panpf.sketch.request.RequestInterceptor
import com.github.panpf.sketch.request.RequestInterceptor.Chain
import com.github.panpf.sketch.request.httpHeaders

class PexelsCompatibleRequestInterceptor : RequestInterceptor {

    override val key: String? = null

    override val sortWeight: Int = 80

    override suspend fun intercept(chain: Chain): Result<ImageData> {
        val request = chain.request
        return if (request.uri.toString().contains("://images.pexels.com/")) {
            val newRequest = request.newBuilder().apply {
                val myHttpHeaders = HttpHeaders {
                    set(
                        name = "User-Agent",
                        value = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                    )
                }
                val newHttpHeaders = request.httpHeaders.merged(myHttpHeaders)
                httpHeaders(newHttpHeaders)
            }.build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(request)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other != null && this::class == other::class
    }

    override fun hashCode(): Int {
        return this::class.hashCode()
    }

    override fun toString(): String {
        return "PexelsCompatibleRequestInterceptor(sortWeight=$sortWeight)"
    }
}