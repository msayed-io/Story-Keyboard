package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.MediaItem
import com.example.data.MediaType
import com.example.ui.theme.*

enum class MediaPickerTab {
    CURATED,
    LOCAL
}

/**
 * نافذة اختيار الوسائط والخلفيات الأدبية والمحلية المدمجة الفاخرة (Compact Floating Modal Dialog).
 *
 * المميزات:
 * 1. طول مدمج ومحدد وثابت (Fixed Compact Height = 430.dp) يناسب 3 صفوف بدقة تامة.
 * 2. كبسولة علوية عائمة حرة (Floating Capsule Header) بدون خطوط فاصلة.
 * 3. تدرج ذوبان علوي ناعم (Dissolving Gradient Backdrop) يسمح بمرور المحتوى من تحت الكبسولة أثناء التمرير.
 * 4. شبكة ثلاثية موحدة تعرض كل الصور الـ 77 (لوحات الدار الأصلية الـ 27 أولاً ثم المناظر المنسقة الـ 50 مباشرة).
 */
@Composable
fun MediaPickerModal(
    isOpen: Boolean,
    selectedMediaId: String,
    curatedItems: List<MediaItem>,
    downloadProgress: Map<String, Int>,
    onSelectCuratedItem: (MediaItem) -> Unit,
    onSelectLocalMedia: (Uri) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    var currentTab by remember { mutableStateOf(MediaPickerTab.CURATED) }

    // لاقط الوسائط الموحد (صور وفيديوهات في زر واحد بدون تكرار)
    val pickVisualMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onSelectLocalMedia(uri)
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            // البطاقة الرئيسية العائمة للنافذة بطول مدمج وثابت تماماً (Fixed Compact Dimensions)
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(430.dp) // طول ثابت مدمج يناسب تماماً 3 صفوف بالشكل الطولي
                    .clip(RoundedCornerShape(30.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { /* إيقاف إغلاق النافذة عند الضغط داخلها */ }
                    )
                    .testTag("media_picker_dialog"),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = AppleCarbon),
                border = BorderStroke(1.2.dp, AppleSteel),
                elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {

                    // =========================================================
                    // 1. مساحة المحتوى الممرر داخلياً (Scrollable Content Underneath)
                    // =========================================================
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220)) togetherWith
                                    fadeOut(animationSpec = tween(180))
                        },
                        label = "tab_content_transition",
                        modifier = Modifier.fillMaxSize()
                    ) { tab ->
                        when (tab) {
                            MediaPickerTab.LOCAL -> {
                                // -----------------------------------------
                                // تبويب محلي
                                // -----------------------------------------
                                LocalMediaUploadSection(
                                    onLaunchPicker = {
                                        pickVisualMedia.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                            )
                                        )
                                    }
                                )
                            }

                            MediaPickerTab.CURATED -> {
                                // -----------------------------------------
                                // تبويب المكتبة الأدبية الكاملة (الـ 77 صورة)
                                // -----------------------------------------
                                CuratedMediaGridSection(
                                    items = curatedItems,
                                    selectedMediaId = selectedMediaId,
                                    downloadProgress = downloadProgress,
                                    onItemClick = { item ->
                                        onSelectCuratedItem(item)
                                    }
                                )
                            }
                        }
                    }

                    // =========================================================
                    // 2. تدرج التذويب العلوي (Dissolve Gradient Backdrop)
                    //    يحمي الكبسولة العائمة من التداخل مع الصور الممررة
                    // =========================================================
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        AppleCarbon,
                                        AppleCarbon.copy(alpha = 0.95f),
                                        AppleCarbon.copy(alpha = 0.75f),
                                        AppleCarbon.copy(alpha = 0.35f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // =========================================================
                    // 3. الكبسولة وزر الإغلاق العائم في الرأس (Floating Capsule Header)
                    // =========================================================
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // زر الإغلاق الدائري الفاخر
                        val closeInteraction = remember { MutableInteractionSource() }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AppleGraphite)
                                .border(1.dp, AppleSteel, CircleShape)
                                .appleElasticPinch(closeInteraction)
                                .clickable(
                                    interactionSource = closeInteraction,
                                    indication = null,
                                    onClick = onDismiss
                                )
                                .testTag("btn_close_media_picker"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = ApplePorcelain,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // الكبسولة الدائرية العائمة المطابقة لتصميم الواجهة الأصلي الفاخر
                        LiterarySegmentedCapsule(
                            currentTab = currentTab,
                            onTabSelected = { currentTab = it }
                        )

                        // مساحة فارغة لموازنة التصميم التناظري
                        Spacer(modifier = Modifier.size(36.dp))
                    }
                }
            }
        }
    }
}

