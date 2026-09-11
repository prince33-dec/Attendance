package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.ui.components.StudentAvatar
import com.example.ui.theme.BirthdayAccent
import com.example.ui.theme.BirthdayBg
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusAbsent
import kotlinx.coroutines.launch

import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import java.time.LocalDate
import java.time.Period

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailBottomSheet(
    student: Student,
    onDismiss: () -> Unit,
    onEditClick: (Student) -> Unit,
    onDeleteClick: (Student) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showPhotoPreview by remember { mutableStateOf(false) }

    // Full photo preview dialog
    if (showPhotoPreview && !student.photoUri.isNullOrBlank()) {
        Dialog(onDismissRequest = { showPhotoPreview = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = student.name.ifEmpty { "Student Photo" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showPhotoPreview = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val imageModel: Any = remember(student.photoUri) {
                        val uriStr = student.photoUri
                        if (uriStr.startsWith("/")) {
                            java.io.File(uriStr)
                        } else if (uriStr.startsWith("file://")) {
                            java.io.File(Uri.parse(uriStr).path ?: uriStr)
                        } else {
                            Uri.parse(uriStr)
                        }
                    }

                    AsyncImage(
                        model = imageModel,
                        contentDescription = "Full photo of ${student.name}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = listOfNotNull(
                            student.studentClass.takeIf { it.isNotBlank() }?.let { "Class: $it" },
                            student.grNoRollNo.takeIf { it.isNotBlank() }?.let { "Roll No: $it" }
                        ).joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Student & All Records") },
            text = {
                Text(
                    "Are you sure you want to delete ${student.name.ifEmpty { "this student" }}?\n\n" +
                    "This will automatically and permanently delete all personal details, photo, and every attendance record associated with this student."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteClick(student)
                        onDismiss()
                    }
                ) {
                    Text("Delete Permanently", color = StatusAbsent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.90f)
        ) {
            // Header with avatar and actions
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = !student.photoUri.isNullOrBlank()) {
                                showPhotoPreview = true
                            }
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            StudentAvatar(
                                name = student.name,
                                photoUri = student.photoUri,
                                size = 68.dp
                            )
                            if (!student.photoUri.isNullOrBlank()) {
                                Surface(
                                    shape = CircleShape,
                                    color = PrimaryBlue,
                                    contentColor = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.size(22.dp),
                                    tonalElevation = 2.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "View Photo",
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = student.name.ifEmpty { "Unnamed Student" },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = listOfNotNull(
                                    student.studentClass.takeIf { it.isNotBlank() }?.let { "Class: $it" },
                                    student.grNoRollNo.takeIf { it.isNotBlank() }?.let { "Roll: $it" }
                                ).joinToString(" • ").ifEmpty { "Student Profile" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!student.photoUri.isNullOrBlank()) {
                                Text(
                                    text = "Tap photo to enlarge",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            // Body
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Academic & Sports
                item {
                    DetailCard(title = "Academic & Sports Details") {
                        DetailRow(icon = Icons.Default.School, label = "School / College", value = student.collegeOrSchool)
                        DetailRow(icon = Icons.Default.School, label = "Standard / Degree", value = student.standardOrDegree)
                        DetailRow(icon = Icons.Default.Badge, label = "Class / Division", value = student.studentClass)
                        DetailRow(icon = Icons.Default.Badge, label = "GR No. / Roll No", value = student.grNoRollNo)
                        DetailRow(icon = Icons.Default.SportsScore, label = "Game Category", value = student.gameCategory)
                    }
                }

                // Personal & Birth
                item {
                    val dobDisplay = remember(student.dob) {
                        if (student.dob.isNotBlank()) {
                            try {
                                val birthDate = LocalDate.parse(student.dob)
                                val age = Period.between(birthDate, LocalDate.now()).years
                                "${student.dob} (${age} yrs)"
                            } catch (_: Exception) {
                                student.dob
                            }
                        } else ""
                    }

                    DetailCard(title = "Personal Information") {
                        DetailRow(icon = Icons.Default.Cake, label = "Date of Birth (DOB)", value = dobDisplay)
                        DetailRow(icon = Icons.Default.Badge, label = "Gender", value = student.gender)
                    }
                }

                // Contact Details
                item {
                    DetailCard(title = "Contact Information") {
                        DetailRow(
                            icon = Icons.Default.Phone,
                            label = "Primary Phone",
                            value = student.phone1,
                            isPhone = true,
                            onPhoneClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${student.phone1}"))
                                context.startActivity(intent)
                            }
                        )
                        DetailRow(
                            icon = Icons.Default.Phone,
                            label = "Secondary Phone",
                            value = student.phone2,
                            isPhone = true,
                            onPhoneClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${student.phone2}"))
                                context.startActivity(intent)
                            }
                        )
                        DetailRow(icon = Icons.Default.Home, label = "Address", value = student.address)
                    }
                }

                // Banking & Identity
                item {
                    DetailCard(title = "Identification & Bank Details") {
                        DetailRow(icon = Icons.Default.Badge, label = "Aadhaar Card Number", value = student.aadhaarNumber)
                        DetailRow(icon = Icons.Default.AccountBalance, label = "Bank Name", value = student.bankName)
                        DetailRow(icon = Icons.Default.AccountBalance, label = "Account Number", value = student.bankAccountNumber)
                        DetailRow(icon = Icons.Default.AccountBalance, label = "Branch", value = student.bankBranch)
                        DetailRow(icon = Icons.Default.AccountBalance, label = "IFSC Code", value = student.ifscCode)
                    }
                }
            }

            // Bottom Actions: Edit and Delete
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusAbsent)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            scope.launch { sheetState.hide() }
                            onEditClick(student)
                        },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Details", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isPhone: Boolean = false,
    onPhoneClick: () -> Unit = {}
) {
    if (value.isBlank()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (isPhone) {
            IconButton(
                onClick = onPhoneClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(PrimaryBlueLight)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
