package ca.qolt.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val presetManager: WidgetPresetManager,
    private val statisticsRepository: StatisticsRepository,
    private val testDataGenerator: TestDataGenerator
) : ViewModel() {

    companion object {
        const val TAG = "StatisticsViewModel"
    }
    
    private val _filters = MutableStateFlow(StatisticsFilters())
    val filters: StateFlow<StatisticsFilters> = _filters.asStateFlow()
    
    private val _widgets = MutableStateFlow<List<Widget>>(emptyList())
    val widgets: StateFlow<List<Widget>> = _widgets.asStateFlow()
    
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()
    
    private val _showFilterDialog = MutableStateFlow(false)
    val showFilterDialog: StateFlow<Boolean> = _showFilterDialog.asStateFlow()
    
    private val _showSearchDialog = MutableStateFlow(false)
    val showSearchDialog: StateFlow<Boolean> = _showSearchDialog.asStateFlow()
    
    private val _showCustomizeDialog = MutableStateFlow(false)
    val showCustomizeDialog: StateFlow<Boolean> = _showCustomizeDialog.asStateFlow()
    
    init {
        loadWidgets()
    }
    
    private fun loadWidgets() {
        viewModelScope.launch {
            val savedPreset = presetManager.loadPreset()
            val widgetTypes = savedPreset?.map { it.type } ?: getDefaultWidgetTypes()
            updateWidgetsWithFilters(widgetTypes)
        }
    }
    
    private suspend fun getDefaultWidgetTypes(): List<WidgetType> {
        val filters = _filters.value
        return listOf(
            generateFocusTimeTodayWidget(filters),
            generateFocusSessionsWidget(filters, "Focus Sessions", "this week"),
            generateFocusSessionsWidget(filters, "Last 7 Days", "last 7 days")
        )
    }
    
    private suspend fun generateFocusTimeTodayWidget(filters: StatisticsFilters): WidgetType.FocusTimeToday {
        val todayHours = statisticsRepository.getTodayFocusHours()
        val weeklyData = statisticsRepository.getWeeklyFocusHours()
        val average = weeklyData.average().toFloat()
        val vsAverage = todayHours - average
        
        val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        return WidgetType.FocusTimeToday(
            todayHours = todayHours,
            vsAverage = vsAverage,
            weeklyData = weeklyData,
            dayLabels = dayLabels
        )
    }
    
    private suspend fun generateFocusSessionsWidget(
        filters: StatisticsFilters,
        subtitle: String,
        period: String
    ): WidgetType.FocusSessions {
        val (currentProgress, completedDays) = statisticsRepository.getWeeklyGoalProgress()
        return WidgetType.FocusSessions(
            subtitle = subtitle,
            currentProgress = currentProgress,
            targetProgress = 7,
            completedDays = completedDays,
            period = period
        )
    }
    
    private suspend fun generateFocusTimeWidget(filters: StatisticsFilters): WidgetType.FocusTime {
        val totalHours = statisticsRepository.getTotalFocusHours(filters)
        val todayHours = statisticsRepository.getTodayFocusHours()
        val period = filters.getDescription()
        
        // Calculate trend (simplified - compare with previous period)
        val trendPercentage = 0f // TODO: Implement trend calculation
        
        return WidgetType.FocusTime(
            totalHours = totalHours,
            period = period,
            todayHours = todayHours,
            trendPercentage = trendPercentage
        )
    }
    
    private suspend fun generateStreakWidget(): WidgetType.Streak {
        val currentStreak = statisticsRepository.getCurrentStreak()
        return WidgetType.Streak(
            currentStreak = currentStreak,
            targetStreak = 30
        )
    }
    
    private suspend fun generateWeeklyGoalWidget(filters: StatisticsFilters): WidgetType.WeeklyGoal {
        val (currentProgress, _) = statisticsRepository.getWeeklyGoalProgress()
        val percentage = (currentProgress * 100) / 7
        return WidgetType.WeeklyGoal(
            currentProgress = currentProgress,
            targetProgress = 7,
            percentage = percentage
        )
    }
    
    private suspend fun generateAppUsageWidget(filters: StatisticsFilters): WidgetType.AppUsage {
        val apps = statisticsRepository.getAppUsage(filters)
        return WidgetType.AppUsage(apps = apps)
    }
    
    private suspend fun generateTotalHoursWidget(): WidgetType.TotalHours {
        val hours = statisticsRepository.getTotalHoursAllTime()
        return WidgetType.TotalHours(
            hours = hours,
            period = "all time"
        )
    }
    
    private suspend fun generateMonthlyOverviewWidget(filters: StatisticsFilters): WidgetType.MonthlyOverview {
        val (totalHours, weeklyData) = statisticsRepository.getMonthlyOverview()
        return WidgetType.MonthlyOverview(
            totalHours = totalHours,
            weeklyData = weeklyData,
            weekLabels = listOf("W1", "W2", "W3", "W4")
        )
    }
    
    private suspend fun generateStatsTodayWidget(filters: StatisticsFilters): WidgetType.StatsToday {
        val todayHours = statisticsRepository.getTodayFocusHours()
        return WidgetType.StatsToday(
            value = todayHours,
            unit = "hours"
        )
    }
    
    private suspend fun updateWidgetsWithFilters(widgetTypes: List<WidgetType>) {
        val currentFilters = _filters.value
        val updatedWidgets = widgetTypes.mapIndexed { index, type ->
            val updatedType = when (type) {
                is WidgetType.FocusTime -> generateFocusTimeWidget(currentFilters)
                is WidgetType.FocusTimeToday -> generateFocusTimeTodayWidget(currentFilters)
                is WidgetType.Streak -> generateStreakWidget()
                is WidgetType.WeeklyGoal -> generateWeeklyGoalWidget(currentFilters)
                is WidgetType.AppUsage -> generateAppUsageWidget(currentFilters)
                is WidgetType.TotalHours -> generateTotalHoursWidget()
                is WidgetType.MonthlyOverview -> generateMonthlyOverviewWidget(currentFilters)
                is WidgetType.StatsToday -> generateStatsTodayWidget(currentFilters)
                is WidgetType.FocusSessions -> {
                    // Regenerate FocusSessions data based on current filters
                    generateFocusSessionsWidget(currentFilters, type.subtitle, type.period)
                }
            }
            Widget(
                type = updatedType,
                position = index
            )
        }
        _widgets.value = updatedWidgets
    }
    
    fun updateFilters(newFilters: StatisticsFilters) {
        _filters.value = newFilters
        // Regenerate widgets with new filter data
        viewModelScope.launch {
            val currentWidgetTypes = _widgets.value.map { it.type }
            updateWidgetsWithFilters(currentWidgetTypes)
        }
    }
    
    fun setTimePeriod(period: TimePeriod) {
        updateFilters(_filters.value.copy(timePeriod = period))
    }
    
    fun setDuration(duration: Duration) {
        updateFilters(_filters.value.copy(duration = duration))
    }
    
    fun setSelectedDate(date: java.time.LocalDate) {
        updateFilters(_filters.value.copy(selectedDate = date))
    }
    
    fun setSearchQuery(query: String) {
        updateFilters(_filters.value.copy(searchQuery = query))
    }
    
    fun toggleFilterDialog() {
        _showFilterDialog.value = !_showFilterDialog.value
    }
    
    fun toggleSearchDialog() {
        _showSearchDialog.value = !_showSearchDialog.value
    }
    
    fun toggleCustomizeDialog() {
        _showCustomizeDialog.value = !_showCustomizeDialog.value
    }
    
    fun toggleEditMode() {
        _isEditMode.value = !_isEditMode.value
    }
    
    fun moveWidgetUp(index: Int) {
        if (index > 0) {
            val currentWidgets = _widgets.value.toMutableList()
            val widget = currentWidgets.removeAt(index)
            currentWidgets.add(index - 1, widget)
            // Update positions - create new list to ensure state change is detected
            val updatedWidgets = currentWidgets.mapIndexed { i, w ->
                Widget(w.type, i)
            }
            _widgets.value = updatedWidgets
        }
    }
    
    fun moveWidgetDown(index: Int) {
        val currentWidgets = _widgets.value.toMutableList()
        if (index < currentWidgets.size - 1) {
            val widget = currentWidgets.removeAt(index)
            currentWidgets.add(index + 1, widget)
            // Update positions - create new list to ensure state change is detected
            val updatedWidgets = currentWidgets.mapIndexed { i, w ->
                Widget(w.type, i)
            }
            _widgets.value = updatedWidgets
        }
    }
    
    fun removeWidget(index: Int) {
        val currentWidgets = _widgets.value.toMutableList()
        currentWidgets.removeAt(index)
        // Update positions
        currentWidgets.forEachIndexed { i, w ->
            currentWidgets[i] = Widget(w.type, i)
        }
        _widgets.value = currentWidgets
    }
    
    fun addWidget(widgetType: WidgetType) {
        viewModelScope.launch {
            val currentWidgets = _widgets.value.toMutableList()
            val filters = _filters.value
            
            val newType = when (widgetType) {
                is WidgetType.FocusTime -> generateFocusTimeWidget(filters)
                is WidgetType.FocusTimeToday -> generateFocusTimeTodayWidget(filters)
                is WidgetType.Streak -> generateStreakWidget()
                is WidgetType.WeeklyGoal -> generateWeeklyGoalWidget(filters)
                is WidgetType.AppUsage -> generateAppUsageWidget(filters)
                is WidgetType.TotalHours -> generateTotalHoursWidget()
                is WidgetType.MonthlyOverview -> generateMonthlyOverviewWidget(filters)
                is WidgetType.StatsToday -> generateStatsTodayWidget(filters)
                is WidgetType.FocusSessions -> widgetType // Keep as is
            }
            
            val newWidget = Widget(
                type = newType,
                position = currentWidgets.size
            )
            currentWidgets.add(newWidget)
            // Update positions
            currentWidgets.forEachIndexed { i, w ->
                currentWidgets[i] = Widget(w.type, i)
            }
            _widgets.value = currentWidgets
        }
    }
    
    fun getAllAvailableWidgetTypes(): List<WidgetType> {
        // Return template types - actual data will be generated when added
        return listOf(
            WidgetType.FocusTimeToday(todayHours = 0f, vsAverage = 0f, weeklyData = emptyList(), dayLabels = emptyList()),
            WidgetType.FocusTime(totalHours = 0f, period = "", todayHours = 0f, trendPercentage = 0f),
            WidgetType.Streak(currentStreak = 0, targetStreak = 30),
            WidgetType.WeeklyGoal(currentProgress = 0, targetProgress = 7, percentage = 0),
            WidgetType.AppUsage(apps = emptyList()),
            WidgetType.TotalHours(hours = 0f),
            WidgetType.StatsToday(value = 0f, unit = "hours"),
            WidgetType.MonthlyOverview(totalHours = 0f, weeklyData = emptyList(), weekLabels = emptyList()),
            WidgetType.FocusSessions(subtitle = "", currentProgress = 0, targetProgress = 7, completedDays = emptyList(), period = "")
        )
    }
    
    fun savePreset() {
        viewModelScope.launch {
            presetManager.savePreset(_widgets.value)
            _isEditMode.value = false
        }
    }
    
    fun cancelEdit() {
        viewModelScope.launch {
            loadWidgets() // Reload from saved preset
            _isEditMode.value = false
        }
    }
    
    // Test data generation methods
    fun generateTestData() {
        viewModelScope.launch {
            testDataGenerator.generateComprehensiveTestData()
            loadWidgets() // Reload widgets to show new data
        }
    }
    
    fun generateLast7DaysTestData() {
        viewModelScope.launch {
            testDataGenerator.generateLast7DaysData()
            loadWidgets()
        }
    }
    
    fun generateTodayTestData(hours: Float) {
        viewModelScope.launch {
            testDataGenerator.generateTodayData(hours)
            loadWidgets()
        }
    }
    
    fun generateStreakTestData(days: Int) {
        viewModelScope.launch {
            testDataGenerator.generateStreakData(days)
            loadWidgets()
        }
    }
    
    fun clearTestData() {
        viewModelScope.launch {
            testDataGenerator.clearAllData()
            loadWidgets()
        }
    }
}

