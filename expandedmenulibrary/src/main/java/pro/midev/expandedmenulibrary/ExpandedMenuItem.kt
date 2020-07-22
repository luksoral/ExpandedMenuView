package pro.midev.expandedmenulibrary

import android.support.annotation.ColorInt

data class ExpandedMenuItem(var icon : Int,
                            var name : String,
                            @ColorInt var iconTint : Int?) {

    constructor(icon : Int, name : String): this(icon, name, null)
}