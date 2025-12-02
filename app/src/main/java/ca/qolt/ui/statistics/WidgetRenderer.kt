package ca.qolt.ui.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Animated counter that counts from 0 to target value
 */
@Composable
fun AnimatedCounter(
    targetValue: Float,
    formatter: (Float) -> String = { String.format("%.1f", it) },
    animationSpec: AnimationSpec<Float> = tween(
        durationMillis = 1500,
        easing = FastOutSlowInEasing
    )
): String {
    var animatedValue by remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(targetValue) {
        animatedValue = 0f
        animatedValue = targetValue
    }
    
    val displayValue by animateFloatAsState(
        targetValue = animatedValue,
        animationSpec = animationSpec,
        label = "counter"
    )
    
    return formatter(displayValue)
}

/**
 * Animated counter for integers
 */
@Composable
fun AnimatedIntCounter(
    targetValue: Int,
    animationSpec: AnimationSpec<Float> = tween(
        durationMillis = 1500,
        easing = FastOutSlowInEasing
    )
): String {
    var animatedValue by remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(targetValue) {
        animatedValue = 0f
        animatedValue = targetValue.toFloat()
    }
    
    val displayValue by animateFloatAsState(
        targetValue = animatedValue,
        animationSpec = animationSpec,
        label = "int_counter"
    )
    
    return displayValue.toInt().toString()
}

@Composable
fun WidgetRenderer(
    widget: Widget,
    modifier: Modifier = Modifier
) {
    // Fade-in animation for widget with staggered delay based on position
    val delay = widget.position * 100L
    
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 600,
            delayMillis = delay.toInt(),
            easing = FastOutSlowInEasing
        ),
        label = "widget_alpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "widget_scale"
    )
    
    Box(
        modifier = modifier
            .alpha(alpha)
            .scale(scale)
    ) {
        when (val type = widget.type) {
        is WidgetType.FocusTime -> {
            Box(modifier = Modifier.padding(8.dp)) {
                FocusTimeWidget(type)
            }
        }
        is WidgetType.Streak -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = StatisticsColors.CardBackground
                )
            ) {
                StreakWidget(type)
            }
        }
        is WidgetType.WeeklyGoal -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = StatisticsColors.CardBackground
                )
            ) {
                WeeklyGoalWidget(type)
            }
        }
        is WidgetType.AppUsage -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = StatisticsColors.CardBackground
                )
            ) {
                AppUsageWidget(type)
            }
        }
        is WidgetType.TotalHours -> {
            Box(modifier = Modifier.padding(8.dp)) {
                TotalHoursWidget(type)
            }
        }
        is WidgetType.MonthlyOverview -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = StatisticsColors.CardBackground
                )
            ) {
                MonthlyOverviewWidget(type)
            }
        }
        is WidgetType.StatsToday -> {
            Box(modifier = Modifier.padding(8.dp)) {
                StatsTodayWidget(type)
            }
        }
        is WidgetType.FocusSessions -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                FocusSessionsWidget(type)
            }
        }
        is WidgetType.FocusTimeToday -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = StatisticsColors.CardBackground
                )
            ) {
                FocusTimeTodayWidget(type)
            }
        }
        }
    }
}

