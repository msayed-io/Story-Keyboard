package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Wifi
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
                    .padding(top = 80.dp, bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Royal Literary Emblem / Logo System
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(top = 12.dp, bottom = 16.dp)
                ) {
                    // Soft ambient halo glow
                    Box(
                        modifier = Modifier
                            .size(118.dp)
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
                            .size(102.dp)
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
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
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
                // 1. LIVE CONNECTION STATUS CARD (بطاقة حالة الاتصال الملكية)
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
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = statusCardBg),
                    border = statusCardBorder,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Status Header Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // LED Indicator
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444)
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isConnected) "موصول بالتابلت بنجاح" else "غير موصول بالتابلت",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = ThmanyahSansFontFamily,
                                    color = if (isConnected) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // IP Badge (if present)
                            if (savedIp.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(
                                            if (isConnected) Color(0xFF10B981).copy(alpha = 0.12f)
                                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = savedIp,
                                        fontSize = 11.sp,
                                        fontFamily = ThmanyahSansFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isConnected) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Descriptive Context Message
                        Text(
                            text = if (isConnected) {
                                "محراب الدار مستعد لتلقي حروفكِ وروايتكِ بلمح البصر دون أي تأخير ❦"
                            } else {
                                "افتحي نافذة كيبورد الكاتبة على التابلت وامسحي رمز الـ QR للبدء"
                            },
                            fontSize = 12.sp,
                            fontFamily = ThmanyahSansFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth(),
                            lineHeight = 18.sp
                        )

                        // Disconnect Action Button (زر قطع الاتصال الصريح)
                        if (isConnected || savedIp.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                thickness = 0.5.dp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val disconnectInteraction = remember { MutableInteractionSource() }
                                OutlinedButton(
                                    onClick = onDisconnect,
                                    modifier = Modifier
                                        .height(38.dp)
                                        .appleElasticPinch(disconnectInteraction)
                                        .testTag("btn_disconnect_session"),
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color(0xFFEF4444).copy(alpha = 0.08f),
                                        contentColor = Color(0xFFEF4444)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                    interactionSource = disconnectInteraction
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WifiOff,
                                        contentDescription = "قطع الاتصال",
                                        modifier = Modifier.size(15.dp),
                                        tint = Color(0xFFEF4444)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "قطع الاتصال وإنهاء الجلسة",
                                        fontSize = 12.sp,
                                        fontFamily = ThmanyahSansFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // ---------------------------------------------------------
                // 2. HERO ACTION: OPEN KEYBOARD (لوحة مفاتيح الرواية)
                // ---------------------------------------------------------
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
                        containerColor = if (isConnected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            if (isAppleDark) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = if (isConnected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isConnected) 6.dp else 2.dp)
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
                                    .background(
                                        if (isConnected) Color.Black.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Keyboard,
                                    contentDescription = "الكيبورد",
                                    tint = if (isConnected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
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
                                    color = if (isConnected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isConnected) "جاهز للكتابة • بث لحظي نشط" else "فتح اللوحة وإعدادات النصوص",
                                    fontSize = 12.sp,
                                    fontFamily = ThmanyahSansFontFamily,
                                    color = if (isConnected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Arrow indicator
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "انتقال",
                            tint = if (isConnected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ---------------------------------------------------------
                // 3. DUAL ACTION GRID (اقتران جديد + مركز التخصيص)
                // ---------------------------------------------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Card 1: Scan QR Code
                    val scanInteraction = remember { MutableInteractionSource() }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp)
                            .appleElasticPinch(scanInteraction)
                            .clickable(
                                interactionSource = scanInteraction,
                                indication = null,
                                onClick = onNavigateToScanner
                            )
                            .testTag("btn_scan_qr"),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = statusCardBg),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "ماسح الـ QR",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "اقتران جديد",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = ThmanyahSansFontFamily,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "مسح رمز QR ذكي",
                                    fontSize = 11.sp,
                                    fontFamily = ThmanyahSansFontFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Card 2: Customization Hub
                    val customInteraction = remember { MutableInteractionSource() }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp)
                            .appleElasticPinch(customInteraction)
                            .clickable(
                                interactionSource = customInteraction,
                                indication = null,
                                onClick = onNavigateToCustomization
                            )
                            .testTag("btn_customization"),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = statusCardBg),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "التخصيص",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "مركز السمات",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = ThmanyahSansFontFamily,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "تخصيص المظهر والألوان",
                                    fontSize = 11.sp,
                                    fontFamily = ThmanyahSansFontFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Literary Signature Footer
                Text(
                    text = "دار الحكايات ❦ كيبورد الكاتبة اللاسلكي",
                    fontSize = 12.sp,
                    fontFamily = ThmanyahSansFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }

            // FLOATING TOP CAPSULE HEADER SYSTEM (نظام الكبسولات العلوية الطافية)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title Capsule
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
                        .padding(horizontal = 20.dp),
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

                // Quick Status Indicator Capsule
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
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isConnected) "متصل 🟢" else "غير متصل 🔴",
                            fontSize = 12.sp,
                            fontFamily = ThmanyahSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
