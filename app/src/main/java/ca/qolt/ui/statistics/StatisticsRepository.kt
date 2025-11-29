package ca.qolt.ui.statistics

import ca.qolt.data.local.dao.BlockedAppDao
import ca.qolt.data.local.dao.UsageSessionDao
import ca.qolt.data.local.entity.UsageSessionEntity
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepository @Inject constructor(
    private val usageSessionDao: UsageSessionDao,
    private val blockedAppDao: BlockedAppDao
) {
    
    /**
     * Gets the start and end timestamps for a given date range based on filters
     */
    private fun getTimeRange(filters: StatisticsFilters): Pair<Long, Long> {
        val startDate = filters.getStartDate()
        val endDate = filters.getEndDate()
        
        val startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()
        
        return Pair(startInstant.toEpochMilli(), endInstant.toEpochMilli())
    }
    
    /**
     * Gets all usage sessions within the filter range
     */
    suspend fun getSessionsInRange(filters: StatisticsFilters): List<UsageSessionEntity> {
        val (startTime, endTime) = getTimeRange(filters)
        return usageSessionDao.getSessionsInRange(startTime, endTime).first()
    }
    
    /**
     * Calculates total focus hours for a given period
     */
    suspend fun getTotalFocusHours(filters: StatisticsFilters): Float {
        val sessions = getSessionsInRange(filters)
        val totalMs = sessions.sumOf { it.durationMs }
        return totalMs / (1000f * 60f * 60f) // Convert ms to hours
    }
    
    /**
     * Gets today's focus hours
     */
    suspend fun getTodayFocusHours(): Float {
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val sessions = usageSessionDao.getSessionsInRange(startOfDay, endOfDay).first()
        val totalMs = sessions.sumOf { it.durationMs }
        return totalMs / (1000f * 60f * 60f)
    }
    
    /**
     * Gets weekly focus hours data (last 7 days)
     */
    suspend fun getWeeklyFocusHours(): List<Float> {
        val today = LocalDate.now()
        val weeklyData = mutableListOf<Float>()
        
        for (i in 6 downTo 0) {
            val date = today.minusDays(i.toLong())
            val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val sessions = usageSessionDao.getSessionsInRange(startOfDay, endOfDay).first()
            val totalMs = sessions.sumOf { it.durationMs }
            weeklyData.add(totalMs / (1000f * 60f * 60f))
        }
        
        return weeklyData
    }
    
    /**
     * Gets monthly overview data (4 weeks)
     */
    suspend fun getMonthlyOverview(): Pair<Float, List<Float>> {
        val today = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1)
        val weeklyData = mutableListOf<Float>()
        var totalHours = 0f
        
        // Calculate 4 weeks
        for (week in 0..3) {
            val weekStart = startOfMonth.plusWeeks(week.toLong())
            val weekEnd = weekStart.plusDays(6)
            val startTime = weekStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endTime = weekEnd.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            val sessions = usageSessionDao.getSessionsInRange(startTime, endTime).first()
            val weekHours = sessions.sumOf { it.durationMs } / (1000f * 60f * 60f)
            weeklyData.add(weekHours)
            totalHours += weekHours
        }
        
        return Pair(totalHours, weeklyData)
    }
    
    /**
     * Gets current streak (consecutive days with focus sessions)
     */
    suspend fun getCurrentStreak(): Int {
        val today = LocalDate.now()
        var streak = 0
        var currentDate = today
        
        while (true) {
            val startOfDay = currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = currentDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val sessions = usageSessionDao.getSessionsInRange(startOfDay, endOfDay).first()
            
            if (sessions.isEmpty() || sessions.sumOf { it.durationMs } == 0L) {
                break
            }
            
            streak++
            currentDate = currentDate.minusDays(1)
        }
        
        return streak
    }
    
    /**
     * Gets weekly goal progress (days with focus sessions this week)
     */
    suspend fun getWeeklyGoalProgress(): Pair<Int, List<Boolean>> {
        val today = LocalDate.now()
        val startOfWeek = today.minusDays((today.dayOfWeek.value - 1).toLong())
        val completedDays = mutableListOf<Boolean>()
        var completedCount = 0
        
        for (i in 0..6) {
            val date = startOfWeek.plusDays(i.toLong())
            val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val sessions = usageSessionDao.getSessionsInRange(startOfDay, endOfDay).first()
            val hasFocus = sessions.isNotEmpty() && sessions.sumOf { it.durationMs } > 0
            completedDays.add(hasFocus)
            if (hasFocus) completedCount++
        }
        
        return Pair(completedCount, completedDays)
    }
    
    /**
     * Gets app usage statistics
     */
    suspend fun getAppUsage(filters: StatisticsFilters): List<AppUsageItem> {
        val apps = blockedAppDao.getAllBlockedApps().first()
        val (startTime, endTime) = getTimeRange(filters)
        
        // For now, we'll use totalBlockedTimeMs from BlockedAppEntity
        // In a real implementation, you might want to track app usage separately
        val totalTime = apps.sumOf { it.totalBlockedTimeMs }
        
        return apps.map { app ->
            val hours = app.totalBlockedTimeMs / (1000f * 60f * 60f)
            val percentage = if (totalTime > 0) {
                (app.totalBlockedTimeMs.toFloat() / totalTime.toFloat()) * 100f
            } else {
                0f
            }
            
            // Assign colors based on app name (you can customize this)
            val color = when {
                app.appName.contains("Instagram", ignoreCase = true) -> 0xFF8B5CF6
                app.appName.contains("Email", ignoreCase = true) || app.appName.contains("Gmail", ignoreCase = true) -> 0xFF42A5F5
                app.appName.contains("Message", ignoreCase = true) || app.appName.contains("SMS", ignoreCase = true) -> 0xFF66BB6A
                app.appName.contains("YouTube", ignoreCase = true) -> 0xFFFF0000
                else -> 0xFF9E9E9E // Default gray
            }
            
            AppUsageItem(app.appName, hours, color, percentage)
        }.sortedByDescending { it.hours }.take(4) // Top 4 apps
    }
    
    /**
     * Gets total focus hours for all time
     */
    suspend fun getTotalHoursAllTime(): Float {
        val sessions = usageSessionDao.getAllSessions().first()
        val totalMs = sessions.sumOf { it.durationMs }
        return totalMs / (1000f * 60f * 60f)
    }
    
    /**
     * Calculates average focus hours for comparison
     */
    suspend fun getAverageFocusHours(filters: StatisticsFilters): Float {
        val sessions = getSessionsInRange(filters)
        if (sessions.isEmpty()) return 0f
        
        val days = when (filters.duration) {
            Duration.ONE_DAY -> 1
            Duration.SEVEN_DAYS -> 7
            Duration.THIRTY_DAYS -> 30
        }
        
        val totalMs = sessions.sumOf { it.durationMs }
        return (totalMs / (1000f * 60f * 60f)) / days
    }
}

