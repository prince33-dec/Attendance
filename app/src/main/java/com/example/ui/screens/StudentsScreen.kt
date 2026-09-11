package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import com.example.data.model.Student
import com.example.ui.components.StudentAvatar
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.viewmodel.AttendanceViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun StudentsScreen(
    viewModel: AttendanceViewModel,
    onAddStudentClick: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        focusRequester.requestFocus()
        focusManager.clearFocus()
    }

    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val distinctClasses by viewModel.distinctClasses.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf<String?>(null) }

    var studentToEdit by remember { mutableStateOf<Student?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isTablet = adaptiveInfo.windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT
    val navigator = rememberListDetailPaneScaffoldNavigator<Student>()

    // Filter students
    val filteredStudents = allStudents.filter { s ->
        val matchesQuery = searchQuery.isBlank() ||
                s.name.contains(searchQuery, ignoreCase = true) ||
                s.grNoRollNo.contains(searchQuery, ignoreCase = true) ||
                s.studentClass.contains(searchQuery, ignoreCase = true) ||
                s.gameCategory.contains(searchQuery, ignoreCase = true) ||
                s.phone1.contains(searchQuery, ignoreCase = true)

        val matchesClass = selectedClass.isNullOrBlank() || s.studentClass.equals(selectedClass, ignoreCase = true)

        matchesQuery && matchesClass
    }

    if (studentToEdit != null) {
        StudentFormBottomSheet(
            studentToEdit = studentToEdit,
            onDismiss = { studentToEdit = null },
            onSave = { updatedStudent ->
                viewModel.saveStudent(updatedStudent)
                studentToEdit = null
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .focusRequester(focusRequester)
            .focusable(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddStudentClick,
                containerColor = PrimaryBlue,
                contentColor = androidx.compose.ui.graphics.Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_student_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Student")
            }
        }
    ) { innerPadding ->
        ListDetailPaneScaffold(
            modifier = Modifier.padding(innerPadding).testTag("students_screen"),
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                AnimatedPane(modifier = Modifier.fillMaxSize()) {
                    StudentListPane(
                        students = filteredStudents,
                        allStudentsCount = allStudents.size,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        selectedClass = selectedClass,
                        onClassSelect = { selectedClass = it },
                        distinctClasses = distinctClasses,
                        onStudentClick = { student ->
                            coroutineScope.launch {
                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, student)
                            }
                        },
                        onCallClick = { phone ->
                            if (phone.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            },
            detailPane = {
                AnimatedPane(modifier = Modifier.fillMaxSize()) {
                    val student = navigator.currentDestination?.contentKey
                    if (student != null) {
                        StudentDetailPane(
                            student = student,
                            onEditClick = { s -> studentToEdit = s },
                            onDeleteClick = { s -> 
                                viewModel.deleteStudent(s)
                                coroutineScope.launch {
                                    navigator.navigateBack()
                                }
                            },
                            onBackClick = { 
                                coroutineScope.launch {
                                    navigator.navigateBack()
                                }
                            },
                            showBack = !isTablet
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Select a student to view details", color = Slate500)
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun StudentListPane(
    students: List<Student>,
    allStudentsCount: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedClass: String?,
    onClassSelect: (String?) -> Unit,
    distinctClasses: List<String>,
    onStudentClick: (Student) -> Unit,
    onCallClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Class filter header
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Student Directory",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${students.size} of $allStudentsCount students",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search by name, roll, class, game, phone...", fontSize = 13.sp) },
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
                            IconButton(onClick = { onSearchQueryChange("") }) {
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

                if (distinctClasses.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedClass == null,
                                onClick = { onClassSelect(null) },
                                label = { Text("All", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlueLight,
                                    selectedLabelColor = PrimaryBlue
                                )
                            )
                        }
                        items(distinctClasses) { cls ->
                            FilterChip(
                                selected = selectedClass == cls,
                                onClick = {
                                    onClassSelect(if (selectedClass == cls) null else cls)
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
        }

        // Student Cards List
        if (students.isEmpty()) {
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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap the + button to add a new student.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = students,
                    key = { it.id }
                ) { student ->
                    StudentCardItem(
                        student = student,
                        onClick = { onStudentClick(student) },
                        onCallClick = onCallClick
                    )
                }
            }
        }
    }
}

@Composable
fun StudentDetailPane(
    student: Student,
    onEditClick: (Student) -> Unit,
    onDeleteClick: (Student) -> Unit,
    onBackClick: () -> Unit,
    showBack: Boolean
) {
    // We can reuse parts of StudentDetailBottomSheet or just implement it here
    // For now, let's just show a simple detail view
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        if (showBack) {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.Default.Clear, contentDescription = "Back")
            }
        }
        
        // Simplified Detail View for demonstration
        StudentDetailContent(
            student = student,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick
        )
    }
}

@Composable
fun StudentDetailContent(
    student: Student,
    onEditClick: (Student) -> Unit,
    onDeleteClick: (Student) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StudentAvatar(name = student.name, photoUri = student.photoUri, size = 120.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = student.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(text = "Class: ${student.studentClass}", style = MaterialTheme.typography.bodyLarge, color = Slate500)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            androidx.compose.material3.Button(onClick = { onEditClick(student) }) {
                Text("Edit")
            }
            androidx.compose.material3.OutlinedButton(onClick = { onDeleteClick(student) }) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun StudentCardItem(
    student: Student,
    onClick: () -> Unit,
    onCallClick: (String) -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudentAvatar(name = student.name, photoUri = student.photoUri, size = 48.dp)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name.ifEmpty { "Unnamed Student" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (student.grNoRollNo.isNotBlank()) {
                        Text(
                            text = "Roll: ${student.grNoRollNo}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue
                        )
                    }

                    if (student.studentClass.isNotBlank()) {
                        if (student.grNoRollNo.isNotBlank()) {
                            Text(text = " • ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = student.studentClass,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (student.gameCategory.isNotBlank()) {
                        Text(text = " • ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = student.gameCategory,
                            fontSize = 12.sp,
                            color = SecondaryTeal,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (student.collegeOrSchool.isNotBlank()) {
                    Text(
                        text = student.collegeOrSchool,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            if (student.phone1.isNotBlank()) {
                IconButton(
                    onClick = { onCallClick(student.phone1) },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(PrimaryBlueLight)
                        .size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call ${student.name}",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
