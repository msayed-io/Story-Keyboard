package com.example.ui.theme

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Shader
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import java.util.Random

/**
 * Creates a beautiful GPU-accelerated repeating linen paper texture shader.
 * Completely offline, high performance, with micro-noise and elegant weave lines.
 */
fun createLinenShaderBrush(isDark: Boolean): ShaderBrush {
    val size = 128
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    
    // Clear transparent
    canvas.drawColor(android.graphics.Color.TRANSPARENT)
    
    // Configure thread paint
    val paint = android.graphics.Paint().apply {
        color = if (isDark) {
            // White threads with subtle alpha
            0x1AFFFFFF.toInt()
        } else {
            // Dark threads with very fine alpha matching royal paper tone
            0x12121A1B.toInt()
        }
        strokeWidth = 1f
        isAntiAlias = true
    }
    
    // Weave spacing (coarse linen weave)
    val spacing = 6
    for (i in 0 until size step spacing) {
        // Horizontal threads with slight jitter to look organic
        val offsetH = (Math.sin(i.toDouble()) * 0.5).toFloat()
        canvas.drawLine(0f, i.toFloat() + offsetH, size.toFloat(), i.toFloat() + offsetH, paint)
        
        // Vertical threads with slight jitter
        val offsetV = (Math.cos(i.toDouble()) * 0.5).toFloat()
        canvas.drawLine(i.toFloat() + offsetV, 0f, i.toFloat() + offsetV, size.toFloat(), paint)
    }
    
    // Fine micro-noise paper grains
    val grainPaint = android.graphics.Paint().apply {
        color = if (isDark) 0x11FFFFFF.toInt() else 0x0C121A1B.toInt()
    }
    val rand = Random(1337)
    for (k in 0..400) {
        val gx = rand.nextInt(size).toFloat()
        val gy = rand.nextInt(size).toFloat()
        canvas.drawPoint(gx, gy, grainPaint)
    }
    
    val shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    return ShaderBrush(shader)
}

/**
 * Procedural Linen Paper background modifier that overlays on top of any canvas.
 * Sets opacity to 30% (alpha = 0.3f) as requested by Dar Al-Hikayat specifications.
 */
fun Modifier.linenBackground(isDark: Boolean): Modifier = this.drawBehind {
    val brush = createLinenShaderBrush(isDark)
    drawRect(
        brush = brush,
        alpha = 0.30f // Strict 30% opacity matching linen token
    )
}

/**
 * Adds an elastic pinch scaling physical feedback on active press,
 * matching the Dar Al-Hikayat interactive physics specifications.
 */
fun Modifier.appleElasticPinch(interactionSource: MutableInteractionSource): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "pinch_scale"
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

fun Modifier.appleElasticPinch(): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "pinch_scale"
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

