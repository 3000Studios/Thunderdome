package com.example.game.model

import androidx.compose.ui.graphics.Color

/**
 * BONUS stage: Warp Tunnel that lights up when the player warps down into the tube.
 * Audio: R.raw.bonus_vortex_tunnel, R.raw.track_bonus_stage_trippy
 * Design brief: docs/ANTIGRAVITY_STAGE_DESIGN_BRIEF.md
 */
object BonusStageSpec {
    const val ID = "bonus_warp_tunnel"
    const val NAME = "Bonus: Warp Tunnel"
    const val SUBTITLE = "Lights up on warp-down entry"

    val ringColors = listOf(
        Color(0xFF00F0FF), // cyan
        Color(0xFFFF007F), // magenta
        Color(0xFFFFD700), // gold
        Color(0xFFB347FF)  // violet
    )

    /** Seconds for the full tunnel run before exiting to next biome. */
    const val durationSec = 18f

    /** How long ring cascade takes to fully light up after entry. */
    const val lightUpRampSec = 2.5f

    const val scrollSpeed = 920f
    const val ringCount = 24
    const val pickupLaneCount = 3

    /** Shader / renderer uniforms Antigravity should drive. */
    fun lightIntensity(warpProgress: Float): Float =
        (warpProgress / lightUpRampSec.coerceAtLeast(0.01f)).coerceIn(0f, 1f)

    fun ringColor(index: Int, intensity: Float): Color {
        val base = ringColors[index % ringColors.size]
        return base.copy(alpha = (0.25f + 0.75f * intensity).coerceIn(0f, 1f))
    }
}
