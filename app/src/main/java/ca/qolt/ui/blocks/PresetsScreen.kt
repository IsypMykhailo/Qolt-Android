package ca.qolt.ui.blocks

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import ca.qolt.R
import ca.qolt.data.local.entity.PresetEntity
import ca.qolt.model.InstalledApp
import ca.qolt.ui.theme.Orange
import java.util.UUID

@Composable
private fun rememberAppIcon(drawable: Drawable): ImageBitmap {
    return remember(drawable) {
        val w = drawable.intrinsicWidth.coerceAtLeast(1)
        val h = drawable.intrinsicHeight.coerceAtLeast(1)
        val bmp = createBitmap(w, h)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bmp.asImageBitmap()
    }
}

@Composable
fun Presets(modifier: Modifier = Modifier, viewModel: PresetsViewModel) {
    val allApps by viewModel.allApps.collectAsState()
    val presets by viewModel.presets.collectAsState()
    val currentPresetId by viewModel.currentPresetId.collectAsState()
    PresetsScreen(
        modifier = modifier,
        allApps = allApps,
        presets = presets,
        currentPresetId = currentPresetId,
        onSavePreset = viewModel::savePreset,
        onDeletePreset = viewModel::deletePreset,
        setCurrentPresetId = viewModel::setCurrentPresetId
    )
}

