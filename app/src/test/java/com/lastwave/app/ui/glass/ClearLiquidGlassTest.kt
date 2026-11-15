package com.lastwave.app.ui.glass

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ClearLiquidGlassTest {

    @Test
    fun glassParams_matchesClearLiquidGlassSpec() {
        val params = GlassParams()

        // 2.1 "Clear" specification
        assertThat(params.tintAlpha).isEqualTo(0.06f)
        assertThat(params.blurDp).isEqualTo(12f)
        assertThat(params.sheetBlurDp).isEqualTo(28f)
        assertThat(params.refraction).isEqualTo(0.18f)
        assertThat(params.dispersion).isEqualTo(0.04f)
        assertThat(params.rimLightAlpha).isEqualTo(0.55f)
        assertThat(params.saturation).isEqualTo(1.60f)
        assertThat(params.brightness).isEqualTo(1.05f)
        assertThat(params.ambientShadowAlpha).isEqualTo(0.18f)
    }

    @Test
    fun safeGlassShape_convertsRectangleShapeToCornerBasedShape() {
        val safe = safeGlassShape(RectangleShape)

        assertThat(safe).isInstanceOf(CornerBasedShape::class.java)
        assertThat(safe).isEqualTo(RoundedCornerShape(0.dp))
    }

    @Test
    fun safeGlassShape_preservesCornerBasedShapes() {
        val circle = CircleShape
        val rounded = RoundedCornerShape(28.dp)

        assertThat(safeGlassShape(circle)).isSameInstanceAs(circle)
        assertThat(safeGlassShape(rounded)).isSameInstanceAs(rounded)
    }

    @Test
    fun hardwareTiers_orderAndCapabilities() {
        assertThat(GlassTier.entries).containsExactly(
            GlassTier.TierA,
            GlassTier.TierB,
            GlassTier.TierC,
        ).inOrder()
    }
}
