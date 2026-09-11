package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MonthlyStudentSummary
import com.example.data.model.SessionStats
import com.example.data.model.StudentWithAttendance
import com.example.ui.components.AppDatePickerDialog
import com.example.ui.components.StatusBadge
import com.example.ui.components.StudentAvatar
import com.example.ui.theme.EveningAccent
import com.example.ui.theme.EveningBg
import com.example.ui.theme.MorningAccent
import com.example.ui.theme.MorningBg
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusAbsentBg
import com.example.ui.theme.StatusLeave
import com.example.ui.theme.StatusLeaveBg
import com.example.ui.theme.StatusPresent
import com.example.ui.theme.StatusPresentBg
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.ui.viewmodel.ReportTab
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun ReportsScreen(
    viewModel: AttendanceViewModel
) {
    val context = LocalContext.current
    val reportTab by viewModel.reportTab.collectAsStateWithLifecycle()
    val selectedReportDate by viewModel.selectedReportDate.collectAsStateWithLifecycle()
    val selectedReportMonth by viewModel.selectedReportMonth.collectAsStateWithLifecycle()

    val dateWiseList by viewModel.dateWiseReportList.collectAsStateWithLifecycle()
    val dateWiseStats by viewModel.dateWiseReportStats.collectAsStateWithLifecycle()
    val monthlyList by viewModel.monthlyReportList.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }

    val parsedReportDate = try {
        LocalDate.parse(selectedReportDate)
    } catch (_: Exception) {
        LocalDate.now()
    }

    val parsedMonth = try {
        YearMonth.parse(selectedReportMonth)
    } catch (_: Exception) {
        YearMonth.now()
    }

    if (showDatePicker) {
        AppDatePickerDialog(
            initialDate = parsedReportDate,
            onDateSelected = { newDate ->
                viewModel.setSelectedReportDate(newDate.toString())
            },
            onDismiss = { showDatePicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen")
    ) {
        // TOP HEADER
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Reports & Analytics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Date-wise and Month-wise reports with Excel and PDF export",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )

                Spacer(modifier = Modifier.height(12.dp))

                // SUB TABS: DATE-WISE vs MONTH-WISE
                TabRow(
                    selectedTabIndex = if (reportTab == ReportTab.DATE_WISE) 0 else 1,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = reportTab == ReportTab.DATE_WISE,
                        onClick = { viewModel.setReportTab(ReportTab.DATE_WISE) },
                        text = {
                            Text(
                                text = "Date-wise Report",
                                fontWeight = if (reportTab == ReportTab.DATE_WISE) FontWeight.Bold else FontWeight.Normal,
                                color = if (reportTab == ReportTab.DATE_WISE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    Tab(
                        selected = reportTab == ReportTab.MONTH_WISE,
                        onClick = { viewModel.setReportTab(ReportTab.MONTH_WISE) },
                        text = {
                            Text(
                                text = "Month-wise Report",
                                fontWeight = if (reportTab == ReportTab.MONTH_WISE) FontWeight.Bold else FontWeight.Normal,
                                color = if (reportTab == ReportTab.MONTH_WISE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
        }

        if (reportTab == ReportTab.DATE_WISE) {
            DateWiseReportContent(
                selectedDate = parsedReportDate,
                onPreviousDay = { viewModel.setSelectedReportDate(parsedReportDate.minusDays(1).toString()) },
                onNextDay = { viewModel.setSelectedReportDate(parsedReportDate.plusDays(1).toString()) },
                onPickDate = { showDatePicker = true },
                morningStats = dateWiseStats.first,
                eveningStats = dateWiseStats.second,
                roster = dateWiseList,
                onExportExcel = { viewModel.exportDateWiseExcel(context) },
                onExportPdf = { viewModel.exportDateWisePdf(context) }
            )
        } else {
            MonthWiseReportContent(
                selectedMonth = parsedMonth,
                onPreviousMonth = { viewModel.setSelectedReportMonth(parsedMonth.minusMonths(1).toString()) },
                onNextMonth = { viewModel.setSelectedReportMonth(parsedMonth.plusMonths(1).toString()) },
                summaries = monthlyList,
                onExportExcel = { viewModel.exportMonthWiseExcel(context) },
                onExportPdf = { viewModel.exportMonthWisePdf(context) }
            )
        }
    }
}

@Composable
fun DateWiseReportContent(
    selectedDate: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onPickDate: () -> Unit,
    morningStats: SessionStats,
    eveningStats: SessionStats,
    roster: List<StudentWithAttendance>,
    onExportExcel: () -> Unit,
    onExportPdf: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Date Navigation Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPreviousDay) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day")
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onPickDate)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    IconButton(onClick = onNextDay) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day")
                    }
                }
            }
        }

        // Export Buttons Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onExportExcel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal)
                ) {
                    Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export Excel (.xlsx)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = onExportPdf,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Session Overview Summary Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Morning mini card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.WbSunny, contentDescription = null, tint = MorningAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Morning", fontWeight = FontWeight.Bold, color = MorningAccent, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = String.format("%.0f%% Present", morningStats.presentPercentage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "P: ${morningStats.present}  •  A: ${morningStats.absent}  •  L: ${morningStats.leave}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Evening mini card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.DarkMode, contentDescription = null, tint = EveningAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Evening", fontWeight = FontWeight.Bold, color = EveningAccent, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = String.format("%.0f%% Present", eveningStats.presentPercentage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "P: ${eveningStats.present}  •  A: ${eveningStats.absent}  •  L: ${eveningStats.leave}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Student Roster Section Title
        item {
            Text(
                text = "Student Attendance Details (${roster.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Student rows
        items(roster) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StudentAvatar(name = item.student.name, photoUri = item.student.photoUri, size = 40.dp)

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.student.name.ifEmpty { "Student" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = listOfNotNull(
                                item.student.grNoRollNo.takeIf { it.isNotBlank() }?.let { "Roll: $it" },
                                item.student.studentClass.takeIf { it.isNotBlank() }
                            ).joinToString(" • ").ifEmpty { "Registered" },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Badges for Morning & Evening
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "M: ", fontSize = 11.sp, color = MorningAccent, fontWeight = FontWeight.Bold)
                            StatusBadge(status = item.morningRecord?.status)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "E: ", fontSize = 11.sp, color = EveningAccent, fontWeight = FontWeight.Bold)
                            StatusBadge(status = item.eveningRecord?.status)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthWiseReportContent(
    selectedMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    summaries: List<MonthlyStudentSummary>,
    onExportExcel: () -> Unit,
    onExportPdf: () -> Unit
) {
    val totalSessions = summaries.firstOrNull()?.totalSessions ?: 0
    val avgAttendance = if (summaries.isNotEmpty()) {
        summaries.map { it.attendancePercentage }.average().toFloat()
    } else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Month Navigation Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPreviousMonth) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                    }

                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    IconButton(onClick = onNextMonth) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                    }
                }
            }
        }

        // Export Buttons Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onExportExcel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal)
                ) {
                    Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export Excel (.xlsx)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = onExportPdf,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Monthly Stats Overview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Monthly Performance Summary",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total Sessions", fontSize = 11.sp, color = Slate500)
                            Text(text = "$totalSessions Conducted", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Column {
                            Text(text = "Students", fontSize = 11.sp, color = Slate500)
                            Text(text = "${summaries.size} Enrolled", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Column {
                            Text(text = "Avg Attendance", fontSize = 11.sp, color = Slate500)
                            Text(
                                text = String.format("%.1f%%", avgAttendance),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (avgAttendance >= 75f) StatusPresent else StatusLeave
                            )
                        }
                    }
                }
            }
        }

        // Student Rankings
        item {
            Text(
                text = "Student Monthly Attendance Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (summaries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No records found for this month.", color = Slate500)
                }
            }
        } else {
            items(summaries) { summary ->
                MonthlyStudentCard(summary = summary)
            }
        }
    }
}

