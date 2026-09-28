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
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .linenBackground(isDark)
        ) {
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
                    .padding(top = 96.dp, bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Royal Literary Emblem / Logo System
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(top = 16.dp, bottom = 18.dp)
                ) {
                    // Soft ambient halo glow
                    Box(
                        modifier = Modifier
                            .size(122.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = if (isConnected) 0.28f else 0.12f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Emblem Frame
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(104.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = 1.5.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                    )
                                ),
                                shape = CircleShape
                            )
                            .shadow(elevation = 8.dp, shape = CircleShape)
                            .padding(5.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_icon),
                            contentDescription = "كيبورد الحكايات",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Title & Literary Typography
                Text(
                    text = "كِيبُورد الحِكَايَاتِ",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = ThmanyahSerifDisplayFontFamily,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                // Elegant Subtitle with Golden Literary Flourish
                Row(
                    modifier = Modifier.padding(top = 4.dp, bottom = 32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "المِحرَابُ اللَّاسِلكِيُّ لِلكَاتِبَةِ الرِّوَائِيَّةِ",
                        fontSize = 13.sp,
                        fontFamily = ThmanyahSansFontFamily,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "  ❦  ",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "بثٌّ فوريّ",
                        fontSize = 13.sp,
                        fontFamily = ThmanyahSansFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // ---------------------------------------------------------
                // 1. LIVE CONNECTION STATUS CARD (بطاقة حالة الاتصال الكبسولية الفاخرة)
                // ---------------------------------------------------------
                val statusCardBg = if (isAppleDark) Color.White.copy(alpha = 0.04f) else MaterialTheme.colorScheme.surface
                val statusCardBorder = if (isConnected) {
                    BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f))
                } else {
                    BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("connection_status_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = statusCardBg),
                    border = statusCardBorder,
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
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
                                    color = if (isConnected) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isConnected && savedIp.isNotEmpty()) savedIp else "امسحي رمز الـ QR للبدء الفوري",
                                    fontSize = 12.sp,
                                    fontFamily = ThmanyahSansFontFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Compact Inline Action Button (قطع الاتصال or مسح الرمز)
                        if (isConnected) {
                            val disconnectInteraction = remember { MutableInteractionSource() }
                            OutlinedButton(
                                onClick = onDisconnect,
                                modifier = Modifier
                                    .height(36.dp)
                                    .appleElasticPinch(disconnectInteraction)
                                    .testTag("btn_disconnect_session"),
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(0.8.dp, Color(0xFFEF4444).copy(alpha = 0.35f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color(0xFFEF4444).copy(alpha = 0.08f),
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
                                    .height(36.dp)
                                    .appleElasticPinch(scanInteraction)
                                    .testTag("btn_quick_scan"),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                interactionSource = scanInteraction
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "مسح",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
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
                }

                // ---------------------------------------------------------
                // 2. HERO ACTION: OPEN KEYBOARD (يظهر فقط في وضع الاتصال)
                // ---------------------------------------------------------
                AnimatedVisibility(
                    visible = isConnected,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { 20 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { 20 })
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(18.dp))

                        val heroInteractionSource = remember { MutableInteractionSource() }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .appleElasticPinch(heroInteractionSource)
                                .clickable(
                                    interactionSource = heroInteractionSource,
                                    indication = null,
                                    onClick = onNavigateToKeyboard
                                )
                                .testTag("btn_start_keyboard"),
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Icon capsule
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(Color.Black.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Keyboard,
                                            contentDescription = "الكيبورد",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column {
                                        Text(
                                            text = "لوحة مفاتيح الرواية",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = ThmanyahSansFontFamily,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "جاهز للكتابة • بث لحظي نشط",
                                            fontSize = 12.sp,
                                            fontFamily = ThmanyahSansFontFamily,
                                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                        )
                                    }
                                }

                                // Arrow indicator
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "انتقال",
                                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // ---------------------------------------------------------
                // 3. INFORMATIVE GUIDE CARD (يظهر فقط في حالة عدم الاتصال لإرشاد الكاتبة)
                // ---------------------------------------------------------
                AnimatedVisibility(
                    visible = !isConnected,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { 20 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { 20 })
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(18.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isAppleDark) Color.White.copy(alpha = 0.02f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
                            ),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
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
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
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
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "افتحي دار الحكايات على التابلت، وانقري على (مسح الرمز) أعلاه للاقتران المباشر عبر الشبكة المحلية.",
                                        fontSize = 11.5.sp,
                                        lineHeight = 16.sp,
                                        fontFamily = ThmanyahSansFontFamily,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Literary Signature Footer
                Text(
                    text = "دار الحكايات ❦ كيبورد الكاتبة اللاسلكي",
                    fontSize = 12.sp,
                    fontFamily = ThmanyahSansFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    textAlign = TextAlign.Center
                )
            }

            // FLOATING TOP CAPSULE HEADER SYSTEM (نظام الكبسولات العلوية الفاخرة)
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
                        .height(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            shape = CircleShape
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
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Combined Quick Status & Settings Capsule (نقطة الحالة + زر الإعدادات المدمج)
                val settingsInteraction = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .height(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                        .padding(start = 14.dp, end = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Status Indicator Dot (دائرة/نقطة الحالة فقط بدون أي نص)
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
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
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
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
