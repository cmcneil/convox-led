package org.convox.lights;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by carson on 6/23/15.
 */
public class ColorDot extends View {
    private Paint mMainPaint = new Paint();
    private Paint mSelectedPaint = new Paint();
    private float[] mColor = new float[3];
    private final int paramPadding = 2;
    private String TAG = "COLOR_DOT_ERROR";
    private boolean mIsSelected = false;
    private OnSelectedListener mSelectedListener;
    private OnDeletedListener mDeletedListener;
    private GestureDetector mGestureDetector;

    public ColorDot(Context context) {
        super(context);
        mMainPaint.setStyle(Paint.Style.FILL);
        mSelectedPaint.setStyle(Paint.Style.STROKE);
        mSelectedPaint.setColor(Color.DKGRAY);
        mSelectedPaint.setStrokeWidth(5);
        mGestureDetector = new GestureDetector(context, new GestureHandler());
    }

    @Override
    public void onDraw(Canvas canvas) {
        int padding = paramPadding * getWidth() / 100;
        float centerX = getWidth() / 2;
        float centerY = getHeight() / 2;
        float radius = getWidth() / 2 - 5 * padding;
        canvas.drawCircle(centerX, centerY, radius, mMainPaint);
        if (mIsSelected) {
            canvas.drawCircle(centerX, centerY, radius + 4 * padding, mSelectedPaint);
        }
    }

    // Make sure it's always square.
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(widthSize, heightSize);
        setMeasuredDimension(size, size);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.mGestureDetector.onTouchEvent(event);
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                int x = (int) event.getX();
                int y = (int) event.getY();
                int cy = y - getHeight() / 2;
                int cx = x - getHeight() / 2;
                int padding = paramPadding * getWidth() / 100;
                double d = Math.sqrt(cx * cx + cy * cy);
                float radius = getWidth() / 2 - 5 * padding;
                if (d <=  radius) {
                    if (mSelectedListener != null) {
                        mSelectedListener.onSelected(this);
                        invalidate();
                    }
                }
        }
        return true;
    }

    public float[] getColor() {
        return mColor;
    }

    public void setColor(float hsv[]) {
        if (hsv.length < 3) {
            Log.e(TAG, "Illegal argument!");
            return;
        }
        mColor[0] = hsv[0];
        mColor[1] = hsv[1];
        mColor[2] = hsv[2];
        mMainPaint.setColor(Color.HSVToColor(hsv));
        invalidate();
    }

    class GestureHandler extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }
        @Override
        public void onLongPress(MotionEvent event) {
            mDeletedListener.onDeleted(ColorDot.this);
        }
    }

    /*** EVENT LISTENERS **/

    public void setOnSelectedListener(OnSelectedListener listener) {
        mSelectedListener = listener;
    }

    public void setIsSelected() {
        mIsSelected = true;
        invalidate();
    }

    public void setNotSelected() {
        mIsSelected = false;
        invalidate();
    }

    public interface OnSelectedListener {
        void onSelected(ColorDot selectedDot);
    }

    public void setOnDeletedListener(OnDeletedListener listener) {
        mDeletedListener = listener;
    }

    public interface OnDeletedListener {
        void onDeleted(ColorDot deletedDot);
    }
}
