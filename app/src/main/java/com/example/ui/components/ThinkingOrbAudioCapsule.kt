package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Full Pill Shape Audio Capsule with 100% circular edges (like keyboard spacebar).
 * Integrates the Audio-Reactive ThinkingOrb alongside the refined greeting text,
 * with a cinematic dimming backdrop and touch protection during audio.
 */
@Composable
fun ThinkingOrbAudioCapsule(
    visible: Boolean,
    audioState: OrbAudioState = OrbAudioState(),
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing)) +
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ),
        exit = fadeOut(animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing)) +
               slideOutVertically(
                   targetOffsetY = { it },
                   animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
               ),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Subtle Cinematic Dimming Backdrop (Touch-protected to prevent accidental interruptions)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .pointerInput(Unit) {
                        detectTapGestures {
                            // Touch protection: swallow touches so speech is not interrupted
                        }
                    }
            )

            // 2. Full Pill Capsule (100% CircleShape curves matching spacebar)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                        .navigationBarsPadding(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .shadow(
                                elevation = 24.dp,
                                shape = CircleShape,
                                spotColor = Color.Black.copy(alpha = 0.7f),
                                ambientColor = Color.Black.copy(alpha = 0.4f)
                            )
                            .clip(CircleShape)
                            .background(AppleGraphite.copy(alpha = 0.96f))
                            .border(
                                width = 1.dp,
                                color = AppleSteel.copy(alpha = 0.9f),
                                shape = CircleShape
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Native Audio-Reactive ThinkingOrb (Free-floating without artificial background circle)
                        Box(
                            modifier = Modifier.size(38.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ThinkingOrb(
                                sizeDp = 38.dp,
                                speed = 4.388f,
                                audioState = audioState
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Single, centered, refined description (Light diacritics & elegant)
                        Text(
                            text = "كيبورد الحكايات يُرحّب بكِ",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = ThmanyahSansFontFamily,
                            color = ApplePorcelain,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
