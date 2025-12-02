package ca.qolt.ui.statistics

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

/**
 * Generates fake data for widgets. This is a demo-ready implementation
 * that can be easily replaced with real data sources later.
 */
object FakeDataGenerator {
    
    private val random = Random(42) // Fixed seed for consistent demo data
    
    fun generateFocusTimeWidget(): WidgetType.FocusTime {
        return WidgetType.FocusTime(
            totalHours = 38.4f,
            period = "this week",
            todayHours = 6.5f,
            trendPercentage = 21f
        )
    }
    
    fun generateStreakWidget(): WidgetType.Streak {
        return WidgetType.Streak(
            currentStreak = 12,
            targetStreak = 30
        )
    }
    
    fun generateWeeklyGoalWidget(): WidgetType.WeeklyGoal {
        return WidgetType.WeeklyGoal(
            currentProgress = 5,
            targetProgress = 7,
            percentage = 85
        )
    }
    
    fun generateAppUsageWidget(): WidgetType.AppUsage {
        val apps = listOf(
            // Instagram: purple (0xFF8B5CF6 or similar purple)
            AppUsageItem("Instagram", 2.5f, 0xFF8B5CF6, 35f),
            // Email: lighter, more saturated blue (0xFF42A5F5 or similar)
            AppUsageItem("Email", 1.8f, 0xFF42A5F5, 25f),
            // Messages: brighter green (0xFF66BB6A or similar)
            AppUsageItem("Messages", 1.2f, 0xFF66BB6A, 17f),
            // YouTube: red (0xFFFF0000 - same red for icon and bar)
            AppUsageItem("YouTube", 3.1f, 0xFFFF0000, 43f)
        )
        return WidgetType.AppUsage(apps = apps)
    }
    
    fun generateFocusTimeTodayWidget(): WidgetType.FocusTimeToday {
        // Weekly data matching the screenshot: Mon=4.5, Tue=6, Wed=5.8, Thu=7.2, Fri=6.5, Sat=3, Sun=5.5
        val weeklyData = listOf(4.5f, 6.0f, 5.8f, 7.2f, 6.5f, 3.0f, 5.5f)
        return WidgetType.FocusTimeToday(
            todayHours = 6.5f,
            vsAverage = 1.2f,
            weeklyData = weeklyData,
            dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        )
    }
    
    fun generateTotalHoursWidget(): WidgetType.TotalHours {
        return WidgetType.TotalHours(
            hours = 2228.28f,
            period = "all time"
        )
    }
    
    fun generateMonthlyOverviewWidget(): WidgetType.MonthlyOverview {
        // Weekly data: W1=28, W2=31, W3=34, W4=37 (total = 130, but display shows 133)
        val weeklyData = listOf(28f, 31f, 34f, 37f)
        return WidgetType.MonthlyOverview(
            totalHours = 133f,
            weeklyData = weeklyData,
            weekLabels = listOf("W1", "W2", "W3", "W4")
        )
    }
    
    fun generateStatsTodayWidget(): WidgetType.StatsToday {
        return WidgetType.StatsToday(
            value = 2.28f,
            unit = "hours"
        )
    }
    
    fun generateFocusSessionsWidget(
        currentProgress: Int = 5,
        targetProgress: Int = 7,
        subtitle: String = "Focus Sessions",
        period: String = "this week",
        completedDays: List<Boolean> = listOf(true, true, true, true, true, false, false)
    ): WidgetType.FocusSessions {
        return WidgetType.FocusSessions(
            subtitle = subtitle,
            currentProgress = currentProgress,
            targetProgress = targetProgress,
            completedDays = completedDays,
            period = period
        )
    }
    
    fun getDefaultWidgetTypes(): List<WidgetType> {
        return listOf(
            // Focus Time Today widget
            generateFocusTimeTodayWidget(),
            // Focus Sessions widgets (matching screenshot)
            generateFocusSessionsWidget(
                currentProgress = 5,
                targetProgress = 7,
                subtitle = "Focus Sessions",
                period = "this week",
                completedDays = listOf(true, true, true, true, true, false, false)
            ),
            generateFocusSessionsWidget(
                currentProgress = 4,
                targetProgress = 7,
                subtitle = "Last 7 Days",
                period = "last 7 days",
                completedDays = listOf(true, true, false, true, false, false, true)
            )
        )
    }
    
    fun generateDefaultWidgets(): List<Widget> {
        return getDefaultWidgetTypes().mapIndexed { index, type ->
            Widget(type, index)
        }
    }
    
    /**
     * Generates a widget with data adjusted based on the current filters.
     * This simulates how real data would change based on time period selection.
     */
    fun generateWidgetWithFilters(widgetType: WidgetType, filters: StatisticsFilters): WidgetType {
        return when (widgetType) {
            is WidgetType.FocusTime -> {
                val multiplier = when (filters.duration) {
                    Duration.ONE_DAY -> 0.15f // ~1/7 of weekly
                    Duration.SEVEN_DAYS -> 1f
                    Duration.THIRTY_DAYS -> 4.3f // ~30/7 of weekly
                }
                val period = when (filters.timePeriod) {
                    TimePeriod.WEEKLY -> filters.getDescription()
                    TimePeriod.MONTHLY -> "this month"
                }
                widgetType.copy(
                    totalHours = widgetType.totalHours * multiplier,
                    period = period,
                    todayHours = if (filters.duration == Duration.ONE_DAY) widgetType.totalHours * multiplier else widgetType.todayHours
                )
            }
            is WidgetType.MonthlyOverview -> {
                // Monthly overview doesn't change with filters for now
                widgetType
            }
            is WidgetType.StatsToday -> {
                val multiplier = when (filters.duration) {
                    Duration.ONE_DAY -> 1f
                    Duration.SEVEN_DAYS -> 0.3f
                    Duration.THIRTY_DAYS -> 0.1f
                }
                widgetType.copy(value = widgetType.value * multiplier)
            }
            is WidgetType.FocusSessions -> {
                // Focus sessions don't change with filters for now
                widgetType
            }
            else -> widgetType // Other widgets don't change with filters for now
        }
    }
}

