package com.github.panpf.zoomimage.sample

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import android.util.Log
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.util.DebugLogger
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.sketch.Sketch
import com.github.panpf.zoomimage.sample.util.ignoreFirst
import com.squareup.picasso.Picasso
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class MyApplication : Application(), SingletonSketch.Factory, SingletonImageLoader.Factory {

    @SuppressLint("VisibleForTests")
    override fun onCreate() {
        super.onCreate()
        handleSSLHandshake()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Picasso.setSingletonInstance(Picasso.Builder(this).apply {
                loggingEnabled(true)
            }.build())
        }

        Glide.init(
            this,
            GlideBuilder().setLogLevel(Log.DEBUG)
        )

        GlobalScope.launch {
            appSettings.viewImageLoader.ignoreFirst().collect {
                onToggleImageLoader(it)
            }
        }
        GlobalScope.launch {
            appSettings.composeImageLoader.ignoreFirst().collect {
                onToggleImageLoader(it)
            }
        }
    }

    override fun createSketch(context: PlatformContext): Sketch {
        return newSketch(context)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(this).logger(DebugLogger()).build()
    }

    private fun handleSSLHandshake() {
        try {
            val trustAllCerts = arrayOf<TrustManager>(@SuppressLint("CustomX509TrustManager")
            object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate?> {
                    return arrayOfNulls(0)
                }

                @SuppressLint("TrustAllX509TrustManager")
                override fun checkClientTrusted(
                    certs: Array<X509Certificate?>?,
                    authType: String?
                ) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                override fun checkServerTrusted(
                    certs: Array<X509Certificate?>?,
                    authType: String?
                ) {
                }
            })
            val sc = SSLContext.getInstance("TLS")
            // trustAllCerts trust all certificates
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
            HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onToggleImageLoader(newImageLoader: String) {
        Log.d("ZoomImage", "Switch image loader to $newImageLoader")
        viewImageLoaders.plus(composeImageLoaders).distinct().forEach {
            if (it != newImageLoader) {
                when (newImageLoader) {
                    "Sketch" -> {
                        SingletonSketch.get(this).memoryCache.clear()
                    }

                    "Coil" -> {
                        SingletonImageLoader.get(this).memoryCache?.clear()
                    }

                    "Glide" -> {
                        Glide.get(this).clearMemory()
                    }

                    "Picasso" -> {
                        val picasso = Picasso.get()
                        try {
                            val cacheField = Picasso::class.java.getDeclaredField("cache")
                            cacheField.isAccessible = true
                            val cache = cacheField.get(picasso) as? com.squareup.picasso.Cache
                            cache?.clear()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    else -> throw IllegalArgumentException("Unknown image loader: $newImageLoader")
                }
                Log.d("ZoomImage", "Clean $it memory cache")
            }
        }
    }
}