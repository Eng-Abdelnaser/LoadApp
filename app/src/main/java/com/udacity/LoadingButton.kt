package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var buttonWidth = 0f
    private var sweepAngle = 0f
    private var label: String
    private var labelWidth=0f
    private var defaultTextSize: Float = resources.getDimension(R.dimen.default_text_size)
    private var loadingColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)
    private var arcColor = ContextCompat.getColor(context, R.color.colorAccent)
    private var buttonBackgroundColor = ContextCompat.getColor(context, R.color.colorPrimary)

    private var paint: Paint = Paint().apply {
        isAntiAlias = true
        textSize = resources.getDimension(R.dimen.default_text_size)
    }
    private var valueAnimator = ValueAnimator()

    var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        if(new==ButtonState.Loading){
            runLoadingAnimation()
        }else{
            valueAnimator.cancel()
            buttonWidth = 0f
            sweepAngle=0f
            invalidate()
        }
    }

    init {
        label = "DownLoad"
        context.withStyledAttributes(attrs, R.styleable.LoadingButton){
            buttonBackgroundColor = getColor(R.styleable.LoadingButton_buttonBackgroundColor, 0)
            loadingColor = getColor(R.styleable.LoadingButton_loadingColor, 0)
            arcColor = getColor(R.styleable.LoadingButton_arcColor, 0)
        }
    }

    private fun runLoadingAnimation() {
        valueAnimator = ValueAnimator.ofFloat(0f, widthSize.toFloat())
        valueAnimator.duration = 4000
        valueAnimator.addUpdateListener { animation ->
            buttonWidth = animation.animatedValue as Float
            sweepAngle = (widthSize.toFloat() / 365) * (animation.animatedValue as Float)
            invalidate()
        }
        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                buttonWidth = 0f
                sweepAngle=0f
                if (buttonState == ButtonState.Loading) {
                    buttonState = ButtonState.Loading
                }
            }
        })
        valueAnimator.start()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // draw the basic container
        paint.color = buttonBackgroundColor
        canvas?.drawRect(0f, 0f, widthSize.toFloat(), heightSize.toFloat(), paint)
        // draw loading button
        paint.color = loadingColor
        canvas?.drawRect(0f, 0f, buttonWidth, heightSize.toFloat(), paint)
        // draw label
        paint.color = Color.WHITE
        labelWidth = paint.measureText(label)
        canvas?.drawText(label, widthSize / 2 - labelWidth / 2, heightSize / 2 - (paint.descent() + paint.ascent()) / 2, paint)
        // draw arc
        val arcLeftPostion = ((widthSize / 3) * 2.5).toFloat()
        val arcTopPostion = ((heightSize - 100) / 2).toFloat()
        canvas?.save()
        paint.color = arcColor
        canvas?.drawArc(
            RectF(
                arcLeftPostion,
                arcTopPostion,
                (arcLeftPostion + 100).toFloat(),
                (arcTopPostion + 100).toFloat()
            ), 0F, sweepAngle * 0.365f, true, paint
        )
        canvas?.restore()

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}