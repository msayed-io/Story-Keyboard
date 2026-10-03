package com.example

import com.example.ui.theme.CropRect
import com.example.ui.theme.ExifTransform
import com.example.ui.theme.GlassMath
import com.example.ui.theme.PixelImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The frosted-glass engine: cropping, high-quality downscaling, the blur itself,
 * the vibrancy pass and the opacity mapping. Every one of these is a pure
 * function, so the numbers below are checked exactly rather than "looked at".
 */
class GlassEffectTest {

    private fun image(width: Int, height: Int, fill: Int) =
        PixelImage(width, height, IntArray(width * height) { fill })

    private fun argb(r: Int, g: Int, b: Int) = (0xFF shl 24) or (r shl 16) or (g shl 8) or b

    // ------------------------------------------------------------ cropping

    @Test
    fun `a wide photo is cropped from both sides, evenly`() {
        val crop: CropRect = GlassMath.centerCrop(1600, 1000, 0.5f)
        assertEquals("a tall screen trims the sides", 500, crop.width)
        assertEquals("the full height is kept", 1000, crop.height)
        assertEquals(550, crop.left)
        assertEquals("the right edge sits where the crop ends", 1050, crop.right)
        assertEquals("both margins are equal", crop.left, 1600 - crop.right)
        assertEquals(0, crop.top)
        assertTrue("the crop must stay inside the photo", crop.right <= 1600)
    }

    @Test
    fun `a tall photo is cropped from top and bottom, evenly`() {
        val crop = GlassMath.centerCrop(1000, 2000, 1.0f)
        assertEquals(1000, crop.width)
        assertEquals(1000, crop.height)
        assertEquals(0, crop.left)
        assertEquals(500, crop.top)
        assertTrue(crop.bottom <= 2000)
    }

    @Test
    fun `a photo that already matches the screen keeps every pixel`() {
        val crop = GlassMath.centerCrop(1080, 2400, 1080f / 2400f)
        assertEquals(0, crop.left)
        assertEquals(0, crop.top)
        assertEquals(1080, crop.width)
        assertEquals(2400, crop.height)
    }

    @Test
    fun `a broken photo never produces a negative or empty rectangle`() {
        val crop = GlassMath.centerCrop(0, 0, 0.5f)
        assertEquals(0, crop.width)
        assertEquals(0, crop.height)
    }

    // ------------------------------------------------------------ downscaling

    @Test
    fun `downscaling averages whole blocks so no detail is lost to aliasing`() {
        val source = PixelImage(
            2, 2, intArrayOf(
                argb(0, 0, 0), argb(200, 200, 200),
                argb(100, 100, 100), argb(100, 100, 100)
            )
        )
        val result = GlassMath.boxDownsample(source, 2)
        assertEquals(1, result.width)
        assertEquals(1, result.height)
        // (0 + 200 + 100 + 100) / 4 = 100
        assertEquals(argb(100, 100, 100), result.pixels[0])
    }

    @Test
    fun `the downsample factor follows the screen width`() {
        assertEquals(4, GlassMath.downsampleFactor(3200, 800))
        assertEquals(1, GlassMath.downsampleFactor(600, 1080))
        assertEquals(1, GlassMath.downsampleFactor(0, 1080))
    }

    @Test
    fun `a factor of one changes nothing`() {
        val source = PixelImage(3, 1, intArrayOf(argb(1, 2, 3), argb(4, 5, 6), argb(7, 8, 9)))
        val result = GlassMath.boxDownsample(source, 1)
        assertEquals(argb(4, 5, 6), result.pixels[1])
        assertEquals(3, result.width)
    }

    // ------------------------------------------------------------ the blur

    @Test
    fun `a blur of zero leaves the photo exactly as it is`() {
        val source = PixelImage(4, 1, intArrayOf(argb(10, 20, 30), argb(40, 50, 60), argb(70, 80, 90), argb(0, 0, 0)))
        val copy = source.pixels.copyOf()
        GlassMath.stackBlur(source, 0)
        assertTrue(source.pixels.contentEquals(copy))
    }

    @Test
    fun `blurring a flat colour returns the same colour, edges included`() {
        val flat = image(21, 21, argb(120, 120, 120))
        GlassMath.stackBlur(flat, 4)
        assertTrue(flat.pixels.all { it == argb(120, 120, 120) })
    }

