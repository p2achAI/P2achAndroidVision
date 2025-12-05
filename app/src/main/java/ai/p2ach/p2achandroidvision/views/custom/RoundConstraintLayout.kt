package ai.p2ach.p2achandroidvision.views.common

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import ai.p2ach.p2achandroidvision.R

class RoundConstraintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val bg = GradientDrawable()

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.RoundConstraintLayout)

        val radius = a.getDimension(
            R.styleable.RoundConstraintLayout_rcl_cornerRadius,
            dp(12f)               // RoundTextView 와 동일한 기본값 적용
        )

        val bgColor = a.getColor(
            R.styleable.RoundConstraintLayout_rcl_backgroundColor,
            Color.parseColor("#66000000")   // RoundTextView 와 비슷한 semi black
        )

        val strokeColor = a.getColor(
            R.styleable.RoundConstraintLayout_rcl_strokeColor,
            Color.TRANSPARENT
        )

        val strokeWidth = a.getDimensionPixelSize(
            R.styleable.RoundConstraintLayout_rcl_strokeWidth,
            0
        )

        a.recycle()

        bg.shape = GradientDrawable.RECTANGLE
        bg.cornerRadius = radius
        bg.setColor(bgColor)

        if (strokeWidth > 0) {
            bg.setStroke(strokeWidth, strokeColor)
        }

        background = bg

        // RoundTextView 기본 padding 과 동일하게
        val defaultPadding = dp(8f).toInt()
        if (paddingLeft == 0 && paddingTop == 0 && paddingRight == 0 && paddingBottom == 0) {
            setPadding(defaultPadding, defaultPadding, defaultPadding, defaultPadding)
        }
    }

    private fun dp(value: Float): Float =
        value * resources.displayMetrics.density
}