package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import coil.compose.AsyncImage
import com.example.data.model.Student
import com.example.ui.components.AppDatePickerDialog
import com.example.ui.components.StudentAvatar
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusAbsent
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFormBottomSheet(
    studentToEdit: Student? = null,
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(studentToEdit?.name ?: "") }
    var dob by remember { mutableStateOf(studentToEdit?.dob ?: "") }
    var gender by remember { mutableStateOf(studentToEdit?.gender ?: "Male") }
    var phone1 by remember { mutableStateOf(studentToEdit?.phone1 ?: "") }
    var phone2 by remember { mutableStateOf(studentToEdit?.phone2 ?: "") }
    var collegeOrSchool by remember { mutableStateOf(studentToEdit?.collegeOrSchool ?: "") }
    var standardOrDegree by remember { mutableStateOf(studentToEdit?.standardOrDegree ?: "") }
    var studentClass by remember { mutableStateOf(studentToEdit?.studentClass ?: "") }
    var grNoRollNo by remember { mutableStateOf(studentToEdit?.grNoRollNo ?: "") }
    var gameCategory by remember { mutableStateOf(studentToEdit?.gameCategory ?: "") }
    var address by remember { mutableStateOf(studentToEdit?.address ?: "") }
    var photoUri by remember { mutableStateOf(studentToEdit?.photoUri) }
    var aadhaarNumber by remember { mutableStateOf(studentToEdit?.aadhaarNumber ?: "") }
    var bankAccountNumber by remember { mutableStateOf(studentToEdit?.bankAccountNumber ?: "") }
    var bankName by remember { mutableStateOf(studentToEdit?.bankName ?: "") }
    var bankBranch by remember { mutableStateOf(studentToEdit?.bankBranch ?: "") }
    var ifscCode by remember { mutableStateOf(studentToEdit?.ifscCode ?: "") }

    var showDobPicker by remember { mutableStateOf(false) }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = saveImageToInternalStorage(context, uri)
            photoUri = savedPath
        }
    }

    if (showDobPicker) {
        val initialDate = try {
            if (dob.isNotBlank()) LocalDate.parse(dob) else LocalDate.now().minusYears(15)
        } catch (_: Exception) {
            LocalDate.now().minusYears(15)
        }

        AppDatePickerDialog(
            initialDate = initialDate,
            onDateSelected = { selected ->
                dob = selected.toString()
            },
            onDismiss = { showDobPicker = false }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            // Header
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
                    Column {
                        Text(
                            text = if (studentToEdit != null) "Edit Student Details" else "Add New Student",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "All fields are optional",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = {
                        scope.launch { sheetState.hide() }
                        onDismiss()
                    }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            // Scrollable Form Body
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // PHOTO UPLOAD SECTION
                item {
                    FormSection(title = "Student Photo") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (!photoUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = Uri.parse(photoUri),
                                    contentDescription = "Selected Photo",
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryBlueLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Button(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                ) {
                                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (photoUri == null) "Upload Photo" else "Change Photo", fontSize = 13.sp)
                                }

                                if (photoUri != null) {
                                    TextButton(
                                        onClick = { photoUri = null },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = StatusAbsent, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Remove", color = StatusAbsent, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // BASIC DETAILS SECTION
                item {
                    FormSection(title = "Basic Information") {
                        AppFormField(
                            value = name,
                            onValueChange = { name = it },
                            label = "Student Full Name",
                            placeholder = "e.g. Aarav Sharma",
                            leadingIcon = Icons.Default.Person
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // DOB Field with date picker
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDobPicker = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Cake,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = "Date of Birth (DOB)", fontSize = 11.sp, color = Slate500)
                                        Text(
                                            text = if (dob.isNotBlank()) dob else "Select DOB (for birthday reminder)",
                                            fontSize = 14.sp,
                                            fontWeight = if (dob.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                                            color = if (dob.isNotBlank()) MaterialTheme.colorScheme.onSurface else Slate400
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = PrimaryBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Gender Selection
                        Column {
                            Text(text = "Gender", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Male", "Female", "Other").forEach { g ->
                                    FilterChip(
                                        selected = gender.equals(g, ignoreCase = true),
                                        onClick = { gender = g },
                                        label = { Text(g, fontSize = 13.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PrimaryBlueLight,
                                            selectedLabelColor = PrimaryBlue
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // CONTACT DETAILS
                item {
                    FormSection(title = "Contact Information") {
                        AppFormField(
                            value = phone1,
                            onValueChange = { phone1 = it },
                            label = "Primary Phone Number",
                            placeholder = "e.g. +91 9876543210",
                            leadingIcon = Icons.Default.Phone
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        AppFormField(
                            value = phone2,
                            onValueChange = { phone2 = it },
                            label = "Secondary Phone Number (Alternative)",
                            placeholder = "e.g. +91 9876543211",
                            leadingIcon = Icons.Default.Phone
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        AppFormField(
                            value = address,
                            onValueChange = { address = it },
                            label = "Address",
                            placeholder = "e.g. Flat 204, Green Heights, City",
                            leadingIcon = Icons.Default.Home,
                            singleLine = false
                        )
                    }
                }

                // ACADEMIC & SPORTS DETAILS
                item {
                    FormSection(title = "Academic & Sports Details") {
                        AppFormField(
                            value = collegeOrSchool,
                            onValueChange = { collegeOrSchool = it },
                            label = "College / School Name",
                            placeholder = "e.g. St. Xavier's High School",
                            leadingIcon = Icons.Default.School
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                AppFormField(
                                    value = standardOrDegree,
                                    onValueChange = { standardOrDegree = it },
                                    label = "Standard / Degree",
                                    placeholder = "e.g. 11th Science",
                                    leadingIcon = Icons.Default.School
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                AppFormField(
                                    value = studentClass,
                                    onValueChange = { studentClass = it },
                                    label = "Class / Section",
                                    placeholder = "e.g. 11-A",
                                    leadingIcon = Icons.Default.Badge
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                AppFormField(
                                    value = grNoRollNo,
                                    onValueChange = { grNoRollNo = it },
                                    label = "GR No. / Roll No",
                                    placeholder = "e.g. 101",
                                    leadingIcon = Icons.Default.Badge
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                AppFormField(
                                    value = gameCategory,
                                    onValueChange = { gameCategory = it },
                                    label = "Game Category",
                                    placeholder = "e.g. Cricket, Football",
                                    leadingIcon = Icons.Default.SportsScore
                                )
                            }
                        }
                    }
                }

                // IDENTIFICATION & BANKING DETAILS
                item {
                    FormSection(title = "Identification & Bank Details") {
                        AppFormField(
                            value = aadhaarNumber,
                            onValueChange = { aadhaarNumber = it },
                            label = "Aadhaar Card Number",
                            placeholder = "e.g. 1234 5678 9012",
                            leadingIcon = Icons.Default.Badge
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        AppFormField(
                            value = bankAccountNumber,
                            onValueChange = { bankAccountNumber = it },
                            label = "Bank Account Number",
                            placeholder = "e.g. 10293847561",
                            leadingIcon = Icons.Default.AccountBalance
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        AppFormField(
                            value = bankName,
                            onValueChange = { bankName = it },
                            label = "Bank Name",
                            placeholder = "e.g. State Bank of India",
                            leadingIcon = Icons.Default.AccountBalance
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                AppFormField(
                                    value = bankBranch,
                                    onValueChange = { bankBranch = it },
                                    label = "Bank Branch",
                                    placeholder = "e.g. Main Branch",
                                    leadingIcon = Icons.Default.AccountBalance
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                AppFormField(
                                    value = ifscCode,
                                    onValueChange = { ifscCode = it.uppercase() },
                                    label = "IFSC Code",
                                    placeholder = "e.g. SBIN0001234",
                                    leadingIcon = Icons.Default.AccountBalance
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Save Button Bar
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
                        onClick = {
                            scope.launch { sheetState.hide() }
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val student = (studentToEdit ?: Student()).copy(
                                name = name.trim().ifEmpty { "Student" },
                                dob = dob.trim(),
                                gender = gender,
                                phone1 = phone1.trim(),
                                phone2 = phone2.trim(),
                                collegeOrSchool = collegeOrSchool.trim(),
                                standardOrDegree = standardOrDegree.trim(),
                                studentClass = studentClass.trim(),
                                grNoRollNo = grNoRollNo.trim(),
                                gameCategory = gameCategory.trim(),
                                address = address.trim(),
                                photoUri = photoUri,
                                aadhaarNumber = aadhaarNumber.trim(),
                                bankAccountNumber = bankAccountNumber.trim(),
                                bankName = bankName.trim(),
                                bankBranch = bankBranch.trim(),
                                ifscCode = ifscCode.trim()
                            )
                            onSave(student)
                            scope.launch { sheetState.hide() }
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Student", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FormSection(
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
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun AppFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        placeholder = { Text(placeholder, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = singleLine,
        maxLines = if (singleLine) 1 else 3,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedBorderColor = MaterialTheme.colorScheme.primary
        )
    )
}

private fun saveImageToInternalStorage(context: Context, sourceUri: Uri): String? {
    return try {
        val photosDir = File(context.filesDir, "student_photos")
        if (!photosDir.exists()) photosDir.mkdirs()

        val destFile = File(photosDir, "photo_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        destFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