    /** يملأ مربعاً ساطعاً في المنتصف — إشارة واقعية ذات طاقة كافية. */
    private fun brightSquare(size: Int, halfSide: Int): PixelImage {
        val spot = image(size, size, argb(0, 0, 0))
        for (y in (size / 2 - halfSide)..(size / 2 + halfSide)) {
            for (x in (size / 2 - halfSide)..(size / 2 + halfSide)) {
                spot.pixels[y * size + x] = argb(255, 255, 255)
            }
        }
        return spot
    }

    @Test
    fun `the blur rounds instead of truncating, so the picture never darkens`() {
        // 1 0 0 with a one-pixel blur: the true average is 0.67 -> must round up, not vanish.
        val line = PixelImage(3, 1, intArrayOf(argb(1, 1, 1), argb(0, 0, 0), argb(0, 0, 0)))
        GlassMath.stackBlur(line, 1)
        assertTrue("a faint highlight must survive the blur", (line.pixels[0] and 0xFF) > 0)

        val grey = image(11, 11, argb(101, 101, 101))
        GlassMath.stackBlur(grey, 5)
        assertTrue("a flat tone must stay exactly as it was", grey.pixels.all { (it and 0xFF) == 101 })
    }

    @Test
    fun `a bright area spreads out and stays symmetric`() {
        val size = 41
        val spot = brightSquare(size, 4)
        GlassMath.stackBlur(spot, 6)

        val centre = spot.pixels[(size / 2) * size + (size / 2)]
        val left = spot.pixels[(size / 2) * size + (size / 2 - 5)]
        val right = spot.pixels[(size / 2) * size + (size / 2 + 5)]
        val up = spot.pixels[(size / 2 - 5) * size + (size / 2)]
        val down = spot.pixels[(size / 2 + 5) * size + (size / 2)]

        assertEquals("horizontal symmetry", left, right)
        assertEquals("vertical symmetry", up, down)
        val far = spot.pixels[(size / 2) * size + (size / 2 + 15)]
        assertTrue("the centre must stay the brightest point", (centre and 0xFF) >= (right and 0xFF))
        assertTrue("and clearly brighter than far away", (centre and 0xFF) > (far and 0xFF))
        assertTrue("the light really spread", (right and 0xFF) > 0)
    }

    @Test
    fun `the spread fades away smoothly without overshoot`() {
        val size = 61
        val spot = brightSquare(size, 5)
        GlassMath.stackBlur(spot, 8)

        val row = size / 2
        var previous = 256
        for (offset in 0..30) {
            val value = spot.pixels[row * size + (size / 2 + offset)] and 0xFF
            assertTrue("must never rise again (no halo)", value <= previous)
            assertTrue("must never exceed the source", value <= 255)
            previous = value
        }
        assertEquals("far away it must be fully settled", 0, previous)
    }

    @Test
    fun `a sharp edge becomes a soft ramp, never a hard jump`() {
        val wide = PixelImage(64, 1, IntArray(64) { if (it < 32) argb(0, 0, 0) else argb(255, 255, 255) })
        GlassMath.stackBlur(wide, 5)
        val values = IntArray(64) { wide.pixels[it] and 0xFF }

        assertTrue("the dark side must have lightened", values[31] > 0)
        assertTrue("the light side must have darkened", values[32] < 255)
        assertTrue("no value may leave the original range", values.all { it in 0..255 })
        val jumps = (0 until 63).count { kotlin.math.abs(values[it + 1] - values[it]) > 64 }
        assertEquals("no hard jump may survive the blur", 0, jumps)
    }

    // ------------------------------------------------------------ vibrancy

    @Test
    fun `full vibrancy leaves the colours untouched`() {
        val photo = PixelImage(2, 1, intArrayOf(argb(200, 100, 50), argb(10, 20, 30)))
        val copy = photo.pixels.copyOf()
        GlassMath.saturate(photo, 1f)
        assertTrue(photo.pixels.contentEquals(copy))
    }

