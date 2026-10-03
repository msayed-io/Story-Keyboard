package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    isConnected: Boolean,
    savedIp: String,
    onNavigateToKeyboard: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onNavigateToCustomization: () -> Unit,
    onDisconnect: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isDark = MaterialTheme.colorScheme.background == Color(0xFF111718)
    val isAppleDark = MaterialTheme.colorScheme.background == Color(0xFF000000)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .linenBackground(isDark)
        ) {
            val minScreenHeight = maxHeight

            // TOP BACKDROP GRADIENT & MAGNETIC SCROLL DISSOLVE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .align(Alignment.TopCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.50f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // Primary Scrollable Page Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .padding(top = 80.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = minScreenHeight - 112.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Modern Apple Dark Minimal Header (No circular logo image)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(AppleCarbon)
                                .border(1.dp, AppleSteel, RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = "كيبورد الحكايات",
                                tint = ApplePorcelain,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "كِيبُورد الحِكَايَاتِ",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = ThmanyahSerifDisplayFontFamily,
                            color = ApplePorcelain,
                            textAlign = TextAlign.Center
                        )

                        Row(
                            modifier = Modifier.padding(top = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "المِحرَابُ اللَّاسِلكِيُّ لِلكَاتِبَةِ الرِّوَائِيَّةِ",
                                fontSize = 13.sp,
                                fontFamily = ThmanyahSansFontFamily,
                                color = ApplePorcelain,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "  •  ",
                                fontSize = 11.sp,
                                color = AppleAsh
                            )
                            Text(
                                text = "بثٌّ فوريّ",
                                fontSize = 13.sp,
                                fontFamily = ThmanyahSansFontFamily,
                                color = AppleAsh
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // ---------------------------------------------------------
                    // CARDS GROUP (Balanced Vertical Container)
                    // ---------------------------------------------------------
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. LIVE CONNECTION STATUS CARD
                        val statusCardBg = if (isAppleDark) AppleCarbon else MaterialTheme.colorScheme.surface
                        val statusCardBorder = if (isConnected) {
                            BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f))
                        } else {
                            BorderStroke(1.dp, AppleSteel)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("connection_status_card"),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = statusCardBg),
                            border = statusCardBorder,
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 18.dp)
                            ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Status Indicator & Short Text
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    // Glowing Pulse LED
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isConnected) Color(0xFF10B981).copy(alpha = 0.25f)
                                                    else Color(0xFFEF4444).copy(alpha = 0.25f)
                                                )
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444)
                                                )
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = if (isConnected) "موصول بالتابلت" else "غير موصول",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = ThmanyahSansFontFamily,
                                            color = if (isConnected) Color(0xFF10B981) else ApplePorcelain
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (isConnected && savedIp.isNotEmpty()) savedIp else "امسحي رمز الـ QR للبدء الفوري",
                                            fontSize = 12.sp,
                                            fontFamily = ThmanyahSansFontFamily,
                                            color = AppleAsh
                                        )
                                    }
                                }

                                // Compact Inline Action Button
                                if (isConnected) {
                                    val disconnectInteraction = remember { MutableInteractionSource() }
                                    OutlinedButton(
                                        onClick = onDisconnect,
                                        modifier = Modifier
                                            .height(38.dp)
                                            .appleElasticPinch(disconnectInteraction)
                                            .testTag("btn_disconnect_session"),
                                        shape = RoundedCornerShape(170.dp),
                                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = Color(0xFFEF4444).copy(alpha = 0.12f),
                                            contentColor = Color(0xFFEF4444)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                        interactionSource = disconnectInteraction
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.WifiOff,
                                            contentDescription = "قطع",
                                            modifier = Modifier.size(14.dp),
                                            tint = Color(0xFFEF4444)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "قطع الاتصال",
                                            fontSize = 12.sp,
                                            fontFamily = ThmanyahSansFontFamily,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    val scanInteraction = remember { MutableInteractionSource() }
                                    Button(
                                        onClick = onNavigateToScanner,
                                        modifier = Modifier
                                            .height(38.dp)
                                            .appleElasticPinch(scanInteraction)
                                            .testTag("btn_quick_scan"),
                                        shape = RoundedCornerShape(170.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AppleWhite,
                                            contentColor = AppleObsidian
                                        ),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                        interactionSource = scanInteraction
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "مسح",
                                            modifier = Modifier.size(14.dp),
                                            tint = AppleObsidian
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "مسح الرمز",
                                            fontSize = 12.sp,
                                            fontFamily = ThmanyahSansFontFamily,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // زر العودة إلى الكيبورد — يظهر فقط والاتصال قائم فعلاً
                            // (حالة الفحص الحقيقية من التابلت، لا مجرّد بيانات محفوظة)
                            if (isConnected && savedIp.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(14.dp))

                                val enterInteraction = remember { MutableInteractionSource() }
                                Button(
                                    onClick = onNavigateToKeyboard,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .appleElasticPinch(enterInteraction)
                                        .testTag("btn_enter_keyboard"),
                                    shape = RoundedCornerShape(170.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppleWhite,
                                        contentColor = AppleObsidian
                                    ),
                                    interactionSource = enterInteraction
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Keyboard,
                                        contentDescription = null,
                                        modifier = Modifier.size(17.dp),
                                        tint = AppleObsidian
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "الدخول إلى الكيبورد",
                                        fontSize = 14.sp,
                                        fontFamily = ThmanyahSansFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "الاتصال ما زال قائماً — تابعي الكتابة من حيث توقّفتِ",
                                    fontSize = 11.sp,
                                    fontFamily = ThmanyahSansFontFamily,
                                    color = AppleAsh,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            }
                        }

                        // 2. INFORMATIVE GUIDE CARD
                        AnimatedVisibility(
                            visible = !isConnected,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { 20 }),
                            exit = fadeOut() + slideOutVertically(targetOffsetY = { 20 })
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(28.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = AppleCarbon
                                ),
                                border = BorderStroke(1.dp, AppleSteel)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 18.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(AppleSteel),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QrCodeScanner,
                                            contentDescription = null,
                                            tint = ApplePorcelain,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = "كيف تبدئين الكتابة؟",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = ThmanyahSansFontFamily,
                                            color = ApplePorcelain
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "افتحي دار الحكايات على التابلت، وانقري على (مسح الرمز) أعلاه للاقتران المباشر عبر الشبكة المحلية.",
                                            fontSize = 11.5.sp,
                                            lineHeight = 16.sp,
                                            fontFamily = ThmanyahSansFontFamily,
                                            color = AppleAsh
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    // Literary Signature Footer
                    Text(
                        text = "دار الحكايات ❦ كيبورد الكاتبة اللاسلكي",
                        fontSize = 12.sp,
                        fontFamily = ThmanyahSansFontFamily,
                        color = AppleAsh,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }

            // FLOATING TOP CAPSULE HEADER SYSTEM (44dp Apple Utility Navigation)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title Capsule (الجانب الأيمن)
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppleGraphite.copy(alpha = 0.94f))
                        .border(
                            width = 1.dp,
                            color = AppleSteel,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "كِيبُورد الحِكَايَاتِ",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = ThmanyahSerifDisplayFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        ),
                        color = ApplePorcelain
                    )
                }

                // Combined Quick Status & Settings Capsule (44dp height)
                val settingsInteraction = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppleGraphite.copy(alpha = 0.94f))
                        .border(
                            width = 1.dp,
                            color = AppleSteel,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(start = 14.dp, end = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Status Indicator Dot
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isConnected) Color(0xFF10B981).copy(alpha = 0.25f)
                                        else Color(0xFFEF4444).copy(alpha = 0.25f)
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444)
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Subtle Vertical Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(16.dp)
                                .background(AppleSteel)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        // Settings Icon Button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .appleElasticPinch(settingsInteraction)
                                .clickable(
                                    interactionSource = settingsInteraction,
                                    indication = null,
                                    onClick = onNavigateToCustomization
                                )
                                .testTag("btn_customization"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "مركز التخصيص والإعدادات",
                                tint = ApplePorcelain,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
