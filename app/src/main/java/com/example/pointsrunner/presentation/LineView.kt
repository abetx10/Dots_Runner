package com.example.pointsrunner.presentation

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class LineView(context: Context) : View(context) {
    companion object {
        const val LINE_THICKNESS = 15f
        const val SPEED = 6
        const val FRAME_DELAY = 16L
    }

    private var screenWidth = 0
    private var screenHeight = 0

    private val paint = Paint().apply {
        color = Color.WHITE
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val updateJob: Job

    private val lines = mutableListOf<Line>()

    init {
        updateJob = coroutineScope.launch {
            while (true) {
                update()
                invalidate()
                delay(FRAME_DELAY)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenWidth = w
        screenHeight = h

        lines.clear()
        repeat(4) { index ->
            val y = -LINE_THICKNESS - screenHeight * index * 0.22f
            val length = calculateNewLength()
            val startX = if (index % 2 == 0) 0f else screenWidth - length
            lines.add(Line(startX, y, length))
        }
    }

    private fun update() {
        lines.forEach { line ->
            line.y += SPEED
            if (line.y > screenHeight) {
                line.y = -LINE_THICKNESS
                line.length = calculateNewLength()
                line.startX = if (lines.indexOf(line) % 2 == 0) 0f else screenWidth - line.length
            }
        }
    }

    private fun calculateNewLength(): Float {
        return screenWidth * 0.4f + Random.nextFloat() * (width * 0.25f)
    }

    fun stop() {
        coroutineScope.coroutineContext.cancelChildren()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        lines.forEach { line ->
            canvas.drawRect(line.startX, line.y, line.startX + line.length, line.y + LINE_THICKNESS, paint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        updateJob.cancel()
    }

    fun getLines(): List<RectF> {
        return lines.map { line ->
            RectF(line.startX, line.y, line.startX + line.length, line.y + LINE_THICKNESS)
        }
    }

    private data class Line(var startX: Float, var y: Float, var length: Float)
}