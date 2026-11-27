package com.example.turnthemiddleage

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.SurfaceView
class levelonehandler (context: Context, attrs: AttributeSet){
    private var thread: Thread? = null
    private var isplaying = false
    private var paint = Paint()

    private var playerx = 200f
    private var playery
}