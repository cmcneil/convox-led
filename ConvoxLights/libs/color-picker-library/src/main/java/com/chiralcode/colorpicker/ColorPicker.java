/*
 * Copyright 2013 Piotr Adamus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chiralcode.colorpicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

//import com.chiralcode.colorpicker.R;

public class ColorPicker extends View {

    /**
     * Customizable display parameters (in percents)
     */
    private final int paramOuterPadding = 2; // outer padding of the whole color picker view

    private Paint colorWheelPaint;
    private Paint valueSliderPaint;

    private Paint colorViewPaint;
    private Paint blackFillPaint;
    private RectF blackButton;

    private Drawable plusIcon;

    private Paint colorPointerPaint;
    private RectF colorPointerCoords;

    private Paint valuePointerPaint;

    private Bitmap colorWheelBitmap;

    private int outerPadding;

    private int colorWheelRadius;

    private OnColorSelectedListener mColorSelectedListener;
    private OnColorAddedListener mColorAddedListener;

    private Matrix gradientRotationMatrix;

    /** Currently selected color */
    private float[] colorHSV = new float[] { 0f, 0f, 1f };

    public ColorPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ColorPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorPicker(Context context) {
        super(context);
        init();
    }

    private void init() {

        colorPointerPaint = new Paint();
        colorPointerPaint.setStyle(Style.STROKE);
        colorPointerPaint.setStrokeWidth(2f);
        colorPointerPaint.setARGB(128, 0, 0, 0);

        valuePointerPaint = new Paint();
        valuePointerPaint.setStyle(Style.STROKE);
        valuePointerPaint.setStrokeWidth(2f);

        blackFillPaint = new Paint();
        blackFillPaint.setStyle(Style.FILL);
        blackFillPaint.setColor(Color.BLACK);
        blackButton = new RectF();

        plusIcon = getResources().getDrawable(R.drawable.plus);

        colorWheelPaint = new Paint();
        colorWheelPaint.setAntiAlias(true);
        colorWheelPaint.setDither(true);

        valueSliderPaint = new Paint();
        valueSliderPaint.setAntiAlias(true);
        valueSliderPaint.setDither(true);

        colorViewPaint = new Paint();
        colorViewPaint.setAntiAlias(true);

        colorPointerCoords = new RectF();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float RATIO = 3f / 2f;
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int maxWidth = (int) (heightSize * RATIO);
        int maxHeight = (int) (widthSize / RATIO);

        if (widthSize > maxWidth) {
            widthSize = maxWidth + getPaddingLeft() + getPaddingRight();
        } else {
            heightSize = maxHeight + getPaddingTop() + getPaddingBottom();
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        int centerY = getHeight() / 2;
        int centerX = centerY;

        // drawing color wheel
        canvas.drawBitmap(colorWheelBitmap, centerX - colorWheelRadius, centerY - colorWheelRadius, null);

        float hueAngle = (float) Math.toRadians(colorHSV[0]);
        int colorPointX = (int) (-Math.cos(hueAngle) * colorHSV[1] * colorWheelRadius) + centerX;
        int colorPointY = (int) (-Math.sin(hueAngle) * colorHSV[1] * colorWheelRadius) + centerY;

        float pointerRadius = 0.075f * colorWheelRadius;
        int pointerX = (int) (colorPointX - pointerRadius / 2);
        int pointerY = (int) (colorPointY - pointerRadius / 2);

        colorPointerCoords.set(pointerX, pointerY, pointerX + pointerRadius, pointerY + pointerRadius);
        canvas.drawOval(colorPointerCoords, colorPointerPaint);

        // Draw black rectangle.
        canvas.drawRoundRect(blackButton, 30, 30, blackFillPaint);

        // Draw + sign.
        plusIcon.draw(canvas);

    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        outerPadding = paramOuterPadding * width / 100;

        colorWheelRadius = height / 2 - outerPadding;

        colorWheelBitmap = createColorWheelBitmap(colorWheelRadius * 2, colorWheelRadius * 2);

        gradientRotationMatrix = new Matrix();
        gradientRotationMatrix.preRotate(270, width / 2, height / 2);

        setBlackButtonSize();
        setPlusIconSize();
    }

    private Bitmap createColorWheelBitmap(int width, int height) {

        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);

        int colorCount = 12;
        int colorAngleStep = 360 / 12;
        int colors[] = new int[colorCount + 1];
        float hsv[] = new float[] { 0f, 1f, 1f };
        for (int i = 0; i < colors.length; i++) {
            hsv[0] = (i * colorAngleStep + 180) % 360;
            colors[i] = Color.HSVToColor(hsv);
        }
        colors[colorCount] = colors[0];

        SweepGradient sweepGradient = new SweepGradient(width / 2, height / 2, colors, null);
        RadialGradient radialGradient = new RadialGradient(width / 2, height / 2, colorWheelRadius, 0xFFFFFFFF, 0x00FFFFFF, TileMode.CLAMP);
        ComposeShader composeShader = new ComposeShader(sweepGradient, radialGradient, PorterDuff.Mode.SRC_OVER);

        colorWheelPaint.setShader(composeShader);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawCircle(width / 2, height / 2, colorWheelRadius, colorWheelPaint);

        return bitmap;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            if (plusIcon.getBounds().contains(x, y)) {
                if (mColorAddedListener != null) {
                    mColorAddedListener.onColorAdded(colorHSV);
                }
            }
        case MotionEvent.ACTION_MOVE:
            int cy = y - getHeight() / 2;
            int cx = x - getHeight() / 2;
            double d = Math.sqrt(cx * cx + cy * cy);

            if (d <= colorWheelRadius) {

                colorHSV[0] = (float) (Math.toDegrees(Math.atan2(cy, cx)) + 180f);
                colorHSV[1] = Math.max(0f, Math.min(1f, (float) (d / colorWheelRadius)));
                colorHSV[2] = 1f;

                invalidate();
            } else if (blackButton.contains(x, y)) {
                colorHSV[0] = 0f;
                colorHSV[1] = 0f;
                colorHSV[2] = 0f;
            }
            if (mColorSelectedListener != null) {
                mColorSelectedListener.onColorSelected(colorHSV);
            }

            return true;
        }
        return super.onTouchEvent(event);
    }

    public void setColor(int color) {
        Color.colorToHSV(color, colorHSV);
    }

    public int getColor() {
        return Color.HSVToColor(colorHSV);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle state = new Bundle();
        state.putFloatArray("color", colorHSV);
        state.putParcelable("super", super.onSaveInstanceState());
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            colorHSV = bundle.getFloatArray("color");
            super.onRestoreInstanceState(bundle.getParcelable("super"));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    // Event Listener stuff
    public interface OnColorSelectedListener {
        void onColorSelected(float[] color);
    }

    public void setColorSelectedListener(OnColorSelectedListener listener) {
        mColorSelectedListener = listener;
    }

    public interface OnColorAddedListener {
        void onColorAdded(float[] color);
    }

    public void setColorAddedListener(OnColorAddedListener listener) {
        mColorAddedListener = listener;
    }

    // Sets the size of the plus icon.
    private void setPlusIconSize() {
        int centerWheelY = getHeight() / 2;
        int centerWheelX = centerWheelY;
        plusIcon.setBounds(centerWheelX + colorWheelRadius + outerPadding,
                           getHeight() / 2 + outerPadding,
                           getWidth() - outerPadding,
                           getHeight() - outerPadding);
    }

    // Sets the size of the black rectangle button.
    private void setBlackButtonSize() {
        int centerWheelY = getHeight() / 2;
        int centerWheelX = centerWheelY;
        blackButton.set(centerWheelX + colorWheelRadius + outerPadding,
                        0 + outerPadding,
                        getWidth() - outerPadding,
                        getWidth() - centerWheelX - colorWheelRadius - 2*outerPadding);
    }
}
