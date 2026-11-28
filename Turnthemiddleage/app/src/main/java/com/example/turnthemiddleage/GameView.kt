package com.example.turnthemiddleage

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.SurfaceView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class GameView(context: Context, attrs: AttributeSet? = null) : SurfaceView(context, attrs), Runnable {
    private var thread: Thread? = null
    private var isPlaying = false
    private var paint = Paint()

    private val levelWidth = 4000f
    private var cameraX = 0f

    private var playerX = 200f
    private var playerY = 0f
    private val playerWidth = 100f
    private val playerHeight = 200f
    private var velocityY = 0f
    private val gravity = 1.5f
    private val jumpStrength = -35f
    private var isGrounded = false
    private var failed = false
    var onGoalReached: (() -> Unit)? = null
    private var goalReached = false
    var onFailed: (() -> Unit)? = null
    private var failReported = false

    var moveLeft = false
    var moveRight = false

    private var screenHeight = 2000f
    private var groundY = 2000f

    private var platforms = listOf(
        RectF(600f, 1650f, 950f, 1700f),
        RectF(100f, 1200f, 450f, 1250f),
        RectF(1200f, 1500f, 1600f, 1550f),
        RectF(1800f, 1200f, 2200f, 1250f),
        RectF(2500f, 1650f, 2900f, 1700f)
    )
    private var goalrectF = RectF(3950f, groundY - 100f, 4000f, groundY)

    private val backgroundbitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.background)
    private val backgroundRect = Rect()
    private val playerbitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.player)
    private val playerRect = RectF()

    init {
        enableFullscreen()
    }

    private fun enableFullscreen() {
        val activity = context as? Activity ?: return
        val window = activity.window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenHeight = h.toFloat()
        groundY = screenHeight
        goalrectF.set(3950f, groundY - 100f, 4000f, groundY)
    }

    override fun run() {
        while (isPlaying) {
            update()
            draw()
            Thread.sleep(1)
        }
    }

    private fun update() {
        if (moveLeft) playerX -= 12f
        if (moveRight) playerX += 12f
        playerX = playerX.coerceIn(0f, levelWidth - playerWidth)

        velocityY += gravity
        playerY += velocityY

        if (playerY + playerHeight >= groundY) {
            playerY = groundY - playerHeight
            velocityY = 0f
            failed = true
        }

        isGrounded = false
        updatePlayerRect()
        for (plat in platforms) {
            if (velocityY > 0 && RectF.intersects(playerRect, plat)) {
                if (playerY + playerHeight - velocityY <= plat.top) {
                    playerY = plat.top - playerHeight
                    velocityY = 0f
                    isGrounded = true
                }
            }
        }
        updatePlayerRect()

        if (width > 0) {
            val maxCameraX = (levelWidth - width).coerceAtLeast(0f)
            cameraX = (playerX + playerWidth / 2f - width / 2f).coerceIn(0f, maxCameraX)
        }
        if (!goalReached && RectF.intersects(playerRect, goalrectF)) {
            goalReached = true
            onGoalReached?.invoke()
        }
        if (failed && !goalReached && !failReported) {
            failReported = true
            onFailed?.invoke()
        }
        moveLeft = false
        moveRight = false
    }

    private fun updatePlayerRect() {
        playerRect.set(playerX, playerY, playerX + playerWidth, playerY + playerHeight)
    }

    private fun draw() {
        if (holder.surface.isValid) {
            val canvas: Canvas = holder.lockCanvas()

            canvas.save()
            canvas.translate(-cameraX, 0f)

            backgroundRect.set(0, 0, levelWidth.toInt(), canvas.height)
            canvas.drawBitmap(backgroundbitmap, null, backgroundRect, null)

            paint.color = Color.DKGRAY
            for (plat in platforms) {
                canvas.drawRect(plat, paint)
            }

            paint.color = Color.TRANSPARENT
            canvas.drawRect(goalrectF, paint)

            canvas.drawBitmap(playerbitmap, null, playerRect, null)

            canvas.restore()
            holder.unlockCanvasAndPost(canvas)
        }
    }

    fun jump() {
        if (isGrounded) {
            velocityY = jumpStrength
            isGrounded = false
        }
    }

    fun resume() {
        if (isPlaying) return
        isPlaying = true
        thread = Thread(this)
        thread?.start()
    }

    fun pause() {
        isPlaying = false
        thread?.join()
    }

    fun moveRight() {
        moveRight = true
    }

    fun moveLeft() {
        moveLeft = true
    }

    fun loadLevel(newPlatforms: List<RectF>) {
        platforms = newPlatforms
        playerX = 200f
        playerY = 0f
        velocityY = 0f
        cameraX = 0f
    }
}