package com.example.pointsrunner.presentation

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tyrantgit.explosionfield.ExplosionField
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class CircleView(context: Context, private val lineView: LineView) : View(context) {
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f
    private var dot1X = 0f
    private var dot1Y = 0f
    private var dot2X = 0f
    private var dot2Y = 0f
    private var angle = Math.PI / 2.0
    private var rotationDirection = 1.0
    private var isDirectionChangeLocked = false
    private var lockDuration = 0L
    private var rotationSpeed = 4.0
    private val gameOverThreshold = 90f
    private var startTime = 0L

    private lateinit var scoreTextView: TextView
    private val paint = Paint()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val updateJob: Job
    private var onGameOverListener: OnGameOverListener? = null
    private val explosionField = ExplosionField.attach2Window(context as Activity)
    private val soundManager = SoundManager(context)

    companion object {
        const val DOT_RADIUS = 15f
        const val LOCK_DURATION = 300L
        const val FRAME_DELAY = 16L
        const val CIRCLE_RADIUS = 18f
    }

    init {
        soundManager.playMediaPlayer()
        startTime = System.currentTimeMillis()

        paint.color = Color.RED
        updateJob = coroutineScope.launch {
            while (true) {
                update()
                checkCollision()
                updateScoreTextView()
                invalidate()
                delay(FRAME_DELAY)
            }
        }
    }

    fun setScoreTextView(textView: TextView) {
        scoreTextView = textView
    }

    private fun updateScoreTextView() {
        val elapsedTime = System.currentTimeMillis() - startTime
        scoreTextView.text = (elapsedTime / 1000).toString()
    }

    fun toggleRotationDirection() {
        if (!isDirectionChangeLocked) {
            rotationDirection = -rotationDirection
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radius = 0.90f * Math.min(w, h) / 2f
        calculateDotPositions()
    }

    private fun update() {
        angle += 2 * Math.PI / 360.0 * rotationDirection * rotationSpeed
        calculateDotPositions()
    }

    private fun checkCollision(): Boolean {
        if (isDirectionChangeLocked) return false

        val lines = lineView.getLines()
        val dot1Circle = Circle(dot1X, dot1Y, CIRCLE_RADIUS)
        val dot2Circle = Circle(dot2X, dot2Y, CIRCLE_RADIUS)

        var dot1Collision = false
        var dot2Collision = false
        var dot1Distance = Float.MAX_VALUE
        var dot2Distance = Float.MAX_VALUE

        for (line in lines) {
            if (circleIntersectsLine(dot1Circle, line)) {
                dot1Collision = true
            } else {
                dot1Distance = min(dot1Distance, distanceFromCircleToLine(dot1Circle, line))
            }
            if (circleIntersectsLine(dot2Circle, line)) {
                dot2Collision = true
            } else {
                dot2Distance = min(dot2Distance, distanceFromCircleToLine(dot2Circle, line))
            }
        }

        if (dot1Collision || dot2Collision) {
            soundManager.playTouchSound()
            rotationDirection = -rotationDirection
            lockDirectionChange()
            return true
        }

        // Game Over condition check
        if (dot1Distance < gameOverThreshold && dot2Distance < gameOverThreshold) {
            onGameOver()
            lineView.stop()
            updateJob.cancel()

            // Call explodeDots() when the game is over
            soundManager.playEndSound()
            explodeDots()
            hideDots()

            return true
        }

        return false
    }


    private fun distanceFromCircleToLine(circle: Circle, line: RectF): Float {
        val deltaX = circle.centerX - max(line.left, min(circle.centerX, line.right))
        val deltaY = circle.centerY - max(line.top, min(circle.centerY, line.bottom))
        return kotlin.math.sqrt(deltaX * deltaX + deltaY * deltaY)
    }

    private fun lockDirectionChange() {
        isDirectionChangeLocked = true
        lockDuration = LOCK_DURATION

        coroutineScope.launch {
            delay(lockDuration)
            isDirectionChangeLocked = false
        }
    }

    private fun circleIntersectsLine(circle: Circle, line: RectF): Boolean {
        val deltaX = circle.centerX - max(line.left, min(circle.centerX, line.right))
        val deltaY = circle.centerY - max(line.top, min(circle.centerY, line.bottom))
        return (deltaX * deltaX + deltaY * deltaY) < (circle.radius * circle.radius)
    }

    private fun calculateDotPositions() {
        dot1X = (centerX + radius * cos(angle)).toFloat()
        dot1Y = (centerY + radius * sin(angle)).toFloat()
        dot2X = (centerX + radius * cos(angle + Math.PI)).toFloat()
        dot2Y = (centerY + radius * sin(angle + Math.PI)).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(dot1X, dot1Y, DOT_RADIUS, paint)
        canvas.drawCircle(dot2X, dot2Y, DOT_RADIUS, paint)
    }

    private fun checkInitialized(): Boolean {
        return width > 0 && height > 0
    }

    private fun explodeDots() {
        if (!checkInitialized()) {
            return
        }

        explosionField.explode(createViewFromDot(dot1X, dot1Y))
        explosionField.explode(createViewFromDot(dot2X, dot2Y))
    }

    private fun createViewFromDot(x: Float, y: Float): View {
        val view = View(context)
        view.x = x - 15f
        view.y = y - 15f
        view.layoutParams = ViewGroup.LayoutParams(30, 30)
        view.setBackgroundColor(Color.RED)

        // Measure and layout the view before adding it to the parent
        val widthSpec = View.MeasureSpec.makeMeasureSpec(30, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(30, View.MeasureSpec.EXACTLY)
        view.measure(widthSpec, heightSpec)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        (parent as ViewGroup).addView(view)
        return view
    }

    private fun hideDots() {
        visibility = View.INVISIBLE
    }

    fun setOnGameOverListener(listener: OnGameOverListener) {
        onGameOverListener = listener
    }

    private fun onGameOver() {
        onGameOverListener?.onGameOver()
        soundManager.stopMediaPlayer()
        soundManager.release()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        updateJob.cancel()
        soundManager.release()
    }
}