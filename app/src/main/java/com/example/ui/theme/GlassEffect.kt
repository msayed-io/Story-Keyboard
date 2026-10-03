package com.example.ui.theme

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt

// =============================================================================
//  محرّك الخلفية الزجاجية (Frosted Glass Backdrop)
//
//  الفكرة: الصورة التي تختارها الكاتبة تُعالَج مرّة واحدة على شبكة بكسل الشاشة
//  نفسها — قصّ إلى نسب الشاشة، ثم تنزيل بجودة عالية (متوسّط الكتل لا يُنزل
//  عيّنة واحدة)، ثم تمويه حقيقي متعدد المرّات (Stack Blur)، ثم إشباع لوني
//  (Vibrancy) — فتبدو زجاجاً مصنوعاً بإتقان لا صورةً مغبّشة.
//
//  كل الحسابات الرقمية هنا دوال نقية (بدون أندرويد) ولذلك مُختبَرة في الوحدة.
// =============================================================================

/** صورة في الذاكرة كأرقام ARGB — الشكل النقي الذي تعمل عليه كل الحسابات. */
internal data class PixelImage(val width: Int, val height: Int, val pixels: IntArray)

/** مستطيل قصّ داخل الصورة الأصلية. */
internal data class CropRect(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val width: Int get() = right - left
    val height: Int get() = bottom - top
}

/** اتجاه الكاميرا المسجَّل في الصورة: كم درجة تدوير + هل فيها انعكاس. */
internal data class ExifTransform(val rotationDegrees: Int, val flipHorizontal: Boolean) {
    val isIdentity: Boolean get() = rotationDegrees == 0 && !flipHorizontal
}

internal object GlassMath {

    /** أقصى نصف قطر تمويه على شبكة الشاشة. */
    const val MAX_BLUR_PX = 25

    /** الإشباع اللوني الافتراضي: يعيد الحيوية التي يسرقها التمويه. */
    const val DEFAULT_VIBRANCY = 1.35f

    /** قوة طبقة النويز الرقيقة جداً التي تقتل تعرّجات التدرّج (Banding). */
    const val NOISE_LAYER_ALPHA = 0.10f

    /**
     * القصّ المركزي إلى نسبة الشاشة (مثل ContentScale.Crop تماماً):
     * الصورة الأعرض من الهدف تُقصّ من الجانبين، والأطول تُقصّ من أعلى وأسفل.
     */
    fun centerCrop(sourceWidth: Int, sourceHeight: Int, targetAspect: Float): CropRect {
        if (sourceWidth <= 0 || sourceHeight <= 0) return CropRect(0, 0, 0, 0)
        if (targetAspect <= 0f) return CropRect(0, 0, sourceWidth, sourceHeight)
        val sourceAspect = sourceWidth.toFloat() / sourceHeight.toFloat()
        return if (sourceAspect > targetAspect) {
            val width = (sourceHeight * targetAspect).roundToInt().coerceIn(1, sourceWidth)
            val left = (sourceWidth - width) / 2
            CropRect(left, 0, left + width, sourceHeight)
        } else {
            val height = (sourceWidth / targetAspect).roundToInt().coerceIn(1, sourceHeight)
            val top = (sourceHeight - height) / 2
            CropRect(0, top, sourceWidth, top + height)
        }
    }

    /** عامل التنزيل الصحيح الذي يجعل العرض قريباً من عرض الشاشة دون تجاوزه كثيراً. */
    fun downsampleFactor(sourceWidth: Int, targetWidth: Int): Int {
        if (sourceWidth <= 0 || targetWidth <= 0) return 1
        return max(1, sourceWidth / targetWidth)
    }

