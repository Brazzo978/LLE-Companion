package com.codex.lle.companion

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

class GraceBackdropView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shape = Path()

    init {
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()

        paint.shader = LinearGradient(
            0f,
            0f,
            0f,
            height,
            intArrayOf(Color.rgb(228, 241, 247), Color.rgb(242, 248, 250), Color.rgb(222, 237, 243)),
            floatArrayOf(0f, 0.46f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width, height, paint)
        paint.shader = null

        shape.reset()
        shape.moveTo(width * 0.62f, 0f)
        shape.lineTo(width, 0f)
        shape.lineTo(width, height * 0.28f)
        shape.lineTo(width * 0.84f, height * 0.20f)
        shape.close()
        paint.color = Color.argb(58, 111, 82, 214)
        canvas.drawPath(shape, paint)

        shape.reset()
        shape.moveTo(0f, height * 0.60f)
        shape.lineTo(width * 0.34f, height * 0.50f)
        shape.lineTo(width * 0.53f, height)
        shape.lineTo(0f, height)
        shape.close()
        paint.color = Color.argb(52, 54, 203, 158)
        canvas.drawPath(shape, paint)

        shape.reset()
        shape.moveTo(0f, height * 0.23f)
        shape.lineTo(width * 0.22f, height * 0.18f)
        shape.lineTo(width * 0.40f, height * 0.42f)
        shape.lineTo(0f, height * 0.48f)
        shape.close()
        paint.color = Color.argb(42, 255, 176, 54)
        canvas.drawPath(shape, paint)

        paint.shader = RadialGradient(
            width * 0.18f,
            height * 0.72f,
            width * 0.32f,
            intArrayOf(Color.argb(48, 255, 92, 166), Color.TRANSPARENT),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(width * 0.18f, height * 0.72f, width * 0.32f, paint)

        paint.shader = RadialGradient(
            width * 0.82f,
            height * 0.42f,
            width * 0.52f,
            intArrayOf(Color.argb(55, 67, 191, 204), Color.TRANSPARENT),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(width * 0.82f, height * 0.42f, width * 0.52f, paint)
        paint.shader = null
    }
}
