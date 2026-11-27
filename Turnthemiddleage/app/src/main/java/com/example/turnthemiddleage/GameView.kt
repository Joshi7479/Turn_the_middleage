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

    private var playerX = 200f
    private var playerY = 500f
    private var playerSize = 100f
    private var velocityY = 0f
    private val gravity = 1.5f
    private val jumpStrength = -35f
    private var isGrounded = false

    var moveLeft = false
    var moveRight = false

    private var groundY = 800f
    private var platform = RectF(600f, 600f, 950f, 650f)

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

        velocityY += gravity
        playerY += velocityY

        if (playerY + playerSize >= groundY) {
            playerY = groundY - playerSize
            velocityY = 0f
            isGrounded = true
        }

        val playerRect = RectF(playerX, playerY, playerX + playerSize, playerY + playerSize)
        if (velocityY > 0 && RectF.intersects(playerRect, platform)) {
            if (playerY + playerSize - velocityY <= platform.top) {
                playerY = platform.top - playerSize
                velocityY = 0f
                isGrounded = true
            }
        }
    }

    private fun draw() {
        if (holder.surface.isValid) {
            val canvas: Canvas = holder.lockCanvas()
            canvas.drawColor(Color.parseColor("#87CEEB"))
            paint.color = Color.parseColor("#00F0F0")
            canvas.drawRect(0f, groundY, width.toFloat(), height.toFloat(), paint)
            paint.color = Color.DKGRAY
            canvas.drawRect(platform, paint)
            paint.color = Color.RED
            canvas.drawRect(playerX, playerY, playerX + playerSize, playerY + playerSize, paint)

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
}
