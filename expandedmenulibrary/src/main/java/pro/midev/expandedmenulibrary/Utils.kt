package pro.midev.expandedmenulibrary

import android.content.res.Resources

fun Int.dpToPx() : Float = this * Resources.getSystem().displayMetrics.density

fun Float.pxToDp() : Int = (this / Resources.getSystem().displayMetrics.density).toInt()

fun Float.spToPx() : Float = this * Resources.getSystem().displayMetrics.scaledDensity