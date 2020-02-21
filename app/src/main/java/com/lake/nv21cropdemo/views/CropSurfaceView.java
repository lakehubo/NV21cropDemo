package com.lake.nv21cropdemo.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.lake.nv21cropdemo.util.NV21Utils;

public class CropSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private boolean created = false;
    private SurfaceHolder holder = null;
    private Paint paint;
    private Matrix matrix;
    private NV21Utils nv21Utils;
    public CropSurfaceView(Context context) {
        this(context, null);
    }

    public CropSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        nv21Utils = new NV21Utils(context);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setFilterBitmap(true);
        matrix = new Matrix();
        getHolder().addCallback(this);
    }

    public void drawBitmap(byte[] data, int width, int height) {
        if (created && holder != null) {
            Canvas canvas = holder.lockCanvas();
            Bitmap bitmap = nv21Utils.nv21ToBitmap(data, width, height);
            if(bitmap!=null){
                Bitmap r90 = NV21Utils.scaleAndRotateBitmap(bitmap,1,90);
                canvas.drawBitmap(r90, matrix, paint);
            }
            holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        created = true;
        this.holder = holder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        this.holder = null;
        created = false;
    }
}