    /**
     * تنزيل بجودة عالية: كل بكسل ناتج = متوسّط كتلة كاملة من الصورة الأصلية،
     * فلا تُفقد أي تفصيلة ولا تظهر حوافّ مسنّنة (Aliasing).
     */
    fun boxDownsample(image: PixelImage, factor: Int): PixelImage {
        if (factor <= 1 || image.width <= 0 || image.height <= 0) return image
        val outWidth = max(1, image.width / factor)
        val outHeight = max(1, image.height / factor)
        val out = IntArray(outWidth * outHeight)
        val blockSize = factor * factor
        for (y in 0 until outHeight) {
            val sourceY = y * factor
            for (x in 0 until outWidth) {
                val sourceX = x * factor
                var r = 0; var g = 0; var b = 0
                for (dy in 0 until factor) {
                    val row = (sourceY + dy) * image.width
                    for (dx in 0 until factor) {
                        val p = image.pixels[row + sourceX + dx]
                        r += (p ushr 16) and 0xFF
                        g += (p ushr 8) and 0xFF
                        b += p and 0xFF
                    }
                }
                out[y * outWidth + x] =
                    OPAQUE or ((r / blockSize) shl 16) or ((g / blockSize) shl 8) or (b / blockSize)
            }
        }
        return PixelImage(outWidth, outHeight, out)
    }

    /**
     * تمويه حقيقي (ثلاث مرّات Box Blur ≈ منحنى غاوسي ناعم) بعرض ثابت على كل
     * الصورة مهما كبر حجمها — نصف القطر يُقصّ عند الحدود بشكل Clamp فلا تُظلم
     * الحواف. يُنفَّذ في مكانه (in place) توفيراً للذاكرة.
     */
    fun stackBlur(image: PixelImage, radius: Int) {
        if (radius <= 0 || image.width <= 0 || image.height <= 0) return
        val r = radius.coerceAtMost(128)
        val width = image.width
        val height = image.height
        val a = IntArray(width * height)
        val b = IntArray(width * height)
        System.arraycopy(image.pixels, 0, a, 0, a.size)
        repeat(3) {
            boxBlurHorizontal(a, b, width, height, r)
            boxBlurVertical(b, a, width, height, r)
        }
        System.arraycopy(a, 0, image.pixels, 0, a.size)
    }

    /** إشباع لوني حول الإضاءة: amount = 1 يترك الصورة، و0 يجعلها رمادية. */
    fun saturate(image: PixelImage, amount: Float) {
        val a = amount.coerceIn(0f, 3f)
        if (a == 1f) return
        val pixels = image.pixels
        for (i in pixels.indices) {
            val p = pixels[i]
            val r = (p ushr 16) and 0xFF
            val g = (p ushr 8) and 0xFF
            val b = p and 0xFF
            val luminance = 0.299f * r + 0.587f * g + 0.114f * b
            val nr = (luminance + (r - luminance) * a).roundToInt().coerceIn(0, 255)
            val ng = (luminance + (g - luminance) * a).roundToInt().coerceIn(0, 255)
            val nb = (luminance + (b - luminance) * a).roundToInt().coerceIn(0, 255)
            pixels[i] = OPAQUE or (nr shl 16) or (ng shl 8) or nb
        }
    }

    /**
     * مقياس رسم الطبقة الزجاجية: صورة مموّهة لا تحتاج دقّة الشاشة كاملة، فنصف
     * المقاس = ربع الذاكرة وأربع مرّات سرعة بالشكل نفسه تماماً. وعند تمويه ضعيف
     * جداً (0-3) نُبقي الدقّة كاملة حتى لا تفقد الصورة حدّتها.
     */
    fun renderScale(blurRadiusPx: Int): Float = if (blurRadiusPx <= 3) 1f else 0.5f

    /** قياس بعد المقياس، ولا يصغر أبداً عن بكسل واحد. */
    fun scaledSize(targetPx: Int, scale: Float): Int =
        max(1, (targetPx * scale).roundToInt())

    /** الحجم المعروض للتمويه: إعداد واحد يبدو بالقدر نفسه على كل الأجهزة. */
    fun blurRadiusPx(setting: Int): Int = setting.coerceIn(0, MAX_BLUR_PX)

    /**
     * ألفا غطاء السطح: الشفافية 45% تعني أن الصورة تظهر بنسبة 55%.
     * الحدّ الأدنى 5% يضمن ألّا يختفي الكيبورد تماماً، والأقصى غطاء كامل.
     */
    fun surfaceCoverAlpha(opacity: Float): Float = opacity.coerceIn(0.05f, 1f)

