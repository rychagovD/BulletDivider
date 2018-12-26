package com.rychagovD.bulletdivider

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

class BulletDivider(
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
  private val DEFAULT_COUNT = 3

  private val ALIGNMENT_LEFT = 0
  private val ALIGNMENT_CENTER = 1
  private val ALIGNMENT_RIGHT = 2

  private val tint: Int
  private val bulletHeight: Int
  private val bulletRadius: Int
  private val bulletAlignment: Int
  private val bulletBigWidth: Int
  private val bulletBigDistance: Int
  private val bulletBigCount: Int
  private val bulletSmallWidth: Int
  private val bulletSmallDistance: Int
  private val bulletSmallCount: Int

  private val bulletSmallDx: Float

  private val bulletPaint: Paint
  private val bulletBigPath: Path
  private val bulletSmallPath: Path

  init {
    val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BulletDivider)

    tint = typedArray.getColor(R.styleable.BulletDivider_android_tint, Color.BLACK)
    bulletHeight = typedArray.getDimensionPixelSize(R.styleable.BulletDivider_bullet_height, DEFAULT_HEIGHT)
    bulletRadius = bulletHeight / 2
    bulletAlignment = typedArray.getInt(R.styleable.BulletDivider_bullet_alignment, ALIGNMENT_CENTER)
    bulletBigWidth = typedArray.getDimensionPixelSize(R.styleable.BulletDivider_bullet_big_width, DEFAULT_WIDTH_BIG)
    bulletBigCount = typedArray.getInt(R.styleable.BulletDivider_bullet_big_count, DEFAULT_COUNT)
    bulletBigDistance = typedArray.getDimensionPixelSize(R.styleable.BulletDivider_bullet_distance, DEFAULT_SPACE) - bulletHeight
    bulletSmallWidth = typedArray.getDimensionPixelSize(R.styleable.BulletDivider_bullet_small_width, DEFAULT_WIDTH_SMALL)
    bulletSmallCount = typedArray.getInt(R.styleable.BulletDivider_bullet_small_count, DEFAULT_COUNT)
    bulletSmallDistance = computeSmallDistance()
    typedArray.recycle()

    bulletSmallDx = when (bulletAlignment) {
      ALIGNMENT_LEFT -> 0f
      ALIGNMENT_RIGHT -> (bulletBigWidth - bulletSmallWidth).toFloat()
      else -> (bulletBigWidth - bulletSmallWidth) / 2f
    }

    bulletPaint = Paint(Paint.ANTI_ALIAS_FLAG)
      .apply {
        colorFilter = PorterDuffColorFilter(tint, PorterDuff.Mode.SRC_IN)
        style = Paint.Style.FILL
      }
    bulletBigPath = generateBigBullet()
    bulletSmallPath = generateSmallBullet()
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val modeWidth = View.MeasureSpec.getMode(widthMeasureSpec)
    val modeHeight = View.MeasureSpec.getMode(heightMeasureSpec)

    val sizeWidth = View.MeasureSpec.getSize(widthMeasureSpec)
    val sizeHeight = View.MeasureSpec.getSize(heightMeasureSpec)

    var resultWidth = bulletBigWidth + paddingLeft + paddingRight
    var resultHeight = bulletBigCount * (bulletHeight + bulletBigDistance) - bulletBigDistance + paddingTop + paddingBottom

    resultWidth = measureSize(modeWidth, sizeWidth, resultWidth)
    resultHeight = measureSize(modeHeight, sizeHeight, resultHeight)

    setMeasuredDimension(resultWidth, resultHeight)
  }

  override fun onDraw(canvas: Canvas) {
    val bigDistance = bulletBigDistance.toFloat()
    val smallDistance = bulletSmallDistance.toFloat()
    val height = bulletHeight.toFloat()

    // draw big bullets
    canvas.save()
    repeat(bulletBigCount) {
      canvas.drawPath(bulletBigPath, bulletPaint)
      canvas.translate(0f, height + bigDistance)
    }
    canvas.restore()

    // draw small bullets
    repeat(bulletBigCount - 1) { bigIndex ->
      canvas.save()
      canvas.translate(bulletSmallDx, 0f)
      canvas.translate(0f, bigIndex * bigDistance)
      canvas.translate(0f, (bigIndex + 1) * height)

      repeat(bulletSmallCount) {
        canvas.translate(0f, smallDistance)
        canvas.drawPath(bulletSmallPath, bulletPaint)
        canvas.translate(0f, height)
      }

      canvas.restore()
    }
  }

  private fun computeSmallDistance(): Int {
    val allSpace = bulletBigDistance - bulletSmallCount * bulletHeight
    return allSpace / (bulletSmallCount + 1)
  }

  private fun generateBigBullet(): Path {
    val rect = RectF(0f, 0f, bulletBigWidth.toFloat(), bulletHeight.toFloat())
    return Path()
      .apply { addRoundRect(rect, bulletRadius.toFloat(), bulletRadius.toFloat(), Path.Direction.CW) }
  }

  private fun generateSmallBullet(): Path {
    val rect = RectF(0f, 0f, bulletSmallWidth.toFloat(), bulletHeight.toFloat())
    return Path()
      .apply { addRoundRect(rect, bulletRadius.toFloat(), bulletRadius.toFloat(), Path.Direction.CW) }
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