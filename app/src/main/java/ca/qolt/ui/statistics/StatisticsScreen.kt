package ca.qolt.ui.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.scale

@Composable
fun Statistics(
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel
) {
    val widgets by viewModel.widgets.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()
    val filters by viewModel.filters.collectAsState()
    val showFilterDialog by viewModel.showFilterDialog.collectAsState()
    val showSearchDialog by viewModel.showSearchDialog.collectAsState()
    val showCustomizeDialog by viewModel.showCustomizeDialog.collectAsState()
    StatisticsScreen(
        modifier = modifier,
        widgets,
        isEditMode,
        filters,
        showFilterDialog,
        showSearchDialog,
        showCustomizeDialog,
        viewModel::toggleSearchDialog,
        viewModel::toggleFilterDialog,
        viewModel::toggleCustomizeDialog,
        viewModel::setDuration,
        viewModel::setSearchQuery,
        viewModel::updateFilters,
        viewModel::moveWidgetUp,
        viewModel::moveWidgetDown,
        viewModel::removeWidget,
        viewModel::savePreset,
        viewModel::generateTestData,
        viewModel::getAllAvailableWidgetTypes,
        viewModel::addWidget
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    widgets: List<Widget>,
    isEditMode: Boolean,
    filters: StatisticsFilters,
    showFilterDialog: Boolean,
    showSearchDialog: Boolean,
    showCustomizeDialog: Boolean,
    toggleSearchDialog: () -> Unit,
    toggleFilterDialog: () -> Unit,
    toggleCustomizeDialog: () -> Unit,
    setDuration: (Duration) -> Unit,
    setSearchQuery: (String) -> Unit,
    updateFilters: (StatisticsFilters) -> Unit,
    moveWidgetUp: (Int) -> Unit,
    moveWidgetDown: (Int) -> Unit,
    removeWidget: (Int) -> Unit,
    savePreset: () -> Unit,
    generateTestData: () -> Unit,
    getAllAvailableWidgetTypes: () -> List<WidgetType>,
    addWidget: (WidgetType) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StatisticsColors.DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 0.dp) // Safe area padding will be handled by top padding in header
        ) {
        // Custom Header with proper spacing and safe area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 24.dp) // Increased top padding for safe area + spacing
        ) {
            // Header Row with centered title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Left: White circular search button
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(48.dp) // Increased from 40dp for chunkier feel
                        .background(Color.White, CircleShape)
                        .clickable { toggleSearchDialog() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF282828),
                        modifier = Modifier.size(22.dp) // Slightly larger icon
                    )
                }
                
                // Center: Statistics title
                Text(
                    text = "Statistics",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center)
                )

                // Right: Orange circular filter button
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Test data button (debug only - remove in production)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF4CAF50), CircleShape)
                            .clickable { generateTestData() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Generate Test Data",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp) // Increased from 40dp for chunkier feel
                            .background(StatisticsColors.Orange, CircleShape)
                            .clickable { toggleFilterDialog() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp) // Slightly larger icon
                        )
                    }
                }
            }
        }
        
        // Duration Selector (1 Day, 7 Days, 30 Days) - matching screenshot
        DurationSelectorCompact(
            selectedDuration = filters.duration,
            onDurationSelected = { setDuration(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

            // Animation visibility state
            var isVisible by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                isVisible = true
            }

            // Helper function to check if a widget is narrow (should be placed side by side)
            fun isNarrowWidget(widget: Widget): Boolean {
                return widget.type is WidgetType.Streak || widget.type is WidgetType.WeeklyGoal
            }

            // Display widgets in their actual order (sorted by position)
            val widgetsToDisplay = if (widgets.isEmpty()) {
                emptyList()
            } else {
                widgets.sortedBy { it.position }
            }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                // STREAKS Section with animation
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(
                            animationSpec = tween(400, delayMillis = 50)
                        ) + slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    ) {
                        SectionHeader(
                            title = "STREAKS",
                            onMoreClick = { toggleCustomizeDialog() }
                        )
                    }
                }

                // Render widgets with smart grouping
                if (widgetsToDisplay.isEmpty()) {
                    // Show loading or empty state
                    item {
                        Text(
                            text = "Loading widgets...",
                            color = Color.White,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }  else {
                    // Group widgets: collect consecutive narrow widgets into rows
                    var i = 0
                    var itemIndex = 0 // Track item index for staggered animation
                    while (i < widgetsToDisplay.size) {
                        val currentWidget = widgetsToDisplay[i]

                        if (isNarrowWidget(currentWidget)) {
                            // Collect consecutive narrow widgets
                            val narrowWidgets = mutableListOf<Widget>()
                            while (i < widgetsToDisplay.size && isNarrowWidget(widgetsToDisplay[i])) {
                                narrowWidgets.add(widgetsToDisplay[i])
                                i++
                            }

                            // Render narrow widgets in a row with animation
                            val currentItemIndex = itemIndex
                            item {
                                AnimatedVisibility(
                                    visible = isVisible,
                                    enter = fadeIn(
                                        animationSpec = tween(400, delayMillis = 100 + currentItemIndex * 50)
                                    ) + slideInVertically(
                                        initialOffsetY = { it / 3 },
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        narrowWidgets.forEach { widget ->
                                            Box(modifier = Modifier.weight(1f)) {
                                                WidgetRenderer(widget = widget)
                                            }
                                        }
                                    }
                                }
                            }
                            itemIndex++
                        } else {
                            // Render full-width widget with animation
                            val currentItemIndex = itemIndex
                            item {
                                AnimatedVisibility(
                                    visible = isVisible,
                                    enter = fadeIn(
                                        animationSpec = tween(400, delayMillis = 100 + currentItemIndex * 50)
                                    ) + slideInVertically(
                                        initialOffsetY = { it / 3 },
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                ) {
                                    WidgetRenderer(widget = currentWidget)
                                }
                            }
                            i++
                            itemIndex++
                        }
                    }
                }
            }
        }
        
        // Search Dialog (overlay)
        if (showSearchDialog) {
            SearchDialog(
                searchQuery = filters.searchQuery,
                onSearchQueryChange = { setSearchQuery(it) },
                onDismiss = { toggleSearchDialog() }
            )
        }
        
        // Filter Dialog (overlay)
        if (showFilterDialog) {
            FilterDialog(
                filters = filters,
                onDismiss = { toggleFilterDialog() },
                onFiltersChange = { updateFilters(it) }
            )
        }
        
        // Customize Dialog (More button) (overlay)
        if (showCustomizeDialog) {
            CustomizeDialog(
                widgets = widgets,
                availableWidgetTypes = getAllAvailableWidgetTypes(),
                onDismiss = { toggleCustomizeDialog() },
                onMoveUp = { moveWidgetUp(it) },
                onMoveDown = { moveWidgetDown(it) },
                onRemove = { removeWidget(it) },
                onAdd = { addWidget(it) },
                onSave = { 
                    savePreset()
                    toggleCustomizeDialog()
                }
            )
        }
    }
}

