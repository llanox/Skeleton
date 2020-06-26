package com.ethanhua.skeleton

import android.support.annotation.ColorRes
import android.support.annotation.IntRange
import android.support.annotation.LayoutRes
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.ViewGroup
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout

/**
 * Created by ethanhua on 2017/7/29.
 */
class ViewSkeletonScreen private constructor(builder: Builder) : SkeletonScreen {
    private val viewReplacer: ViewReplacer
    private val actualView: View
    private val skeletonResID: Int
    private val shimmerColor: Int
    private val shimmer: Boolean
    private val shimmerDuration: Long
    private val shimmerAngle: Int
    private fun generateShimmerContainerLayout(parentView: ViewGroup): ShimmerFrameLayout {
        val shimmerLayout = LayoutInflater.from(actualView.context).inflate(R.layout.layout_shimmer, parentView, false) as ShimmerFrameLayout
        val shimmerBuilder = Shimmer.ColorHighlightBuilder()
        val shimmer = shimmerBuilder
                .setDuration(shimmerDuration)
                .setBaseAlpha(1f)
                .setDropoff(0.2f)
                .setTilt(shimmerAngle.toFloat())
                .setBaseColor(shimmerColor)
                .setHighlightColor(shimmerColor)
                .build()

        shimmerLayout.setShimmer(shimmer)
        val innerView = LayoutInflater.from(actualView.context).inflate(skeletonResID, shimmerLayout, false)
        val lp = innerView.layoutParams
        if (lp != null) {
            shimmerLayout.layoutParams = lp
        }
        shimmerLayout.addView(innerView)
        shimmerLayout.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                shimmerLayout.startShimmer()
            }

            override fun onViewDetachedFromWindow(v: View) {
                shimmerLayout.stopShimmer()
            }
        })
        shimmerLayout.startShimmer()
        return shimmerLayout
    }

    private fun generateSkeletonLoadingView(): View? {
        val viewParent = actualView.parent
        if (viewParent == null) {
            Log.e(TAG, "the source view have not attach to any view")
            return null
        }
        val parentView = viewParent as ViewGroup
        return if (shimmer) {
            generateShimmerContainerLayout(parentView)
        } else LayoutInflater.from(actualView.context).inflate(skeletonResID, parentView, false)
    }

    override fun show() {
        val skeletonLoadingView = generateSkeletonLoadingView()
        if (skeletonLoadingView != null) {
            viewReplacer.replace(skeletonLoadingView)
        }
    }

    override fun hide() {
        if (viewReplacer.targetView is ShimmerFrameLayout) {
            (viewReplacer.targetView as ShimmerFrameLayout?)?.stopShimmer()
        }
        viewReplacer.restore()
    }

    class Builder(val view: View) {
        var skeletonLayoutResID = 0
            private set

        var shimmer = true
            private set
        @ColorRes
        var shimmerColor: Int
            private set

        var shimmerDuration = 1000L
            private set

        var shimmerAngle = 20
            private set


        /**
         * @param skeletonLayoutResID the loading skeleton layoutResID
         */
        fun load(@LayoutRes skeletonLayoutResID: Int) = apply {
            this.skeletonLayoutResID = skeletonLayoutResID
        }

        /**
         * @param shimmerColorId the shimmer color id
         */
        fun color(@ColorRes shimmerColorId: Int) = apply {
            this.shimmerColor = ContextCompat.getColor(view.context, shimmerColorId)
        }

        /**
         * @param shimmer whether show shimmer animation
         */
        fun shimmer(shimmer: Boolean) = apply {
            this.shimmer = shimmer
        }

        /**
         * the duration of the animation , the time it will take for the highlight to move from one end of the layout
         * to the other.
         *
         * @param shimmerDuration Duration of the shimmer animation, in milliseconds
         */
        fun duration(shimmerDuration: Long) = apply {
            this.shimmerDuration = shimmerDuration
        }

        /**
         * @param shimmerAngle the angle of the shimmer effect in clockwise direction in degrees.
         */
        fun angle(@IntRange(from = 0, to = 30) shimmerAngle: Int) = apply {
            this.shimmerAngle = shimmerAngle
        }

        fun show(): ViewSkeletonScreen {
            val skeletonScreen = ViewSkeletonScreen(this)
            skeletonScreen.show()
            return skeletonScreen
        }

        init {
            shimmerColor = ContextCompat.getColor(view.context, R.color.shimmer_color)
        }
    }

    companion object {
        private val TAG = ViewSkeletonScreen::class.java.name
    }

    init {
        actualView = builder.view
        skeletonResID = builder.skeletonLayoutResID
        shimmer = builder.shimmer
        shimmerDuration = builder.shimmerDuration
        shimmerAngle = builder.shimmerAngle
        shimmerColor = builder.shimmerColor
        viewReplacer = ViewReplacer(builder.view)
    }
}