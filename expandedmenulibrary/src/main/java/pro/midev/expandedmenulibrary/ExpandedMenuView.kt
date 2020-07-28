package pro.midev.expandedmenulibrary

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import kotlin.math.min

class ExpandedMenuView : View {

    var currentState = CLOSE_STATE
    private var lastState = CLOSE_STATE

    private val menuPaint = Paint()
    private val textPaint = Paint()

    private val menuRect = RectF()

    private var menuWidthOffset: Int = 0
    private var menuIconAlpha: Int = 255
    private var menuCloseIconAlpha: Int = 0
    private var canTouchThis: Boolean = true
    private var menuItemAlpha: Int = 0
    private var menuItemScaleOffset: Float = 0f
    private var menuTextAlpha: Int = 0

    private var menuOutsideMargin: Float = 24f
    private var menuCornerRadius: Float = 18.dpToPx()
    private var menuBackground: Int = Color.WHITE
    private var shadowColor: Int = Color.BLACK
    private var textColor: Int = Color.BLACK
    private var textFontFamily: String = "sans-serif-medium"
    private var menuIcon: Drawable = resources.getDrawable(android.R.drawable.ic_menu_help, null)
    private var menuCloseIcon: Drawable =
        resources.getDrawable(android.R.drawable.ic_menu_revert, null)
    private var menuItems: MutableList<ExpandedMenuItem> = mutableListOf()
    private var isOnClickClosable: Boolean = false

