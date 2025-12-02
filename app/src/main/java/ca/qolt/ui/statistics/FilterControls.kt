package ca.qolt.ui.statistics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import ca.qolt.ui.statistics.StatisticsColors

@Composable
fun FilterControls(
    filters: StatisticsFilters,
    onTimePeriodChange: (TimePeriod) -> Unit,
    onDurationChange: (Duration) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Time Period Selector
        SegmentedControl(
            items = listOf("Weekly", "Monthly"),
            selectedIndex = if (filters.timePeriod == TimePeriod.WEEKLY) 0 else 1,
            onItemSelected = { index ->
                onTimePeriodChange(if (index == 0) TimePeriod.WEEKLY else TimePeriod.MONTHLY)
            }
        )
        
        // Duration Selector
        SegmentedControl(
            items = listOf("1 Day", "7 Days", "30 Days"),
            selectedIndex = when (filters.duration) {
                Duration.ONE_DAY -> 0
                Duration.SEVEN_DAYS -> 1
                Duration.THIRTY_DAYS -> 2
            },
            onItemSelected = { index ->
                onDurationChange(
                    when (index) {
                        0 -> Duration.ONE_DAY
                        1 -> Duration.SEVEN_DAYS
                        else -> Duration.THIRTY_DAYS
                    }
                )
            }
        )
        
        // Today Button
        TodayButton(
            selectedDate = filters.selectedDate,
            onDateSelected = onDateChange
        )
        
        // Calendar
        WeekCalendar(
            selectedDate = filters.selectedDate,
            onDateSelected = onDateChange
        )
    }
}

@Composable
private fun SegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color(0xFF2A2A2A), RoundedCornerShape(22.dp))
            .padding(3.dp)
    ) {
        val density = LocalDensity.current
        val segmentWidth = maxWidth / items.size
        
        val slideProgress by animateFloatAsState(
            targetValue = selectedIndex.toFloat(),
            animationSpec = tween(durationMillis = 300),
            label = "slide"
        )
        
        // Sliding indicator - fills exactly one segment, positioned precisely
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(segmentWidth)
                .offset(x = with(density) { 
                    // Calculate precise offset to fill to edges
                    (slideProgress * segmentWidth.toPx()).toDp() 
                })
                .clip(RoundedCornerShape(19.dp))
                .background(StatisticsColors.Orange)
        )
        
        // Labels - use exact widths to match pill positioning
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items.forEachIndexed { index, item ->
                Box(
                    modifier = Modifier
                        .width(segmentWidth)
                        .fillMaxHeight()
                        .clickable { onItemSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 15.sp,
                            fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (index == selectedIndex) Color.Black else Color(0xFF9E9E9E)
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayButton(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("d MMM")
    
    // Generate more dates around selected date (show 15 days before and after for smooth scrolling)
    val dateRange = (-15..15).map { selectedDate.plusDays(it.toLong()) }
    val selectedIndex = dateRange.indexOf(selectedDate).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex.coerceIn(0, dateRange.size - 1))
    val scope = rememberCoroutineScope()
    
    // Scroll to center when selected date changes
    LaunchedEffect(selectedDate) {
        val index = dateRange.indexOf(selectedDate)
        if (index >= 0) {
            scope.launch {
                listState.animateScrollToItem(index)
            }
        }
    }
    
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val containerWidth = maxWidth
        
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = containerWidth / 2 - 20.dp), // Center padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(dateRange.size) { index ->
                val date = dateRange[index]
                val isSelected = date == selectedDate
                val isToday = date == today
            
                if (isToday && isSelected) {
                    // Show "Today, 5 May" format when today is selected
                    Box(
                    modifier = Modifier
                        .height(44.dp)
                        .background(StatisticsColors.Orange, RoundedCornerShape(22.dp))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .clickable { 
                            onDateSelected(date)
                            scope.launch {
                                // Scroll to center the selected item
                                listState.animateScrollToItem(index)
                            }
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Today, ${date.format(formatter)}",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.Black
                        )
                    }
                } else if (isToday) {
                    // Show "Today, 5 May" format when today is not selected
                    Box(
                    modifier = Modifier
                        .height(44.dp)
                        .background(StatisticsColors.Orange, RoundedCornerShape(22.dp))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .clickable { 
                            onDateSelected(date)
                            scope.launch {
                                // Scroll to center the selected item
                                listState.animateScrollToItem(index)
                            }
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Today, ${date.format(formatter)}",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.Black
                        )
                    }
                } else if (isSelected) {
                    // Show selected date with format like "5th May"
                    Box(
                        modifier = Modifier
                        .height(44.dp)
                        .background(StatisticsColors.Orange, RoundedCornerShape(22.dp))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .clickable { 
                            onDateSelected(date)
                            scope.launch {
                                // Scroll to center the selected item
                                listState.animateScrollToItem(index)
                            }
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${getOrdinal(date.dayOfMonth)} ${date.format(DateTimeFormatter.ofPattern("MMM"))}",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.Black
                        )
                    }
                } else {
                    // Show unselected date as just number
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { 
                                onDateSelected(date)
                                scope.launch {
                                    // Scroll to center the selected item
                                    listState.animateScrollToItem(index)
                                    kotlinx.coroutines.delay(50)
                                    val layoutInfo = listState.layoutInfo
                                    val viewportWidth = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }
            }
        }
    }
}

