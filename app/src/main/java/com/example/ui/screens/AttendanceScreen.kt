package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AttendanceSession
import com.example.data.model.AttendanceStatus
import com.example.data.model.StudentWithAttendance
import com.example.ui.components.AppDatePickerDialog
import com.example.ui.components.StudentAvatar
import com.example.ui.theme.EveningAccent
import com.example.ui.theme.EveningBg
import com.example.ui.theme.MorningAccent
import com.example.ui.theme.MorningBg
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusAbsentBg
import com.example.ui.theme.StatusAbsentDark
import com.example.ui.theme.StatusLeave
import com.example.ui.theme.StatusLeaveBg
import com.example.ui.theme.StatusLeaveDark
import com.example.ui.theme.StatusPresent
import com.example.ui.theme.StatusPresentBg
import com.example.ui.theme.StatusPresentDark
import com.example.ui.viewmodel.AttendanceViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val selectedSession by viewModel.selectedSession.collectAsStateWithLifecycle()
    val attendanceRoster by viewModel.attendanceRoster.collectAsStateWithLifecycle()
    val dateStats by viewModel.selectedDateStats.collectAsStateWithLifecycle()
    val searchQuery by viewModel.attendanceSearchQuery.collectAsStateWithLifecycle()
    val classFilter by viewModel.selectedClassFilter.collectAsStateWithLifecycle()
    val distinctClasses by viewModel.distinctClasses.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }

    val parsedDate = try {
        LocalDate.parse(selectedDate)
    } catch (_: Exception) {
        LocalDate.now()
    }

    val isToday = parsedDate == LocalDate.now()
    val currentStats = if (selectedSession == AttendanceSession.MORNING) dateStats.first else dateStats.second

    if (showDatePicker) {
        AppDatePickerDialog(
            initialDate = parsedDate,
            onDateSelected = { newDate ->
                viewModel.setSelectedDate(newDate.toString())
            },
            onDismiss = { showDatePicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("attendance_screen")
    ) {
        // TOP DATE BAR
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
                // Date navigation row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            viewModel.setSelectedDate(parsedDate.minusDays(1).toString())
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Day",
                            tint = Slate700
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clickable { showDatePicker = true }
                            .padding(horizontal = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Pick Date",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = (if (isToday) "Today • " else "") + parsedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            viewModel.setSelectedDate(parsedDate.plusDays(1).toString())
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Day",
                            tint = Slate700
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // SESSION TABS: MORNING vs EVENING
                TabRow(
                    selectedTabIndex = if (selectedSession == AttendanceSession.MORNING) 0 else 1,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedSession == AttendanceSession.MORNING,
                        onClick = { viewModel.setSelectedSession(AttendanceSession.MORNING) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = null,
                                    tint = if (selectedSession == AttendanceSession.MORNING) MorningAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Morning Session",
                                    fontWeight = if (selectedSession == AttendanceSession.MORNING) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedSession == AttendanceSession.MORNING) MorningAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )

                    Tab(
                        selected = selectedSession == AttendanceSession.EVENING,
                        onClick = { viewModel.setSelectedSession(AttendanceSession.EVENING) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DarkMode,
                                    contentDescription = null,
                                    tint = if (selectedSession == AttendanceSession.EVENING) EveningAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Evening Session",
                                    fontWeight = if (selectedSession == AttendanceSession.EVENING) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedSession == AttendanceSession.EVENING) EveningAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // LIVE STATS COUNTER ROW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MiniCounterChip("Total", "${currentStats.total}", MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
                    MiniCounterChip("Present", "${currentStats.present}", StatusPresentDark, StatusPresentBg)
                    MiniCounterChip("Absent", "${currentStats.absent}", StatusAbsentDark, StatusAbsentBg)
                    MiniCounterChip("Leave", "${currentStats.leave}", StatusLeaveDark, StatusLeaveBg)
                    MiniCounterChip("Pending", "${currentStats.unrecorded}", MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // QUICK MARK ALL ACTIONS ROW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.markAllInSession(AttendanceStatus.PRESENT) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = null,
                            tint = StatusPresent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("All Present", fontSize = 12.sp, color = StatusPresentDark, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.markAllInSession(AttendanceStatus.ABSENT) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = StatusAbsent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("All Absent", fontSize = 12.sp, color = StatusAbsentDark, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.clearCurrentSession() },
                        modifier = Modifier.weight(0.8f),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                            tint = Slate500,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear", fontSize = 12.sp, color = Slate600)
                    }
                }
            }
        }

        // SEARCH & FILTER BAR
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setAttendanceSearchQuery(it) },
                placeholder = { Text("Search by name, roll no, or class...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Slate400,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setAttendanceSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = Slate400)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = Slate200,
                    focusedBorderColor = PrimaryBlue
                )
            )

            // Class filter chips
            if (distinctClasses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = classFilter == null,
                            onClick = { viewModel.setSelectedClassFilter(null) },
                            label = { Text("All", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlueLight,
                                selectedLabelColor = PrimaryBlue
                            )
                        )
                    }
                    items(distinctClasses) { cls ->
                        FilterChip(
                            selected = classFilter == cls,
                            onClick = {
                                if (classFilter == cls) viewModel.setSelectedClassFilter(null)
                                else viewModel.setSelectedClassFilter(cls)
                            },
                            label = { Text(cls, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlueLight,
                                selectedLabelColor = PrimaryBlue
                            )
                        )
                    }
                }
            }
        }

        // STUDENT ATTENDANCE ROSTER
        if (attendanceRoster.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No Students Found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate600
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try clearing search or register students in the Students tab.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = attendanceRoster,
                    key = { it.student.id }
                ) { item ->
                    val record = if (selectedSession == AttendanceSession.MORNING) {
                        item.morningRecord
                    } else {
                        item.eveningRecord
                    }

                    StudentAttendanceItem(
                        studentWithAttendance = item,
                        currentStatus = record?.status,
                        onStatusSelected = { status ->
                            viewModel.setAttendanceStatus(item.student.id, status)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StudentAttendanceItem(
    studentWithAttendance: StudentWithAttendance,
    currentStatus: String?,
    onStatusSelected: (AttendanceStatus) -> Unit
) {
    val student = studentWithAttendance.student

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudentAvatar(
                name = student.name,
                photoUri = student.photoUri,
                size = 44.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name.ifEmpty { "Unnamed Student" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (student.grNoRollNo.isNotBlank()) {
                        Text(
                            text = "Roll: ${student.grNoRollNo}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue
                        )
                        Text(text = " • ", fontSize = 11.sp, color = Slate400)
                    }

                    if (student.studentClass.isNotBlank()) {
                        Text(
                            text = student.studentClass,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (student.gameCategory.isNotBlank()) {
                        Text(text = " • ", fontSize = 11.sp, color = Slate400)
                        Text(
                            text = student.gameCategory,
                            fontSize = 11.sp,
                            color = SecondaryTeal,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 3-WAY STATUS BUTTONS: [ P ] [ A ] [ L ]
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Present Button (P)
                StatusButton(
                    label = "P",
                    isSelected = currentStatus.equals("PRESENT", ignoreCase = true),
                    activeColor = StatusPresent,
                    activeBg = StatusPresentBg,
                    onClick = { onStatusSelected(AttendanceStatus.PRESENT) }
                )

                // Absent Button (A)
                StatusButton(
                    label = "A",
                    isSelected = currentStatus.equals("ABSENT", ignoreCase = true),
                    activeColor = StatusAbsent,
                    activeBg = StatusAbsentBg,
                    onClick = { onStatusSelected(AttendanceStatus.ABSENT) }
                )

                // Leave Button (L)
                StatusButton(
                    label = "L",
                    isSelected = currentStatus.equals("LEAVE", ignoreCase = true),
                    activeColor = StatusLeave,
                    activeBg = StatusLeaveBg,
                    onClick = { onStatusSelected(AttendanceStatus.LEAVE) }
                )
            }
        }
    }
}

@Composable
fun StatusButton(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    activeBg: Color,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant,
        label = "status_btn_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "status_btn_text"
    )

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = textColor
        )
    }
}

@Composable
fun MiniCounterChip(
    label: String,
    count: String,
    textColor: Color,
    bgColor: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                fontSize = 11.sp,
                color = textColor.copy(alpha = 0.8f)
            )
            Text(
                text = count,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