    private var onItemClickListener: ExpandedMenuClickListener? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val attributes = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ExpandedMenuView,
            defStyleAttr,
            0
        )
        initByAttributes(attributes)
        attributes.recycle()
    }

    private fun initByAttributes(attributes: TypedArray) {
        menuOutsideMargin = attributes.getDimensionPixelOffset(
            R.styleable.ExpandedMenuView_em_outside_margin,
            menuOutsideMargin.toInt()
        ).toFloat()
        menuCornerRadius = attributes.getDimensionPixelOffset(
            R.styleable.ExpandedMenuView_em_corner_radius,
            menuCornerRadius.toInt()
        ).toFloat()
        menuBackground =
            attributes.getColor(R.styleable.ExpandedMenuView_em_background_color, menuBackground)
        shadowColor = attributes.getColor(R.styleable.ExpandedMenuView_em_shadow_color, shadowColor)
        textColor = attributes.getColor(R.styleable.ExpandedMenuView_em_text_color, textColor)
        textFontFamily =
            attributes.getString(R.styleable.ExpandedMenuView_em_font_family) ?: textFontFamily
        val iconMenu = attributes.getDrawable(R.styleable.ExpandedMenuView_em_menu_icon)
        if (iconMenu != null) menuIcon = iconMenu
        menuIcon.mutate()
        val iconCloseMenu = attributes.getDrawable(R.styleable.ExpandedMenuView_em_close_menu_icon)
        if (iconCloseMenu != null) menuCloseIcon = iconCloseMenu
        menuCloseIcon.mutate()
        isOnClickClosable = attributes.getBoolean(
            R.styleable.ExpandedMenuView_em_is_on_click_closable,
            isOnClickClosable
        )
    }

    override fun invalidate() {
        initPainters()
        super.invalidate()
    }

    private fun initPainters() {
        menuPaint.apply {
            color = menuBackground
            style = Paint.Style.FILL
            isAntiAlias = true
            setShadowLayer(8.dpToPx(), 0f, 4.dpToPx(), shadowColor)
        }


        textPaint.apply {
            color = textColor
            textSize = 10f.spToPx()
            isAntiAlias = true
            style = Paint.Style.FILL
            typeface = Typeface.createFromAsset(context.assets, textFontFamily)
            alpha = menuTextAlpha
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(measure(widthMeasureSpec), measure(heightMeasureSpec))
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun measure(measureSpec: Int): Int {
        var result: Int
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)
        if (mode == MeasureSpec.EXACTLY) {
            result = size
        } else {
            result = 0
            if (mode == MeasureSpec.AT_MOST) {
                result = min(result, size)
            }
        }
        return result
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        initPainters()

        if (currentState == OPEN_STATE) {
            menuWidthOffset =
                (measuredWidth - MENU_CLOSE_WIDTH_AND_HEIGHT - menuOutsideMargin * 2).toInt()
        }
        menuRect.set(
            measuredWidth - menuOutsideMargin - MENU_CLOSE_WIDTH_AND_HEIGHT - menuWidthOffset,
            measuredHeight - menuOutsideMargin - MENU_CLOSE_WIDTH_AND_HEIGHT,
            measuredWidth - menuOutsideMargin,
            measuredHeight - menuOutsideMargin
        )
        canvas.drawRoundRect(menuRect, menuCornerRadius, menuCornerRadius, menuPaint)

        menuIcon.setBounds(
            (measuredWidth - menuOutsideMargin - 40.dpToPx()).toInt(),
            (measuredHeight - menuOutsideMargin - 40.dpToPx()).toInt(),
            (measuredWidth - menuOutsideMargin - 16.dpToPx()).toInt(),
            (measuredHeight - menuOutsideMargin - 16.dpToPx()).toInt()
        )
        menuIcon.alpha = menuIconAlpha
        menuIcon.draw(canvas)

        menuCloseIcon.setBounds(
            (measuredWidth - menuOutsideMargin - 48.dpToPx()).toInt(),
            (measuredHeight - menuOutsideMargin - 40.dpToPx()).toInt(),
            (measuredWidth - menuOutsideMargin - 24.dpToPx()).toInt(),
            (measuredHeight - menuOutsideMargin - 16.dpToPx()).toInt()
        )
        menuCloseIcon.alpha = menuCloseIconAlpha
        menuCloseIcon.draw(canvas)

        val itemWidth: Float =
            (measuredWidth - menuOutsideMargin * 2 - 8.dpToPx() * (menuItems.size + 2)) / (menuItems.size + 1)

        for (i in 0 until menuItems.size) {
            val item = resources.getDrawable(menuItems[i].icon, null)

            menuItems[i].iconTint?.let {
                item.mutate()
                item.setTint(it)
            }
            item.setBounds(
                (menuOutsideMargin + 8.dpToPx() * (i + 1) + itemWidth / 2 - menuItemScaleOffset + itemWidth * i).toInt(),
                (measuredHeight - menuOutsideMargin - MENU_CLOSE_WIDTH_AND_HEIGHT + 8.dpToPx() + 12.dpToPx() - menuItemScaleOffset).toInt(),
                (menuOutsideMargin + 8.dpToPx() * (i + 1) + itemWidth / 2 + menuItemScaleOffset + itemWidth * i).toInt(),
                (measuredHeight - menuOutsideMargin - 24.dpToPx() - 12.dpToPx() + menuItemScaleOffset).toInt()
            )
            item.alpha = menuItemAlpha
            item.draw(canvas)

            canvas.drawText(
                menuItems[i].name,
                menuOutsideMargin + 8.dpToPx() * (i + 1) + itemWidth / 2 + itemWidth * i - textPaint.measureText(
                    menuItems[i].name
                ) / 2,
                measuredHeight - menuOutsideMargin - 6.dpToPx() - 5f.spToPx(),
                textPaint
            )
        }
    }

    fun setIcons(
        first: ExpandedMenuItem,
        second: ExpandedMenuItem,
        third: ExpandedMenuItem,
        fourth: ExpandedMenuItem? = null
    ) {
        menuItems.clear()
        menuItems.add(first)
        menuItems.add(second)
        menuItems.add(third)
        if (fourth != null) {
            menuItems.add(fourth)
        }
    }

    fun setOnItemClickListener(listener: ExpandedMenuClickListener) {
        this.onItemClickListener = listener
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null && canTouchThis) {
            val x = event.x
            val y = event.y

            val closeAndOpenMenuPlace = RectF()
            val itemsSetList: MutableList<RectF> = mutableListOf()

            when (currentState) {
                CLOSE_STATE -> {
                    closeAndOpenMenuPlace.set(
                        measuredWidth - menuOutsideMargin - MENU_CLOSE_WIDTH_AND_HEIGHT,
                        measuredHeight - menuOutsideMargin - MENU_CLOSE_WIDTH_AND_HEIGHT,
                        measuredWidth - menuOutsideMargin,
                        measuredHeight - menuOutsideMargin
                    )
                }
                OPEN_STATE -> {
                    closeAndOpenMenuPlace.set(
                        measuredWidth - menuOutsideMargin - MENU_CLOSE_WIDTH_AND_HEIGHT - 16.dpToPx(),
                        measuredHeight - menuOutsideMargin - MENU_CLOSE_WIDTH_AND_HEIGHT,
                        measuredWidth - menuOutsideMargin,
                        measuredHeight - menuOutsideMargin
                    )

                    val itemWidth: Float =
                        (measuredWidth - menuOutsideMargin * 2 - 8.dpToPx() * (menuItems.size + 2)) / (menuItems.size + 1)

                    for (i in 0 until menuItems.size) {
                        itemsSetList.add(RectF().apply {
                            set(
                                menuOutsideMargin + 8.dpToPx() * (i + 1) + itemWidth * i,
                                measuredHeight - menuOutsideMargin - MENU_CLOSE_WIDTH_AND_HEIGHT,
                                menuOutsideMargin + 8.dpToPx() * (i + 1) + itemWidth * (i + 1),
                                measuredHeight - menuOutsideMargin
                            )
                        })
                    }
                }
            }

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (closeAndOpenMenuPlace.contains(x, y)) {
                        ExpandedMenuAnimation().getCurrentAnimatorSet().start()
                        return true
                    }

                    for (i in 0 until itemsSetList.size) {
                        if (itemsSetList[i].contains(x, y)) {
                            onItemClickListener?.onItemClick(i)
                            if (isOnClickClosable) ExpandedMenuAnimation().getCurrentAnimatorSet()
                                .start()
                            return true
                        }
                    }

                    return false
                }
            }
        }
        return false
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState())
        bundle.putInt(INSTANCE_MENU_STATE, currentState)
        bundle.putInt(INSTANCE_MENU_ICON_ALPHA, menuIconAlpha)
        bundle.putInt(INSTANCE_MENU_CLOSE_ICON_ALPHA, menuCloseIconAlpha)
        bundle.putBoolean(INSTANCE_MENU_CAN_TOUCH_THIS, canTouchThis)
        bundle.putInt(INSTANCE_MENU_ITEM_ALPHA, menuItemAlpha)
        bundle.putFloat(INSTANCE_MENU_ITEM_SCALE_OFFSET, menuItemScaleOffset)
        bundle.putInt(INSTANCE_MENU_TEXT_ALPHA, menuTextAlpha)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            currentState = state.getInt(INSTANCE_MENU_STATE)
            menuIconAlpha = state.getInt(INSTANCE_MENU_ICON_ALPHA)
            menuCloseIconAlpha = state.getInt(INSTANCE_MENU_CLOSE_ICON_ALPHA)
            canTouchThis = state.getBoolean(INSTANCE_MENU_CAN_TOUCH_THIS)
            menuItemAlpha = state.getInt(INSTANCE_MENU_ITEM_ALPHA)
            menuItemScaleOffset = state.getFloat(INSTANCE_MENU_ITEM_SCALE_OFFSET)
            menuTextAlpha = state.getInt(INSTANCE_MENU_TEXT_ALPHA)
            super.onRestoreInstanceState(state.getParcelable(INSTANCE_STATE))
            return
        }
        super.onRestoreInstanceState(state)
    }

    companion object {

        private val MENU_CLOSE_WIDTH_AND_HEIGHT = 56.dpToPx()

        private const val CLOSE_STATE = 0
        private const val OPEN_STATE = 1
        private const val DRAGGLING = 2


        private const val INSTANCE_STATE = "saved_instance"
        private const val INSTANCE_MENU_STATE = "menu_state"
        private const val INSTANCE_MENU_ICON_ALPHA = "menu_icon_alpha"
        private const val INSTANCE_MENU_CLOSE_ICON_ALPHA = "menu_close_icon_alpha"
        private const val INSTANCE_MENU_CAN_TOUCH_THIS = "menu_can_touch_this"
        private const val INSTANCE_MENU_ITEM_ALPHA = "menu_item_alpha"
        private const val INSTANCE_MENU_ITEM_SCALE_OFFSET = "menu_item_scale_offset"
        private const val INSTANCE_MENU_TEXT_ALPHA = "menu_text_alpha"
    }

    inner class ExpandedMenuAnimation {

        fun getCurrentAnimatorSet(): AnimatorSet {
            val animatorSet = AnimatorSet()

            when (currentState) {
                OPEN_STATE -> {
                    animatorSet.apply {
                        playSequentially(
                            AnimatorSet().apply {
                                playSequentially(
                                    menuItemTextAnimation(),
                                    AnimatorSet().apply {
                                        playTogether(
                                            menuItemsScaleAnimation(),
                                            menuItemsAlphaAnimation()
                                        )
                                    }
                                )
                            },
                            AnimatorSet().apply {
                                playTogether(
                                    menuBackgroundAnimation(),
                                    AnimatorSet().apply {
                                        playSequentially(
                                            menuCloseAnimation(),
                                            menuIconAnimation()
                                        )
                                    }
                                )
                            }
                        )
                    }
                }
                CLOSE_STATE -> {
                    animatorSet.apply {
                        playTogether(
                            AnimatorSet().apply {
                                playSequentially(
                                    menuIconAnimation(),
                                    menuCloseAnimation()
                                )
                            },
                            AnimatorSet().apply {
                                playSequentially(
                                    menuBackgroundAnimation(),
                                    AnimatorSet().apply {
                                        playTogether(
                                            menuItemsScaleAnimation(),
                                            menuItemsAlphaAnimation()
                                        )
                                    },
                                    menuItemTextAnimation()
                                )
                            }
                        )
                    }
                }
            }

            animatorSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {}

                override fun onAnimationEnd(animation: Animator?) {
                    currentState = if (lastState == CLOSE_STATE) OPEN_STATE else CLOSE_STATE
                    canTouchThis = true
                }

                override fun onAnimationCancel(animation: Animator?) {}

                override fun onAnimationStart(animation: Animator?) {
                    lastState = currentState
                    currentState = DRAGGLING
                    canTouchThis = false
                }
            })

            return animatorSet
        }

        private fun menuBackgroundAnimation(): ValueAnimator? {
            var menuScaleAnimation: ValueAnimator? = null

            when (currentState) {
                OPEN_STATE -> {
                    menuScaleAnimation = ValueAnimator.ofInt(
                        (measuredWidth - MENU_CLOSE_WIDTH_AND_HEIGHT - menuOutsideMargin * 2).toInt(),
                        0
                    )
                    menuScaleAnimation.duration = 200
                }
                CLOSE_STATE -> {
                    menuScaleAnimation = ValueAnimator.ofInt(
                        menuOutsideMargin.toInt(),
                        (measuredWidth - MENU_CLOSE_WIDTH_AND_HEIGHT - menuOutsideMargin * 2).toInt()
                    )
                    menuScaleAnimation.duration = 300
                }
            }

            menuScaleAnimation?.addUpdateListener {
                menuWidthOffset = it.animatedValue as Int
                invalidate()
            }

            menuScaleAnimation?.interpolator = DecelerateInterpolator()

            return menuScaleAnimation
        }

        private fun menuIconAnimation(): ValueAnimator? {
            var menuIconAnimation: ValueAnimator? = null

            when (currentState) {
                OPEN_STATE -> {
                    menuIconAnimation = ValueAnimator.ofInt(0, 255)
                }
                CLOSE_STATE -> {
                    menuIconAnimation = ValueAnimator.ofInt(255, 0)
                }
            }

            menuIconAnimation?.addUpdateListener {
                menuIconAlpha = it.animatedValue as Int
                invalidate()
            }

            menuIconAnimation?.duration = 100

            return menuIconAnimation
        }

        private fun menuCloseAnimation(): ValueAnimator? {
            var menuCloseIconAnimation: ValueAnimator? = null

            when (currentState) {
                OPEN_STATE -> {
                    menuCloseIconAnimation = ValueAnimator.ofInt(255, 0)
                }
                CLOSE_STATE -> {
                    menuCloseIconAnimation = ValueAnimator.ofInt(0, 255)
                }
            }

            menuCloseIconAnimation?.addUpdateListener {
                menuCloseIconAlpha = it.animatedValue as Int
                invalidate()
            }

            menuCloseIconAnimation?.duration = 100

            return menuCloseIconAnimation
        }

        private fun menuItemsAlphaAnimation(): ValueAnimator? {
            var menuIconAlphaAnimation: ValueAnimator? = null

            when (currentState) {
                OPEN_STATE -> {
                    menuIconAlphaAnimation = ValueAnimator.ofInt(255, 0)
                }
                CLOSE_STATE -> {
                    menuIconAlphaAnimation = ValueAnimator.ofInt(0, 255)
                }
            }

            menuIconAlphaAnimation?.addUpdateListener {
                menuItemAlpha = it.animatedValue as Int
                invalidate()
            }

            menuIconAlphaAnimation?.duration = 100

            return menuIconAlphaAnimation
        }

        private fun menuItemsScaleAnimation(): ValueAnimator? {
            var menuIconScaleAnimation: ValueAnimator? = null

            when (currentState) {
                OPEN_STATE -> {
                    menuIconScaleAnimation = ValueAnimator.ofFloat(12.dpToPx(), 0f)
                }
                CLOSE_STATE -> {
                    menuIconScaleAnimation = ValueAnimator.ofFloat(0f, 12.dpToPx())
                }
            }

            menuIconScaleAnimation?.addUpdateListener {
                menuItemScaleOffset = it.animatedValue as Float
                invalidate()
            }

            menuIconScaleAnimation?.duration = 100

            return menuIconScaleAnimation
        }

        private fun menuItemTextAnimation(): ValueAnimator? {
            var menuTextAlphaAnimation: ValueAnimator? = null

            when (currentState) {
                OPEN_STATE -> {
                    menuTextAlphaAnimation = ValueAnimator.ofInt(255, 0)
                    menuTextAlphaAnimation.interpolator = AccelerateInterpolator()
                }
                CLOSE_STATE -> {
                    menuTextAlphaAnimation = ValueAnimator.ofInt(0, 255)
                    menuTextAlphaAnimation.interpolator = DecelerateInterpolator()
                }
            }

            menuTextAlphaAnimation?.addUpdateListener {
                menuTextAlpha = it.animatedValue as Int
                invalidate()
            }

            menuTextAlphaAnimation?.duration = 100
            return menuTextAlphaAnimation
        }
    }
}