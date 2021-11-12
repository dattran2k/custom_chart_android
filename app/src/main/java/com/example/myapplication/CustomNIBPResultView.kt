package com.example.myapplication

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

class CustomNIBPResultView(private val mContext: Context, attrs: AttributeSet) :
    View(mContext, attrs, 0) {
    private val listHeight = listOf(40, 90, 120, 140, 160, 180).asReversed()
    private val listWidth = listOf(40, 60, 80, 90, 100, 120).asReversed()
    private lateinit var mThumbPaint: Paint
    private lateinit var mReactPaint: Paint
    private lateinit var mReactBorderPaint: Paint
    private lateinit var mTextTitlePaint: Paint
    private lateinit var mTextNumberPaint: Paint
    private var mThumbX = 0f
    private var mThumbY = 0f
    var widthRect: Float = 0F
    var heightRect: Float = 0F
    val listTitle = listOf(
        "Low**",
        "Normal",
        "Prehyprertension",
        "High: Stage 1 hyprertension ",
        "High: Stage 2 hyprertension "
    ).asReversed()
    private val listColorRes = listOf(
        R.color.nibp_40_90,
        R.color.nibp_90_120,
        R.color.nibp_120_140,
        R.color.nibp_140_160,
        R.color.nibp_160_180,
    ).asReversed()
    private var currentPositionXY: Array<Int> = arrayOf(200, 200)
    fun setCurrentPosition(value: Array<Int>) {
        currentPositionXY = value
        computeThumbPos(value)
        postInvalidate()
    }

    companion object {
        const val THUMB_WIDTH = 19F
        const val THUMB_RADIUS = 20F
        private const val DEFAULT_EDGE_LENGTH = 260
        private const val DEFAULT_BORDER_WIDTH = 5
        private const val MAX_HEIGHT = 180
        private const val MAX_WIDTH = 120
        private const val MIN_WIDTH = 40
        private const val MIN_HEIGHT = 40
        private const val FIX_PADDING = 100F
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var ws = MeasureSpec.getSize(widthMeasureSpec)
        var wm = MeasureSpec.getMode(widthMeasureSpec)
        var hs = MeasureSpec.getSize(heightMeasureSpec)
        var hm = MeasureSpec.getMode(heightMeasureSpec)
        if (wm == MeasureSpec.UNSPECIFIED) {
            wm = MeasureSpec.EXACTLY
            ws = dp2px(DEFAULT_EDGE_LENGTH)
        } else if (wm == MeasureSpec.AT_MOST) {
            wm = MeasureSpec.EXACTLY
            ws = Math.min(dp2px(DEFAULT_EDGE_LENGTH), ws)
        }
        if (hm == MeasureSpec.UNSPECIFIED) {
            hm = MeasureSpec.EXACTLY
            hs = dp2px(DEFAULT_EDGE_LENGTH)
        } else if (hm == MeasureSpec.AT_MOST) {
            hm = MeasureSpec.EXACTLY
            hs = Math.min(dp2px(DEFAULT_EDGE_LENGTH), hs)
        }
        setMeasuredDimension(
            MeasureSpec.makeMeasureSpec(ws, wm),
            MeasureSpec.makeMeasureSpec(hs, hm)
        )
    }

    init {
        isSaveEnabled = true
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        initThumbPaint()
        initRectPaint()
        initReactBorderPant()
        initTextTitlePaint()
        initTextNumberPaint()
    }

    private fun initTextNumberPaint() {
        mTextNumberPaint = Paint()
        mTextNumberPaint.color = Color.parseColor("#737678")
        mTextNumberPaint.textSize = px2dp(14)
        mTextNumberPaint.isAntiAlias = true
    }

    private fun initTextTitlePaint() {
        mTextTitlePaint = Paint()
        mTextTitlePaint.color = Color.parseColor("#FFFFFF")
        mTextTitlePaint.textSize = px2dp(12)
        mTextTitlePaint.isAntiAlias = true
    }

    private fun initReactBorderPant() {
        mReactBorderPaint = Paint()
        mReactBorderPaint.isAntiAlias = true
        mReactBorderPaint.strokeWidth = DEFAULT_BORDER_WIDTH.toFloat()
        mReactBorderPaint.color = getColor(R.color.white)
        mReactBorderPaint.style = Paint.Style.STROKE
        mReactBorderPaint.strokeCap = Paint.Cap.ROUND
    }

    private fun initRectPaint() {
        mReactPaint = Paint()
        mReactPaint.isAntiAlias = true
        mReactPaint.style = Paint.Style.FILL
        mReactPaint.strokeCap = Paint.Cap.ROUND
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val safeW = w - paddingLeft - paddingRight
        val safeH = h - paddingTop - paddingBottom
        widthRect = safeW - FIX_PADDING*2
        heightRect = safeH - FIX_PADDING*2
        computeThumbPos(currentPositionXY)
    }


    private fun initThumbPaint() {
        mThumbPaint = Paint()
        mThumbPaint.isAntiAlias = true
        mThumbPaint.color = getColor(R.color.white)
        mThumbPaint.strokeWidth = THUMB_WIDTH
        mThumbPaint.strokeCap = Paint.Cap.ROUND
        mThumbPaint.style = Paint.Style.STROKE
    }

    private fun computeThumbPos(present: Array<Int>) {
        mThumbX =
            ((present[0].toFloat() - MIN_WIDTH) / (MAX_WIDTH - MIN_WIDTH).toFloat()) * widthRect + FIX_PADDING
        mThumbY = (MAX_HEIGHT - present[1].toFloat()) / (MAX_HEIGHT - MIN_HEIGHT) * heightRect + FIX_PADDING
        if(mThumbX < FIX_PADDING)
            mThumbX = FIX_PADDING
        else if(mThumbX > FIX_PADDING + widthRect)
            mThumbX =FIX_PADDING + widthRect

        if(mThumbY < FIX_PADDING)
            mThumbY = FIX_PADDING
        else if(mThumbY > FIX_PADDING + heightRect)
            mThumbY =FIX_PADDING + heightRect

    }

    private fun dp2px(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    private fun px2dp(px: Int): Float {
        return px * resources.displayMetrics.density
    }

    private fun getColor(@ColorRes idColor: Int) = ContextCompat.getColor(mContext, idColor)
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null) {
            drawRect(canvas)
            canvas.drawCircle(mThumbX, mThumbY, THUMB_RADIUS, mThumbPaint)
        }

    }

    private fun drawRect(canvas: Canvas) {
        listHeight.forEachIndexed { index, value ->
            val left = FIX_PADDING
            var top = heightRect + FIX_PADDING
            var right = FIX_PADDING
            val bottom = heightRect + FIX_PADDING
            if (index < listColorRes.size) {
                mReactPaint.color = getColor(listColorRes[index])
                right =
                    ((listWidth[index] - MIN_WIDTH).toFloat() / (MAX_WIDTH - MIN_WIDTH)) * widthRect + FIX_PADDING
                top =
                    ((MAX_HEIGHT - listHeight[index]).toFloat() / (MAX_HEIGHT - MIN_HEIGHT)) * heightRect + FIX_PADDING
                canvas.drawRoundRect(
                    left,
                    top,
                    right,
                    bottom,
                    10F,
                    10F,
                    mReactBorderPaint
                )
                canvas.drawRoundRect(
                    left,
                    top,
                    right,
                    bottom,
                    10F,
                    10F,
                    mReactPaint
                )
                drawContentTitle(canvas, left, top, listTitle[index])
            }
            drawStraitTitle(
                canvas,
                left - 20,
                top + if (index < listTitle.size) 20 else -10,
                if (index == 0) "$value+" else value.toString()
            )
            drawLineTitle(
                canvas,
                right,
                bottom + 20,
                if (index == 0) "${listWidth[index]}+" else listWidth[index].toString()
            )
        }
    }

    private fun drawLineTitle(canvas: Canvas, left: Float, bottom: Float, text: String) {
        val bounds = Rect()
        mTextNumberPaint.getTextBounds(text, 0, text.length, bounds)
        val width = bounds.width()
        canvas.drawText(text, left - width / 2, bottom + 20F, mTextNumberPaint)
    }

    private fun drawStraitTitle(canvas: Canvas, left: Float, top: Float, text: String) {
        val bounds = Rect()
        mTextNumberPaint.getTextBounds(text, 0, text.length, bounds)
        val width = bounds.width()
        canvas.drawText(text, left - width, top, mTextNumberPaint)

    }

    private fun drawContentTitle(canvas: Canvas, left: Float, top: Float, text: String) {
        canvas.drawText(text, left + px2dp(8), top + px2dp(16), mTextTitlePaint)
    }
}