// =============================================================================
//  كبسولة الاختيار الرئيسية
// =============================================================================
@Composable
private fun LiterarySegmentedCapsule(
    currentTab: MediaPickerTab,
    onTabSelected: (MediaPickerTab) -> Unit
) {
    Box(
        modifier = Modifier
            .height(42.dp)
            .clip(RoundedCornerShape(21.dp))
            .background(Color(0xFF0F0F10))
            .border(1.dp, AppleSteel.copy(alpha = 0.7f), RoundedCornerShape(21.dp))
            .padding(3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // 1. المكتبة الأدبية ✨
            val isCurated = currentTab == MediaPickerTab.CURATED
            val curatedInteraction = remember { MutableInteractionSource() }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isCurated) AppleWhite else Color.Transparent)
                    .appleElasticPinch(curatedInteraction)
                    .clickable(
                        interactionSource = curatedInteraction,
                        indication = null,
                        onClick = { onTabSelected(MediaPickerTab.CURATED) }
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "المكتبة الأدبية",
                        fontSize = 13.sp,
                        fontWeight = if (isCurated) FontWeight.Black else FontWeight.Bold,
                        fontFamily = ThmanyahSansFontFamily,
                        color = if (isCurated) AppleObsidian else AppleAsh
                    )
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isCurated) AppleObsidian else AppleAsh,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(3.dp))

            // 2. محلي 📁
            val isLocal = currentTab == MediaPickerTab.LOCAL
            val localInteraction = remember { MutableInteractionSource() }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isLocal) AppleWhite else Color.Transparent)
                    .appleElasticPinch(localInteraction)
                    .clickable(
                        interactionSource = localInteraction,
                        indication = null,
                        onClick = { onTabSelected(MediaPickerTab.LOCAL) }
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "محلي",
                        fontSize = 13.sp,
                        fontWeight = if (isLocal) FontWeight.Black else FontWeight.Bold,
                        fontFamily = ThmanyahSansFontFamily,
                        color = if (isLocal) AppleObsidian else AppleAsh
                    )
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = if (isLocal) AppleObsidian else AppleAsh,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

// =============================================================================
//  قسم محلي: رفع صورة أو فيديو من الجهاز
// =============================================================================
@Composable
private fun LocalMediaUploadSection(
    onLaunchPicker: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 64.dp, bottom = 16.dp, start = 14.dp, end = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(185.dp)
                .clip(RoundedCornerShape(22.dp))
                .appleElasticPinch(interaction)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    onClick = onLaunchPicker
                )
                .testTag("btn_local_media_upload_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = AppleGraphite.copy(alpha = 0.75f)),
            border = BorderStroke(1.2.dp, AppleSteel)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AppleCarbon)
                        .border(1.dp, AppleSteel, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "إرفاق وسائط",
                        tint = ApplePorcelain,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "أرفِقي وسائطكِ من ألبومكِ",
                    fontSize = 15.sp,
                    fontFamily = ThmanyahSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = ApplePorcelain
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "اختاري صورة أو مقطع فيديو انسيابي لروايتكِ ❦",
                    fontSize = 11.5.sp,
                    fontFamily = ThmanyahSansFontFamily,
                    color = AppleAsh,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// =============================================================================
//  قسم المكتبة الأدبية: شبكة 3 أعمدة نقية وخلابة لجميع الـ 77 صورة
// =============================================================================
@Composable
private fun CuratedMediaGridSection(
    items: List<MediaItem>,
    selectedMediaId: String,
    downloadProgress: Map<String, Int>,
    onItemClick: (MediaItem) -> Unit
) {
    val context = LocalContext.current

    if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 66.dp, bottom = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = AppleWhite,
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "جارٍ استدعاء المكتبة الأدبية...",
                    fontSize = 13.sp,
                    fontFamily = ThmanyahSansFontFamily,
                    color = AppleAsh
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(
                top = 66.dp, // يتيح للمحتوى البدء أسفل الكبسولة والمرور من تحتها بسلاسة
                bottom = 14.dp,
                start = 14.dp,
                end = 14.dp
            )
        ) {
            items(items, key = { it.id }) { item ->
                val isSelected = item.id == selectedMediaId
                val progress = downloadProgress[item.id]
                val isDownloading = progress != null && progress > 0 && progress < 100
                val cardInteraction = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.15f)
                        .clip(RoundedCornerShape(16.dp))
                        .then(
                            if (isSelected) {
                                Modifier.border(2.5.dp, AppleWhite, RoundedCornerShape(16.dp))
                            } else {
                                Modifier.border(1.dp, AppleSteel.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            }
                        )
                        .appleElasticPinch(cardInteraction)
                        .clickable(
                            interactionSource = cardInteraction,
                            indication = null,
                            onClick = { onItemClick(item) }
                        )
                        .testTag("media_item_${item.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    // الصورة المعروضة داخل البطاقة
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(item.previewUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // تدرج ظلي خفيف لحماية الأيقونات الداخلية
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.25f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.35f)
                                    )
                                )
                            )
                    )

                    // تمييز الفيديو بأيقونة فيديو / تشغيل صغيرة داخل البطاقة فقط
                    if (item.type == MediaType.VIDEO) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(5.dp)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f))
                                .border(0.8.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "فيديو",
                                tint = AppleWhite,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    // مؤشر ونسبة التحميل المئوية الحقيقية في منتصف البطاقة
                    if (isDownloading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.70f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = { (progress ?: 0) / 100f },
                                    modifier = Modifier.size(26.dp),
                                    color = AppleWhite,
                                    strokeWidth = 2.5.dp,
                                    trackColor = AppleSteel
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${progress}%",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppleWhite,
                                    fontFamily = ThmanyahSansFontFamily
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
