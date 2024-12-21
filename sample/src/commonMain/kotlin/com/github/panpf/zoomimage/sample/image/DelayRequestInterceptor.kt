package com.github.panpf.zoomimage.sample.image

import com.github.panpf.sketch.request.ImageData
import com.github.panpf.sketch.request.RequestInterceptor
import kotlinx.coroutines.delay

data class DelayRequestInterceptor(val delay: Long) : RequestInterceptor {

    override val key: String? = null

    override val sortWeight: Int
        get() = 0

    override suspend fun intercept(chain: RequestInterceptor.Chain): Result<ImageData> {
        val result = chain.proceed(chain.request)
        delay(delay)
        return result
    }
}