    /** ألفا التعتيم المكمّل — هو ما يُبقي الكيبورد مقروءاً فوق أي صورة. */
    fun tintAlpha(opacity: Float): Float = 1f - surfaceCoverAlpha(opacity)

    /**
     * أقلّ تنزيل (بقوى الرقم 2) يُبقي أطول ضلع مساوياً للدقّة المطلوبة أو أكبر،
     * فلا نحمّل صورة أضخم من الحاجة ولا نخسر تفاصيل نحتاجها.
     */
    fun sampleSizeFor(width: Int, height: Int, targetLongest: Int): Int {
        if (width <= 0 || height <= 0 || targetLongest <= 0) return 1
        val longest = max(width, height)
        var sample = 1
        while (longest / (sample * 2) >= targetLongest) sample *= 2
        return sample
    }

    /**
     * تحويل اتجاه الكاميرا (EXIF: 1..8) إلى تدوير + انعكاس، حتى لا تظهر الصورة
     * مقلوبة أو نائمة على جانبها.
     */
    fun exifTransform(orientation: Int): ExifTransform = when (orientation) {
        2 -> ExifTransform(0, true)
        3 -> ExifTransform(180, false)
        4 -> ExifTransform(180, true)
        5 -> ExifTransform(90, true)
        6 -> ExifTransform(90, false)
        7 -> ExifTransform(270, true)
        8 -> ExifTransform(270, false)
        else -> ExifTransform(0, false)
    }

    /** نويز ثابت (نفس البذرة = نفس النتيجة) لكسر تعرّجات التدرّج. */
    fun noisePixels(size: Int, seed: Int = 20261003): IntArray {
        var state = seed or 1
        val pixels = IntArray(size * size)
        for (i in pixels.indices) {
            state = state xor (state shl 13)
            state = state xor (state ushr 17)
            state = state xor (state shl 5)
            val speck = (state ushr 24) and 0x20
            pixels[i] = (speck shl 24) or 0x00FFFFFF
        }
        return pixels
    }

    private const val OPAQUE = 0xFF shl 24
}

// ---------------------------------------------------------------- تمويه كل صف

private fun boxBlurHorizontal(
    source: IntArray,
    target: IntArray,
    width: Int,
    height: Int,
    radius: Int
) {
    val divisor = radius * 2 + 1
    val half = divisor / 2
    for (y in 0 until height) {
        val row = y * width
        var sumR = 0; var sumG = 0; var sumB = 0
        for (i in -radius..radius) {
            val p = source[row + i.coerceIn(0, width - 1)]
            sumR += (p ushr 16) and 0xFF
            sumG += (p ushr 8) and 0xFF
            sumB += p and 0xFF
        }
        for (x in 0 until width) {
            // القسمة مُقرَّبة: الاقتطاع يجعل كل دورة تُظلم الصورة قليلاً وتأكل التفاصيل.
            target[row + x] = (0xFF shl 24) or
                (((sumR + half) / divisor) shl 16) or
                (((sumG + half) / divisor) shl 8) or ((sumB + half) / divisor)
            val leaving = source[row + (x - radius).coerceIn(0, width - 1)]
            val entering = source[row + (x + radius + 1).coerceIn(0, width - 1)]
            sumR += ((entering ushr 16) and 0xFF) - ((leaving ushr 16) and 0xFF)
            sumG += ((entering ushr 8) and 0xFF) - ((leaving ushr 8) and 0xFF)
            sumB += (entering and 0xFF) - (leaving and 0xFF)
        }
    }
}

private fun boxBlurVertical(
    source: IntArray,
    target: IntArray,
    width: Int,
    height: Int,
    radius: Int
) {
    val divisor = radius * 2 + 1
    val half = divisor / 2
    for (x in 0 until width) {
        var sumR = 0; var sumG = 0; var sumB = 0
        for (i in -radius..radius) {
            val p = source[i.coerceIn(0, height - 1) * width + x]
            sumR += (p ushr 16) and 0xFF
            sumG += (p ushr 8) and 0xFF
            sumB += p and 0xFF
        }
        for (y in 0 until height) {
            target[y * width + x] = (0xFF shl 24) or
                (((sumR + half) / divisor) shl 16) or
                (((sumG + half) / divisor) shl 8) or ((sumB + half) / divisor)
            val leaving = source[(y - radius).coerceIn(0, height - 1) * width + x]
            val entering = source[(y + radius + 1).coerceIn(0, height - 1) * width + x]
            sumR += ((entering ushr 16) and 0xFF) - ((leaving ushr 16) and 0xFF)
            sumG += ((entering ushr 8) and 0xFF) - ((leaving ushr 8) and 0xFF)
            sumB += (entering and 0xFF) - (leaving and 0xFF)
        }
    }
}

