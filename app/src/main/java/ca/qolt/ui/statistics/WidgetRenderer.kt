package ca.qolt.ui.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WidgetRenderer(
    widget: Widget,
    modifier: Modifier = Modifier
) {
    when (val type = widget.type) {
        is WidgetType.FocusTime -> {
            Box(modifier = modifier.padding(8.dp)) {
                FocusTimeWidget(type)
            }
        }
        is WidgetType.Streak -> {
            Card(
                modifier = modifier
                    .width(160.dp)
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
                modifier = modifier
                    .width(160.dp)
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
                modifier = modifier
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
            Box(modifier = modifier.padding(8.dp)) {
                TotalHoursWidget(type)
            }
        }
        is WidgetType.CircularProgress -> {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                CircularProgressWidget(type)
            }
        }
        is WidgetType.BarChart -> {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                BarChartWidget(type)
            }
        }
        is WidgetType.StatsToday -> {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                StatsTodayWidget(type)
            }
        }
        is WidgetType.FocusSessions -> {
            Card(
                modifier = modifier
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
    }
}

@Composable
private fun FocusTimeWidget(type: WidgetType.FocusTime) {
    val formattedHours = String.format("%.1f", type.totalHours)

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
                            Color(0xFFFF8A4A),
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
                        text = formattedHours,
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
                val progress = type.currentStreak.toFloat() / type.targetStreak
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
            text = "${type.currentStreak}",
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
                val percentage = type.percentage / 100f
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
                    val iconText = when (app.name.lowercase()) {
                        "instagram" -> "📱"
                        "email" -> "✉️"
                        "messages" -> "💬"
                        "youtube" -> "▶️"
                        else -> app.name.take(1).uppercase()
                    }
                    Text(
                        text = iconText,
                        color = Color.White,
                        fontSize = 20.sp
                    )
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
        LinearProgressIndicator(
            progress = { app.percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color(app.color),
            trackColor = Color.White.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun TotalHoursWidget(type: WidgetType.TotalHours) {
    val formattedHours = String.format("%.2f", type.hours).replace(",", "")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF4F4F4)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .background(
                    StatisticsColors.Orange,
                    RoundedCornerShape(20.dp)
                )
                .padding(24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Total Hours",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color.White
                )
                Text(
                    text = formattedHours,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun CircularProgressWidget(type: WidgetType.CircularProgress) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { type.percentage / 100f },
                modifier = Modifier.size(120.dp),
                color = StatisticsColors.Orange,
                strokeWidth = 12.dp
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${type.percentage}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${type.current.toInt()}/${type.total.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = type.title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BarChartWidget(type: WidgetType.BarChart) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = type.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = type.period,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            val maxValue = type.dataPoints.maxOrNull() ?: 1f
            type.dataPoints.forEachIndexed { index, value ->
                val height = (value / maxValue) * 100
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height((height * 0.9f).dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(StatisticsColors.Orange)
                    )
                    if (index < type.labels.size) {
                        Text(
                            text = type.labels[index],
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsTodayWidget(type: WidgetType.StatsToday) {
    val isFirst = type.value == 2.28f
    val backgroundColor = if (isFirst) StatisticsColors.Orange else StatisticsColors.CardBackground
    val textColor = Color.White
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "STATS TODAY",
                style = MaterialTheme.typography.labelMedium,
                color = if (isFirst) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.7f)
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${type.value}",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = textColor
                )
                if (isFirst) {
                    Text(
                        text = type.unit,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
            if (isFirst) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(7) { index ->
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height((20 + index * 3).dp)
                                    .background(
                                        Color.White.copy(alpha = 0.7f),
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusSessionsWidget(type: WidgetType.FocusSessions) {
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
                Column {
                    Text(
                        text = type.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = type.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = "${type.currentProgress}/${type.targetProgress}",
                    style = MaterialTheme.typography.titleMedium,
                    color = StatisticsColors.Orange
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
                type.completedDays.forEachIndexed { index, isCompleted ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
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
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall
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
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
