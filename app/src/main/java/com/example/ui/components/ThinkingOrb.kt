package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import kotlin.math.*

/**
 * Real-time audio reactive state passed directly into the ThinkingOrb renderer.
 */
data class OrbAudioState(
    val envelope: Float = 0f,
    val bass: Float = 0f,
    val mid: Float = 0f,
    val treble: Float = 0f,
    val transient: Float = 0f
)

private class OrbDot(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f,
    var r: Float = 0f,
    var white: Float = 0f
)

/**
 * Audio-Reactive ThinkingOrb (listening state).
 * Preserves 100% of the original mathematical lattice geometry, rings, and speed,
 * while breathing and radiating organically with voice harmonics (envelope, ripple, pulse, light).
 */
@Composable
fun ThinkingOrb(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 42.dp,
    speed: Float = 4.388f,
    audioState: OrbAudioState = OrbAudioState()
) {
    // Exact parameters from presets.ts for listening mode
    val rings = 9
    val lonDensity = 23
    val rBase = 0.6f
    val rDepth = 1.7f
    val rsPow = 0.6f
    val rMin = 0.3f

    var timeInSeconds by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(speed) {
        val startTime = System.nanoTime()
        while (isActive) {
            withFrameNanos { frameTimeNanos ->
                val elapsed = (frameTimeNanos - startTime) / 1_000_000_000f
                // Base speed + subtle organic rotation modulation
                val rotationMod = 1f + audioState.envelope * 0.018f
                timeInSeconds = elapsed * speed * rotationMod
            }
        }
    }

    // Pre-allocated dot pool for 60/120 FPS zero-allocation rendering
    val dotPool = remember { ArrayList<OrbDot>(256) }

    Canvas(modifier = modifier.size(sizeDp)) {
        val canvasSize = size.minDimension
        if (canvasSize <= 0f) return@Canvas

        val cx = canvasSize / 2f
        val cy = canvasSize / 2f
        val R = (canvasSize / 2f) * 0.874f
        val t = timeInSeconds

        // Audio state parameters
        val voice = audioState.envelope
        val bass = audioState.bass
        val mid = audioState.mid
        val treble = audioState.treble
        val transient = audioState.transient

        // Exact core.ts projector matrix (yaw = t * 0.18, tilt = 0.38)
        val yaw = t * 0.18f
        val tilt = 0.38f
        val st = sin(tilt)
        val ct = cos(tilt)
        val sy = sin(yaw)
        val cyw = cos(yaw)

        // Exact core.ts radius scaling
        val rs = (canvasSize / 300f).pow(rsPow)

        // 6. Audio Scale (Breathing without geometric distortion, max 3.5% + 1.8% bass)
        val audioScale = 1f + voice * 0.035f + bass * 0.018f

        // 9. Transient pulse at speech onset
        val transientPulse = transient * 0.06f

        // 7. Dot pulse based on voice envelope and vocal frequencies
        val dotPulse = 1f + voice * 0.10f + mid * 0.045f + treble * 0.025f + transientPulse

        var dotIndex = 0

        for (ri in 0..rings) {
            val lat = -PI.toFloat() / 2f + (ri.toFloat() / rings.toFloat()) * PI.toFloat()
            val cosLat = cos(lat)
            val sinLat = sin(lat)

            // 5. Original wave + subtle internal voice ripple
            val voiceWave = 0.62f * sin(t * 2.1f - ri * 0.52f) + 0.38f * sin(t * 1.27f + ri * 0.83f)
            val audioRipple = voice * (
                0.11f * sin(ri * 0.92f - t * 3.2f) +
                0.045f * sin(ri * 1.7f + t * 4.1f)
            )
            val w = voiceWave + audioRipple

            val rr = R * (0.88f + 0.105f * w) * audioScale
            val lonCount = max(1, (abs(cosLat) * lonDensity).roundToInt())

            for (lj in 0 until lonCount) {
                val lon = (lj.toFloat() / lonCount.toFloat()) * 2f * PI.toFloat()
                val xOrig = cosLat * cos(lon) * rr
                val yOrig = sinLat * rr
                val zOrig = cosLat * sin(lon) * rr

                // Apply 3D Rotation Matrix Projection
                val x1 = xOrig * cyw + zOrig * sy
                val z1 = -xOrig * sy + zOrig * cyw
                val y1 = yOrig * ct - z1 * st
                val z2 = yOrig * st + z1 * ct

                val px = cx + x1
                val py = cy - y1
                val pz = z2

                val depth = (pz / R + 1f) / 2f
                val crest = max(0f, w)

                // 7. Point size with audio pulse
                val dotR = max(rMin, (rBase + rDepth * depth) * (1f + 0.4f * crest) * rs * dotPulse)

                // 8. Front-illuminating audio lighting
                val audioLight = voice * (0.08f + 0.08f * depth) + treble * 0.025f
                val dotWhite = (0.66f - 0.56f * depth - 0.1f * crest - audioLight).coerceIn(0f, 1f)

                if (dotIndex < dotPool.size) {
                    val dot = dotPool[dotIndex]
                    dot.x = px
                    dot.y = py
                    dot.z = pz
                    dot.r = dotR
                    dot.white = dotWhite
                } else {
                    dotPool.add(OrbDot(px, py, pz, dotR, dotWhite))
                }
                dotIndex++
            }
        }

        val activeDots = dotPool.subList(0, dotIndex)

        // Exact z-depth sorting (back to front)
        activeDots.sortBy { it.z }

        // Render each dot with ink color inversion
        for (i in 0 until dotIndex) {
            val d = activeDots[i]
            val g = ((1f - d.white) * 255f).roundToInt().coerceIn(0, 255)
            drawCircle(
                color = Color(g, g, g, 255),
                radius = d.r,
                center = Offset(d.x, d.y)
            )
        }
    }
}
