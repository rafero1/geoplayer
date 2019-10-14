package br.ufc.smd.geoplayer

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView


class CustomTouchListener(context: Context, clickListener: ItemTouchListener) : RecyclerView.OnItemTouchListener {

    //Gesture detector to intercept the touch events
    var gestureDetector: GestureDetector? = null
    private var clickListener: ItemTouchListener? = clickListener

    init {
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }
        })
    }

    override fun onInterceptTouchEvent(recyclerView: RecyclerView, e: MotionEvent): Boolean {

        val child = recyclerView.findChildViewUnder(e.x, e.y)
        if (child != null && clickListener != null && gestureDetector!!.onTouchEvent(e)) {
            clickListener?.onClick(child, recyclerView.getChildLayoutPosition(child))
        }
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

    }
}