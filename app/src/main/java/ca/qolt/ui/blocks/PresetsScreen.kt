package ca.qolt.ui.blocks

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.Top)
        ) {
            if (presets.isEmpty()) {
                Text(
                    text = "Blocks",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                    )
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFFDFDFD),
                                shape = RoundedCornerShape(100)
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(
                            0.dp,
                            Alignment.CenterVertically
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = "search",
                            contentScale = ContentScale.None,
                        )
                    }
                    Text(
                        text = "Blocks",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    )
                    Column(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFFF4400),
                                shape = RoundedCornerShape(100)
                            )
                            .padding(16.dp)
                            .clickable {
                                isCreatingNew = true
                                editingPreset = PresetEntity(
                                    id = UUID.randomUUID().toString(),
                                    name = "",
                                    description = "",
                                    blockedApps = emptyList()
                                )
                            },
                        verticalArrangement = Arrangement.spacedBy(
                            0.dp,
                            Alignment.CenterVertically
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = "add",
                            contentScale = ContentScale.None,
                        )
                    }
                }
            }

            if (presets.isEmpty()){
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Text(
                            text = "You don't have any blocks yet",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .background(
                                    color = Color(0xFFFF4400),
                                    shape = RoundedCornerShape(48.dp)
                                )
                                .clickable {
                                    isCreatingNew = true
                                    editingPreset = PresetEntity(
                                        id = UUID.randomUUID().toString(),
                                        name = "",
                                        description = "",
                                        blockedApps = emptyList()
                                    )
                                }
                                .padding(horizontal = 8.dp, vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Create Your First Block",
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top)
                ) {
                    if(currentPreset != null) {
                        item{
                            Text(
                                text = "CURRENT",
                                style = TextStyle(
                                    color = Color(0xFFFDFDFD),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Light
                                )
                            )
                        }
                        item {
                            PresetRow(
                                preset = currentPreset,
                                isCurrent = true,
                                menuExpandedForId = menuExpandedForId,
                                onMenuExpandedChange = {menuExpandedForId = it},
                                onSetCurrent = {setCurrentPresetId(currentPreset.id)},
                                onEdit = {
                                    isCreatingNew = false
                                    editingPreset = currentPreset
                                },
                                onDelete = {onDeletePreset(currentPreset)}
                            )
                        }

                        if(recentPresets.isNotEmpty()) {
                            item {
                                Text(
                                    text = "RECENT",
                                    style = TextStyle(
                                        color = Color(0xFFFDFDFD),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Light
                                    )
                                )
                            }
                        }
                    } else {
                        item {
                            Text(
                                text = "RECENT",
                                style = TextStyle(
                                    color = Color(0xFFFDFDFD),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Light
                                )
                            )
                        }
                    }
                    items(
                        items = recentPresets,
                        key = { it.id }
                    ) { preset ->
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
                            onDelete = { onDeletePreset(preset) }
                        )
                    }
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
}

@Composable
private fun PresetRow(
    preset: PresetEntity,
    isCurrent: Boolean,
    menuExpandedForId: String?,
    onMenuExpandedChange: (String?) -> Unit,
    onSetCurrent: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFFF4400),
                shape = RoundedCornerShape(48.dp)
            )
            .clickable {
                onMenuExpandedChange(preset.id)
            }
            .padding(vertical = 16.dp, horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = preset.emoji,
            style = TextStyle(
                fontSize = 22.sp
            )
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = preset.name.ifBlank { "Untitled preset" },
                style = TextStyle(
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            if (preset.description.isNotBlank()) {
                Text(
                    text = preset.description,
                    style = TextStyle(
                        color = Color(0xFFFFEDE3),
                        fontSize = 13.sp
                    )
                )
            }
        }

        Box {
            IconButton(
                onClick = {
                    onMenuExpandedChange(preset.id)
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreHoriz,
                    contentDescription = "Preset options",
                    tint = Color.White
                )
            }

            DropdownMenu(
                expanded = menuExpandedForId == preset.id,
                onDismissRequest = { onMenuExpandedChange(null) }
            ) {
                if(!isCurrent) {
                    DropdownMenuItem(
                        text = { Text("Set as current") },
                        onClick = {
                            onMenuExpandedChange(null)
                            onSetCurrent()
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        onMenuExpandedChange(null)
                        onEdit()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        onMenuExpandedChange(null)
                        onDelete()
                    }
                )
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

    val filteredApps = remember(allApps, searchQuery, showOnlySelected, selectedCategory, sortSelectedFirst, selectedPackages) {
        allApps
            .asSequence()
            .filter { app ->
                val matchesSearch = app.appName.contains(searchQuery, ignoreCase = true)
                val matchesSelected = !showOnlySelected || app.packageName in selectedPackages
                val appCategory = app.toCategoryFilter()
                val matchesCategory = selectedCategory == AppCategoryFilter.All || appCategory == selectedCategory
                matchesSearch && matchesSelected && matchesCategory
            }
            .sortedWith(
                compareBy<InstalledApp>(
                    { if (sortSelectedFirst) !(it.packageName in selectedPackages) else false }, // selected first if enabled
                    { it.appName.lowercase() } // then A–Z by name
                )
            )
            .toList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (preset.name.isBlank()) "New preset" else "Edit preset",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Preset name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Emoji",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        emojiOptions.forEach { option ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (emoji == option) Color(0x33FFFFFF) else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { emoji = option }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = option,
                                    style = TextStyle(fontSize = 24.sp)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Blocked apps",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Search
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search apps") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AppCategoryFilter.values().forEach { category ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (selectedCategory == category) Color(0xFFFF4400) else Color(0x33FFFFFF),
                                            shape = RoundedCornerShape(50)
                                        )
                                        .clickable { selectedCategory = category }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = category.label,
                                        style = TextStyle(fontSize = 12.sp, color = Color.White)
                                    )
                                }
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (showOnlySelected) Color(0xFFFF4400) else Color(0x33FFFFFF),
                                        shape = RoundedCornerShape(50)
                                    )
                                    .clickable { showOnlySelected = !showOnlySelected }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (showOnlySelected) "Selected only" else "All apps",
                                    style = TextStyle(fontSize = 12.sp, color = Color.White)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (sortSelectedFirst) Color(0xFFFF4400) else Color(0x33FFFFFF),
                                        shape = RoundedCornerShape(50)
                                    )
                                    .clickable { sortSelectedFirst = !sortSelectedFirst }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (sortSelectedFirst) "Selected first" else "A–Z only",
                                    style = TextStyle(fontSize = 12.sp, color = Color.White)
                                )
                            }
                        }
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
                                    style = TextStyle(fontSize = 13.sp, color = Color.Gray)
                                )
                            }
                        }

                        filteredApps.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No apps match your filters",
                                    style = TextStyle(fontSize = 13.sp, color = Color.Gray)
                                )
                            }
                        }

                        else -> {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(filteredApps) { app ->
                                    val isChecked = app.packageName in selectedPackages
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val iconBitmap = rememberAppIcon(app.icon)
                                        Image(
                                            bitmap = iconBitmap,
                                            contentDescription = app.appName,
                                            modifier = Modifier
                                                .height(32.dp)
                                                .padding(end = 8.dp)
                                        )

                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = app.appName,
                                                style = TextStyle(fontSize = 14.sp)
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
                                            }
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
            TextButton(
                onClick = {
                    onSave(
                        preset.copy(
                            name = name,
                            description = description,
                            blockedApps = selectedPackages.toList(),
                            emoji = emoji
                        )
                    )
                }
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

