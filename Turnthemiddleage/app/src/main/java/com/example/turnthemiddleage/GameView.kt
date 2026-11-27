package com.example.turnthemiddleage

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.SurfaceView

class GameView(context: Context, attrs: AttributeSet? = null) : SurfaceView(context, attrs), Runnable {
    private var thread: Thread? = null
    private var isPlaying = false
    private var paint = Paint()

    private val levelWidth = 4000f
    private var cameraX = 0f

    private var playerX = 200f
    private var playerY = 0f
    private var playerSize = 100f
    private var velocityY = 0f
    private val gravity = 1.5f
    private val jumpStrength = -35f
    private var isGrounded = false

    var moveLeft = false
    var moveRight = false

    private var groundY = 2000f
    private var platforms = listOf(
        RectF(600f, 1650f, 950f, 1700f),
        RectF(100f, 1200f, 450f, 1250f),
        RectF(1200f, 1500f, 1600f, 1550f),
        RectF(1800f, 1200f, 2200f, 1250f),
        RectF(2500f, 1650f, 2900f, 1700f)
    )

    override fun run() {
        while (isPlaying) {
            update()
            draw()
            Thread.sleep(16)
        }
    }

    private fun update() {
        if (moveLeft) playerX -= 12f
        if (moveRight) playerX += 12f
        playerX = playerX.coerceIn(0f, levelWidth - playerSize)

        velocityY += gravity
        playerY += velocityY

        if (playerY + playerSize >= groundY) {
            playerY = groundY - playerSize
            velocityY = 0f
            isGrounded = true
        }

        val playerRect = RectF(playerX, playerY, playerX + playerSize, playerY + playerSize)

        for (plat in platforms) {
            if (velocityY > 0 && RectF.intersects(playerRect, plat)) {
                if (playerY + playerSize - velocityY <= plat.top) {
                    playerY = plat.top - playerSize
                    velocityY = 0f
                    isGrounded = true
                }
            }
        }
        if (width > 0) {
            val maxCameraX = (levelWidth - width).coerceAtLeast(0f)
            cameraX = (playerX + playerSize / 2f - width / 2f).coerceIn(0f, maxCameraX)
        }
    }

    private fun draw() {
        if (holder.surface.isValid) {
            val canvas: Canvas = holder.lockCanvas()
            canvas.drawColor(Color.parseColor("#87CEEB"))
            canvas.save()
            canvas.translate(-cameraX, 0f)

            paint.color = Color.parseColor("#00F0F0")
            canvas.drawRect(0f, groundY, levelWidth, height.toFloat(), paint)

            paint.color = Color.DKGRAY
            for (plat in platforms) {
                canvas.drawRect(plat, paint)
            }

            paint.color = Color.RED
            canvas.drawRect(playerX, playerY, playerX + playerSize, playerY + playerSize, paint)

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
        isPlaying = true
        thread = Thread(this)
        thread?.start()
    }

    fun pause() {
        isPlaying = false
        thread?.join()
    }

    fun moveRight() {
        playerX += 12
    }

    fun moveLeft() {
        playerX -= 12
    }

    fun loadLevel(newPlatforms: List<RectF>) {
        platforms = newPlatforms
        playerX = 200f
        playerY = 0f
        velocityY = 0f
        cameraX = 0f
    }
}
