package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_records",
    indices = [
        Index(value = ["studentId", "date", "session"], unique = true),
        Index(value = ["date", "session"]),
        Index(value = ["date"])
    ]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val date: String, // Format: YYYY-MM-DD
    val session: String, // MORNING, EVENING
    val status: String, // PRESENT, ABSENT, LEAVE
    val notes: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
