package com.rychagovD.nickdivider

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.rychagov.decordivider.R

class NickDivider(
  context: Context,
  attrs: AttributeSet? = null
) : View(
  context,
  attrs
) {

  private val DEFAULT_HEIGHT = 4.toPx()
  private val DEFAULT_WIDTH_BIG = 20.toPx()
  private val DEFAULT_WIDTH_SMALL = 10.toPx()
  private val DEFAULT_SPACE = 40.toPx()
  private val DEFAULT_MAX_COUNT = 5
  private val DEFAULT_COUNT = 0

  private val tint: Int
  private val nickHeight: Int
  private val nickRadius: Int
  private val nickBigWidth: Int
  private val nickBigDistance: Int
  private val nickBigMaxCount: Int
  private var nickBigCount: Int = DEFAULT_COUNT
  private val nickSmallWidth: Int
  private val nickSmallDistance: Int
  private val nickSmallCount: Int

  private val nickPaint: Paint
  private val nickBigPath: Path
  private val nickSmallPath: Path

  init {
    val typedArray = context.obtainStyledAttributes(attrs, R.styleable.NickDivider)

    tint = typedArray.getColor(R.styleable.NickDivider_android_tint, Color.BLACK)
    nickHeight = typedArray.getDimensionPixelSize(R.styleable.NickDivider_nick_height, DEFAULT_HEIGHT)
    nickRadius = nickHeight / 2
    nickBigWidth = typedArray.getDimensionPixelSize(R.styleable.NickDivider_nick_big_width, DEFAULT_WIDTH_BIG)
    nickBigMaxCount = typedArray.getInt(R.styleable.NickDivider_nick_big_max_count, DEFAULT_MAX_COUNT)
    nickBigDistance = typedArray.getDimensionPixelSize(R.styleable.NickDivider_nick_distance, DEFAULT_SPACE)
    nickSmallWidth = typedArray.getDimensionPixelSize(R.styleable.NickDivider_nick_small_width, DEFAULT_WIDTH_SMALL)
    nickSmallCount = typedArray.getInt(R.styleable.NickDivider_nick_small_count, DEFAULT_COUNT)
    nickSmallDistance = computeSmallDistance()
    typedArray.recycle()

    nickPaint = Paint(Paint.ANTI_ALIAS_FLAG)
      .apply {
        colorFilter = PorterDuffColorFilter(tint, PorterDuff.Mode.SRC_IN)
        style = Paint.Style.FILL
      }
    nickBigPath = generateBigNick()
    nickSmallPath = generateSmallNick()
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val modeWidth = View.MeasureSpec.getMode(widthMeasureSpec)
    val modeHeight = View.MeasureSpec.getMode(heightMeasureSpec)

    val sizeWidth = View.MeasureSpec.getSize(widthMeasureSpec)
    val sizeHeight = View.MeasureSpec.getSize(heightMeasureSpec)

    val maxContentHeight = nickBigMaxCount * (nickHeight + nickBigDistance) + nickHeight
    nickBigCount = if (maxContentHeight > sizeHeight) {
      sizeHeight / nickHeight
    } else {
      nickBigMaxCount
    }

    var resultWidth = nickBigWidth
    var resultHeight = nickBigCount * (nickHeight + nickBigDistance) + nickHeight

    resultWidth += paddingLeft + paddingRight
    resultHeight += paddingTop + paddingBottom

    resultWidth = measureSize(modeWidth, sizeWidth, resultWidth)
    resultHeight = measureSize(modeHeight, sizeHeight, resultHeight)

    setMeasuredDimension(resultWidth, resultHeight)
  }

  override fun onDraw(canvas: Canvas) {
    val bigDistance = nickBigDistance.toFloat()
    val smallDistance = nickSmallDistance.toFloat()
    val height = nickHeight.toFloat()
    val dx = (nickBigWidth - nickSmallWidth) / 2f

    canvas.save()
    repeat(nickBigCount) {
      canvas.drawPath(nickBigPath, nickPaint)
      canvas.translate(0f, height + bigDistance)
    }
    canvas.restore()

    repeat(nickBigCount - 1) { bigIndex ->
      canvas.save()
      canvas.translate(dx, 0f)
      canvas.translate(0f, bigIndex * bigDistance)
      canvas.translate(0f, (bigIndex + 1) * height)

      repeat(nickSmallCount) {
        canvas.translate(0f, smallDistance)
        canvas.drawPath(nickSmallPath, nickPaint)
        canvas.translate(0f, height)
      }

      canvas.restore()
    }
  }

  private fun computeSmallDistance(): Int {
    val allSpace = nickBigDistance - nickSmallCount * nickHeight
    return allSpace / (nickSmallCount + 1)
  }

  private fun generateBigNick(): Path {
    val rect = RectF(0f, 0f, nickBigWidth.toFloat(), nickHeight.toFloat())
    return Path()
      .apply { addRoundRect(rect, nickRadius.toFloat(), nickRadius.toFloat(), Path.Direction.CW) }
  }

  private fun generateSmallNick(): Path {
    val rect = RectF(0f, 0f, nickSmallWidth.toFloat(), nickHeight.toFloat())
    return Path()
      .apply { addRoundRect(rect, nickRadius.toFloat(), nickRadius.toFloat(), Path.Direction.CW) }
  }

  private fun measureSize(mode: Int, sizeExpect: Int, sizeActual: Int): Int {
    var realSize: Int
    if (mode == View.MeasureSpec.EXACTLY) {
      realSize = sizeExpect
    } else {
      realSize = sizeActual
      if (mode == View.MeasureSpec.AT_MOST) {
        realSize = Math.min(realSize, sizeExpect)
      }
    }
    return realSize
  }

  private fun Int.toPx(): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics).toInt()
}