    @Test
    fun `zero vibrancy turns the photo grey without touching its brightness`() {
        val photo = PixelImage(1, 1, intArrayOf(argb(200, 100, 50)))
        GlassMath.saturate(photo, 0f)
        val pixel = photo.pixels[0]
        val r = (pixel ushr 16) and 0xFF
        val g = (pixel ushr 8) and 0xFF
        val b = pixel and 0xFF
        assertEquals("grey means equal channels", r, g)
        assertEquals(g, b)
        val expected = (0.299f * 200 + 0.587f * 100 + 0.114f * 50).toInt()
        assertTrue("brightness is preserved", kotlin.math.abs(r - expected) <= 1)
    }

    @Test
    fun `more vibrancy widens the colour gap`() {
        val photo = PixelImage(1, 1, intArrayOf(argb(180, 120, 60)))
        GlassMath.saturate(photo, 1.4f)
        val pixel = photo.pixels[0]
        val red = (pixel ushr 16) and 0xFF
        val blue = pixel and 0xFF
        assertTrue("red must move further from blue", red - blue > 120)
        assertTrue("channels stay inside the byte range", red in 0..255 && blue in 0..255)
    }

    @Test
    fun `extreme vibrancy cannot overflow the colour channels`() {
        val photo = PixelImage(1, 1, intArrayOf(argb(255, 0, 0)))
        GlassMath.saturate(photo, 3f)
        val pixel = photo.pixels[0]
        assertEquals(0xFF, (pixel ushr 24) and 0xFF)
        assertTrue((pixel ushr 16) and 0xFF in 0..255)
        assertTrue(pixel and 0xFF in 0..255)
    }

    // ------------------------------------------------------------ slider mapping

    @Test
    fun `the transparency slider decides how much of the photo shows through the keys`() {
        assertEquals(0.36f, GlassMath.surfaceTintAlpha(0.36f), 0.0001f)
        assertEquals("64% of the photo shows at the default", 0.64f, GlassMath.photoVisibility(0.36f), 0.0001f)
        assertEquals("the default really is 36%", 0.36f, GlassMath.DEFAULT_TINT_ALPHA, 0.0001f)
        assertEquals("nothing shows through a fully covered key", 0f, GlassMath.photoVisibility(1f), 0.0001f)
    }

    @Test
    fun `the white keys stay prominent while still being glass`() {
        val soft = GlassMath.accentTintAlpha(0.05f)
        val strong = GlassMath.accentTintAlpha(0.9f)
        assertTrue("a white key is always more covered than a normal one", soft > 0.05f)
        assertTrue("more photo means a paler white key too", strong > soft)
        assertEquals("its palest state", 0.65f, GlassMath.accentTintAlpha(4f), 0.0001f)
        assertTrue("never fully covered", GlassMath.accentTintAlpha(4f) <= 0.9f)
    }

    @Test
    fun `the sliders can never produce a value outside its range`() {
        assertEquals(25, GlassMath.blurRadiusPx(999))
        assertEquals(0, GlassMath.blurRadiusPx(-4))
        assertEquals(0.05f, GlassMath.surfaceTintAlpha(0f), 0.0001f)
        assertEquals(1f, GlassMath.surfaceTintAlpha(4f), 0.0001f)
    }

    // ------------------------------------------------------------ light through glass

    @Test
    fun `light passing through the glass is tamed but never lost`() {
        assertEquals(0, GlassMath.transmittedChannel(0))
        assertTrue("pure white stays bright", GlassMath.transmittedChannel(255) > 180)
        assertTrue("but it is tamed", GlassMath.transmittedChannel(255) < 255)
        assertTrue("a bright area cannot outshine itself", GlassMath.transmittedChannel(255) <= 255)

        var previous = -1
        for (value in 0..255) {
            val out = GlassMath.transmittedChannel(value)
            assertTrue("brighter stays brighter", out >= previous)
            previous = out
        }
    }

    // ------------------------------------------------------------ the rim of light

    @Test
    fun `the rim glows at the top, calms at the sides and whispers at the bottom`() {
        val top = GlassMath.edgeRimAlpha(0f)
        val side = GlassMath.edgeRimAlpha(0.5f)
        val bottom = GlassMath.edgeRimAlpha(1f)

        assertEquals("the top is the brightest", GlassMath.EDGE_RIM_ALPHA_TOP, top, 0.0001f)
        assertEquals("the sides are calm", GlassMath.EDGE_RIM_ALPHA_SIDE, side, 0.0001f)
        assertEquals("the bottom is a whisper", GlassMath.EDGE_RIM_ALPHA_BOTTOM, bottom, 0.0001f)
        assertTrue("top > sides > bottom", top > side && side > bottom)
    }

