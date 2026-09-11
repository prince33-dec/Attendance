package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AttendanceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records WHERE date = :date")
    fun getRecordsForDate(date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date = :date AND session = :session")
    fun getRecordsForDateAndSession(date: String, session: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date LIKE :monthPrefix || '%'")
    fun getRecordsForMonth(monthPrefix: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date LIKE :monthPrefix || '%'")
    suspend fun getRecordsForMonthDirect(monthPrefix: String): List<AttendanceRecord>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY date DESC")
    fun getRecordsForStudent(studentId: Long): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: AttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(records: List<AttendanceRecord>)

    @Query("DELETE FROM attendance_records WHERE studentId = :studentId AND date = :date AND session = :session")
    suspend fun deleteRecord(studentId: Long, date: String, session: String)

    @Query("DELETE FROM attendance_records WHERE date = :date AND session = :session")
    suspend fun clearSessionRecords(date: String, session: String)

    @Query("SELECT COUNT(*) FROM attendance_records WHERE date = :date AND session = :session")
    fun getSessionRecordCount(date: String, session: String): Flow<Int>

    @Query("DELETE FROM attendance_records WHERE studentId = :studentId")
    suspend fun deleteRecordsForStudent(studentId: Long)

    @Query("DELETE FROM attendance_records WHERE studentId NOT IN (SELECT id FROM students)")
    suspend fun deleteOrphanedRecords()
}
