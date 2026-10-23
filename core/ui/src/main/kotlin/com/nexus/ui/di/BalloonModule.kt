package com.nexus.ui.di

import android.view.View
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.ArrowPositionRules
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val NEXUS_BG = 0xFF1E2536.toInt()
private const val NEXUS_TEXT = 0xFFE8EDFB.toInt()

@Module
@InstallIn(SingletonComponent::class)
object BalloonModule {

    @Provides
    @Singleton
    fun provideTooltipManager(): TooltipManager {
        return TooltipManager()
    }
}

@Singleton
class TooltipManager {

    fun showFeatureDiscovery(view: View, text: String, title: String? = null) {
        val balloon = Balloon.Builder(view.context).apply {
            setArrowSize(8)
            setArrowOrientation(ArrowOrientation.TOP)
            setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            setArrowPosition(0.5f)
            setWidthRatio(0.85f)
            setBackgroundColor(NEXUS_BG)
            setTextColor(NEXUS_TEXT)
            setTextSize(14f)
            setBalloonAnimation(BalloonAnimation.FADE)
            setPadding(12)
            if (title != null) {
                setText("$title\n$text")
            } else {
                setText(text)
            }
        }.build()
        balloon.showAsDropDown(view, 0, 16)
    }

    fun showInfo(view: View, text: String) {
        Balloon.Builder(view.context).apply {
            setArrowSize(6)
            setArrowOrientation(ArrowOrientation.TOP)
            setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            setArrowPosition(0.5f)
            setWidthRatio(0.9f)
            setBackgroundColor(NEXUS_BG)
            setTextColor(NEXUS_TEXT)
            setTextSize(13f)
            setText(text)
            setBalloonAnimation(BalloonAnimation.FADE)
            setPadding(12)
            setDismissWhenTouchOutside(true)
        }.build().showAsDropDown(view, 0, 16)
    }
}
