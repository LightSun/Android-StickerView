package com.heaven7.android.sticker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by heaven7 on 2018/11/7 0007.
 */
/*public*/ final class Utils {

    //draw text in center of rect
    public static void computeTextDrawingCoordinate(String text, Paint paint, Rect srcRange, RectF out){
        out.set(srcRange);
        out.right = paint.measureText(text);
        out.bottom = paint.descent() - paint.ascent();
        out.left += (srcRange.width() - out.right) / 2.0f;
        out.top += (srcRange.height() - out.bottom) / 2.0f;
        //  canvas.drawText(text, bounds.left, bounds.top - mPaint.ascent(), mPaint);
    }
    //better performance for direct scale
    public static Bitmap rotate(Bitmap bm, float degree) {
        Matrix m = new Matrix();
        m.setRotate(degree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        float targetX, targetY;
        if (degree == 90) {
            targetX = bm.getHeight();
            targetY = 0;
        } else {
            targetX = bm.getHeight();
            targetY = bm.getWidth();
        }

        final float[] values = new float[9];
        m.getValues(values);
        float x1 = values[Matrix.MTRANS_X];
        float y1 = values[Matrix.MTRANS_Y];
        m.postTranslate(targetX - x1, targetY - y1);

        Bitmap bm1 = Bitmap.createBitmap(bm.getHeight(), bm.getWidth(), Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bm1);
        canvas.drawBitmap(bm, m, paint);
        return bm1;
    }
}
