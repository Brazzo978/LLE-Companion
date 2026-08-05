package com.codex.lle.companion

import android.content.Context
import android.graphics.Canvas
import android.graphics.Movie
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

@Suppress("DEPRECATION")
class LoopingGifView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var movie: Movie? = null
    private var startedAt = 0L

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun setGifResource(resourceId: Int) {
        movie = resources.openRawResource(resourceId).use(Movie::decodeStream)
        startedAt = SystemClock.uptimeMillis()
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val gif = movie
        val desiredWidth = gif?.width() ?: suggestedMinimumWidth
        val desiredHeight = gif?.height() ?: suggestedMinimumHeight
        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val gif = movie ?: return
        val duration = gif.duration().takeIf { it > 0 } ?: DEFAULT_DURATION_MS
        val elapsed = ((SystemClock.uptimeMillis() - startedAt) % duration).toInt()
        gif.setTime(elapsed)

        val scale = min(width.toFloat() / gif.width(), height.toFloat() / gif.height())
        val left = (width - gif.width() * scale) / 2f
        val top = (height - gif.height() * scale) / 2f
        canvas.save()
        canvas.translate(left, top)
        canvas.scale(scale, scale)
        gif.draw(canvas, 0f, 0f)
        canvas.restore()

        if (isShown) postInvalidateDelayed(FRAME_DELAY_MS)
    }

    private companion object {
        const val DEFAULT_DURATION_MS = 1_000
        const val FRAME_DELAY_MS = 125L
    }
}