@Composable
private fun DurationSelectorCompact(
    selectedDuration: Duration,
    onDurationSelected: (Duration) -> Unit,
    modifier: Modifier = Modifier
) {
    val outerHeight = 40.dp // ~40dp height
    val outerRadius = 20.dp // Border radius: 20 (or could use 999 for perfect capsule)

    // Very light grey pill-shaped container with inner padding
    Box(
        modifier = modifier
            .height(outerHeight)
            .background(Color(0xFFF4F4F4), RoundedCornerShape(outerRadius)) // Very light grey background
            .padding(horizontal = 11.dp) // 10-12px horizontal padding
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Duration.values().forEachIndexed { index, duration ->
                val isSelected = selectedDuration == duration

                // Animate scale for selected pill
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.98f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "durationScale"
                )

                // Animate background color
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) StatisticsColors.Orange else Color.Transparent,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "durationBackgroundColor"
                )

                // Animate text color
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFF000000) else Color(0xFF8A8A8A),
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "durationTextColor"
                )

                // Orange pill shape - perfect capsule with 999 radius
                val orangePillShape = RoundedCornerShape(999.dp) // Perfect capsule

                // Segment container with margins for orange pill
                Box(
                    modifier = Modifier
                        .weight(1f) // Equal flex distribution
                        .fillMaxHeight()
                        .padding(
                            horizontal = 5.dp, // 4-6px horizontal margin
                            vertical = 4.dp // 4px vertical margin
                        )
                        .scale(scale)
                        .background(backgroundColor, orangePillShape)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        onClick = { onDurationSelected(duration) },
                        shape = orangePillShape,
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp), // 14-18px horizontal padding inside orange pill
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (duration) {
                                    Duration.ONE_DAY -> "1 Day"
                                    Duration.SEVEN_DAYS -> "7 Days"
                                    Duration.THIRTY_DAYS -> "30 Days"
                                },
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 14.5.sp, // 14-15px font size
                                    fontWeight = if (isSelected) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    }
                                ),
                                color = textColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
        TextButton(onClick = onMoreClick) {
            Text(
                text = "More",
                color = StatisticsColors.Orange,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun SearchDialog(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Animation state for dialog entrance
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    Dialog(onDismissRequest = onDismiss) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(300)) +
                    slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + scaleIn(
                        initialScale = 0.9f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ),
            exit = fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.95f)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = StatisticsColors.CardBackground
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Search Statistics",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        label = { Text("Search...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Close", color = StatisticsColors.Orange)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterDialog(
    filters: StatisticsFilters,
    onDismiss: () -> Unit,
    onFiltersChange: (StatisticsFilters) -> Unit
) {
    // Animation state for slide-up entrance
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    Dialog(onDismissRequest = onDismiss) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(300)) +
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ),
            exit = fadeOut(animationSpec = tween(200)) +
                    slideOutVertically(targetOffsetY = { it })
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = StatisticsColors.CardBackground
            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Header
                    Text(
                        text = "Filter options",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 28.dp)
                    )

                    // Filter controls
                    FilterControls(
                        filters = filters,
                        onTimePeriodChange = { onFiltersChange(filters.copy(timePeriod = it)) },
                        onDurationChange = { onFiltersChange(filters.copy(duration = it)) },
                        onDateChange = { onFiltersChange(filters.copy(selectedDate = it)) }
                    )
                }

                // Done button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp, top = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .background(StatisticsColors.Orange, RoundedCornerShape(24.dp))
                            .padding(horizontal = 40.dp, vertical = 12.dp)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Done",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.Black
                        )
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun CustomizeDialog(
    widgets: List<Widget>,
    availableWidgetTypes: List<WidgetType>,
    onDismiss: () -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onAdd: (WidgetType) -> Unit,
    onSave: () -> Unit
) {
    var showAddWidgets by remember { mutableStateOf(false) }
    val currentWidgetIds = widgets.map { it.type.id }.toSet()
    val availableToAdd = availableWidgetTypes.filter { it.id !in currentWidgetIds }

    // Animation state for dialog entrance
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    Dialog(onDismissRequest = onDismiss) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(300)) +
                    slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + scaleIn(
                        initialScale = 0.9f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ),
            exit = fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.95f)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = StatisticsColors.CardBackground
                )
            ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showAddWidgets) "Add Widgets" else "Customize Widgets",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    if (!showAddWidgets && availableToAdd.isNotEmpty()) {
                        TextButton(onClick = { showAddWidgets = true }) {
                            Text("Add", color = StatisticsColors.Orange)
                        }
                    } else if (showAddWidgets) {
                        TextButton(onClick = { showAddWidgets = false }) {
                            Text("Back", color = StatisticsColors.Orange)
                        }
                    }
                }

                if (showAddWidgets) {
                    // Show available widgets to add
                    LazyColumn(
                        modifier = Modifier.height(400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (availableToAdd.isEmpty()) {
                            item {
                                Text(
                                    text = "All widgets are already added",
                                    color = Color(0xFF9E9E9E),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            items(availableToAdd.size) { index ->
                                val widgetType = availableToAdd[index]
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onAdd(widgetType) },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = widgetType.title,
                                        color = Color.White,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        Icons.Filled.Add,
                                        contentDescription = "Add",
                                        tint = StatisticsColors.Orange
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Show current widgets
                    LazyColumn(
                        modifier = Modifier.height(400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(
                            items = widgets,
                            key = { index, widget -> "${widget.type.id}_${widget.position}" }
                        ) { index, widget ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = widget.type.title,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )

                                Row {
                                    IconButton(
                                        onClick = { onMoveUp(index) },
                                        enabled = index > 0
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowUpward,
                                            contentDescription = "Move up",
                                            tint = if (index > 0) StatisticsColors.Orange else Color(0xFF424242)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onMoveDown(index) },
                                        enabled = index < widgets.size - 1
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowDownward,
                                            contentDescription = "Move down",
                                            tint = if (index < widgets.size - 1) StatisticsColors.Orange else Color(0xFF424242)
                                        )
                                    }

                                    IconButton(onClick = { onRemove(index) }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Remove",
                                            tint = Color(0xFFFF5252)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF9E9E9E))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatisticsColors.Orange
                        )
                    ) {
                        Text("Save", color = Color.White)
                    }
                }
            }
            }
        }
    }
}