private fun getOrdinal(day: Int): String {
    return when {
        day in 11..13 -> "${day}th"
        day % 10 == 1 -> "${day}st"
        day % 10 == 2 -> "${day}nd"
        day % 10 == 3 -> "${day}rd"
        else -> "${day}th"
    }
}

@Composable
private fun WeekCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val startOfWeek = selectedDate.minusDays((selectedDate.dayOfWeek.value - 1).toLong())
    val days = (0..6).map { startOfWeek.plusDays(it.toLong()) }
    
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val columnWidth = maxWidth / 7
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Weekday labels row
            Row(modifier = Modifier.fillMaxWidth()) {
                days.forEachIndexed { index, date ->
                    val isSelected = date == selectedDate
                    val dayName = getDayAbbreviation(date.dayOfWeek)
                    
                    if (!isSelected) {
                        Box(
                            modifier = Modifier
                                .width(columnWidth)
                                .height(24.dp)
                                .clickable { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                color = Color(0xFF9E9E9E)
                            )
                        }
                        } else {
                        Spacer(modifier = Modifier.width(columnWidth).height(24.dp))
                    }
                }
            }
            
            // Date numbers row
            Row(modifier = Modifier.fillMaxWidth()) {
                days.forEachIndexed { index, date ->
                    val isSelected = date == selectedDate
                    
                    if (!isSelected) {
                        Box(
                            modifier = Modifier
                                .width(columnWidth)
                                .height(24.dp)
                                .clickable { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                color = Color(0xFF9E9E9E)
                            )
                        }
                        } else {
                        Spacer(modifier = Modifier.width(columnWidth).height(24.dp))
                    }
                }
            }
        }
        
        // Selected date chip (overlay)
        days.forEachIndexed { index, date ->
            if (date == selectedDate) {
                val dayName = getDayAbbreviation(date.dayOfWeek)
                val density = LocalDensity.current
                val offsetX = with(density) { (index * columnWidth.toPx()).toDp() }
                
                Box(
                    modifier = Modifier
                        .offset(x = offsetX)
                        .width(columnWidth)
                        .height(58.dp) // Spans both rows
                        .background(StatisticsColors.Orange, RoundedCornerShape(22.dp))
                        .padding(vertical = 8.dp)
                        .clickable { onDateSelected(date) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 18.sp,
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

private fun getDayAbbreviation(dayOfWeek: DayOfWeek): String {
    return when (dayOfWeek) {
        DayOfWeek.MONDAY -> "Mon"
        DayOfWeek.TUESDAY -> "Tue"
        DayOfWeek.WEDNESDAY -> "Wed"
        DayOfWeek.THURSDAY -> "Thu"
        DayOfWeek.FRIDAY -> "Fri"
        DayOfWeek.SATURDAY -> "Sat"
        DayOfWeek.SUNDAY -> "Sun"
    }
}
