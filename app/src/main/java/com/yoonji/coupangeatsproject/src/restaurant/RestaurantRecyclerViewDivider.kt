package com.yoonji.coupangeatsproject.src.restaurant

import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView

class RestaurantRecyclerViewDivider(private val height: Float,
                                    private val padding: Float,
                                    @ColorInt
                          private val color: Int ) : RecyclerView.ItemDecoration() {

    private val paint = Paint()

    init {
        paint.color = color
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingStart + padding
        val right = parent.width - parent.paddingEnd - padding

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = (child.bottom + params.bottomMargin).toFloat()
            val bottom = top + height

            canvas.drawRect (left, top, right, bottom, paint)
        }
    }
}