// ---------------------------------------------------------------- خط أنابيب العرض

/**
 * يحوّل الصورة المختارة إلى طبقة زجاجية جاهزة للرسم على شبكة بكسل الشاشة:
 * قصّ إلى نسب الشاشة ⇒ تنزيل بجودة عالية ⇒ تمويه ناعم ⇒ إشباع لوني.
 */
internal fun renderGlassBitmap(
    source: Bitmap,
    targetWidthPx: Int,
    targetHeightPx: Int,
    blurRadiusPx: Int,
    saturation: Float = GlassMath.DEFAULT_VIBRANCY
): Bitmap? {
    if (targetWidthPx <= 0 || targetHeightPx <= 0 || source.width <= 0 || source.height <= 0) {
        return null
    }

    // صورة مموّهة لا تحتاج بكسل الشاشة كاملاً: نصف المقاس يعطي الشكل نفسه
    // بربع الذاكرة وبأربع مرّات سرعة. وعند تمويه ضعيف (0-3) نُبقي الدقّة كاملة
    // حتى تظهر الصورة حادّة تماماً.
    val renderScale = GlassMath.renderScale(blurRadiusPx)
    val renderWidth = GlassMath.scaledSize(targetWidthPx, renderScale)
    val renderHeight = GlassMath.scaledSize(targetHeightPx, renderScale)
    val renderRadius = max(0, (blurRadiusPx * renderScale).roundToInt())

    val aspect = renderWidth.toFloat() / renderHeight.toFloat()
    val crop = GlassMath.centerCrop(source.width, source.height, aspect)
    if (crop.width <= 0 || crop.height <= 0) return null

    val pixels = IntArray(crop.width * crop.height)
    source.getPixels(pixels, 0, crop.width, crop.left, crop.top, crop.width, crop.height)

    var image = PixelImage(crop.width, crop.height, pixels)
    image = GlassMath.boxDownsample(image, GlassMath.downsampleFactor(crop.width, renderWidth))

    val scaled = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
    scaled.setPixels(image.pixels, 0, image.width, 0, 0, image.width, image.height)

    val exact = if (image.width != renderWidth || image.height != renderHeight) {
        Bitmap.createScaledBitmap(scaled, renderWidth, renderHeight, true).also { scaled.recycle() }
    } else {
        scaled
    }

    val working = PixelImage(
        renderWidth,
        renderHeight,
        IntArray(renderWidth * renderHeight).also {
            exact.getPixels(it, 0, renderWidth, 0, 0, renderWidth, renderHeight)
        }
    )
    exact.recycle()
    GlassMath.stackBlur(working, GlassMath.blurRadiusPx(renderRadius))
    GlassMath.saturate(working, saturation)

    val output = Bitmap.createBitmap(renderWidth, renderHeight, Bitmap.Config.ARGB_8888)
    output.setPixels(working.pixels, 0, renderWidth, 0, 0, renderWidth, renderHeight)
    return output
}

/** يفكّ ترميز الصورة المحفوظة مرة واحدة لكل تغيير. */
internal fun decodeBase64Bitmap(base64: String): Bitmap? {
    if (base64.isEmpty()) return null
    return try {
        val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
        android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
        null
    }
}

/** ذاكرة صغيرة لبصمة واحدة: الشاشة والمعاينة تطلبان النتيجة ذاتها فلا تُحسَب مرّتين. */
private object GlassCache {
    private var key: String? = null
    private var bitmap: Bitmap? = null

    @Synchronized
    fun get(sourceKey: String, width: Int, height: Int, blur: Int, saturation: Float): Bitmap? {
        val wanted = "$sourceKey|$width|$height|$blur|$saturation"
        return if (key == wanted) bitmap else null
    }

