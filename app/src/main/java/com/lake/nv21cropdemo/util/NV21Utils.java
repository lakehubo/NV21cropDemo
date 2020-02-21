package com.lake.nv21cropdemo.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

public class NV21Utils {
    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private Type.Builder yuvType, rgbaType;
    private Allocation in, out;
    private int mWidth, mHeight;

    public NV21Utils(Context context) {
        rs = RenderScript.create(context);
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
    }

    /**
     * nv21转bitmap
     *
     * @param nv21
     * @param width
     * @param height
     * @return
     */
    public Bitmap nv21ToBitmap(byte[] nv21, int width, int height) {
        if (width <= 0 || height <= 0 || nv21 == null || nv21.length != width * height * 3 / 2) {
            return null;
        }
        if (yuvType == null || mWidth != width || mHeight != height) {
            mWidth = width;
            mHeight = height;
            yuvType = new Type.Builder(rs, Element.U8(rs)).setX(nv21.length);
            in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

            rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
            out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
        }

        in.copyFrom(nv21);

        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);

        Bitmap bmpout = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        out.copyTo(bmpout);
        return bmpout;
    }

    /**
     * 按比例缩放并且旋转图片
     *
     * @param origin 原图
     * @param ratio  比例
     * @return 新的bitmap
     */
    public static Bitmap scaleAndRotateBitmap(Bitmap origin, float ratio, float degree) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(ratio, ratio);
        matrix.preRotate(degree);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }


    /**
     * NV21裁剪  算法效率 3ms
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
    public static byte[] clipNV21(byte[] src, int width, int height, int left, int top, int clip_w, int clip_h) {
        if (left > width || top > height || left + clip_w > width || top + clip_h > height) {
            return null;
        }
        //取偶
        int x = left / 4 * 4, y = top / 4 * 4;
        int w = clip_w / 4 * 4, h = clip_h / 4 * 4;
        int y_unit = w * h;
        int uv = y_unit / 2;
        byte[] nData = new byte[y_unit + uv];
        int uv_index_dst = w * h - y / 2 * w;
        int uv_index_src = width * height + x;
        for (int i = y; i < y + h; i++) {
            System.arraycopy(src, i * width + x, nData, (i - y) * w, w);//y内存块复制
            if (i % 2 == 0) {
                System.arraycopy(src, uv_index_src + (i >> 1) * width, nData, uv_index_dst + (i >> 1) * w, w);//uv内存块复制
            }
        }
        return nData;
    }

    /**
     * 剪切数据并且镜像 算法效率14ms
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
    public static byte[] clipMirrorNV21(byte[] src, int width, int height, int left, int top, int clip_w, int clip_h) {
        if (left > width || top > height) {
            return null;
        }
        //取偶
        int x = left, y = top;
        int w = clip_w, h = clip_h;
        int y_unit = w * h;
        int src_unit = width * height;
        int uv = y_unit / 2;
        byte[] nData = new byte[y_unit + uv];
        int nPos = (y - 1) * width;
        int mPos;
        for (int i = y, len_i = y + h; i < len_i; i++) {
            nPos += width;
            mPos = src_unit + i / 2 * width;
            for (int j = x, len_j = x + w; j < len_j; j++) {
                nData[(i - y + 1) * w - j + x - 1] = src[nPos + j];
                if (i % 2 == 0) {
                    int m = y_unit + ((i - y) / 2 + 1) * w - j + x - 1;
                    if (m % 2 == 0) {
                        m++;
                        nData[m] = src[mPos + j];
                        continue;
                    }
                    m--;
                    nData[m] = src[mPos + j];
                }
            }
        }
        return nData;
    }
}
