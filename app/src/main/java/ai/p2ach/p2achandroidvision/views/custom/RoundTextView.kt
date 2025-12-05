package ai.p2ach.p2achandroidvision.views.custom

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import ai.p2ach.p2achandroidvision.R

class RoundTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val bg = GradientDrawable()

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.RoundTextView)

        val radius = a.getDimension(R.styleable.RoundTextView_rtvCornerRadius, dp(12f))
        val bgColor = a.getColor(
            R.styleable.RoundTextView_rtvBgColor,
            0x66000000
        )
        val txtColor = a.getColor(
            R.styleable.RoundTextView_rtvTextColor,
            currentTextColor
        )
        val hPadding = a.getDimensionPixelSize(
            R.styleable.RoundTextView_rtvHorizontalPadding,
            dpInt(12f)
        )
        val vPadding = a.getDimensionPixelSize(
            R.styleable.RoundTextView_rtvVerticalPadding,
            dpInt(6f)
        )
        val strokeWidth = a.getDimensionPixelSize(
            R.styleable.RoundTextView_rtvStrokeWidth,
            0
        )
        val strokeColor = a.getColor(
            R.styleable.RoundTextView_rtvStrokeColor,
            0x00000000
        )

        a.recycle()

        bg.cornerRadius = radius
        bg.setColor(bgColor)
        if (strokeWidth > 0) {
            bg.setStroke(strokeWidth, strokeColor)
        }

        background = bg
        setTextColor(txtColor)
        setPadding(hPadding, vPadding, hPadding, vPadding)
    }

    private fun dp(value: Float): Float =
        value * resources.displayMetrics.density

    private fun dpInt(value: Float): Int =
        (value * resources.displayMetrics.density).toInt()
}