@Composable
private fun FocusTimeWidget(type: WidgetType.FocusTime) {
    val animatedHours = AnimatedCounter(
        targetValue = type.totalHours,
        formatter = { String.format("%.1f", it) }
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0xFFFF25A03),
                            StatisticsColors.Orange
                        )
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Total Focus Time",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color.White
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = animatedHours,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "hrs",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 16.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Text(
                    text = type.period,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color.White,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun StreakWidget(type: WidgetType.Streak) {
    // Animate progress
    val animatedProgress by animateFloatAsState(
        targetValue = type.currentStreak.toFloat() / type.targetStreak,
        animationSpec = tween(
            durationMillis = 1200,
            easing = FastOutSlowInEasing
        ),
        label = "streak_progress"
    )
    
    val animatedCount = AnimatedIntCounter(targetValue = type.currentStreak)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(80.dp),
                color = Color(0xFF2A2A2A),
                strokeWidth = 8.dp
            )
            androidx.compose.foundation.Canvas(
                modifier = Modifier.size(80.dp)
            ) {
                val strokeWidth = 8.dp.toPx()
                val radius = size.minDimension / 2 - strokeWidth / 2
                val center = Offset(size.width / 2, size.height / 2)
                val segmentCount = 4
                val segmentAngle = 60f
                val gapAngle = 30f
                val totalAngle = segmentAngle + gapAngle
                val progress = animatedProgress
                val filledSegments = (progress * segmentCount).toInt()
                val partialSegmentProgress = (progress * segmentCount) - filledSegments
                for (i in 0 until segmentCount) {
                    val startAngle = -90f + (i * totalAngle)
                    if (i < filledSegments) {
                        drawArc(
                            color = StatisticsColors.Orange,
                            startAngle = startAngle,
                            sweepAngle = segmentAngle,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round
                            )
                        )
                    } else if (i == filledSegments && partialSegmentProgress > 0f) {
                        drawArc(
                            color = StatisticsColors.Orange,
                            startAngle = startAngle,
                            sweepAngle = segmentAngle * partialSegmentProgress,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round
                            )
                        )
                    }
                }
            }
            Text(
                text = "🔥",
                fontSize = 32.sp,
                color = StatisticsColors.Orange
            )
        }
        Text(
            text = animatedCount,
            style = MaterialTheme.typography.displayMedium.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.White
        )
        Text(
            text = "Day Streak",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            ),
            color = Color(0xFFB0B0B0)
        )
    }
}

@Composable
private fun WeeklyGoalWidget(type: WidgetType.WeeklyGoal) {
    // Animate progress
    val animatedProgress by animateFloatAsState(
        targetValue = type.percentage / 100f,
        animationSpec = tween(
            durationMillis = 1200,
            easing = FastOutSlowInEasing
        ),
        label = "weekly_goal_progress"
    )
    
    val animatedCount = AnimatedIntCounter(targetValue = type.currentProgress)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(80.dp),
                color = Color(0xFF2A2A2A),
                strokeWidth = 8.dp
            )
            androidx.compose.foundation.Canvas(
                modifier = Modifier.size(80.dp)
            ) {
                val strokeWidth = 8.dp.toPx()
                val radius = size.minDimension / 2 - strokeWidth / 2
                val center = Offset(size.width / 2, size.height / 2)
                val segmentCount = 4
                val segmentAngle = 60f
                val gapAngle = 30f
                val totalAngle = segmentAngle + gapAngle
                val percentage = animatedProgress
                val filledSegments = (percentage * segmentCount).toInt()
                val partialSegmentProgress = (percentage * segmentCount) - filledSegments
                for (i in 0 until segmentCount) {
                    val startAngle = -90f + (i * totalAngle)
                    if (i < filledSegments) {
                        drawArc(
                            color = Color(0xFF2196F3),
                            startAngle = startAngle,
                            sweepAngle = segmentAngle,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round
                            )
                        )
                    } else if (i == filledSegments && partialSegmentProgress > 0f) {
                        drawArc(
                            color = Color(0xFF2196F3),
                            startAngle = startAngle,
                            sweepAngle = segmentAngle * partialSegmentProgress,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round
                            )
                        )
                    }
                }
            }
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(Color(0xFF2196F3).copy(alpha = 0.3f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(Color(0xFF2196F3).copy(alpha = 0.5f), CircleShape)
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "${type.percentage}",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
            Text(
                text = "%",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = Color.White,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Text(
            text = "Weekly Goal",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            ),
            color = Color(0xFFB0B0B0)
        )
    }
}

@Composable
private fun AppUsageWidget(type: WidgetType.AppUsage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        type.apps.forEach { app ->
            AppUsageItemRow(app)
        }
    }
}

