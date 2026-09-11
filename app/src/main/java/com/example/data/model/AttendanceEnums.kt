package com.example.data.model

enum class AttendanceSession(val displayName: String) {
    MORNING("Morning"),
    EVENING("Evening");

    companion object {
        fun fromString(value: String): AttendanceSession {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: MORNING
        }
    }
}

enum class AttendanceStatus(val displayName: String, val shortCode: String) {
    PRESENT("Present", "P"),
    ABSENT("Absent", "A"),
    LEAVE("Leave", "L");

    companion object {
        fun fromString(value: String?): AttendanceStatus? {
            if (value == null) return null
            return entries.firstOrNull { 
                it.name.equals(value, ignoreCase = true) || 
                it.displayName.equals(value, ignoreCase = true) ||
                it.shortCode.equals(value, ignoreCase = true)
            }
        }
    }
}