    @Synchronized
    fun put(
        sourceKey: String,
        width: Int,
        height: Int,
        blur: Int,
        saturation: Float,
        value: Bitmap?
    ) {
        // لا تُعاد تدوير الصورة القديمة أبداً: قد تكون قيد الرسم لحظتها.
        key = "$sourceKey|$width|$height|$blur|$saturation"
        bitmap = value
    }
}

/**
 * الطبقة الزجاجية الكاملة للخلفية: الصورة المُعالجة + غطاء الثيم بدرجة
 * الشفافية المختارة + تدرّج خفيف يعطي العمق + نويز رقيق يمنع التعرّجات.
 *
 * تُستخدم في شاشة الكيبورد وفي المعاينة الحيّة بالإعدادات — نفس الطبقة تماماً،
 * فيكون ما تراه الكاتبة في المعاينة هو ما ستراه على الكيبورد فعلاً.
 */
@Composable
internal fun GlassBackdrop(
    sourceBitmap: Bitmap?,
    sourceKey: String,
    opacity: Float,
    blurRadiusPx: Int,
    tintColor: Color,
    modifier: Modifier = Modifier,
    saturation: Float = GlassMath.DEFAULT_VIBRANCY
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val targetWidthPx = remember(configuration.screenWidthDp, density.density) {
        with(density) { configuration.screenWidthDp.dp.roundToPx() }
    }
    val targetHeightPx = remember(configuration.screenHeightDp, density.density) {
        with(density) { configuration.screenHeightDp.dp.roundToPx() }
    }

    // نُبقي آخر طبقة معروضة أثناء إعادة الحساب: تحريك شريط التمويه
    // يُبدّل الصورة بهدوء بدل أن تُفرغ الشاشة لحظة ثم تعود.
    val glassState = remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(sourceKey, sourceBitmap, targetWidthPx, targetHeightPx, blurRadiusPx, saturation) {
        val bitmap = sourceBitmap ?: return@LaunchedEffect

        val cached = GlassCache.get(sourceKey, targetWidthPx, targetHeightPx, blurRadiusPx, saturation)
        if (cached != null) {
            glassState.value = cached.asImageBitmap()
            return@LaunchedEffect
        }

        // تهدئة قصيرة: أثناء سحب الشريط تُلغى الحسابات المتلاحقة قبل أن تبدأ
        delay(70)
        val rendered = withContext(Dispatchers.Default) {
            renderGlassBitmap(bitmap, targetWidthPx, targetHeightPx, blurRadiusPx, saturation)
        }
        if (rendered != null) {
            GlassCache.put(sourceKey, targetWidthPx, targetHeightPx, blurRadiusPx, saturation, rendered)
            glassState.value = rendered.asImageBitmap()
        }
    }
    val glass = glassState.value

    Box(modifier = modifier) {
        glass?.let { image ->
            Image(
                bitmap = image,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                filterQuality = FilterQuality.Medium,
                modifier = Modifier.fillMaxSize()
            )
        }

        // غطاء الثيم: هو "زجاج" اللوحة — كل ما قلّت الشفافية زاد ظهور الصورة.
        val tint = GlassMath.tintAlpha(opacity)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            tintColor.copy(alpha = tint * 0.82f),
                            tintColor.copy(alpha = tint)
                        )
                    )
                )
        )

        // نويز بنسخ متكرّر 1:1 — الحبيبات كما هي بلا تكبير، فيتكسّر التدرّج
        // ولا تظهر خطوط "السلالم" التي تشوّه التمويه الكبير.
        val noiseImage: ImageBitmap = remember {
            val side = 48
            val bitmap = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(GlassMath.noisePixels(side), 0, side, 0, 0, side, side)
            bitmap.asImageBitmap()
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val brush = ShaderBrush(
                        ImageShader(noiseImage, TileMode.Repeated, TileMode.Repeated)
                    )
                    onDrawBehind {
                        drawRect(brush = brush, alpha = GlassMath.NOISE_LAYER_ALPHA)
                    }
                }
        )
    }
}
