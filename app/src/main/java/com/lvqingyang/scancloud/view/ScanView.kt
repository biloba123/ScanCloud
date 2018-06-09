package com.lvqingyang.scancloud.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.lvqingyang.mylibrary.helper.DisplayHelper.dp2px
import com.lvqingyang.scancloud.R


class ScanView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null,
                                         defStyleAttr: Int = 0)
    : View(context, attributeSet, defStyleAttr) {

    private val scanPaint: Paint

    private val stokePaint: Paint

    private val paint: Paint

    private val bgPaint: Paint

    private val shapePaint: Paint

    private val lineStoke: Int

    init {
        scanPaint=Paint().apply {
            isAntiAlias = true
            color = context.getColor(R.color.scan_indicate)
            strokeWidth = dp2px(context, 2).toFloat()
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_ATOP)
        }

        stokePaint=Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            strokeWidth = dp2px(context, 2).toFloat()
            style = Paint.Style.STROKE
        }

        paint=Paint().apply {
            isAntiAlias = true
            alpha = 125
        }

        bgPaint=Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
        }

        shapePaint=Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            xfermode = PorterDuffXfermode(PorterDuff.Mode.XOR)
        }

        lineStoke=dp2px(context, 1)

    }

    private var w = 0f
    private var h = 0f
    private var d = 0f //六边形到边到内切圆的距离
    //设置一个Bitmap
    private lateinit var bitmap: Bitmap
    //创建该Bitmap的画布
    private lateinit var bitmapCanvas: Canvas
    private var currentH = -1f
    private var ani: ValueAnimator? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        this.w = w.toFloat()
        this.h = h.toFloat()
        this.d = (w / 4 * (2 - Math.sqrt(3.0))).toFloat()//六边形到边到内切圆的距离
    }

    fun startScan() {
        ani?.cancel()
        ani = ValueAnimator.ofFloat(h / 2 - w / 2, h / 2 + w / 2)
                .apply {
                    addUpdateListener {
                        currentH = it.animatedValue as Float
                        invalidate()
                    }
                    interpolator = LinearInterpolator()
                    repeatCount = -1
                    duration = 5000
                    start()
                }
    }

    fun stopScan() {
        ani?.cancel()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444)
        bitmapCanvas = Canvas(bitmap)
        bitmapCanvas.drawRect(RectF(0f, 0f, w, h), bgPaint)
        val hexagonPath = getHexagonPath()
        bitmapCanvas.drawPath(hexagonPath, shapePaint)
        if (currentH >= 0f) {
            bitmapCanvas.drawLine(0f, currentH, w, currentH + lineStoke, scanPaint)
        }
        bitmapCanvas.drawPath(hexagonPath, stokePaint)
        canvas?.drawBitmap(bitmap, 0f, 0f, paint)
    }

    fun getHexagonPath(): Path {
        val mPath = Path()

        val r = w / 4

        val p1x = r * 3

        val p2x = w
        val p2y = w / 2

        val p3y = w - d

        val p5x = 0f

        mPath.reset()
        mPath.moveTo(r, d)
        mPath.lineTo(p1x, d)
        mPath.lineTo(p2x, p2y)
        mPath.lineTo(p1x, p3y)
        mPath.lineTo(r, p3y)
        mPath.lineTo(p5x, p2y)
        mPath.lineTo(r, d)
        mPath.offset(0f, (h / 2 - d) / 2)

        val mMatrix = Matrix()
        val bounds = RectF()
        mPath.computeBounds(bounds, true)
        mMatrix.postRotate(90f, bounds.centerX(), bounds.centerY())
        mPath.transform(mMatrix)


        return mPath
    }
}