package com.shortstracker.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.View

class RingView(context: Context) : View(context) {

    var count = 0
    var limit = 30

    private val density = resources.displayMetrics.density
    private val stroke = 16 * density
    private val rect = RectF()

    private val track = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
        color = Color.parseColor("#2A2E3D")
    }
    private val arc = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
        strokeCap = Paint.Cap.ROUND
    }
    private val big = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 56 * density
        typeface = Typeface.DEFAULT_BOLD
    }
    private val small = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B6BAC9")
        textAlign = Paint.Align.CENTER
        textSize = 16 * density
    }

    fun update(count: Int, limit: Int) {
        this.count = count
        this.limit = limit
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = (240 * density).toInt()
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        val pad = stroke / 2 + 4 * density
        rect.set(pad, pad, width - pad, height - pad)
        canvas.drawOval(rect, track)

        val over = count >= limit
        arc.color = Color.parseColor(if (over) "#FF4D4D" else "#FF7A1A")
        val sweep = 360f * (count.toFloat() / limit.coerceAtLeast(1)).coerceAtMost(1f)
        canvas.drawArc(rect, -90f, sweep, false, arc)

        val cx = width / 2f
        val cy = height / 2f
        canvas.drawText(count.toString(), cx, cy + 14 * density, big)
        canvas.drawText("of $limit today", cx, cy + 42 * density, small)
    }
}