@Composable
private fun AppUsageItemRow(app: AppUsageItem) {
    // Animate progress bar
    val animatedProgress by animateFloatAsState(
        targetValue = app.percentage / 100f,
        animationSpec = tween(
            durationMillis = 1000,
            delayMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "app_usage_progress"
    )
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(app.color)),
                    contentAlignment = Alignment.Center
                ) {
                    when (app.name.lowercase()) {
                        "instagram" -> {
                            Icon(
                                imageVector = Icons.Default.Smartphone,
                                contentDescription = "Instagram",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        "email" -> {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        "messages" -> {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Message,
                                contentDescription = "Messages",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        "youtube" -> {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "YouTube",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        else -> {
                            Text(
                                text = app.name.take(1).uppercase(),
                                color = Color.White,
                                fontSize = 20.sp
                            )
                        }
                    }
                }
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
            if (app.name.lowercase() == "instagram") {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = String.format("%.1f", app.hours),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "h",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            } else {
                Text(
                    text = String.format("%.1fh", app.hours),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        // Custom progress bar: dark track with colored fill from left
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF2A2A2A)) // Dark track
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(app.color)) // Colored fill from left
            )
        }
    }
}

@Composable
private fun TotalHoursWidget(type: WidgetType.TotalHours) {
    // Animated counter with thousands separator formatting
    val animatedHours = AnimatedCounter(
        targetValue = type.hours,
        formatter = { java.text.DecimalFormat("#,##0.00").format(it) }
    )
    
    // Orange color
    val orangeColor = Color(0xFFF25A03)
    
    // Single orange pill-shaped rectangle directly on dark background
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(
                orangeColor,
                RoundedCornerShape(24.dp) // Wide pill shape (not circular)
            )
            .padding(vertical = 24.dp, horizontal = 32.dp), // Less vertical padding for wider shape
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Total Hours",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal // Regular weight
                ),
                color = Color.Black
            )
            Text(
                text = animatedHours,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold // Bold but not ultra-bold
                ),
                color = Color.Black
            )
        }
    }
}


