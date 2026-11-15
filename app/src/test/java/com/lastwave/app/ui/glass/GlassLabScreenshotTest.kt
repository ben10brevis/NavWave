package com.lastwave.app.ui.glass

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class GlassLabScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun captureGlassLabScreenshots() {
        composeTestRule.setContent {
            GlassLabScreen(onBack = {})
        }

        val bitmap = composeTestRule.onRoot().captureToImage().asAndroidBitmap()
        val outDir = File("C:/Users/Bala/.gemini/antigravity/brain/9682d94b-4b00-4ff0-a94f-9209c23a51ec")
        outDir.mkdirs()
        val outFile = File(outDir, "screenshot_glass_lab.png")
        FileOutputStream(outFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }
}
