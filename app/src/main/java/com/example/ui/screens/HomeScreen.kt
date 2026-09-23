package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val isDark = MaterialTheme.colorScheme.background == Color(0xFF111718)

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
                .height(140.dp)
                .align(Alignment.TopCenter)
        ) {
            // Layer A: Ambient Top Vignette Gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.45f),
                                Color.Transparent
                            )
                        )
                    )
            )
            // Layer B: Magnetic Dissolve
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background.copy(alpha = 0.75f),
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
                .padding(horizontal = 24.dp)
                .padding(top = 100.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Decoupled Branding Logo & Floating Illustration
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .size(130.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .padding(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_app_icon),
                    contentDescription = "كيبورد الحكايات",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Arabic Literary Text Intro
            Text(
                text = "كِيبُورد الحِكَايَاتِ",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                fontFamily = ThmanyahSerifDisplayFontFamily,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            // Zero-Pill Quiet Typographic Ornaments
            Row(
                modifier = Modifier.padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "مِحْرَابُ الكَاتِبَةِ الروائية",
                    fontSize = 13.sp,
                    fontFamily = ThmanyahSansFontFamily,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "  ❦  ",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Text(
                    text = "تحكم لاسلكي كامل",
                    fontSize = 13.sp,
                    fontFamily = ThmanyahSansFontFamily,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Connection Status Card
            val isAppleDark = MaterialTheme.colorScheme.background == Color(0xFF000000)
            val statusInteractionSource = remember { MutableInteractionSource() }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .appleElasticPinch(statusInteractionSource)
                    .testTag("connection_status_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = borderStrokeLight(),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isAppleDark) 16.dp else 4.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Dynamic glowing LED status indicator dot
                        val dotColor = if (isAppleDark) {
                            if (isConnected) Color(0xFF30D158) else Color(0xFFFF453A)
                        } else {
                            if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        }
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = if (isConnected) "موصول بمحراب الدار 🟢" else "غير موصول بالتابلت 🔴",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = ThmanyahSansFontFamily,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))

                    if (isConnected && savedIp.isNotEmpty()) {
                        Text(
                            text = "العنوان الرقمي: $savedIp",
                            fontSize = 13.sp,
                            fontFamily = ThmanyahSansFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "الدار مستعدة لتلقي حروفكِ وأطيافكِ بلمح البصر",
                            fontSize = 12.sp,
                            fontFamily = ThmanyahSansFontFamily,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    } else {
                        Text(
                            text = "الرجاء مسح رمز الاقتران QR من شاشة التابلت للبدء",
                            fontSize = 13.sp,
                            fontFamily = ThmanyahSansFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Central Actions Grid
            val btn1BgColor = if (isAppleDark) {
                Color(0xFFF5F5F5) // فضي ناصع في داكن آبل
            } else {
                if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
            }
            val btn1ContentColor = if (isAppleDark) {
                Color(0xFF000000) // نص أسود مطفأ في داكن آبل
            } else {
                MaterialTheme.colorScheme.onPrimary
            }

            val secBgColor = if (isAppleDark) Color.White.copy(alpha = 0.03f) else Color.Transparent
            val secBorder = if (isAppleDark) {
                BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
            } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            }
            val secContentColor = if (isAppleDark) Color(0xFFF5F5F5) else MaterialTheme.colorScheme.onBackground
            val secIconTint = if (isAppleDark) Color(0xFFF5F5F5) else MaterialTheme.colorScheme.primary

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Button 1: Start Writing Keyboard
                val btn1InteractionSource = remember { MutableInteractionSource() }
                Button(
                    onClick = onNavigateToKeyboard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .appleElasticPinch(btn1InteractionSource)
                        .testTag("btn_start_keyboard"),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = btn1BgColor,
                        contentColor = btn1ContentColor
                    ),
                    interactionSource = btn1InteractionSource
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "كيبورد الحكايات",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "فتح لوحة مفاتيح الرواية الروائية",
                        fontSize = 16.sp,
                        fontFamily = ThmanyahSansFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Button 2: Scan QR for pairing
                val btn2InteractionSource = remember { MutableInteractionSource() }
                OutlinedButton(
                    onClick = onNavigateToScanner,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .appleElasticPinch(btn2InteractionSource)
                        .testTag("btn_scan_qr"),
                    shape = RoundedCornerShape(28.dp),
                    border = secBorder,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = secBgColor,
                        contentColor = secContentColor
                    ),
                    interactionSource = btn2InteractionSource
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "ماسح الـ QR",
                        modifier = Modifier.size(20.dp),
                        tint = secIconTint
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "اقتران جديد عبر رمز الـ QR",
                        fontSize = 15.sp,
                        fontFamily = ThmanyahSansFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Button 3: Customization hub
                val btn3InteractionSource = remember { MutableInteractionSource() }
                OutlinedButton(
                    onClick = onNavigateToCustomization,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .appleElasticPinch(btn3InteractionSource)
                        .testTag("btn_customization"),
                    shape = RoundedCornerShape(28.dp),
                    border = secBorder,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = secBgColor,
                        contentColor = secContentColor
                    ),
                    interactionSource = btn3InteractionSource
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "التخصيص",
                        modifier = Modifier.size(20.dp),
                        tint = secIconTint
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "مركز التخصيص والثيمات المدمجة",
                        fontSize = 15.sp,
                        fontFamily = ThmanyahSansFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // FLOATING TOP CAPSULE HEADER SYSTEM (نظام الكبسولات العلوية الطافية)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .height(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "كِيبُورد الحِكَايَاتِ",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = ThmanyahSerifDisplayFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun borderStrokeLight(): BorderStroke {
    val isAppleDark = MaterialTheme.colorScheme.background == Color(0xFF000000)
    return if (isAppleDark) {
        BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
    } else {
        BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
    }
}