@Composable
private fun MonthlyOverviewWidget(type: WidgetType.MonthlyOverview) {
    val orangeColor = Color(0xFFF25A03)
    val lightOrange = Color(0xFFF25A03).copy(alpha = 0.8f)
    val gridColor = Color.White.copy(alpha = 0.2f)
    val maxValue = 40f // Y-axis max value
    
    // Animate total hours counter
    val animatedTotalHours = AnimatedIntCounter(targetValue = type.totalHours.toInt())
    
    // Animate each bar with staggered delay
    val animatedBar1 by animateFloatAsState(
        targetValue = type.weeklyData.getOrNull(0) ?: 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 0, easing = FastOutSlowInEasing),
        label = "bar_1"
    )
    val animatedBar2 by animateFloatAsState(
        targetValue = type.weeklyData.getOrNull(1) ?: 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 150, easing = FastOutSlowInEasing),
        label = "bar_2"
    )
    val animatedBar3 by animateFloatAsState(
        targetValue = type.weeklyData.getOrNull(2) ?: 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 300, easing = FastOutSlowInEasing),
        label = "bar_3"
    )
    val animatedBar4 by animateFloatAsState(
        targetValue = type.weeklyData.getOrNull(3) ?: 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 450, easing = FastOutSlowInEasing),
        label = "bar_4"
    )
    val animatedBars = listOf(animatedBar1, animatedBar2, animatedBar3, animatedBar4)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Top section: Total This Month
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Start
        ) {
            Column {
                Text(
                    text = "Total This Month",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color(0xFFB0B0B0) // Light grey
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = animatedTotalHours,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "hours",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = Color(0xFFB0B0B0), // Light grey
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
        
        // Bar chart section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            // Draw grid lines and bars
            Canvas(modifier = Modifier.fillMaxSize()) {
                val padding = 40.dp.toPx()
                val graphWidth = size.width - padding * 2
                val graphHeight = size.height - padding * 2
                
                // Draw dashed grid lines
                val gridLines = listOf(0f, 10f, 20f, 30f, 40f)
                gridLines.forEach { value ->
                    val y = padding + graphHeight - (value / maxValue) * graphHeight
                    drawLine(
                        color = gridColor,
                        start = Offset(padding, y),
                        end = Offset(size.width - padding, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                    )
                }
                
                // Draw bars with gradient
                if (animatedBars.isNotEmpty()) {
                    val barWidth = (graphWidth / animatedBars.size) * 0.6f
                    val barSpacing = (graphWidth / animatedBars.size) * 0.4f
                    
                    animatedBars.forEachIndexed { index, value ->
                        val barHeight = (value / maxValue) * graphHeight
                        val x = padding + index * (barWidth + barSpacing) + barSpacing / 2
                        val y = padding + graphHeight - barHeight
                        
                        // Draw bar with gradient (lighter at top, darker at bottom)
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(lightOrange, orangeColor),
                                startY = y,
                                endY = y + barHeight
                            ),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight)
                        )
                    }
                }
            }
            
            // Y-axis labels (hours: 0, 10, 20, 30, 40)
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 8.dp, top = 40.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(40f, 30f, 20f, 10f, 0f).forEach { value ->
                    Text(
                        text = value.toInt().toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp
                        ),
                        color = Color.White
                    )
                }
            }
            
            // X-axis labels (weeks: W1, W2, W3, W4)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(start = 40.dp, end = 40.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                type.weekLabels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsTodayWidget(type: WidgetType.StatsToday) {
    val orangeColor = Color(0xFFF25A03)
    
    // Animate counter
    val animatedValue = AnimatedCounter(
        targetValue = type.value,
        formatter = { String.format("%.2f", it) }
    )
    
    // Generate wavy line data based on the actual value
    // The graph should change shape based on the hours value
    val numPoints = 14
    val maxHoursForGraph = 12f // Maximum hours for graph scaling
    
    // Track animation state to restart when value changes
    var animationKey by remember { mutableStateOf(0) }
    
    // Reset animations when value changes
    LaunchedEffect(type.value) {
        animationKey++
    }
    
    // Create a simple, natural-looking wavy line graph
    // Recalculate when value changes
    val baseGraphData = remember(type.value, animationKey) {
        // Normalize value to 0-1 range for graph height
        val normalizedValue = (type.value / maxHoursForGraph).coerceIn(0.15f, 1f)
        
        (0 until numPoints).map { index ->
            val progress = index.toFloat() / (numPoints - 1)
            
            // Simple, clean wavy pattern: smooth upward trend with gentle waves
            // Base level: starts around 30%, ends around 70%
            val baseLevel = 0.3f + (progress * 0.4f)
            
            // Add a gentle sine wave (2 cycles) for natural wavy look
            val wave = kotlin.math.sin((progress * kotlin.math.PI * 2.0).toDouble()).toFloat() * 0.08f
            
            // Combine and scale by value - higher hours = higher graph
            val combined = (baseLevel + wave) * normalizedValue
            combined.coerceIn(0.2f, 0.8f)
        }
    }
    
    // Animate each point with staggered delay - restart when animationKey changes
    val graphData = baseGraphData.mapIndexed { index, value ->
        var animatedValue by remember(animationKey) { mutableFloatStateOf(0f) }
        
        LaunchedEffect(animationKey, value) {
            animatedValue = 0f
            kotlinx.coroutines.delay(index * 60L)
            animatedValue = value
        }
        
        animateFloatAsState(
            targetValue = animatedValue,
            animationSpec = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            label = "wave_point_$index"
        ).value
    }
    
    // Animate path drawing progress - restart when value changes
    var pathProgressValue by remember(animationKey) { mutableFloatStateOf(0f) }
    
    LaunchedEffect(animationKey) {
        pathProgressValue = 0f
        kotlinx.coroutines.delay(200)
        pathProgressValue = 1f
    }
    
    val pathProgress by animateFloatAsState(
        targetValue = pathProgressValue,
        animationSpec = tween(
            durationMillis = 1500,
            easing = FastOutSlowInEasing
        ),
        label = "path_progress"
    )
    
    // Animate circle appearance - restart when value changes
    var circleAlphaValue by remember(animationKey) { mutableFloatStateOf(0f) }
    
    LaunchedEffect(animationKey) {
        kotlinx.coroutines.delay(1400)
        circleAlphaValue = 1f
    }
    
    val circleAlpha by animateFloatAsState(
        targetValue = circleAlphaValue,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "circle_alpha"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Square shape
            .background(orangeColor, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section: STATS TODAY and value
            Column {
                Text(
                    text = "STATS TODAY",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = animatedValue,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    Text(
                        text = type.unit,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
            
            // Bottom section: Wavy line graph
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val padding = 8.dp.toPx()
                    val graphWidth = size.width - padding * 2
                    val graphHeight = size.height - padding * 2
                    
                    // Create wavy line path
                    val fullPath = Path().apply {
                        val pointSpacing = graphWidth / (graphData.size - 1)
                        moveTo(padding, padding + graphHeight * (1 - graphData[0]))
                        
                        graphData.forEachIndexed { index, value ->
                            if (index > 0) {
                                val x = padding + index * pointSpacing
                                val y = padding + graphHeight * (1 - value)
                                lineTo(x, y)
                            }
                        }
                    }
                    
                    // Animate path drawing by creating a partial path
                    val animatedPath = Path().apply {
                        val pointSpacing = graphWidth / (graphData.size - 1)
                        val numPointsToDraw = (graphData.size * pathProgress).toInt().coerceIn(1, graphData.size)
                        
                        moveTo(padding, padding + graphHeight * (1 - graphData[0]))
                        
                        for (index in 1 until numPointsToDraw) {
                            val x = padding + index * pointSpacing
                            val y = padding + graphHeight * (1 - graphData[index])
                            lineTo(x, y)
                        }
                        
                        // If we're animating the last point, interpolate its position
                        if (numPointsToDraw < graphData.size) {
                            val prevIndex = numPointsToDraw - 1
                            val nextIndex = numPointsToDraw
                            val t = (pathProgress * graphData.size) - prevIndex
                            
                            val prevX = padding + prevIndex * pointSpacing
                            val nextX = padding + nextIndex * pointSpacing
                            val prevY = padding + graphHeight * (1 - graphData[prevIndex])
                            val nextY = padding + graphHeight * (1 - graphData[nextIndex])
                            
                            val currentX = prevX + (nextX - prevX) * t
                            val currentY = prevY + (nextY - prevY) * t
                            lineTo(currentX, currentY)
                        }
                    }
                    
                    // Draw white wavy line
                    drawPath(
                        path = animatedPath,
                        color = Color.White,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                    
                    // Draw hollow circle at the end (only when path is complete)
                    if (pathProgress >= 1f) {
                        val lastX = padding + (graphData.size - 1) * (graphWidth / (graphData.size - 1))
                        val lastY = padding + graphHeight * (1 - graphData.last())
                        drawCircle(
                            color = Color.White.copy(alpha = circleAlpha),
                            radius = 4.dp.toPx(),
                            center = Offset(lastX, lastY),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusSessionsWidget(type: WidgetType.FocusSessions) {
    // Animate progress counter
    val animatedProgress = AnimatedIntCounter(
        targetValue = type.currentProgress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = StatisticsColors.CardBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = type.subtitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color.White
                )
                Text(
                    text = "$animatedProgress/${type.targetProgress}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = StatisticsColors.Orange
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
                // Ensure we have 7 days, pad with false if needed
                val completedDays = if (type.completedDays.size < 7) {
                    type.completedDays + List(7 - type.completedDays.size) { false }
                } else {
                    type.completedDays.take(7)
                }
                completedDays.forEachIndexed { index, isCompleted ->
                    // Animate each day circle with staggered delay
                    val delay = index * 80L
                    val alpha by animateFloatAsState(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 400,
                            delayMillis = delay.toInt(),
                            easing = FastOutSlowInEasing
                        ),
                        label = "day_alpha_$index"
                    )
                    val scale by animateFloatAsState(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "day_scale_$index"
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .alpha(alpha)
                            .scale(scale)
                    ) {
                        Box(
                            modifier = Modifier.size(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                // Animate checkmark appearance
                                val checkmarkAlpha by animateFloatAsState(
                                    targetValue = 1f,
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        delayMillis = delay.toInt() + 200,
                                        easing = FastOutSlowInEasing
                                    ),
                                    label = "checkmark_alpha_$index"
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            StatisticsColors.Orange,
                                            RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "✓",
                                        color = Color.White.copy(alpha = checkmarkAlpha),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 16.sp
                                        )
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Color.Transparent,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .border(
                                            2.dp,
                                            StatisticsColors.Orange,
                                            RoundedCornerShape(16.dp)
                                        )
                                )
                            }
                        }
                        Text(
                            text = dayLabels[index],
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp
                            ),
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusTimeTodayWidget(type: WidgetType.FocusTimeToday) {
    val orangeColor = Color(0xFFF25A03)
    val lightOrange = Color(0xFFF25A03).copy(alpha = 0.3f)
    val gridColor = Color.White.copy(alpha = 0.2f)
    
    // Animate counters
    val animatedTodayHours = AnimatedCounter(
        targetValue = type.todayHours,
        formatter = { String.format("%.1f", it) }
    )
    val animatedVsAverage = AnimatedCounter(
        targetValue = type.vsAverage,
        formatter = { 
            val formatted = String.format("%.1f", it)
            if (it >= 0) "+$formatted" else formatted
        }
    )
    
    // Animate line graph points
    val animatedData = type.weeklyData.mapIndexed { index, value ->
        animateFloatAsState(
            targetValue = value,
            animationSpec = tween(
                durationMillis = 1000,
                delayMillis = index * 100,
                easing = FastOutSlowInEasing
            ),
            label = "line_point_$index"
        ).value
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top section: Today hours on left, vs average on right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Left side: Today hours
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = animatedTodayHours,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                Text(
                    text = "hours focused",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color(0xFFB0B0B0) // Light grey
                )
            }
            
            // Right side: vs average - aligned with "hours focused" text
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(top = 28.dp) // Align with "hours focused" text
            ) {
                Text(
                    text = "${animatedVsAverage}h",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = orangeColor
                )
                Text(
                    text = "vs. average",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color(0xFFB0B0B0) // Light grey
                )
            }
        }
        
        // Line graph section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            // Draw grid lines and graph
            Canvas(modifier = Modifier.fillMaxSize()) {
                val padding = 40.dp.toPx()
                val graphWidth = size.width - padding * 2
                val graphHeight = size.height - padding * 2
                val maxValue = 8f // Fixed max for Y-axis labels
                
                // Draw dashed grid lines
                val gridLines = listOf(0f, 2f, 4f, 6f, 8f)
                gridLines.forEach { value ->
                    val y = padding + graphHeight - (value / maxValue) * graphHeight
                    drawLine(
                        color = gridColor,
                        start = Offset(padding, y),
                        end = Offset(size.width - padding, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                    )
                }
                
                // Draw orange line and filled area
                if (animatedData.isNotEmpty()) {
                    val maxAnimatedValue = animatedData.maxOrNull() ?: 8f
                    val pointSpacing = graphWidth / (animatedData.size - 1)
                    val points = animatedData.mapIndexed { index, value ->
                        Offset(
                            padding + index * pointSpacing,
                            padding + graphHeight - (value / maxAnimatedValue) * graphHeight
                        )
                    }
                    
                    // Draw filled area under the line
                    val fillPath = Path().apply {
                        moveTo(points.first().x, padding + graphHeight)
                        points.forEach { point ->
                            lineTo(point.x, point.y)
                        }
                        lineTo(points.last().x, padding + graphHeight)
                        close()
                    }
                    // Draw filled area with darker orange (not transparent)
                    drawPath(
                        path = fillPath,
                        color = lightOrange
                    )
                    
                    // Draw orange line
                    val linePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        points.drop(1).forEach { point ->
                            lineTo(point.x, point.y)
                        }
                    }
                    drawPath(
                        path = linePath,
                        color = orangeColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
            
            // Y-axis labels (hours: 0, 2, 4, 6, 8)
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 8.dp, top = 40.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(8f, 6f, 4f, 2f, 0f).forEach { value ->
                    Text(
                        text = value.toInt().toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp
                        ),
                        color = Color.White
                    )
                }
            }
            
            // X-axis labels (days of week)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(start = 40.dp, end = 40.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                type.dayLabels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}
