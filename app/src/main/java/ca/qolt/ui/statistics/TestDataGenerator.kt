package ca.qolt.ui.statistics

import ca.qolt.data.local.dao.UsageSessionDao
import ca.qolt.data.local.entity.UsageSessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Helper class to generate test data for statistics testing.
 * This allows you to easily add usage sessions to test the statistics screen.
 */
class TestDataGenerator(
    private val usageSessionDao: UsageSessionDao
) {
    
    /**
     * Generates test data for the last 7 days with varying session durations
     */
    suspend fun generateLast7DaysData() = withContext(Dispatchers.IO) {
        val today = LocalDate.now()
        
        // Generate 2-4 sessions per day for the last 7 days
        for (dayOffset in 6 downTo 0) {
            val date = today.minusDays(dayOffset.toLong())
            val numSessions = Random.nextInt(2, 5) // 2 to 4 sessions
            
            for (sessionNum in 0 until numSessions) {
                // Random hour between 8 AM and 10 PM
                val hour = Random.nextInt(8, 23)
                val minute = Random.nextInt(0, 60)
                
                val startTime = date.atTime(hour, minute)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                
                // Random duration between 30 minutes and 3 hours
                val durationMinutes = Random.nextInt(30, 181)
                val durationMs = TimeUnit.MINUTES.toMillis(durationMinutes.toLong())
                val endTime = startTime + durationMs
                
                val session = UsageSessionEntity(
                    startTime = startTime,
                    endTime = endTime,
                    durationMs = durationMs,
                    blockedAppsCount = Random.nextInt(1, 6)
                )
                
                usageSessionDao.insertSession(session)
            }
        }
    }
    
    /**
     * Generates test data for today with specific hours
     */
    suspend fun generateTodayData(hours: Float) = withContext(Dispatchers.IO) {
        val today = LocalDate.now()
        val durationMs = (hours * 3600 * 1000).toLong()
        
        // Create a session starting at 9 AM today
        val startTime = today.atTime(9, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        
        val session = UsageSessionEntity(
            startTime = startTime,
            endTime = startTime + durationMs,
            durationMs = durationMs,
            blockedAppsCount = 3
        )
        
        usageSessionDao.insertSession(session)
    }
    
    /**
     * Generates test data for a specific date range
     */
    suspend fun generateDateRangeData(
        startDate: LocalDate,
        endDate: LocalDate,
        sessionsPerDay: Int = 3,
        minHoursPerSession: Float = 0.5f,
        maxHoursPerSession: Float = 2.0f
    ) = withContext(Dispatchers.IO) {
        var currentDate = startDate
        
        while (!currentDate.isAfter(endDate)) {
            for (i in 0 until sessionsPerDay) {
                val hour = Random.nextInt(8, 21)
                val minute = Random.nextInt(0, 60)
                
                val startTime = currentDate.atTime(hour, minute)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                
                val hours = minHoursPerSession + Random.nextFloat() * (maxHoursPerSession - minHoursPerSession)
                val durationMs = (hours * 3600 * 1000).toLong()
                val endTime = startTime + durationMs
                
                val session = UsageSessionEntity(
                    startTime = startTime,
                    endTime = endTime,
                    durationMs = durationMs,
                    blockedAppsCount = Random.nextInt(1, 6)
                )
                
                usageSessionDao.insertSession(session)
            }
            
            currentDate = currentDate.plusDays(1)
        }
    }
    
    /**
     * Generates a streak of consecutive days with sessions
     */
    suspend fun generateStreakData(days: Int, hoursPerDay: Float = 2.0f) = withContext(Dispatchers.IO) {
        val today = LocalDate.now()
        
        for (dayOffset in (days - 1) downTo 0) {
            val date = today.minusDays(dayOffset.toLong())
            val durationMs = (hoursPerDay * 3600 * 1000).toLong()
            
            // Create a session at 10 AM
            val startTime = date.atTime(10, 0)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            
            val session = UsageSessionEntity(
                startTime = startTime,
                endTime = startTime + durationMs,
                durationMs = durationMs,
                blockedAppsCount = 3
            )
            
            usageSessionDao.insertSession(session)
        }
    }
    
    /**
     * Clears all test data
     */
    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        usageSessionDao.deleteAllSessions()
    }
    
    /**
     * Generates comprehensive test data for all statistics widgets
     */
    suspend fun generateComprehensiveTestData() = withContext(Dispatchers.IO) {
        clearAllData()
        
        // Generate last 30 days with varying data
        val today = LocalDate.now()
        val thirtyDaysAgo = today.minusDays(30)
        generateDateRangeData(
            startDate = thirtyDaysAgo,
            endDate = today,
            sessionsPerDay = 3,
            minHoursPerSession = 0.5f,
            maxHoursPerSession = 3.0f
        )
        
        // Generate a streak of 15 days
        generateStreakData(days = 15, hoursPerDay = 2.5f)
        
        // Add some today data
        generateTodayData(hours = 4.5f)
    }
}

