package com.lastwave.app.ui.glass

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.lastwave.app.ui.theme.LocalIsDarkTheme
import com.lastwave.app.ui.theme.LocalLiquidGlassBackdrop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Interactive touch state for Clear Liquid Glass.
 * Handles press scale (0.96f), brightening, rim intensification,
 * and liquid drag stretching with overshoot springs.
 */
class GlassInteractionState(
    private val scope: CoroutineScope,
) {
    val pressSpring = spring<Float>(dampingRatio = 0.55f, stiffness = 380f)
    val pressAnim = Animatable(0f)
    val stretchXAnim = Animatable(0f)
    val stretchYAnim = Animatable(0f)

    val pressProgress: Float get() = pressAnim.value
    val stretchX: Float get() = stretchXAnim.value
    val stretchY: Float get() = stretchYAnim.value

    var touchOffset by mutableStateOf(Offset.Zero)
        private set

    suspend fun handlePointerInput(pointerScope: PointerInputScope, enableStretch: Boolean) {
        pointerScope.awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            touchOffset = down.position
            scope.launch { pressAnim.animateTo(1f, pressSpring) }

            var prevPos = down.position
            try {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val change = event.changes.fastFirstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) break

                    val delta = change.position - prevPos
                    prevPos = change.position
                    touchOffset = change.position

                    if (enableStretch) {
                        val dx = (delta.x / 500f).coerceIn(-0.08f, 0.08f)
                        val dy = (delta.y / 500f).coerceIn(-0.08f, 0.08f)
                        scope.launch {
                            stretchXAnim.animateTo(dx, pressSpring)
                            stretchYAnim.animateTo(dy, pressSpring)
                        }
                    }
                }
            } finally {
                scope.launch {
                    pressAnim.animateTo(0f, pressSpring)
                    stretchXAnim.animateTo(0f, pressSpring)
                    stretchYAnim.animateTo(0f, pressSpring)
                }
            }
        }
    }
}

@Composable
fun rememberGlassInteractionState(): GlassInteractionState {
    val scope = rememberCoroutineScope()
    return remember(scope) { GlassInteractionState(scope) }
}

/**
 * Safe corner shape converter to satisfy Kyant's SDF lens shader requirements.
 */
fun safeGlassShape(shape: Shape): Shape =
    if (shape == RectangleShape) RoundedCornerShape(0.dp) else shape

/**
 * Modifier implementing the 7-Layer Clear Liquid Glass rendering pipeline:
 * 1. Backdrop capture (light blur + saturation boost 1.6x + brightness 1.05x)
 * 2. Edge refraction lens & chromatic dispersion (Tier A)
 * 3. Translucent tint (White 6% dark / Black 4% light with subtle gradient)
 * 4. Specular rim light (1dp gradient stroke: 55% top-left -> 5% mid -> 35% bottom-right)
 * 5. Inner highlight (top edge) & inner shadow (bottom edge)
 * 6. Ambient drop shadow (24dp blur, 8dp Y-offset, 18% black)
 * 7. Motion-reactive sheen (gyroscope tilt + scroll response)
 */
