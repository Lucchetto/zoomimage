package com.github.panpf.zoomimage.core.test.subsampling

import com.github.panpf.zoomimage.subsampling.ImageSource
import org.junit.Assert
import org.junit.Test
import java.io.File

class ImageSourceTest {

    @Test
    fun testFileImageSource() {
        val imageFile = File("/sample/sample_cat.jpg")
        ImageSource.fromFile(imageFile).apply {
            Assert.assertEquals(imageFile.path, key)
            Assert.assertEquals("FileImageSource('$imageFile')", toString())
        }

        val imageFile2 = File("/sample/sample_dog.jpg")
        ImageSource.fromFile(imageFile2).apply {
            Assert.assertEquals(imageFile2.path, key)
            Assert.assertEquals("FileImageSource('$imageFile2')", toString())
        }
    }
}