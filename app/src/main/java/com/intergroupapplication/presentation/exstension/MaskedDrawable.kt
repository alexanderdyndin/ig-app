package com.intergroupapplication.presentation.exstension

import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build

class MaskedDrawableBitmapShader(private val mPictureBitmap: Bitmap) : Drawable() {
    private val mPaintShader = Paint()
    private var mBitmapShader: BitmapShader? = null
    private lateinit var mPath: Path

    override fun draw(canvas: Canvas) {
        mBitmapShader = BitmapShader(mPictureBitmap,
                Shader.TileMode.REPEAT,
                Shader.TileMode.REPEAT)
        mPaintShader.shader = mBitmapShader
        mPath = Path()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPath.addOval(0f, 0f, intrinsicWidth.toFloat(), intrinsicHeight.toFloat(), Path.Direction.CW)
        }
        val subPath = Path()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            subPath.addOval(intrinsicWidth * 0.7f, intrinsicHeight * 0.7f, intrinsicWidth.toFloat(), intrinsicHeight.toFloat(), Path.Direction.CW)
        }
        mPath.op(subPath, Path.Op.DIFFERENCE)
        canvas.drawPath(mPath, mPaintShader)
    }

    override fun setAlpha(alpha: Int) {
        mPaintShader.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter) {
        mPaintShader.colorFilter = cf
    }

    override fun getOpacity(): Int {
        return PixelFormat.UNKNOWN
    }

    override fun getIntrinsicWidth(): Int {
        return mPictureBitmap.width
    }

    override fun getIntrinsicHeight(): Int {
        return mPictureBitmap.height
    }
}