@Composable
fun Modifier.clearLiquidGlass(
    backdrop: Backdrop? = LocalLiquidGlassBackdrop.current,
    shape: Shape = RoundedCornerShape(28.dp),
    isSheet: Boolean = false,
    interactive: Boolean = true,
    enableStretch: Boolean = false,
    customBlurDp: Dp? = null,
    tintColor: Color? = null,
    interactionState: GlassInteractionState? = if (interactive) rememberGlassInteractionState() else null,
): Modifier {
    val tier = LocalGlassTier.current
    val params = LocalGlassParams.current
    val isDark = LocalIsDarkTheme.current
    val density = LocalDensity.current
    val tilt = LocalDeviceTilt.current

    val blurRadius = customBlurDp ?: (if (isSheet) params.sheetBlurDp.dp else params.blurDp.dp)
    val resolvedShape = safeGlassShape(shape)

    // Layer 6: Soft ambient drop shadow
    val shadowModifier = Modifier.shadow(
        elevation = 8.dp,
        shape = shape,
        clip = false,
        ambientColor = Color.Black.copy(alpha = params.ambientShadowAlpha),
        spotColor = Color.Black.copy(alpha = params.ambientShadowAlpha * 0.8f),
    )

    // Interactive scale & liquid stretch
    val interactiveTransformModifier = if (interactionState != null) {
        Modifier.graphicsLayer {
            val p = interactionState.pressProgress
            val baseScale = lerp(1f, 0.96f, p)
            scaleX = baseScale + (if (enableStretch) interactionState.stretchX else 0f)
            scaleY = baseScale + (if (enableStretch) interactionState.stretchY else 0f)
        }.pointerInput(interactionState) {
            interactionState.handlePointerInput(this, enableStretch)
        }
    } else Modifier

    // Tier C or No Backdrop Fallback: Translucent Tint + Specular Rim Light
    if (tier == GlassTier.TierC || backdrop == null) {
        return this
            .then(shadowModifier)
            .then(interactiveTransformModifier)
            .clip(shape)
            .background(
                if (isDark) {
                    Color.White.copy(alpha = params.tintAlpha + 0.04f)
                } else {
                    Color.Black.copy(alpha = params.tintAlpha + 0.02f)
                }
            )
            .drawGlassRimAndSheen(
                shape = shape,
                isDark = isDark,
                params = params,
                tilt = tilt,
                pressProgress = interactionState?.pressProgress ?: 0f,
            )
    }

    // Tier A & Tier B: Hardware Backdrop Pipeline
    return this
        .then(shadowModifier)
        .then(interactiveTransformModifier)
        .drawBackdrop(
            backdrop = backdrop,
            shape = { resolvedShape },
            highlight = { Highlight.Default },
            effects = {
                // Layer 1: Saturation 1.6x + Brightness 1.05x + Blur
                colorControls(
                    brightness = params.brightness - 1f,
                    contrast = 1f,
                    saturation = params.saturation,
                )
                blur(with(density) { blurRadius.toPx() })

                // Layer 2: Edge refraction lens (Tier A only)
                if (tier == GlassTier.TierA) {
                    val lensHeight = (size.minDimension * params.refraction).coerceAtMost(24f)
                    val lensAmount = (size.minDimension * 0.35f)
                    lens(lensHeight, lensAmount, false)
                }
            },
            onDrawBackdrop = { drawBackdrop ->
                drawBackdrop()
            },
            onDrawSurface = {
                val press = interactionState?.pressProgress ?: 0f

                // Layer 3: Tint (White 6% dark / Black 4% light with subtle gradient, or artwork tint)
                val baseTint = tintColor ?: if (isDark) Color.White else Color.Black
                val effectiveAlpha = if (tintColor != null) {
                    (0.12f + press * 0.04f).coerceIn(0.04f, 0.25f)
                } else {
                    (params.tintAlpha + press * 0.04f).coerceIn(0.02f, 0.20f)
                }
                drawRect(
                    brush = Brush.verticalGradient(
                        0f to baseTint.copy(alpha = effectiveAlpha * 1.35f),
                        1f to baseTint.copy(alpha = effectiveAlpha * 0.70f),
                    )
                )

                // Layer 5: Inner highlight (top edge) & inner shadow (bottom edge)
                drawRect(
                    brush = Brush.verticalGradient(
                        0f to Color.White.copy(alpha = if (isDark) 0.16f else 0.24f),
                        0.25f to Color.Transparent,
                        0.75f to Color.Transparent,
                        1f to Color.Black.copy(alpha = if (isDark) 0.22f else 0.08f),
                    ),
                    blendMode = BlendMode.SrcOver,
                )

                // Layer 7: Motion-reactive sheen (device tilt + touch glint)
                val sheenCenterX = size.width * (0.5f + tilt.x * 0.35f)
                val sheenCenterY = size.height * (0.3f + tilt.y * 0.25f)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.14f + press * 0.08f),
                            Color.Transparent,
                        ),
                        center = Offset(sheenCenterX, sheenCenterY),
                        radius = size.maxDimension * 0.85f,
                    ),
                    blendMode = BlendMode.Plus,
                )
            },
        )
        .drawGlassRimAndSheen(
            shape = shape,
            isDark = isDark,
            params = params,
            tilt = tilt,
            pressProgress = interactionState?.pressProgress ?: 0f,
        )
}

/**
 * Layer 4: Specular Rim Light: 1dp gradient stroke
 * White 55% top-left fading to 5% mid-edge to 35% bottom-right.
 */
private fun Modifier.drawGlassRimAndSheen(
    shape: Shape,
    isDark: Boolean,
    params: GlassParams,
    tilt: Offset,
    pressProgress: Float,
): Modifier = this.drawWithContent {
    drawContent()

    // 1dp specular rim gradient
    val strokeWidth = 1.dp.toPx()
    val rimAlpha = (params.rimLightAlpha + pressProgress * 0.25f).coerceIn(0.1f, 1f)

    val rimBrush = Brush.linearGradient(
        0f to Color.White.copy(alpha = 0.55f * rimAlpha),
        0.5f to Color.White.copy(alpha = 0.06f * rimAlpha),
        1f to Color.White.copy(alpha = 0.35f * rimAlpha),
        start = Offset(0f, 0f),
        end = Offset(size.width, size.height),
    )

    val outline = shape.createOutline(size, layoutDirection, this)
    drawOutline(
        outline = outline,
        brush = rimBrush,
        style = Stroke(width = strokeWidth),
    )
}

/**
 * Standard Clear Liquid Glass container surface.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    isSheet: Boolean = false,
    interactive: Boolean = false,
    enableStretch: Boolean = false,
    customBlurDp: Dp? = null,
    onClick: (() -> Unit)? = null,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit,
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        )
    } else Modifier

    Box(
        modifier = modifier
            .clearLiquidGlass(
                shape = shape,
                isSheet = isSheet,
                interactive = interactive || onClick != null,
                enableStretch = enableStretch,
                customBlurDp = customBlurDp,
            )
            .then(clickModifier),
        contentAlignment = contentAlignment,
        content = content,
    )
}
