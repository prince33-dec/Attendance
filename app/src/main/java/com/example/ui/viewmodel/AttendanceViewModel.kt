package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceSession
import com.example.data.model.AttendanceStatus
import com.example.data.model.BirthdayStudent
import com.example.data.model.MonthlyStudentSummary
import com.example.data.model.SessionStats
import com.example.data.model.Student
import com.example.data.model.StudentWithAttendance
import com.example.data.repository.AttendanceRepository
import com.example.util.ExportManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class AppScreen(val title: String) {
    DASHBOARD("Dashboard"),
    ATTENDANCE("Attendance"),
    STUDENTS("Students"),
    REPORTS("Reports")
}

enum class ReportTab {
    DATE_WISE,
    MONTH_WISE
}

@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AttendanceRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = AttendanceRepository(db.studentDao(), db.attendanceDao())
    }

    // Navigation State
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // Selected Date & Session for marking attendance
    private val _selectedDate = MutableStateFlow(LocalDate.now().toString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedSession = MutableStateFlow(
        if (java.time.LocalTime.now().hour >= 13) AttendanceSession.EVENING else AttendanceSession.MORNING
    )
    val selectedSession: StateFlow<AttendanceSession> = _selectedSession.asStateFlow()

    // Filters
    private val _attendanceSearchQuery = MutableStateFlow("")
    val attendanceSearchQuery: StateFlow<String> = _attendanceSearchQuery.asStateFlow()

    private val _selectedClassFilter = MutableStateFlow<String?>(null)
    val selectedClassFilter: StateFlow<String?> = _selectedClassFilter.asStateFlow()

    private val _studentSearchQuery = MutableStateFlow("")
    val studentSearchQuery: StateFlow<String> = _studentSearchQuery.asStateFlow()

    // Reports State
    private val _selectedReportMonth = MutableStateFlow(
        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
    )
    val selectedReportMonth: StateFlow<String> = _selectedReportMonth.asStateFlow()

    private val _selectedReportDate = MutableStateFlow(LocalDate.now().toString())
    val selectedReportDate: StateFlow<String> = _selectedReportDate.asStateFlow()

    private val _reportTab = MutableStateFlow(ReportTab.DATE_WISE)
    val reportTab: StateFlow<ReportTab> = _reportTab.asStateFlow()

    // Students list
    val allStudents: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val distinctClasses: StateFlow<List<String>> = repository.distinctClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val distinctGameCategories: StateFlow<List<String>> = repository.distinctGameCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studentCount: StateFlow<Int> = repository.studentCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Today's stats for Dashboard
    val todayDateStr = LocalDate.now().toString()

    val todayMorningStats: StateFlow<SessionStats> = repository.getSessionStats(todayDateStr, AttendanceSession.MORNING)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionStats())

    val todayEveningStats: StateFlow<SessionStats> = repository.getSessionStats(todayDateStr, AttendanceSession.EVENING)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionStats())

    // Birthday students
    val birthdayStudents: StateFlow<List<BirthdayStudent>> = repository.getBirthdayStudents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active attendance marking roster
    val attendanceRoster: StateFlow<List<StudentWithAttendance>> = _selectedDate
        .flatMapLatest { date ->
            repository.getStudentsWithAttendanceForDate(date)
        }
        .combine(_attendanceSearchQuery) { list, query ->
            if (query.isBlank()) list
            else list.filter {
                it.student.name.contains(query, ignoreCase = true) ||
                it.student.grNoRollNo.contains(query, ignoreCase = true) ||
                it.student.studentClass.contains(query, ignoreCase = true)
            }
        }
        .combine(_selectedClassFilter) { list, classFilter ->
            if (classFilter.isNullOrBlank()) list
            else list.filter { it.student.studentClass.equals(classFilter, ignoreCase = true) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stats for the selected date & session in Attendance screen
    val selectedDateStats: StateFlow<Pair<SessionStats, SessionStats>> = _selectedDate
        .flatMapLatest { date ->
            combine(
                repository.getSessionStats(date, AttendanceSession.MORNING),
                repository.getSessionStats(date, AttendanceSession.EVENING)
            ) { mStats, eStats -> Pair(mStats, eStats) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(SessionStats(), SessionStats()))

    // Report data
    val dateWiseReportList: StateFlow<List<StudentWithAttendance>> = _selectedReportDate
        .flatMapLatest { date -> repository.getStudentsWithAttendanceForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dateWiseReportStats: StateFlow<Pair<SessionStats, SessionStats>> = _selectedReportDate
        .flatMapLatest { date ->
            combine(
                repository.getSessionStats(date, AttendanceSession.MORNING),
                repository.getSessionStats(date, AttendanceSession.EVENING)
            ) { mStats, eStats -> Pair(mStats, eStats) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(SessionStats(), SessionStats()))

    val monthlyReportList: StateFlow<List<MonthlyStudentSummary>> = _selectedReportMonth
        .flatMapLatest { month -> repository.getMonthlyReport(month) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Seed check
    init {
        viewModelScope.launch {
            // Clean up any orphaned records from deleted students immediately
            repository.cleanOrphanedRecords()
            // In-built students removed: Do not seed demo students automatically on installation
        }
    }

    // Modifiers & Actions
    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun setSelectedSession(session: AttendanceSession) {
        _selectedSession.value = session
    }

    fun setAttendanceSearchQuery(query: String) {
        _attendanceSearchQuery.value = query
    }

    fun setSelectedClassFilter(filter: String?) {
        _selectedClassFilter.value = filter
    }

    fun setStudentSearchQuery(query: String) {
        _studentSearchQuery.value = query
    }

    fun setSelectedReportDate(date: String) {
        _selectedReportDate.value = date
    }

    fun setSelectedReportMonth(month: String) {
        _selectedReportMonth.value = month
    }

    fun setReportTab(tab: ReportTab) {
        _reportTab.value = tab
    }

    fun setAttendanceStatus(studentId: Long, status: AttendanceStatus) {
        viewModelScope.launch {
            repository.setAttendanceStatus(
                studentId = studentId,
                date = _selectedDate.value,
                session = _selectedSession.value,
                status = status
            )
        }
    }

    fun markAllInSession(status: AttendanceStatus) {
        viewModelScope.launch {
            val students = attendanceRoster.value.map { it.student }
            repository.markAllStudents(
                date = _selectedDate.value,
                session = _selectedSession.value,
                students = students,
                status = status
            )
        }
    }

    fun clearCurrentSession() {
        viewModelScope.launch {
            repository.clearSessionRecords(
                date = _selectedDate.value,
                session = _selectedSession.value
            )
        }
    }

    fun saveStudent(student: Student, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (student.id == 0L) {
                repository.insertStudent(student)
            } else {
                repository.updateStudent(student)
            }
            onComplete()
        }
    }

    fun deleteStudent(student: Student, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            onComplete()
        }
    }

    fun resetWithDemoData() {
        viewModelScope.launch {
            repository.seedDemoDataIfEmpty(force = true)
        }
    }

    // Export Handlers
    fun exportDateWiseExcel(context: Context) {
        val list = dateWiseReportList.value
        val stats = dateWiseReportStats.value
        val file = ExportManager.exportDateWiseXlsx(
            context = context,
            date = _selectedReportDate.value,
            studentsWithAttendance = list,
            morningStats = stats.first,
            eveningStats = stats.second
        )
        if (file != null) {
            ExportManager.shareFile(
                context = context,
                file = file,
                mimeType = ExportManager.MIME_TYPE_XLSX,
                title = "Attendance Daily Report - ${_selectedReportDate.value}.xlsx"
            )
        }
    }

    // Keep legacy alias for backward compatibility
    fun exportDateWiseCsv(context: Context) = exportDateWiseExcel(context)

    fun exportDateWisePdf(context: Context) {
        val list = dateWiseReportList.value
        val stats = dateWiseReportStats.value
        val file = ExportManager.exportDateWisePdf(
            context = context,
            date = _selectedReportDate.value,
            studentsWithAttendance = list,
            morningStats = stats.first,
            eveningStats = stats.second
        )
        if (file != null) {
            ExportManager.shareFile(
                context = context,
                file = file,
                mimeType = "application/pdf",
                title = "Attendance Daily Report - ${_selectedReportDate.value}"
            )
        }
    }

    fun exportMonthWiseExcel(context: Context) {
        val list = monthlyReportList.value
        val file = ExportManager.exportMonthWiseXlsx(
            context = context,
            monthPrefix = _selectedReportMonth.value,
            summaries = list
        )
        if (file != null) {
            ExportManager.shareFile(
                context = context,
                file = file,
                mimeType = ExportManager.MIME_TYPE_XLSX,
                title = "Attendance Monthly Report - ${_selectedReportMonth.value}.xlsx"
            )
        }
    }

    // Keep legacy alias for backward compatibility
    fun exportMonthWiseCsv(context: Context) = exportMonthWiseExcel(context)

    fun exportMonthWisePdf(context: Context) {
        val list = monthlyReportList.value
        val file = ExportManager.exportMonthWisePdf(
            context = context,
            monthPrefix = _selectedReportMonth.value,
            summaries = list
        )
        if (file != null) {
            ExportManager.shareFile(
                context = context,
                file = file,
                mimeType = "application/pdf",
                title = "Attendance Monthly Report - ${_selectedReportMonth.value}"
            )
        }
    }
}
