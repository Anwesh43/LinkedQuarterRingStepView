package com.anwesh.uiprojects.quarterringstepview

/**
 * Created by anweshmishra on 28/10/18.
 */

import android.app.Activity
import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.content.Context
import android.content.pm.ActivityInfo

val nodes : Int = 5
val rings : Int = 4

fun Canvas.drawQRSNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val deg : Float = 360f / rings
    val r : Float = gap / 3
    val rRing : Float = gap/15
    val scGap : Float = 1f / rings
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = Math.min(w, h) / 90
    paint.strokeCap = Paint.Cap.ROUND
    paint.color = Color.parseColor("#673AB7")
    save()
    translate(gap + i * gap, h/2)
    for (j in 0..rings - 1) {
        val sc : Float = Math.min(scGap, Math.max(0f, scale - scGap * j)) * rings
        save()
        rotate(deg * j)
        translate(0f, -(r - rRing))
        drawArc(RectF(-rRing, -rRing, rRing, rRing), -90f, 360f * sc, false, paint)
        restore()
    }
    restore()
}

class QuarterRingStepView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += (0.1f / rings) * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class QRSNode(var i : Int, val state : State = State()) {

        private var prev : QRSNode? = null
        private var next : QRSNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = QRSNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawQRSNode(i, state.scale, paint)
            prev?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : QRSNode {
            var curr : QRSNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class QuarterRingStep(var i : Int) {

        private var curr : QRSNode = QRSNode(0)

        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i , scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : QuarterRingStepView) {

        private val qrs : QuarterRingStep = QuarterRingStep(0)

        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            qrs.draw(canvas, paint)
            animator.animate {
                qrs.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            qrs.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : QuarterRingStepView {
            val view : QuarterRingStepView = QuarterRingStepView(activity)
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            activity.setContentView(view)
            return view
        }
    }
}