    @Test
    fun `the rim fades smoothly, with no step anywhere`() {
        var previous = GlassMath.edgeRimAlpha(0f)
        for (i in 1..200) {
            val value = GlassMath.edgeRimAlpha(i / 200f)
            assertTrue("no sudden jump in the rim", kotlin.math.abs(value - previous) < 0.06f)
            previous = value
        }
        assertEquals("values below 0 are treated as the top", GlassMath.edgeRimAlpha(-3f), GlassMath.edgeRimAlpha(0f), 0.0001f)
        assertEquals("values above 1 are treated as the bottom", GlassMath.edgeRimAlpha(9f), GlassMath.edgeRimAlpha(1f), 0.0001f)
    }

    @Test
    fun `the rim is a thin hairline, never a thick band`() {
        assertTrue("thinner than a single pixel at common densities", GlassMath.EDGE_RIM_WIDTH_DP <= 0.8f)
        assertTrue("still visible", GlassMath.EDGE_RIM_WIDTH_DP >= 0.4f)
        assertTrue(GlassMath.EDGE_RIM_WIDTH_HEADER_DP >= GlassMath.EDGE_RIM_WIDTH_DP)
    }

    // ------------------------------------------------------------ aligning the backdrop

    @Test
    fun `the backdrop keeps the safe margin so nothing shifts behind the keys`() {
        val crop = GlassMath.insetCrop(1000, 2000, 0.12f)
        assertEquals("a 12% margin on a 1000px photo", 120, crop.left)
        assertEquals("and on a 2000px photo", 240, crop.top)
        assertEquals("symmetrical", crop.left, 1000 - crop.right)
        assertEquals("symmetrical vertically", crop.top, 2000 - crop.bottom)
        assertTrue("the crop can never be empty", crop.width > 0 && crop.height > 0)
    }

    @Test
    fun `an extreme margin still yields a usable photo`() {
        val crop = GlassMath.insetCrop(10, 10, 5f)
        assertTrue(crop.width > 0 && crop.height > 0)
        val broken = GlassMath.insetCrop(0, 0, 0.12f)
        assertEquals(0, broken.width)
    }

    @Test
    fun `the glass layer is drawn at half size once the blur is real`() {
        assertEquals("a sharp photo keeps full detail", 1f, GlassMath.renderScale(0), 0.0001f)
        assertEquals(1f, GlassMath.renderScale(3), 0.0001f)
        assertEquals("a blurred photo needs far fewer pixels", 0.5f, GlassMath.renderScale(4), 0.0001f)
        assertEquals(0.5f, GlassMath.renderScale(25), 0.0001f)

        assertEquals(1200, GlassMath.scaledSize(2400, 0.5f))
        assertEquals(1080, GlassMath.scaledSize(1080, 1f))
        assertEquals("never zero, whatever the numbers", 1, GlassMath.scaledSize(1, 0.5f))
        assertEquals(1, GlassMath.scaledSize(0, 0.5f))
    }

    // ------------------------------------------------------------ camera files

    @Test
    fun `a huge camera photo is loaded at screen resolution, not full size`() {
        assertEquals(1, GlassMath.sampleSizeFor(800, 600, 1080))
        assertEquals(2, GlassMath.sampleSizeFor(4000, 3000, 1600))
        assertEquals(1, GlassMath.sampleSizeFor(0, 0, 1600))
    }

    @Test
    fun `a sideways camera photo is straightened, and an upright one is untouched`() {
        assertEquals(ExifTransform(0, false), GlassMath.exifTransform(1))
        assertEquals(true, GlassMath.exifTransform(1).isIdentity)

        assertEquals(90, GlassMath.exifTransform(6).rotationDegrees)
        assertEquals(270, GlassMath.exifTransform(8).rotationDegrees)
        assertEquals(180, GlassMath.exifTransform(3).rotationDegrees)

        assertEquals(true, GlassMath.exifTransform(2).flipHorizontal)
        assertEquals(true, GlassMath.exifTransform(5).flipHorizontal)
        assertEquals(false, GlassMath.exifTransform(7).isIdentity)

        assertEquals("an unknown value must never rotate the photo",
            ExifTransform(0, false), GlassMath.exifTransform(99))
    }

}
