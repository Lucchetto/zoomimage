package com.github.panpf.zoomimage.compose.glide.test

import android.content.Context
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import com.bumptech.glide.Glide
import com.github.panpf.zoomimage.glide.GlideModelToImageSource
import com.github.panpf.zoomimage.glide.GlideModelToImageSourceImpl
import com.github.panpf.zoomimage.rememberGlideZoomState
import com.github.panpf.zoomimage.subsampling.ImageSource.Factory
import com.github.panpf.zoomimage.test.TestLifecycle
import kotlinx.collections.immutable.toImmutableList
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class GlideZoomStateTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun testRememberGlideZoomState() {
        rule.setContent {
            TestLifecycle {
                val zoomState1 = rememberGlideZoomState()
                assertEquals(
                    expected = "GlideZoomAsyncImage",
                    actual = zoomState1.logger.tag
                )
                assertEquals(
                    expected = listOf(GlideModelToImageSourceImpl()).joinToString { it::class.qualifiedName!! },
                    actual = zoomState1.modelToImageSources.joinToString { it::class.qualifiedName!! }
                )

                val modelToImageSources = remember {
                    listOf(TestGlideModelToImageSource()).toImmutableList()
                }
                val zoomState2 = rememberGlideZoomState(
                    modelToImageSources = modelToImageSources,
                )
                assertEquals(
                    expected = listOf(
                        TestGlideModelToImageSource(),
                        GlideModelToImageSourceImpl()
                    ).joinToString { it::class.qualifiedName!! },
                    actual = zoomState2.modelToImageSources.joinToString { it::class.qualifiedName!! }
                )
            }
        }
    }

    class TestGlideModelToImageSource : GlideModelToImageSource {

        override suspend fun modelToImageSource(
            context: Context,
            glide: Glide,
            model: Any
        ): Factory? {
            return null
        }
    }
}