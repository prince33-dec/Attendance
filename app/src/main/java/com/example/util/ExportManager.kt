package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.AttendanceSession
import com.example.data.model.MonthlyStudentSummary
import com.example.data.model.SessionStats
import com.example.data.model.StudentWithAttendance
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ExportManager {

    // ==========================================
    // EXCEL (.XLSX) & CSV EXPORT
    // ==========================================

    const val MIME_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    const val MIME_TYPE_CSV = "text/csv"

    fun exportDateWiseXlsx(
        context: Context,
        date: String,
        studentsWithAttendance: List<StudentWithAttendance>,
        morningStats: SessionStats,
        eveningStats: SessionStats
    ): File? {
        return try {
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()

            val file = File(reportsDir, "Attendance_Daily_${date}.xlsx")
            val xlsx = SimpleXlsxBuilder()

            // Header & Metadata
            xlsx.addRow("ATTENDANCE DAILY REPORT", "", "", "", "", "", "", "")
            xlsx.addRow("Date:", date, "", "", "", "", "", "")
            xlsx.addRow("Total Students:", studentsWithAttendance.size, "", "", "", "", "", "")
            xlsx.addRow()

            // Session Summary Block
            xlsx.addRow("SESSION SUMMARY", "", "", "", "")
            xlsx.addRow("Session", "Present", "Absent", "Leave", "Attendance %")
            xlsx.addRow(
                "Morning",
                morningStats.present,
                morningStats.absent,
                morningStats.leave,
                String.format("%.1f%%", morningStats.presentPercentage)
            )
            xlsx.addRow(
                "Evening",
                eveningStats.present,
                eveningStats.absent,
                eveningStats.leave,
                String.format("%.1f%%", eveningStats.presentPercentage)
            )
            xlsx.addRow()

            // Student Roster
            xlsx.addRow("STUDENT ATTENDANCE DETAILS", "", "", "", "", "", "", "")
            xlsx.addRow("S.No", "Roll / GR No", "Student Name", "Class / Standard", "School / College", "Morning Session", "Evening Session", "Phone Number")

            studentsWithAttendance.forEachIndexed { index, item ->
                val s = item.student
                val mStatus = item.morningRecord?.status ?: "UNRECORDED"
                val eStatus = item.eveningRecord?.status ?: "UNRECORDED"

                xlsx.addRow(
                    index + 1,
                    s.grNoRollNo,
                    s.name,
                    s.studentClass,
                    s.collegeOrSchool,
                    mStatus,
                    eStatus,
                    s.phone1
                )
            }

            xlsx.writeToFile(file)
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportMonthWiseXlsx(
        context: Context,
        monthPrefix: String, // e.g. "2026-09"
        summaries: List<MonthlyStudentSummary>
    ): File? {
        return try {
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()

            val file = File(reportsDir, "Attendance_Monthly_${monthPrefix}.xlsx")
            val xlsx = SimpleXlsxBuilder()

            xlsx.addRow("MONTHLY ATTENDANCE REPORT", "", "", "", "", "", "", "", "", "", "", "")
            xlsx.addRow("Month:", monthPrefix)
            xlsx.addRow("Total Registered Students:", summaries.size)
            val totalSessions = summaries.firstOrNull()?.totalSessions ?: 0
            xlsx.addRow("Total Sessions Tracked:", totalSessions)
            xlsx.addRow()

            xlsx.addRow("S.No", "Roll / GR No", "Student Name", "Class", "Game Category", "Total Sessions", "Present Days", "Absent Days", "Leave Days", "Morning Present", "Evening Present", "Attendance %")

            summaries.forEachIndexed { index, summary ->
                val s = summary.student
                xlsx.addRow(
                    index + 1,
                    s.grNoRollNo,
                    s.name,
                    s.studentClass,
                    s.gameCategory,
                    summary.totalSessions,
                    summary.presentCount,
                    summary.absentCount,
                    summary.leaveCount,
                    summary.morningPresent,
                    summary.eveningPresent,
                    String.format("%.1f%%", summary.attendancePercentage)
                )
            }

            xlsx.writeToFile(file)
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportDateWiseCsv(
        context: Context,
        date: String,
        studentsWithAttendance: List<StudentWithAttendance>,
        morningStats: SessionStats,
        eveningStats: SessionStats
    ): File? {
        return try {
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()

            val file = File(reportsDir, "Attendance_Daily_${date}.csv")
            FileOutputStream(file).use { out ->
                val sb = StringBuilder()
                // Title and Metadata
                sb.append("ATTENDANCE REPORT\n")
                sb.append("Date:,${date}\n")
                sb.append("Total Students:,${studentsWithAttendance.size}\n\n")

                // Session Summary
                sb.append("SESSION SUMMARY\n")
                sb.append("Session,Present,Absent,Leave,Attendance %\n")
                sb.append("Morning,${morningStats.present},${morningStats.absent},${morningStats.leave},${String.format("%.1f%%", morningStats.presentPercentage)}\n")
                sb.append("Evening,${eveningStats.present},${eveningStats.absent},${eveningStats.leave},${String.format("%.1f%%", eveningStats.presentPercentage)}\n\n")

                // Student Details Table
                sb.append("STUDENT ATTENDANCE DETAILS\n")
                sb.append("S.No,Roll / GR No,Student Name,Class / Standard,School / College,Morning Session,Evening Session,Phone Number\n")

                studentsWithAttendance.forEachIndexed { index, item ->
                    val s = item.student
                    val mStatus = item.morningRecord?.status ?: "UNRECORDED"
                    val eStatus = item.eveningRecord?.status ?: "UNRECORDED"

                    sb.append("${index + 1},")
                    sb.append("\"${escapeCsv(s.grNoRollNo)}\",")
                    sb.append("\"${escapeCsv(s.name)}\",")
                    sb.append("\"${escapeCsv(s.studentClass)}\",")
                    sb.append("\"${escapeCsv(s.collegeOrSchool)}\",")
                    sb.append("\"$mStatus\",")
                    sb.append("\"$eStatus\",")
                    sb.append("\"${escapeCsv(s.phone1)}\"\n")
                }

                out.write(sb.toString().toByteArray(Charsets.UTF_8))
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportMonthWiseCsv(
        context: Context,
        monthPrefix: String, // e.g. "2026-09"
        summaries: List<MonthlyStudentSummary>
    ): File? {
        return try {
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()

            val file = File(reportsDir, "Attendance_Monthly_${monthPrefix}.csv")
            FileOutputStream(file).use { out ->
                val sb = StringBuilder()
                sb.append("MONTHLY ATTENDANCE REPORT\n")
                sb.append("Month:,${monthPrefix}\n")
                sb.append("Total Registered Students:,${summaries.size}\n")
                val totalSessions = summaries.firstOrNull()?.totalSessions ?: 0
                sb.append("Total Sessions Tracked:,${totalSessions}\n\n")

                sb.append("S.No,Roll / GR No,Student Name,Class,Game Category,Total Sessions,Present Days,Absent Days,Leave Days,Morning Present,Evening Present,Attendance %\n")

                summaries.forEachIndexed { index, summary ->
                    val s = summary.student
                    sb.append("${index + 1},")
                    sb.append("\"${escapeCsv(s.grNoRollNo)}\",")
                    sb.append("\"${escapeCsv(s.name)}\",")
                    sb.append("\"${escapeCsv(s.studentClass)}\",")
                    sb.append("\"${escapeCsv(s.gameCategory)}\",")
                    sb.append("${summary.totalSessions},")
                    sb.append("${summary.presentCount},")
                    sb.append("${summary.absentCount},")
                    sb.append("${summary.leaveCount},")
                    sb.append("${summary.morningPresent},")
                    sb.append("${summary.eveningPresent},")
                    sb.append("\"${String.format("%.1f%%", summary.attendancePercentage)}\"\n")
                }

                out.write(sb.toString().toByteArray(Charsets.UTF_8))
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }

    // ==========================================
    // PDF EXPORT (Native Android Vector PDF)
    // ==========================================

    fun exportDateWisePdf(
        context: Context,
        date: String,
        studentsWithAttendance: List<StudentWithAttendance>,
        morningStats: SessionStats,
        eveningStats: SessionStats
    ): File? {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 standard width in points
        val pageHeight = 842 // A4 standard height in points
        val itemsPerPage = 22

        val titlePaint = Paint().apply {
            color = Color.rgb(15, 23, 42) // Slate 900
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(71, 85, 105) // Slate 600
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val tableHeaderPaint = Paint().apply {
            color = Color.WHITE
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val rowTextPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val rowTextBold = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val bgPaint = Paint().apply { isAntiAlias = true }
        val borderPaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 1f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val totalPages = (studentsWithAttendance.size + itemsPerPage - 1).coerceAtLeast(1) / itemsPerPage
        var studentIndex = 0

        for (pageNumber in 1..totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            var currentY = 40f

            // Draw Header on Page 1
            if (pageNumber == 1) {
                // Header background pill
                bgPaint.color = Color.rgb(241, 245, 249)
                canvas.drawRoundRect(RectF(30f, currentY, (pageWidth - 30).toFloat(), currentY + 70f), 8f, 8f, bgPaint)

                canvas.drawText("DAILY ATTENDANCE REGISTER", 45f, currentY + 28f, titlePaint)
                canvas.drawText("Date: $date  |  Total Registered: ${studentsWithAttendance.size} Students", 45f, currentY + 50f, subtitlePaint)

                currentY += 85f

                // Summary Stats Cards (Morning & Evening)
                val cardWidth = (pageWidth - 75f) / 2f

                // Morning Card
                bgPaint.color = Color.rgb(224, 242, 254) // Light blue
                canvas.drawRoundRect(RectF(30f, currentY, 30f + cardWidth, currentY + 50f), 6f, 6f, bgPaint)
                val morningTitle = Paint().apply {
                    color = Color.rgb(2, 132, 199)
                    textSize = 10f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText("MORNING SESSION", 40f, currentY + 18f, morningTitle)
                canvas.drawText("Present: ${morningStats.present}  |  Absent: ${morningStats.absent}  |  Leave: ${morningStats.leave}  (${String.format("%.0f%%", morningStats.presentPercentage)})", 40f, currentY + 36f, subtitlePaint)

                // Evening Card
                val evX = 30f + cardWidth + 15f
                bgPaint.color = Color.rgb(238, 242, 255) // Light indigo
                canvas.drawRoundRect(RectF(evX, currentY, evX + cardWidth, currentY + 50f), 6f, 6f, bgPaint)
                val eveningTitle = Paint().apply {
                    color = Color.rgb(99, 102, 241)
                    textSize = 10f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText("EVENING SESSION", evX + 10f, currentY + 18f, eveningTitle)
                canvas.drawText("Present: ${eveningStats.present}  |  Absent: ${eveningStats.absent}  |  Leave: ${eveningStats.leave}  (${String.format("%.0f%%", eveningStats.presentPercentage)})", evX + 10f, currentY + 36f, subtitlePaint)

                currentY += 65f
            } else {
                canvas.drawText("DAILY ATTENDANCE REGISTER (Contd. Page $pageNumber of $totalPages)", 30f, currentY + 15f, subtitlePaint)
                currentY += 30f
            }

            // Draw Table Header
            val tableTop = currentY
            val headerHeight = 24f
            bgPaint.color = Color.rgb(15, 23, 42) // Dark Navy
            canvas.drawRoundRect(RectF(30f, tableTop, (pageWidth - 30).toFloat(), tableTop + headerHeight), 4f, 4f, bgPaint)

            // Columns: No (35), GR (70), Name (200), Class (80), Morning (75), Evening (75)
            val cNo = 38f
            val cGr = 65f
            val cName = 120f
            val cClass = 310f
            val cMorn = 390f
            val cEve = 470f

            canvas.drawText("#", cNo, tableTop + 16f, tableHeaderPaint)
            canvas.drawText("GR/Roll", cGr, tableTop + 16f, tableHeaderPaint)
            canvas.drawText("Student Name", cName, tableTop + 16f, tableHeaderPaint)
            canvas.drawText("Class", cClass, tableTop + 16f, tableHeaderPaint)
            canvas.drawText("Morning", cMorn, tableTop + 16f, tableHeaderPaint)
            canvas.drawText("Evening", cEve, tableTop + 16f, tableHeaderPaint)

            currentY += headerHeight

            // Draw Rows
            val rowHeight = 22f
            var pageItemCount = 0

            while (studentIndex < studentsWithAttendance.size && pageItemCount < itemsPerPage) {
                val item = studentsWithAttendance[studentIndex]
                val s = item.student
                val isEven = (studentIndex % 2 == 0)

                bgPaint.color = if (isEven) Color.rgb(248, 250, 252) else Color.WHITE
                canvas.drawRect(30f, currentY, (pageWidth - 30).toFloat(), currentY + rowHeight, bgPaint)
                canvas.drawLine(30f, currentY + rowHeight, (pageWidth - 30).toFloat(), currentY + rowHeight, borderPaint)

                val textY = currentY + 15f
                canvas.drawText("${studentIndex + 1}", cNo, textY, rowTextPaint)
                canvas.drawText(s.grNoRollNo.ifEmpty { "-" }, cGr, textY, rowTextBold)
                canvas.drawText(truncateText(s.name.ifEmpty { "Student #${studentIndex + 1}" }, 26), cName, textY, rowTextBold)
                canvas.drawText(truncateText(s.studentClass.ifEmpty { "-" }, 12), cClass, textY, rowTextPaint)

                // Status with badge color
                val mStatus = item.morningRecord?.status ?: "—"
                drawStatusPill(canvas, mStatus, cMorn, currentY + 3f)

                val eStatus = item.eveningRecord?.status ?: "—"
                drawStatusPill(canvas, eStatus, cEve, currentY + 3f)

                currentY += rowHeight
                studentIndex++
                pageItemCount++
            }

            // Footer
            val footerPaint = Paint().apply {
                color = Color.rgb(148, 163, 184)
                textSize = 8.5f
                isAntiAlias = true
            }
            canvas.drawText("AttendanceMate Offline  •  Page $pageNumber of $totalPages", 30f, pageHeight - 25f, footerPaint)
            canvas.drawText("Generated on ${LocalDate.now()}", (pageWidth - 160).toFloat(), pageHeight - 25f, footerPaint)

            pdfDocument.finishPage(page)
        }

        return try {
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()
            val file = File(reportsDir, "Attendance_Report_${date}.pdf")
            FileOutputStream(file).use { pdfDocument.writeTo(it) }
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    fun exportMonthWisePdf(
        context: Context,
        monthPrefix: String,
        summaries: List<MonthlyStudentSummary>
    ): File? {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val itemsPerPage = 22

        val titlePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val tableHeaderPaint = Paint().apply {
            color = Color.WHITE
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val rowTextPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val rowTextBold = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val bgPaint = Paint().apply { isAntiAlias = true }
        val borderPaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 1f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val totalPages = (summaries.size + itemsPerPage - 1).coerceAtLeast(1) / itemsPerPage
        var summaryIndex = 0

        for (pageNumber in 1..totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            var currentY = 40f

            if (pageNumber == 1) {
                bgPaint.color = Color.rgb(241, 245, 249)
                canvas.drawRoundRect(RectF(30f, currentY, (pageWidth - 30).toFloat(), currentY + 65f), 8f, 8f, bgPaint)

                canvas.drawText("MONTHLY ATTENDANCE SUMMARY", 45f, currentY + 28f, titlePaint)
                val totalSessions = summaries.firstOrNull()?.totalSessions ?: 0
                canvas.drawText("Month: $monthPrefix  |  Sessions Recorded: $totalSessions  |  Students: ${summaries.size}", 45f, currentY + 50f, subtitlePaint)

                currentY += 80f
            } else {
                canvas.drawText("MONTHLY ATTENDANCE SUMMARY (Page $pageNumber of $totalPages)", 30f, currentY + 15f, subtitlePaint)
                currentY += 30f
            }

            // Table Header
            val headerHeight = 24f
            bgPaint.color = Color.rgb(15, 23, 42)
            canvas.drawRoundRect(RectF(30f, currentY, (pageWidth - 30).toFloat(), currentY + headerHeight), 4f, 4f, bgPaint)

            val cNo = 36f
            val cGr = 58f
            val cName = 110f
            val cClass = 270f
            val cPresent = 340f
            val cAbsent = 390f
            val cLeave = 440f
            val cPct = 490f

            canvas.drawText("#", cNo, currentY + 16f, tableHeaderPaint)
            canvas.drawText("GR", cGr, currentY + 16f, tableHeaderPaint)
            canvas.drawText("Student Name", cName, currentY + 16f, tableHeaderPaint)
            canvas.drawText("Class", cClass, currentY + 16f, tableHeaderPaint)
            canvas.drawText("Present", cPresent, currentY + 16f, tableHeaderPaint)
            canvas.drawText("Absent", cAbsent, currentY + 16f, tableHeaderPaint)
            canvas.drawText("Leave", cLeave, currentY + 16f, tableHeaderPaint)
            canvas.drawText("Attend %", cPct, currentY + 16f, tableHeaderPaint)

            currentY += headerHeight

            val rowHeight = 22f
            var pageItemCount = 0

            while (summaryIndex < summaries.size && pageItemCount < itemsPerPage) {
                val item = summaries[summaryIndex]
                val s = item.student
                val isEven = (summaryIndex % 2 == 0)

                bgPaint.color = if (isEven) Color.rgb(248, 250, 252) else Color.WHITE
                canvas.drawRect(30f, currentY, (pageWidth - 30).toFloat(), currentY + rowHeight, bgPaint)
                canvas.drawLine(30f, currentY + rowHeight, (pageWidth - 30).toFloat(), currentY + rowHeight, borderPaint)

                val textY = currentY + 15f
                canvas.drawText("${summaryIndex + 1}", cNo, textY, rowTextPaint)
                canvas.drawText(s.grNoRollNo.ifEmpty { "-" }, cGr, textY, rowTextBold)
                canvas.drawText(truncateText(s.name.ifEmpty { "Student #${summaryIndex + 1}" }, 24), cName, textY, rowTextBold)
                canvas.drawText(truncateText(s.studentClass.ifEmpty { "-" }, 10), cClass, textY, rowTextPaint)
                canvas.drawText("${item.presentCount}", cPresent, textY, rowTextPaint)
                canvas.drawText("${item.absentCount}", cAbsent, textY, rowTextPaint)
                canvas.drawText("${item.leaveCount}", cLeave, textY, rowTextPaint)

                val pct = item.attendancePercentage
                val pctPaint = Paint().apply {
                    textSize = 8.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    color = when {
                        pct >= 75f -> Color.rgb(16, 185, 129)
                        pct >= 50f -> Color.rgb(245, 158, 11)
                        else -> Color.rgb(239, 68, 68)
                    }
                }
                canvas.drawText(String.format("%.1f%%", pct), cPct, textY, pctPaint)

                currentY += rowHeight
                summaryIndex++
                pageItemCount++
            }

            // Footer
            val footerPaint = Paint().apply {
                color = Color.rgb(148, 163, 184)
                textSize = 8.5f
                isAntiAlias = true
            }
            canvas.drawText("AttendanceMate Offline  •  Page $pageNumber of $totalPages", 30f, pageHeight - 25f, footerPaint)
            canvas.drawText("Generated on ${LocalDate.now()}", (pageWidth - 160).toFloat(), pageHeight - 25f, footerPaint)

            pdfDocument.finishPage(page)
        }

        return try {
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()
            val file = File(reportsDir, "Attendance_Monthly_${monthPrefix}.pdf")
            FileOutputStream(file).use { pdfDocument.writeTo(it) }
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    private fun drawStatusPill(canvas: Canvas, status: String, x: Float, y: Float) {
        val pillPaint = Paint().apply { isAntiAlias = true }
        val textPaint = Paint().apply {
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        when (status.uppercase()) {
            "PRESENT" -> {
                pillPaint.color = Color.rgb(220, 252, 231) // Green bg
                textPaint.color = Color.rgb(21, 128, 61) // Green text
                canvas.drawRoundRect(RectF(x, y, x + 54f, y + 16f), 4f, 4f, pillPaint)
                canvas.drawText("Present", x + 8f, y + 12f, textPaint)
            }
            "ABSENT" -> {
                pillPaint.color = Color.rgb(254, 226, 226) // Red bg
                textPaint.color = Color.rgb(185, 28, 28) // Red text
                canvas.drawRoundRect(RectF(x, y, x + 50f, y + 16f), 4f, 4f, pillPaint)
                canvas.drawText("Absent", x + 8f, y + 12f, textPaint)
            }
            "LEAVE" -> {
                pillPaint.color = Color.rgb(254, 243, 199) // Amber bg
                textPaint.color = Color.rgb(180, 83, 9) // Amber text
                canvas.drawRoundRect(RectF(x, y, x + 44f, y + 16f), 4f, 4f, pillPaint)
                canvas.drawText("Leave", x + 8f, y + 12f, textPaint)
            }
            else -> {
                pillPaint.color = Color.rgb(241, 245, 249)
                textPaint.color = Color.rgb(148, 163, 184)
                canvas.drawRoundRect(RectF(x, y, x + 40f, y + 16f), 4f, 4f, pillPaint)
                canvas.drawText("—", x + 16f, y + 12f, textPaint)
            }
        }
    }

    private fun truncateText(text: String, maxLength: Int): String {
        return if (text.length > maxLength) text.take(maxLength - 1) + "…" else text
    }

    // ==========================================
    // SHARING INTENT DISPATCHER
    // ==========================================

    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        try {
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share $title").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error sharing file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
