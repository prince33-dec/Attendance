package com.example.data.model

data class SessionStats(
    val present: Int = 0,
    val absent: Int = 0,
    val leave: Int = 0,
    val unrecorded: Int = 0,
    val total: Int = 0
) {
    val presentPercentage: Float
        get() = if (total > 0) (present.toFloat() / total) * 100f else 0f
}

data class StudentWithAttendance(
    val student: Student,
    val morningRecord: AttendanceRecord?,
    val eveningRecord: AttendanceRecord?
)

data class MonthlyStudentSummary(
    val student: Student,
    val totalSessions: Int,
    val presentCount: Int,
    val absentCount: Int,
    val leaveCount: Int,
    val morningPresent: Int,
    val eveningPresent: Int
) {
    val attendancePercentage: Float
        get() = if (totalSessions > 0) (presentCount.toFloat() / totalSessions) * 100f else 0f
}

data class BirthdayStudent(
    val student: Student,
    val isToday: Boolean,
    val daysUntil: Int,
    val formattedDob: String
)
