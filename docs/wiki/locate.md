## Locate

Translations: [简体中文](locate_zh.md)

> * The following example takes precedence over the Compose version component for demonstration
> * [ZoomState].zoomable is equivalent to [ZoomImageView].zoomable
> * [ZoomState].subsampling is equivalent to [ZoomImageView].subsampling

### locate()

ZoomImage provides a modified `locate()` method to locate the specified position of the image,
which is displayed in the middle of the screen (except for the edge position), which has three
parameters:

* contentPoint: IntOffset: The anchor on the content, the origin is the upper-left corner of the
  content
* targetScale: Float = transform.scaleX: The target magnification, which defaults to the current
  magnification
* animated: Boolean = false: Whether to use animation, the default is false

example：

```kotlin
val state: ZoomState by rememberZoomState()

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)

val coroutineScope = rememberCoroutineScope()
Button(
    onClick = {
        // Locate to the center of the content and zoom to mediumScale if the current zoom factor is less than MediumScale
        coroutineScope.launch {
            state.zoomable.locate(
                contentPoint = state.zoomable.contentSize.center,
                targetScale = state.zoomable.transform.scaleX.coerceAtLeast(state.zoomable.mediumScale),
                animated = true,
            )
        }
    }
) {
    Text(text = "locate to center")
}
```

[ZoomImageView]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt