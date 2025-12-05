package ai.p2ach.p2achandroidvision.views.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import ai.p2ach.p2achandroidvision.R

class StatusLightView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class State {
        CONNECTED, DISCONNECTED, CONNECTING
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var connectedColor: Int = 0
    private var disconnectedColor: Int = 0
    private var connectingColor: Int = 0
    private val defaultSizeDp = 12f

    var state: State = State.DISCONNECTED
        set(value) {
            field = value
            updateColor()
        }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.StatusLightView)
        connectedColor = a.getColor(
            R.styleable.StatusLightView_slv_connectedColor,
            0xFF00FF00.toInt()
        )
        disconnectedColor = a.getColor(
            R.styleable.StatusLightView_slv_disconnectedColor,
            0xFFFF0000.toInt()
        )
        connectingColor = a.getColor(
            R.styleable.StatusLightView_slv_connectingColor,
            0xFFFFFF00.toInt()
        )
        a.recycle()
        updateColor()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val defaultSize = dp(defaultSizeDp).toInt()
        val desiredWidth = defaultSize + paddingLeft + paddingRight
        val desiredHeight = defaultSize + paddingTop + paddingBottom

        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val availW = width - paddingLeft - paddingRight
        val availH = height - paddingTop - paddingBottom
        val diameter = minOf(availW, availH)
        if (diameter <= 0) return

        val radius = diameter / 2f
        val cx = paddingLeft + availW / 2f
        val cy = paddingTop + availH / 2f

        canvas.drawCircle(cx, cy, radius, paint)
    }

    private fun updateColor() {
        val color = when (state) {
            State.CONNECTED -> connectedColor
            State.DISCONNECTED -> disconnectedColor
            State.CONNECTING -> connectingColor
        }
        paint.color = color
        invalidate()
    }

    private fun dp(v: Float): Float =
        v * resources.displayMetrics.density
}