package com.lastwave.app.ui.glass

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Performance & hardware execution tiers for Clear Liquid Glass.
 */
enum class GlassTier {
    /** Android 13+ (API 33+): Full AGSL SDF lens refraction + chromatic dispersion. */
    TierA,
    /** Android 12 (API 31-32): RenderEffect hardware blur + specular rim lighting. */
    TierB,
    /** Low-end, battery saver, or accessibility reduce-transparency: Translucent tint + rim light. */
    TierC,
}

/**
 * Live tunable parameters for the Clear Liquid Glass optical engine.
 * Tunable live in Glass Lab.
 */
@Immutable
data class GlassParams(
    val blurDp: Float = 12f,
    val sheetBlurDp: Float = 28f,
    val tintAlpha: Float = 0.06f,
    val refraction: Float = 0.18f,
    val dispersion: Float = 0.04f,
    val rimLightAlpha: Float = 0.55f,
    val saturation: Float = 1.60f,
    val brightness: Float = 1.05f,
    val ambientShadowAlpha: Float = 0.18f,
)

val LocalGlassTier = staticCompositionLocalOf { GlassTier.TierA }
val LocalGlassParams = compositionLocalOf { GlassParams() }
val LocalDeviceTilt = compositionLocalOf { Offset.Zero }

/**
 * Automatically detects the best hardware tier for the device,
 * respecting battery saver and system accessibility settings.
 */
@Composable
fun rememberAutoGlassTier(): GlassTier {
    val context = LocalContext.current
    return remember(context) {
        val am = runCatching { context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager }.getOrNull()
        val pm = runCatching { context.getSystemService(Context.POWER_SERVICE) as? PowerManager }.getOrNull()
        
        // Check battery saver or low-RAM
        val isLowRam = am?.isLowRamDevice == true
        val isPowerSave = pm?.isPowerSaveMode == true
        
        // Check system Reduce Transparency accessibility setting if available
        val reduceTransparency = runCatching {
            Settings.Global.getInt(context.contentResolver, "accessibility_reduce_transparency", 0) != 0
        }.getOrDefault(false)

        if (isLowRam || isPowerSave || reduceTransparency) {
            GlassTier.TierC
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            GlassTier.TierA
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            GlassTier.TierB
        } else {
            GlassTier.TierC
        }
    }
}
