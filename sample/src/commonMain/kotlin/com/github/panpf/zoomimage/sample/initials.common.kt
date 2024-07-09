package com.github.panpf.zoomimage.sample

import coil3.ImageLoader
import coil3.util.DebugLogger
import com.github.panpf.sketch.ComponentRegistry
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.decode.supportSvg
import com.github.panpf.sketch.fetch.supportComposeResources
import com.github.panpf.sketch.http.KtorStack
import com.github.panpf.sketch.util.Logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun newSketch(context: PlatformContext): Sketch {
    val appSettings = context.appSettings
    return Sketch.Builder(context).apply {
        httpStack(KtorStack())
        components {
            supportSvg()
            supportComposeResources()

            // TODO new version use addComponents
            platformSketchComponents(context)?.let { components ->
                components.decodeInterceptorList.forEach {
                    addDecodeInterceptor(it)
                }
                components.requestInterceptorList.forEach {
                    addRequestInterceptor(it)
                }
                components.decoderFactoryList.forEach {
                    addDecoder(it)
                }
                components.fetcherFactoryList.forEach {
                    addFetcher(it)
                }
            }
        }

        // For print the Sketch initialization log
        logger(level = if (appSettings.debugLog.value) Logger.Level.Debug else Logger.Level.Info)

        platformSketchInitial(context)
    }.build().apply {
        @Suppress("OPT_IN_USAGE")
        GlobalScope.launch {
            appSettings.debugLog.collect { debugLog ->
                logger.level = if (debugLog) Logger.Level.Debug else Logger.Level.Info
            }
        }
    }
}

expect fun Sketch.Builder.platformSketchInitial(context: PlatformContext)
expect fun platformSketchComponents(context: PlatformContext): ComponentRegistry?

fun newCoil(context: coil3.PlatformContext): ImageLoader {
    return ImageLoader.Builder(context).apply {
        platformCoilInitial(context)
        logger(DebugLogger())
    }.build()
}

expect fun ImageLoader.Builder.platformCoilInitial(context: coil3.PlatformContext)