@Composable
fun PresetsScreen(
    modifier: Modifier = Modifier,
    allApps: List<InstalledApp>,
    presets: List<PresetEntity>,
    currentPresetId: String?,
    onSavePreset: (PresetEntity, Boolean) -> Unit,
    onDeletePreset: (PresetEntity) -> Unit,
    setCurrentPresetId: (String) -> Unit
) {
    var editingPreset by remember { mutableStateOf<PresetEntity?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var menuExpandedForId by remember { mutableStateOf<String?>(null) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val currentPreset = remember(presets, currentPresetId) {
        presets.firstOrNull { it.id == currentPresetId }
    }
    val recentPresets = remember(presets, currentPreset) {
        if (currentPreset != null) {
            presets.filterNot { it.id == currentPreset.id }
        } else {
            presets
        }
    }

    val scrollState = rememberScrollState()

    // Animated FAB scale
    val fabScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fabScale"
    )

    val topBarOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else (-50).dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "topBarOffset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (presets.isEmpty()) {
            // Empty State with animations
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.Top)
            ) {
                Text(
                    text = "Blocks",
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (isVisible) 1f else 0f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(600)) + scaleIn(
                            initialScale = 0.8f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )

                            Text(
                                text = "You don't have any blocks yet",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )

                            FilledTonalButton(
                                onClick = {
                                    isCreatingNew = true
                                    editingPreset = PresetEntity(
                                        id = UUID.randomUUID().toString(),
                                        name = "",
                                        description = "",
                                        blockedApps = emptyList()
                                    )
                                },
                                modifier = Modifier.padding(top = 8.dp),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Create Your First Block",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.Top)
            ) {
                // Top Bar with animations
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = topBarOffset),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Search icon (placeholder)
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shadowElevation = 2.dp,
                        tonalElevation = 2.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Text(
                        text = "Blocks",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )

                    // Add button with animation
                    FloatingActionButton(
                        onClick = {
                            isCreatingNew = true
                            editingPreset = PresetEntity(
                                id = UUID.randomUUID().toString(),
                                name = "",
                                description = "",
                                blockedApps = emptyList()
                            )
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .scale(fabScale),
                        containerColor = Orange,
                        shape = CircleShape,
                        elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add preset",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Presets List with animations
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top)
                ) {
                    AnimatedVisibility(
                        visible = currentPreset != null,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 })
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "CURRENT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    letterSpacing = 1.2.sp
                                )
                            )
                            currentPreset?.let { preset ->
                                PresetRow(
                                    preset = preset,
                                    isCurrent = true,
                                    menuExpandedForId = menuExpandedForId,
                                    onMenuExpandedChange = { menuExpandedForId = it },
                                    onSetCurrent = { setCurrentPresetId(preset.id) },
                                    onEdit = {
                                        isCreatingNew = false
                                        editingPreset = preset
                                    },
                                    onDelete = { onDeletePreset(preset) },
                                    isVisible = isVisible
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = recentPresets.isNotEmpty(),
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = if (currentPreset != null) "RECENT" else "ALL PRESETS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    letterSpacing = 1.2.sp
                                )
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                recentPresets.forEachIndexed { index, preset ->
                                    AnimatedVisibility(
                                        visible = isVisible,
                                        enter = fadeIn(
                                            animationSpec = tween(
                                                durationMillis = 400,
                                                delayMillis = index * 50
                                            )
                                        ) + slideInVertically(
                                            initialOffsetY = { it / 3 },
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        )
                                    ) {
                                        PresetRow(
                                            preset = preset,
                                            isCurrent = false,
                                            menuExpandedForId = menuExpandedForId,
                                            onMenuExpandedChange = { menuExpandedForId = it },
                                            onSetCurrent = { setCurrentPresetId(preset.id) },
                                            onEdit = {
                                                isCreatingNew = false
                                                editingPreset = preset
                                            },
                                            onDelete = { onDeletePreset(preset) },
                                            isVisible = true
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        val presetToEdit = editingPreset
        if (presetToEdit != null) {
            PresetEditorDialog(
                preset = presetToEdit,
                allApps = allApps,
                onDismiss = {
                    editingPreset = null
                    isCreatingNew = false
                },
                onSave = { updated ->
                    onSavePreset(updated, isCreatingNew)
                    editingPreset = null
                    isCreatingNew = false
                }
            )
        }
    }
}


@Composable
private fun PresetRow(
    preset: PresetEntity,
    isCurrent: Boolean,
    menuExpandedForId: String?,
    onMenuExpandedChange: (String?) -> Unit,
    onSetCurrent: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isVisible: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "presetScale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isCurrent) 4.dp else 2.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "presetElevation"
    )

    val cardColor by animateColorAsState(
        targetValue = if (isCurrent) Orange else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = Color.White.copy(alpha = 0.2f))
                ) {
                    if (!isCurrent) onSetCurrent()
                }
                .padding(vertical = 16.dp, horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji with scale animation
            val emojiScale by animateFloatAsState(
                targetValue = if (isCurrent) 1.1f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "emojiScale"
            )

            Text(
                text = preset.emoji,
                style = TextStyle(fontSize = 28.sp),
                modifier = Modifier.scale(emojiScale)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = preset.name.ifBlank { "Untitled preset" },
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    if (isCurrent) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.padding(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Current",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(2.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${preset.blockedApps.size} ${if (preset.blockedApps.size == 1) "app" else "apps"}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isCurrent)
                                Color.White.copy(alpha = 0.8f)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    )
                    if (preset.description.isNotBlank()) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isCurrent)
                                    Color.White.copy(alpha = 0.6f)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                        Text(
                            text = preset.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isCurrent)
                                    Color.White.copy(alpha = 0.8f)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
            }

            Box {
                IconButton(
                    onClick = { onMenuExpandedChange(preset.id) }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Preset options",
                        tint = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = menuExpandedForId == preset.id,
                    onDismissRequest = { onMenuExpandedChange(null) }
                ) {
                    if (!isCurrent) {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text("Set as current")
                                }
                            },
                            onClick = {
                                onMenuExpandedChange(null)
                                onSetCurrent()
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("Edit")
                            }
                        },
                        onClick = {
                            onMenuExpandedChange(null)
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text("Delete", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        onClick = {
                            onMenuExpandedChange(null)
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetEditorDialog(
    preset: PresetEntity,
    allApps: List<InstalledApp>,
    onDismiss: () -> Unit,
    onSave: (PresetEntity) -> Unit
) {
    var name by remember { mutableStateOf(preset.name) }
    val description = "Black List"
    var selectedPackages by remember { mutableStateOf(preset.blockedApps.toSet()) }

    val emojiOptions = listOf(
        "\uD83D\uDCD6",
        "\uD83D\uDCDA",
        "\uD83D\uDCA4",
        "\uD83D\uDCBC",
        "\uD83C\uDFAE",
        "\uD83C\uDFA7",
        "\uD83D\uDCF5"
    )
    var emoji by remember { mutableStateOf(preset.emoji.ifBlank { "\uD83D\uDCD6" }) }

    var searchQuery by remember { mutableStateOf("") }
    var showOnlySelected by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(AppCategoryFilter.All) }
    var sortSelectedFirst by remember { mutableStateOf(true) }

    val filteredApps = remember(
        allApps,
        searchQuery,
        showOnlySelected,
        selectedCategory,
        sortSelectedFirst,
        selectedPackages
    ) {
        allApps
            .asSequence()
            .filter { app ->
                val matchesSearch = app.appName.contains(searchQuery, ignoreCase = true)
                val matchesSelected = !showOnlySelected || app.packageName in selectedPackages
                val appCategory = app.toCategoryFilter()
                val matchesCategory =
                    selectedCategory == AppCategoryFilter.All || appCategory == selectedCategory
                matchesSearch && matchesSelected && matchesCategory
            }
            .sortedWith(
                compareBy<InstalledApp>(
                    { if (sortSelectedFirst) !(it.packageName in selectedPackages) else false },
                    { it.appName.lowercase() }
                )
            )
            .toList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (preset.name.isBlank()) "New Preset" else "Edit Preset",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Preset name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Orange,
                        focusedLabelColor = Orange
                    )
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Icon",
                        style = MaterialTheme.typography.labelLarge
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        emojiOptions.forEach { option ->
                            val isSelected = emoji == option
                            val scale by animateFloatAsState(
                                targetValue = if (isSelected) 1.1f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessHigh
                                ),
                                label = "emojiScale"
                            )

                            Surface(
                                modifier = Modifier.scale(scale),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Orange.copy(alpha = 0.2f) else Color.Transparent,
                                tonalElevation = if (isSelected) 4.dp else 0.dp
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clickable { emoji = option }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option,
                                        style = TextStyle(fontSize = 28.sp)
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "Blocked Apps (${selectedPackages.size})",
                    style = MaterialTheme.typography.labelLarge
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search apps") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Orange,
                            focusedLabelColor = Orange,
                            focusedLeadingIconColor = Orange
                        )
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppCategoryFilter.values().forEach { category ->
                            val isSelected = selectedCategory == category
                            FilterChip(
                                label = category.label,
                                isSelected = isSelected,
                                onClick = { selectedCategory = category }
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            label = if (showOnlySelected) "Selected only" else "All apps",
                            isSelected = showOnlySelected,
                            onClick = { showOnlySelected = !showOnlySelected }
                        )

                        FilterChip(
                            label = if (sortSelectedFirst) "Selected first" else "A–Z only",
                            isSelected = sortSelectedFirst,
                            onClick = { sortSelectedFirst = !sortSelectedFirst }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    when {
                        allApps.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Loading apps...",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )
                            }
                        }

                        filteredApps.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    )
                                    Text(
                                        text = "No apps match your filters",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                            }
                        }

                        else -> {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                itemsIndexed(filteredApps, key = { _, app -> app.packageName }) { index, app ->
                                    val isChecked = app.packageName in selectedPackages
                                    val alpha by animateFloatAsState(
                                        targetValue = 1f,
                                        animationSpec = tween(
                                            durationMillis = 300,
                                            delayMillis = index * 20
                                        ),
                                        label = "appAlpha"
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .alpha(alpha)
                                            .clickable {
                                                selectedPackages = if (isChecked) {
                                                    selectedPackages - app.packageName
                                                } else {
                                                    selectedPackages + app.packageName
                                                }
                                            }
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val iconBitmap = rememberAppIcon(app.icon)
                                        Image(
                                            bitmap = iconBitmap,
                                            contentDescription = app.appName,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .padding(end = 12.dp)
                                        )

                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = app.appName,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal
                                                )
                                            )
                                        }

                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                selectedPackages = if (checked) {
                                                    selectedPackages + app.packageName
                                                } else {
                                                    selectedPackages - app.packageName
                                                }
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = Orange,
                                                checkmarkColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    onSave(
                        preset.copy(
                            name = name,
                            description = description,
                            blockedApps = selectedPackages.toList(),
                            emoji = emoji
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Orange else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "chipBackground"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "chipScale"
    )

    Surface(
        modifier = Modifier.scale(scale),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        tonalElevation = if (isSelected) 4.dp else 0.dp,
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Box(
            modifier = Modifier
                .clickable { onClick() }
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            )
        }
    }
}

private enum class AppCategoryFilter(val label: String) {
    All("All"),
    Games("Games"),
    Social("Social"),
    Productivity("Productivity"),
    Media("Media"),
    Other("Other")
}

private fun InstalledApp.toCategoryFilter(): AppCategoryFilter {
    return when (category) {
        ApplicationInfo.CATEGORY_GAME -> AppCategoryFilter.Games
        ApplicationInfo.CATEGORY_SOCIAL -> AppCategoryFilter.Social
        ApplicationInfo.CATEGORY_PRODUCTIVITY -> AppCategoryFilter.Productivity

        ApplicationInfo.CATEGORY_AUDIO,
        ApplicationInfo.CATEGORY_VIDEO,
        ApplicationInfo.CATEGORY_IMAGE,
        ApplicationInfo.CATEGORY_NEWS -> AppCategoryFilter.Media

        else -> AppCategoryFilter.Other
    }
}