@Composable
fun MonthlyStudentCard(summary: MonthlyStudentSummary) {
    val pct = summary.attendancePercentage
    val pctColor = when {
        pct >= 75f -> StatusPresent
        pct >= 50f -> StatusLeave
        else -> StatusAbsent
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StudentAvatar(
                        name = summary.student.name,
                        photoUri = summary.student.photoUri,
                        size = 42.dp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = summary.student.name.ifEmpty { "Student" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = listOfNotNull(
                                summary.student.grNoRollNo.takeIf { it.isNotBlank() }?.let { "Roll: $it" },
                                summary.student.studentClass.takeIf { it.isNotBlank() }
                            ).joinToString(" • ").ifEmpty { "Registered" },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = pctColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = String.format("%.1f%%", pct),
                        fontWeight = FontWeight.Bold,
                        color = pctColor,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { if (summary.totalSessions > 0) summary.presentCount.toFloat() / summary.totalSessions else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = pctColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Present: ${summary.presentCount}",
                    fontSize = 11.sp,
                    color = StatusPresent,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Absent: ${summary.absentCount}",
                    fontSize = 11.sp,
                    color = StatusAbsent,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Leave: ${summary.leaveCount}",
                    fontSize = 11.sp,
                    color = StatusLeave,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "M: ${summary.morningPresent} | E: ${summary.eveningPresent}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
