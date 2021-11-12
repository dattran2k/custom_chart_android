package com.example.myapplication;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;

public class CustomSeekBar extends View {
    private static final int DEFAULT_EDGE_LENGTH = 260;
    private static final int DEFAULT_ARC_WIDTH = 6;
    private static final float DEFAULT_ROTATE_ANGLE = 0;
    private static final int DEFAULT_BORDER_WIDTH = 0;
    private static final int DEFAULT_BORDER_COLOR = 0xffffffff;

    private static final int DEFAULT_THUMB_COLOR = 0xffffffff;
    private static final int DEFAULT_THUMB_WIDTH = 2;
    private static final int DEFAULT_THUMB_RADIUS = 3;
    private static final int DEFAULT_THUMB_SHADOW_RADIUS = 0;
    private static final int DEFAULT_THUMB_SHADOW_COLOR = 0xFF000000;

    private static final int DEFAULT_SHADOW_RADIUS = 0;

    private static final int THUMB_MODE_STROKE = 0;
    private static final int THUMB_MODE_FILL = 1;
    private static final int THUMB_MODE_FILL_STROKE = 2;

    private static final int DEFAULT_MAX_VALUE = 100;
    private static final int DEFAULT_MIN_VALUE = 0;

    private static final String KEY_PROGRESS_PRESENT = "PRESENT";


    private float[] mStepColor = {};

    private int[] mArcColors = {};
    private float mArcWidth;
    private float mRotateAngle;
    private int mBorderWidth;
    private int mBorderColor;

    private int mThumbColor;
    private float mThumbWidth;
    private float mThumbRadius;
    private float mThumbShadowRadius;
    private int mThumbShadowColor;
    private int mThumbMode;

    private int mShadowRadius;

    private float mMaxValue;
    private float mMinValue;

    private float mCenterX;
    private float mCenterY;

    private float mThumbX;
    private float mThumbY;

    private Path mSeekPath;
    private Path mBorderPath;
    private Paint mArcPaint;
    private Paint mThumbPaint;
    private Paint mBorderPaint;
    private Paint mShadowPaint;
    private Paint mTextPaint;

    private float[] mTempPos;
    private float[] mTempTan;
    private String[] mUpperText;
    private String[] mBottomText;

    private PathMeasure mSeekPathMeasure;

    private float mProgressPresent = 0;
    private Matrix mInvertMatrix;
    private Region mArcRegion;

    private Float width;
    private boolean topTextCenter = false;

    public CustomSeekBar(Context context) {
        this(context, null);
    }

