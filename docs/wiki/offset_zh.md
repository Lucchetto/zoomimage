## 偏移

翻译：[English](offset.md)

> * 以下示例优先用 Compose 版本的组件来演示
> * [ZoomState].zoomable 等价于 [ZoomImageView].zoomable
> * [ZoomState].subsampling 等价于 [ZoomImageView].subsampling

ZoomImage 支持单指拖动、双指拖动、惯性滑动，以及 `offset()` 方法来移动图像。

### offset()

ZoomImage 提供了 `offset()` 方法用来移动图像到指定位置，它有两个参数：

* `targetOffset: Offset`: 目标偏移位置，offset 原点是组件的左上角
* `animated: Boolean = false`: 是否使用动画，默认为 false

示例：

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
        coroutineScope.launch {
            val targetOffset = state.zoomable.transform.offset + Offset(x = 100, y = 200)
            state.zoomable.offset(targetOffset = targetOffset, animated = true)
        }
    }
) {
    Text(text = "offset + Offset(100, 200)")
}

Button(
    onClick = {
        coroutineScope.launch {
            val targetOffset = state.zoomable.transform.offset - Offset(x = 100, y = 200)
            state.zoomable.offset(targetScale = targetScale, animated = true)
        }
    }
) {
    Text(text = "offset - Offset(100, 200)")
}
```

### 限制偏移边界

ZoomImage 默认不管你设置的是什么 [ContentScale]
都可以拖动查看图像的全部内容，例如你设置了 [ContentScale] 为 Crop，[Alignment] 为
Center，那么默认只显示图像中间的部分，然后你还可以单指或双指拖动来查看图像的全部内容

如果你希望图像只能在 [ContentScale] 和 [Alignment] 所限制的区域内移动，不能查看全部内容，这时你可以修改
`limitOffsetWithinBaseVisibleRect` 参数为 true 来达到此目的

示例：

```kotlin
val state: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    state.limitOffsetWithinBaseVisibleRect = true
}

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### 可访问属性

```kotlin
// compose
val state: ZoomState by rememberZoomState()
SketchZoomAsyncImage(state = state)
val zoomable: ZoomableState = state.zoomable

// view
val sketchZoomImageView = SketchZoomImageView(context)
val zoomable: ZoomableEngine = sketchZoomImageView.zoomable
```

> * 注意：view 版本的相关属性用 StateFlow 包装，所以其名字相比 compose 版本都以 State 为后缀

* `zoomable.transform.offset: Offset`: 当前偏移量（baseTransform.offset + userTransform.offset）
* `zoomable.baseTransform.offset: Offset`: 当前基础偏移量，受 alignment 参数和 rotate 方法影响
* `zoomable.userTransform.offset: Offset`: 当前用户偏移量，受 offset()、locate() 以及用户手势拖动影响
* `zoomable.scrollEdge: ScrollEdge`: 当前偏移的边界状态

#### 监听属性变化

* compose 版本的相关属性是用 State 包装的，在 Composable 函数中直接读取它即可实现监听
* view 的相关属性是用 StateFlow 包装，调用其 collect 函数即可实现监听

[ZoomImageView]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt

[ContentScale]: https://developer.android.com/reference/kotlin/androidx/compose/ui/layout/ContentScale

[Alignment]: https://developer.android.com/reference/kotlin/androidx/compose/ui/Alignment

[ZoomableState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/zoom/ZoomableState.kt

[ScrollEdge]: ../../zoomimage-core/src/commonMain/kotlin/com/github/panpf/zoomimage/zoom/ScrollEdge.kt