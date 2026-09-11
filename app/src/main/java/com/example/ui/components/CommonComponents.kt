package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AttendanceSession
import com.example.data.model.AttendanceStatus
import com.example.ui.theme.EveningAccent
import com.example.ui.theme.EveningBg
import com.example.ui.theme.MorningAccent
import com.example.ui.theme.MorningBg
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusAbsentBg
import com.example.ui.theme.StatusLeave
import com.example.ui.theme.StatusLeaveBg
import com.example.ui.theme.StatusPresent
import com.example.ui.theme.StatusPresentBg
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun StudentAvatar(
    name: String,
    photoUri: String?,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    if (!photoUri.isNullOrBlank()) {
        val imageModel: Any = if (photoUri.startsWith("/")) {
            java.io.File(photoUri)
        } else if (photoUri.startsWith("file://")) {
            java.io.File(Uri.parse(photoUri).path ?: photoUri)
        } else {
            Uri.parse(photoUri)
        }

        AsyncImage(
            model = imageModel,
            contentDescription = "Photo of $name",
            modifier = modifier
                .size(size)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        val initials = name.trim().split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
            .ifEmpty { "S" }

        val gradient = Brush.linearGradient(
            colors = listOf(PrimaryBlue, SecondaryTeal)
        )

        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontSize = (size.value * 0.4f).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatusBadge(
    status: String?,
    modifier: Modifier = Modifier
) {
    val (bg, textColor, label, icon) = when (status?.uppercase()) {
        "PRESENT" -> Quad(StatusPresentBg, StatusPresent, "Present", Icons.Default.Check)
        "ABSENT" -> Quad(StatusAbsentBg, StatusAbsent, "Absent", Icons.Default.Close)
        "LEAVE" -> Quad(StatusLeaveBg, StatusLeave, "Leave", Icons.Default.HourglassEmpty)
        else -> Quad(MaterialTheme.colorScheme.surfaceVariant, Slate400, "Unrecorded", null)
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(12.dp)
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(4.dp))
            }
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SessionPill(
    session: AttendanceSession,
    modifier: Modifier = Modifier
) {
    val isMorning = session == AttendanceSession.MORNING
    val bg = if (isMorning) MorningBg else EveningBg
    val tint = if (isMorning) MorningAccent else EveningAccent
    val icon = if (isMorning) Icons.Default.WbSunny else Icons.Default.DarkMode

    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = session.displayName,
                color = tint,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val selected = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                    onDateSelected(selected)
                }
                onDismiss()
            }) {
                Text("Select", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