    public CustomSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setSaveEnabled(true);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        initAttrs(context, attrs);
        initData();
        initPaint();
    }


    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CustomSeekBar);
        mArcColors = getArcColors(context, ta);
        mStepColor = getArcStep(context, ta);
        mUpperText = getArcString(context, ta, R.styleable.CustomSeekBar_arc_custom_string_upper);
        mBottomText = getArcString(context, ta, R.styleable.CustomSeekBar_arc_custom_string_bottom);
        mArcWidth = ta.getDimensionPixelSize(R.styleable.CustomSeekBar_arc_custom_width, dp2px(DEFAULT_ARC_WIDTH));
        mRotateAngle = ta.getFloat(R.styleable.CustomSeekBar_arc_custom_rotate_angle, DEFAULT_ROTATE_ANGLE);
        mMaxValue = ta.getInt(R.styleable.CustomSeekBar_arc_custom_max, DEFAULT_MAX_VALUE);
        mMinValue = ta.getInt(R.styleable.CustomSeekBar_arc_custom_min, DEFAULT_MIN_VALUE);

        if (mMaxValue <= mMinValue) {
            mMaxValue = DEFAULT_MAX_VALUE;
            mMinValue = DEFAULT_MIN_VALUE;
        }
        float progress = ta.getFloat(R.styleable.CustomSeekBar_arc_custom_progress, mMinValue);
        setProgress(progress);
        mBorderWidth = ta.getDimensionPixelSize(R.styleable.CustomSeekBar_arc_custom_border_width, dp2px(DEFAULT_BORDER_WIDTH));
        mBorderColor = ta.getColor(R.styleable.CustomSeekBar_arc_custom_border_color, DEFAULT_BORDER_COLOR);
        mThumbColor = ta.getColor(R.styleable.CustomSeekBar_arc_custom_thumb_color, DEFAULT_THUMB_COLOR);
        mThumbRadius = ta.getDimensionPixelSize(R.styleable.CustomSeekBar_arc_custom_thumb_radius, dp2px(DEFAULT_THUMB_RADIUS));
        mThumbShadowRadius = ta.getDimensionPixelSize(R.styleable.CustomSeekBar_arc_custom_thumb_shadow_radius, dp2px(DEFAULT_THUMB_SHADOW_RADIUS));
        mThumbShadowColor = ta.getColor(R.styleable.CustomSeekBar_arc_custom_thumb_shadow_color, DEFAULT_THUMB_SHADOW_COLOR);
        mThumbWidth = ta.getDimensionPixelSize(R.styleable.CustomSeekBar_arc_custom_thumb_width, dp2px(DEFAULT_THUMB_WIDTH));
        mThumbMode = ta.getInt(R.styleable.CustomSeekBar_arc_custom_thumb_mode, THUMB_MODE_STROKE);

        mShadowRadius = ta.getDimensionPixelSize(R.styleable.CustomSeekBar_arc_custom_shadow_radius, dp2px(DEFAULT_SHADOW_RADIUS));
        ta.recycle();
    }

    public void setStepColor(float[] mStepColor) {
        this.mStepColor = mStepColor;
        resetShaderColor();
    }

    public void setHeadText(String[] mUpperText) {
        this.mUpperText = mUpperText;
        postInvalidate();

    }

    public void setBottomText(String[] mBottomText) {
        this.mBottomText = mBottomText;
        postInvalidate();
    }

    private int[] getArcColors(Context context, TypedArray ta) {
        int[] ret;
        int resId = ta.getResourceId(R.styleable.CustomSeekBar_arc_custom_colors, 0);
        if (0 == resId) {
            resId = R.array.arc_colors_default;
        }
        ret = getColorsByArrayResId(context, resId);
        return ret;
    }

    private float[] getArcStep(Context context, TypedArray ta) {
        float[] ret;
        int resId = ta.getResourceId(R.styleable.CustomSeekBar_arc_custom_step, 0);
        if (0 == resId) {
            resId = R.array.arc_step;
        }
        ret = getStepByArrayResId(context, resId);
        return ret;
    }

    private String[] getArcString(Context context, TypedArray ta, int style) {
        String[] ret;
        int resId = ta.getResourceId(style, 0);
        if (0 == resId) {
            resId = R.array.arc_string_temp;
        }
        ret = getStringByArrayResId(context, resId);
        return ret;
    }

    private String[] getStringByArrayResId(Context context, int resId) {
        String[] ret;
        TypedArray colorArray = context.getResources().obtainTypedArray(resId);
        ret = new String[colorArray.length()];
        for (int i = 0; i < colorArray.length(); i++) {
            ret[i] = colorArray.getString(i);
        }
        return ret;
    }

    private float[] getStepByArrayResId(Context context, int resId) {
        float[] ret;
        TypedArray colorArray = context.getResources().obtainTypedArray(resId);
        ret = new float[colorArray.length()];
        for (int i = 0; i < colorArray.length(); i++) {
            ret[i] = colorArray.getFloat(i, 0);
        }
        return ret;
    }

    private int[] getColorsByArrayResId(Context context, int resId) {
        int[] ret;
        TypedArray colorArray = context.getResources().obtainTypedArray(resId);
        ret = new int[colorArray.length()];
        for (int i = 0; i < colorArray.length(); i++) {
            ret[i] = colorArray.getColor(i, 0);
        }
        return ret;
    }
    
    private void initData() {
        mSeekPath = new Path();
        mBorderPath = new Path();
        mSeekPathMeasure = new PathMeasure();
        mTempPos = new float[2];
        mTempTan = new float[2];
        mInvertMatrix = new Matrix();
        mArcRegion = new Region();
    }


    private void initPaint() {
        initArcPaint();
        initThumbPaint();
        initBorderPaint();
        initShadowPaint();
        initTextPaint();
    }

    private void initTextPaint() {
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.parseColor("#737678"));
        mTextPaint.setTextSize(30);
        mArcPaint.setAntiAlias(true);
    }


    private void initArcPaint() {
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStrokeWidth(mArcWidth);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeCap(Paint.Cap.ROUND);
    }


    private void initThumbPaint() {
        mThumbPaint = new Paint();
        mThumbPaint.setAntiAlias(true);
        mThumbPaint.setColor(mThumbColor);
        mThumbPaint.setStrokeWidth(mThumbWidth);
        mThumbPaint.setStrokeCap(Paint.Cap.ROUND);
        if (mThumbMode == THUMB_MODE_FILL) {
            mThumbPaint.setStyle(Paint.Style.FILL);
        } else if (mThumbMode == THUMB_MODE_FILL_STROKE) {
            mThumbPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        } else {
            mThumbPaint.setStyle(Paint.Style.STROKE);
        }
        mThumbPaint.setTextSize(10);
    }


    private void initBorderPaint() {
        mBorderPaint = new Paint();
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStrokeWidth(mBorderWidth);
        mBorderPaint.setStyle(Paint.Style.STROKE);
    }


    private void initShadowPaint() {
        mShadowPaint = new Paint();
        mShadowPaint.setAntiAlias(true);
        mShadowPaint.setStrokeWidth(mBorderWidth);
        mShadowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putFloat(KEY_PROGRESS_PRESENT, mProgressPresent);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            this.mProgressPresent = bundle.getFloat(KEY_PROGRESS_PRESENT);
            state = bundle.getParcelable("superState");
        }
        if (null != mOnProgressChangeListener) {
            mOnProgressChangeListener.onProgressChanged(this, getProgress(), false);
        }
        super.onRestoreInstanceState(state);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int ws = MeasureSpec.getSize(widthMeasureSpec);
        int wm = MeasureSpec.getMode(widthMeasureSpec);
        int hs = MeasureSpec.getSize(heightMeasureSpec);
        int hm = MeasureSpec.getMode(heightMeasureSpec);

        if (wm == MeasureSpec.UNSPECIFIED) {
            wm = MeasureSpec.EXACTLY;
            ws = dp2px(DEFAULT_EDGE_LENGTH);
        } else if (wm == MeasureSpec.AT_MOST) {
            wm = MeasureSpec.EXACTLY;
            ws = Math.min(dp2px(DEFAULT_EDGE_LENGTH), ws);
        }
        if (hm == MeasureSpec.UNSPECIFIED) {
            hm = MeasureSpec.EXACTLY;
            hs = dp2px(DEFAULT_EDGE_LENGTH);
        } else if (hm == MeasureSpec.AT_MOST) {
            hm = MeasureSpec.EXACTLY;
            hs = Math.min(dp2px(DEFAULT_EDGE_LENGTH), hs);
        }
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(ws, wm), MeasureSpec.makeMeasureSpec(hs, hm));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int safeW = w - getPaddingLeft() - getPaddingRight();
        int safeH = h - getPaddingTop() - getPaddingBottom();
        float edgeLength, startX, startY;
        float fix = mArcWidth / 2 + mBorderWidth;
        edgeLength = safeW - fix;
        startX = 0;
        startY = safeH / 2 - fix / 2;
        width = edgeLength - fix;
        Path test = new Path();
        test.moveTo(startX + fix, startY + fix);
        test.lineTo(startX + edgeLength, startY + fix);
        mCenterX = width / 2;
        mCenterY = edgeLength;
        mSeekPath.addPath(test);
        mSeekPathMeasure.setPath(mSeekPath, false);
        computeThumbPos(mProgressPresent);
        resetShaderColor();
        resetText();
        mInvertMatrix.reset();
        mInvertMatrix.preRotate(mRotateAngle, mCenterX, mCenterY);
        mArcPaint.getFillPath(mSeekPath, mBorderPath);
        mBorderPath.close();
        mArcRegion.setPath(mBorderPath, new Region(0, 0, w, h));
    }

    private void resetText() {

    }

    private void resetShaderColor() {
        if (width == null)
            return;
        int[] colors;
//        mArcColors = new int[]{R.color.red, R.color.white};
        ArrayList arrayColor = new ArrayList<Integer>();
        for (int i = 0; i < mArcColors.length * 2; i = i + 2) {
            arrayColor.add(mArcColors[i / 2]);
            arrayColor.add(mArcColors[i / 2]);
        }
        colors = new int[arrayColor.size()];
        for (int i = 0; i < arrayColor.size(); i++)
            colors[i] = (int) arrayColor.get(i);
        float pos[] = new float[colors.length];
        pos[0] = 0;
        pos[colors.length - 1] = 1;
        for (int i = 1; i < colors.length - 1; i++) {
            pos[i] = (mStepColor[(i - 1) / 2] - mMinValue) / (mMaxValue - mMinValue);
        }
        LinearGradient gradient = new LinearGradient(0, 0, width, 0, colors,
                pos,
                Shader.TileMode.CLAMP);
        mArcPaint.setShader(gradient);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.rotate(mRotateAngle, mCenterX, mCenterY);
        mShadowPaint.setShadowLayer(mShadowRadius * 2, 0, 0, getColor());
//        canvas.drawPath(mBorderPath, mShadowPaint);
        // Vẽ màu ?
        canvas.drawPath(mSeekPath, mArcPaint);
        // vẽ border ?
        if (mBorderWidth > 0) {
            canvas.drawPath(mBorderPath, mBorderPaint);
        }
        drawText(canvas);
        if (mThumbShadowRadius > 0) {
            mThumbPaint.setShadowLayer(mThumbShadowRadius, 0, 0, mThumbShadowColor);
            canvas.drawCircle(mThumbX, mThumbY, mThumbRadius, mThumbPaint);
            mThumbPaint.clearShadowLayer();
        }
        // Vẽ icon
        canvas.drawCircle(mThumbX, mThumbY, mThumbRadius, mThumbPaint);
        canvas.restore();
    }

    private void drawText(Canvas canvas) {
        for (int i = 0; i < mStepColor.length; i++) {
            String text;
            if (i >= mUpperText.length) {
                text = "emptyTop";
            } else
                text = mUpperText[i];
            Rect bounds = new Rect();
            mTextPaint.getTextBounds(text, 0, text.length(), bounds);
            int widthText = bounds.width();
            if (topTextCenter) {
                float preLocation = 0;
                if (i != 0) {
                    preLocation = ((mStepColor[i - 1] - mMinValue) / (mMaxValue - mMinValue)) * width;
                }
                float currentLocation;
                if (i < mStepColor.length)
                    currentLocation = ((mStepColor[i] - mMinValue) / (mMaxValue - mMinValue)) * width;
                else
                    currentLocation = width;
                canvas.drawTextOnPath(text, mSeekPath, preLocation + (currentLocation - preLocation) / 2 - widthText / 2, -30, mTextPaint);
            } else
                canvas.drawTextOnPath(text, mSeekPath, +((mStepColor[i] - mMinValue) / (mMaxValue - mMinValue)) * width - widthText / 2, -30, mTextPaint);
        }
        for (int i = 0; i < mStepColor.length + 1; i++) {
            String text;
            if (i >= mBottomText.length) {
                text = "emptyBT";
            } else
                text = mBottomText[i];
            float y = 50;
            text = delegateText(text);
            for (String line : text.split("\n")) {
                Rect bounds = new Rect();
                mTextPaint.getTextBounds(line, 0, line.length(), bounds);
                int widthText = bounds.width();
                float preLocation = 0;
                if (i != 0) {
                    preLocation = ((mStepColor[i - 1] - mMinValue) / (mMaxValue - mMinValue)) * width;
                }
                float currentLocation;
                if (i < mStepColor.length)
                    currentLocation = ((mStepColor[i] - mMinValue) / (mMaxValue - mMinValue)) * width;
                else
                    currentLocation = width;
                canvas.drawTextOnPath(line, mSeekPath, preLocation + (currentLocation - preLocation) / 2 - widthText / 2, y, mTextPaint);
                y += -mTextPaint.ascent() + mTextPaint.descent();
            }
        }
    }

    private String delegateText(String text) {
        StringBuilder text2 = new StringBuilder(text);
        int count = 0;
        for (int i = 0; i < text2.length() - 1; i++) {
            if (text2.charAt(i) == ' ') {
                count++;
                if (count % 2 == 0)
                    text2.setCharAt(i, '\n');
            }
        }
        return text2.toString();
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources().getDisplayMetrics());
    }

    private void computeThumbPos(float present) {
        if (present < 0) present = 0;
        if (present > 1) present = 1;
        if (null == mSeekPathMeasure) return;
        float distance = mSeekPathMeasure.getLength() * present;
        mSeekPathMeasure.getPosTan(distance, mTempPos, mTempTan);
        mThumbX = mTempPos[0];
        mThumbY = mTempPos[1];
    }

    public int getColor() {
        return getColor(mProgressPresent);
    }


    private int getColor(float radio) {
        float distance = 1.0f / (mArcColors.length - 1);
        int startColor;
        int endColor;
        if (radio >= 1) {
            return mArcColors[mArcColors.length - 1];
        }
        for (int i = 0; i < mArcColors.length; i++) {
            if (radio <= i * distance) {
                if (i == 0) {
                    return mArcColors[0];
                }
                startColor = mArcColors[i - 1];
                endColor = mArcColors[i];
                float areaRadio = getAreaRadio(radio, distance * (i - 1), distance * i);
                return getColorFrom(startColor, endColor, areaRadio);
            }
        }
        return -1;
    }


    private float getAreaRadio(float radio, float startPosition, float endPosition) {
        return (radio - startPosition) / (endPosition - startPosition);
    }


    private int getColorFrom(int startColor, int endColor, float radio) {
        int redStart = Color.red(startColor);
        int blueStart = Color.blue(startColor);
        int greenStart = Color.green(startColor);
        int redEnd = Color.red(endColor);
        int blueEnd = Color.blue(endColor);
        int greenEnd = Color.green(endColor);
        int red = (int) (redStart + ((redEnd - redStart) * radio + 0.5));
        int greed = (int) (greenStart + ((greenEnd - greenStart) * radio + 0.5));
        int blue = (int) (blueStart + ((blueEnd - blueStart) * radio + 0.5));
        return Color.argb(255, red, greed, blue);
    }


    public void setProgress(float progress) {
        System.out.println("setProgress = " + progress);
        if (progress > mMaxValue) progress = mMaxValue;
        if (progress < mMinValue) progress = mMinValue;
        mProgressPresent = (progress - mMinValue) * 1.0f / (mMaxValue - mMinValue);
        System.out.println("setProgress present = " + mProgressPresent);
        if (null != mOnProgressChangeListener) {
            mOnProgressChangeListener.onProgressChanged(this, progress, false);
        }
        computeThumbPos(mProgressPresent);
        postInvalidate();
    }

    public float getProgress() {
        return (mProgressPresent * (mMaxValue - mMinValue)) + mMinValue;
    }

    public void setArcColors(int[] colors) {
        mArcColors = colors;
        resetShaderColor();
        postInvalidate();
    }


    public void setMaxValue(float max) {
        mMaxValue = max;
    }

    public void setMinValue(float min) {
        mMinValue = min;
    }


    public void setArcColors(int colorArrayRes) {
        setArcColors(getColorsByArrayResId(getContext(), colorArrayRes));
    }


    private OnProgressChangeListener mOnProgressChangeListener;

    public void setOnProgressChangeListener(OnProgressChangeListener onProgressChangeListener) {
        mOnProgressChangeListener = onProgressChangeListener;
    }

    public void setTopTextCenter(boolean b) {
        topTextCenter = b;
    }

    public interface OnProgressChangeListener {

        void onProgressChanged(CustomSeekBar seekBar, float progress, boolean isUser);


        void onStartTrackingTouch(CustomSeekBar seekBar);

        void onStopTrackingTouch(CustomSeekBar seekBar);
    }
    // endregion -----------------------------------------------------------------------------------
}