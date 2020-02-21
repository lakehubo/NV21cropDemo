# NV21cropDemo
NV21图像裁剪

在相机预览数据回调方法中调用
例如将1920x1080 的nv21数据 上下左右裁剪掉一半
```
@Override
public void onPreview(byte[] nv21, Camera camera) {
  if (nv21 != null && nv21.length == 1920 * 1080 * 3 / 2) {
    int offsetW = 1920 / 4;
    int offsetH = 1080 / 4;
    byte[] data = NV21Utils.clipNV21(nv21, 1920, 1080, offsetW, offsetH, offsetW * 2, offsetH * 2);
    Log.e(TAG, "onPreviewFrame: " + data.length);
   }
}
```

工具所带的裁剪方法目前有两种，一种只裁剪，一种再裁剪的同时进行镜像操作
```
/**
 * NV21裁剪  算法效率 友善rk3399开发板测试 3ms
 *
 * @param src    源数据
 * @param width  源宽
 * @param height 源高
 * @param left   顶点坐标
 * @param top    顶点坐标
 * @param clip_w 裁剪后的宽
 * @param clip_h 裁剪后的高
 * @return 裁剪后的数据
 */
byte[] clipNV21(byte[] src, int width, int height, int left, int top, int clip_w, int clip_h);

/**
 * 剪切数据并且镜像 算法效率 友善rk3399开发板测试 14ms
 *
 * @param src
 * @param width
 * @param height
 * @param left
 * @param top
 * @param clip_w
 * @param clip_h
 * @return
 */
byte[] clipMirrorNV21(byte[] src, int width, int height, int left, int top, int clip_w, int clip_h)
```
