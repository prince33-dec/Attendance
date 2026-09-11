package com.example.data.repository

import com.example.data.dao.AttendanceDao
import com.example.data.dao.StudentDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceSession
import com.example.data.model.AttendanceStatus
import com.example.data.model.BirthdayStudent
import com.example.data.model.MonthlyStudentSummary
import com.example.data.model.SessionStats
import com.example.data.model.Student
import com.example.data.model.StudentWithAttendance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class AttendanceRepository(
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao
) {
    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()
    val distinctClasses: Flow<List<String>> = studentDao.getDistinctClasses()
    val distinctGameCategories: Flow<List<String>> = studentDao.getDistinctGameCategories()
    val studentCount: Flow<Int> = studentDao.getStudentCount()

    fun getStudentById(id: Long): Flow<Student?> = studentDao.getStudentById(id)

    suspend fun insertStudent(student: Student): Long = studentDao.insertStudent(student)

    suspend fun updateStudent(student: Student) = studentDao.updateStudent(student)

    suspend fun deleteStudent(student: Student) {
        // Automatically delete all attendance records for this student
        attendanceDao.deleteRecordsForStudent(student.id)
        studentDao.deleteStudent(student)
        // Clean up internal photo file if saved
        student.photoUri?.let { uriStr ->
            try {
                val file = java.io.File(uriStr)
                if (file.exists()) file.delete()
            } catch (_: Exception) {}
        }
    }

    suspend fun deleteStudentById(id: Long) {
        val student = studentDao.getStudentByIdDirect(id)
        // Automatically delete all attendance records for this student
        attendanceDao.deleteRecordsForStudent(id)
        studentDao.deleteStudentById(id)
        student?.photoUri?.let { uriStr ->
            try {
                val file = java.io.File(uriStr)
                if (file.exists()) file.delete()
            } catch (_: Exception) {}
        }
    }

    suspend fun cleanOrphanedRecords() {
        attendanceDao.deleteOrphanedRecords()
    }

    fun getStudentsWithAttendanceForDate(date: String): Flow<List<StudentWithAttendance>> {
        return combine(
            studentDao.getAllStudents(),
            attendanceDao.getRecordsForDate(date)
        ) { students, records ->
            val validStudentIds = students.map { it.id }.toSet()
            val validRecords = records.filter { it.studentId in validStudentIds }

            val morningMap = validRecords.filter { it.session == AttendanceSession.MORNING.name }
                .associateBy { it.studentId }
            val eveningMap = validRecords.filter { it.session == AttendanceSession.EVENING.name }
                .associateBy { it.studentId }

            students.map { student ->
                StudentWithAttendance(
                    student = student,
                    morningRecord = morningMap[student.id],
                    eveningRecord = eveningMap[student.id]
                )
            }
        }
    }

    fun getSessionStats(date: String, session: AttendanceSession): Flow<SessionStats> {
        return combine(
            studentDao.getAllStudents(),
            attendanceDao.getRecordsForDateAndSession(date, session.name)
        ) { students, records ->
            val totalStudents = students.size
            if (totalStudents == 0) {
                return@combine SessionStats(total = 0)
            }

            // Map records only for currently existing students
            val validStudentIds = students.map { it.id }.toSet()
            val recordMap = records.filter { it.studentId in validStudentIds }
                .associateBy { it.studentId }

            var present = 0
            var absent = 0
            var leave = 0
            var unrecorded = 0

            // Strictly count based on current existing students
            for (student in students) {
                when (recordMap[student.id]?.status) {
                    AttendanceStatus.PRESENT.name -> present++
                    AttendanceStatus.ABSENT.name -> absent++
                    AttendanceStatus.LEAVE.name -> leave++
                    else -> unrecorded++
                }
            }

            SessionStats(
                present = present,
                absent = absent,
                leave = leave,
                unrecorded = unrecorded,
                total = totalStudents
            )
        }
    }

    suspend fun setAttendanceStatus(
        studentId: Long,
        date: String,
        session: AttendanceSession,
        status: AttendanceStatus
    ) {
        val record = AttendanceRecord(
            studentId = studentId,
            date = date,
            session = session.name,
            status = status.name,
            updatedAt = System.currentTimeMillis()
        )
        attendanceDao.insertOrUpdate(record)
    }

    suspend fun clearAttendanceStatus(
        studentId: Long,
        date: String,
        session: AttendanceSession
    ) {
        attendanceDao.deleteRecord(studentId, date, session.name)
    }

    suspend fun markAllStudents(
        date: String,
        session: AttendanceSession,
        students: List<Student>,
        status: AttendanceStatus
    ) {
        val records = students.map { student ->
            AttendanceRecord(
                studentId = student.id,
                date = date,
                session = session.name,
                status = status.name,
                updatedAt = System.currentTimeMillis()
            )
        }
        attendanceDao.insertOrUpdateAll(records)
    }

    suspend fun clearSessionRecords(date: String, session: AttendanceSession) {
        attendanceDao.clearSessionRecords(date, session.name)
    }

    fun getMonthlyReport(monthPrefix: String): Flow<List<MonthlyStudentSummary>> {
        // monthPrefix is e.g. "2026-09"
        return combine(
            studentDao.getAllStudents(),
            attendanceDao.getRecordsForMonth(monthPrefix)
        ) { students, records ->
            val validStudentIds = students.map { it.id }.toSet()
            val validRecords = records.filter { it.studentId in validStudentIds }

            // Distinct sessions conducted in this month: each distinct (date, session) pair
            val conductedSessions = validRecords.map { "${it.date}_${it.session}" }.distinct()
            val totalConducted = conductedSessions.size

            val recordsByStudent = validRecords.groupBy { it.studentId }

            students.map { student ->
                val studentRecords = recordsByStudent[student.id] ?: emptyList()
                val presentCount = studentRecords.count { it.status == AttendanceStatus.PRESENT.name }
                val absentCount = studentRecords.count { it.status == AttendanceStatus.ABSENT.name }
                val leaveCount = studentRecords.count { it.status == AttendanceStatus.LEAVE.name }
                val morningPresent = studentRecords.count { 
                    it.session == AttendanceSession.MORNING.name && it.status == AttendanceStatus.PRESENT.name 
                }
                val eveningPresent = studentRecords.count { 
                    it.session == AttendanceSession.EVENING.name && it.status == AttendanceStatus.PRESENT.name 
                }

                MonthlyStudentSummary(
                    student = student,
                    totalSessions = totalConducted,
                    presentCount = presentCount,
                    absentCount = absentCount,
                    leaveCount = leaveCount,
                    morningPresent = morningPresent,
                    eveningPresent = eveningPresent
                )
            }.sortedByDescending { it.attendancePercentage }
        }
    }

    fun getBirthdayStudents(today: LocalDate = LocalDate.now()): Flow<List<BirthdayStudent>> {
        return combine(studentDao.getAllStudents()) { array ->
            val students = array[0]
            val birthdayList = mutableListOf<BirthdayStudent>()

            for (student in students) {
                val dobStr = student.dob.trim()
                if (dobStr.isEmpty()) continue

                val parsedDob = parseFlexibleDate(dobStr) ?: continue

                // Check birthday for this year
                var nextBirthday = parsedDob.withYear(today.year)
                if (nextBirthday.isBefore(today)) {
                    nextBirthday = parsedDob.withYear(today.year + 1)
                }

                val daysUntil = ChronoUnit.DAYS.between(today, nextBirthday).toInt()
                val isToday = (daysUntil == 0)

                if (isToday || daysUntil in 1..30) {
                    birthdayList.add(
                        BirthdayStudent(
                            student = student,
                            isToday = isToday,
                            daysUntil = daysUntil,
                            formattedDob = formatDisplayDate(parsedDob)
                        )
                    )
                }
            }

            birthdayList.sortedWith(compareBy({ !it.isToday }, { it.daysUntil }))
        }
    }

    private fun parseFlexibleDate(dateStr: String): LocalDate? {
        return try {
            if (dateStr.contains("-")) {
                LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
            } else if (dateStr.contains("/")) {
                val parts = dateStr.split("/")
                if (parts.size == 3) {
                    if (parts[0].length == 4) {
                        LocalDate.of(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
                    } else {
                        LocalDate.of(parts[2].toInt(), parts[1].toInt(), parts[0].toInt())
                    }
                } else null
            } else null
        } catch (_: Exception) {
            null
        }
    }

    private fun formatDisplayDate(date: LocalDate): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
            date.format(formatter)
        } catch (_: Exception) {
            date.toString()
        }
    }

    suspend fun seedDemoDataIfEmpty(force: Boolean = false) {
        val existingCount = studentDao.getStudentCountDirect()
        if (!force && existingCount > 0) {
            // Students already exist, do not seed demo data
            return
        }

        val today = LocalDate.now()
        val todayStr = today.toString()
        val yesterdayStr = today.minusDays(1).toString()
        val twoDaysAgoStr = today.minusDays(2).toString()

        val sampleStudents = listOf(
            Student(
                name = "Aarav Sharma",
                dob = today.minusYears(16).toString(), // Birthday today!
                gender = "Male",
                phone1 = "+91 9876543210",
                phone2 = "+91 9876543211",
                collegeOrSchool = "St. Xavier's High School",
                standardOrDegree = "11th Science",
                studentClass = "11-A",
                grNoRollNo = "101",
                gameCategory = "Cricket",
                address = "Flat 402, Green Valley Apartments, Mumbai",
                aadhaarNumber = "4589 1234 5678",
                bankAccountNumber = "91827364501",
                bankName = "State Bank of India",
                bankBranch = "Andheri West",
                ifscCode = "SBIN0001234"
            ),
            Student(
                name = "Ananya Patel",
                dob = today.plusDays(3).minusYears(15).toString(), // Birthday in 3 days!
                gender = "Female",
                phone1 = "+91 9823456789",
                phone2 = "+91 9823456780",
                collegeOrSchool = "St. Xavier's High School",
                standardOrDegree = "11th Science",
                studentClass = "11-A",
                grNoRollNo = "102",
                gameCategory = "Badminton",
                address = "12 Nilgiri Heights, Pune",
                aadhaarNumber = "8745 6321 9012",
                bankAccountNumber = "30294857162",
                bankName = "HDFC Bank",
                bankBranch = "FC Road",
                ifscCode = "HDFC0000456"
            ),
            Student(
                name = "Rohan Verma",
                dob = today.plusDays(7).minusYears(16).toString(), // Birthday in 7 days!
                gender = "Male",
                phone1 = "+91 9712345678",
                phone2 = "",
                collegeOrSchool = "St. Xavier's High School",
                standardOrDegree = "11th Commerce",
                studentClass = "11-B",
                grNoRollNo = "103",
                gameCategory = "Football",
                address = "Sector 14, Navi Mumbai",
                aadhaarNumber = "3214 7896 5412",
                bankAccountNumber = "48392019482",
                bankName = "ICICI Bank",
                bankBranch = "Vashi",
                ifscCode = "ICIC0000789"
            ),
            Student(
                name = "Priya Nair",
                dob = "2008-11-20",
                gender = "Female",
                phone1 = "+91 9654321098",
                phone2 = "",
                collegeOrSchool = "St. Xavier's High School",
                standardOrDegree = "11th Commerce",
                studentClass = "11-B",
                grNoRollNo = "104",
                gameCategory = "Athletics",
                address = "B-103 Sunshine Park, Thane",
                aadhaarNumber = "6547 8912 3450",
                bankAccountNumber = "57483920192",
                bankName = "Axis Bank",
                bankBranch = "Thane West",
                ifscCode = "UTIB0000321"
            ),
            Student(
                name = "Kabir Singh",
                dob = "2007-04-12",
                gender = "Male",
                phone1 = "+91 9543210987",
                phone2 = "+91 9543210986",
                collegeOrSchool = "National Sports Academy",
                standardOrDegree = "12th Arts",
                studentClass = "12-A",
                grNoRollNo = "105",
                gameCategory = "Basketball",
                address = "74 Marine Drive, Mumbai",
                aadhaarNumber = "9874 5612 3045",
                bankAccountNumber = "68574930291",
                bankName = "Kotak Mahindra Bank",
                bankBranch = "Churchgate",
                ifscCode = "KKBK0000112"
            ),
            Student(
                name = "Meera Joshi",
                dob = "2009-01-05",
                gender = "Female",
                phone1 = "+91 9432109876",
                phone2 = "",
                collegeOrSchool = "National Sports Academy",
                standardOrDegree = "10th Standard",
                studentClass = "10-C",
                grNoRollNo = "106",
                gameCategory = "Chess",
                address = "Green Park Residency, Nashik",
                aadhaarNumber = "1234 5678 9012",
                bankAccountNumber = "10293847561",
                bankName = "Bank of Baroda",
                bankBranch = "College Road",
                ifscCode = "BARB0COLLEG"
            )
        )

        studentDao.insertStudents(sampleStudents)

        // Seed some attendance records for today, yesterday, and two days ago
        val insertedStudents = studentDao.searchStudents("").let { flow ->
            // Let's seed for IDs 1 to 6
            listOf(1L, 2L, 3L, 4L, 5L, 6L)
        }

        val demoRecords = mutableListOf<AttendanceRecord>()
        val dates = listOf(twoDaysAgoStr, yesterdayStr, todayStr)

        for (d in dates) {
            // Morning
            demoRecords.add(AttendanceRecord(studentId = 1L, date = d, session = "MORNING", status = "PRESENT"))
            demoRecords.add(AttendanceRecord(studentId = 2L, date = d, session = "MORNING", status = "PRESENT"))
            demoRecords.add(AttendanceRecord(studentId = 3L, date = d, session = "MORNING", status = "ABSENT"))
            demoRecords.add(AttendanceRecord(studentId = 4L, date = d, session = "MORNING", status = "PRESENT"))
            demoRecords.add(AttendanceRecord(studentId = 5L, date = d, session = "MORNING", status = "LEAVE"))
            demoRecords.add(AttendanceRecord(studentId = 6L, date = d, session = "MORNING", status = "PRESENT"))

            // Evening
            demoRecords.add(AttendanceRecord(studentId = 1L, date = d, session = "EVENING", status = "PRESENT"))
            demoRecords.add(AttendanceRecord(studentId = 2L, date = d, session = "EVENING", status = "PRESENT"))
            demoRecords.add(AttendanceRecord(studentId = 3L, date = d, session = "EVENING", status = "PRESENT"))
            demoRecords.add(AttendanceRecord(studentId = 4L, date = d, session = "EVENING", status = "LEAVE"))
            demoRecords.add(AttendanceRecord(studentId = 5L, date = d, session = "EVENING", status = "ABSENT"))
            demoRecords.add(AttendanceRecord(studentId = 6L, date = d, session = "EVENING", status = "PRESENT"))
        }

        attendanceDao.insertOrUpdateAll(demoRecords)
    }
}
