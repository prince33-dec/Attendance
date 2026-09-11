package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY CASE WHEN grNoRollNo = '' THEN 999999 ELSE CAST(grNoRollNo AS INTEGER) END ASC, name ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    fun getStudentById(id: Long): Flow<Student?>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentByIdDirect(id: Long): Student?

    @Query("""
        SELECT * FROM students 
        WHERE name LIKE '%' || :query || '%' 
           OR grNoRollNo LIKE '%' || :query || '%' 
           OR studentClass LIKE '%' || :query || '%' 
           OR gameCategory LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchStudents(query: String): Flow<List<Student>>

    @Query("SELECT DISTINCT studentClass FROM students WHERE studentClass != '' ORDER BY studentClass ASC")
    fun getDistinctClasses(): Flow<List<String>>

    @Query("SELECT DISTINCT gameCategory FROM students WHERE gameCategory != '' ORDER BY gameCategory ASC")
    fun getDistinctGameCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudentById(id: Long)

    @Query("SELECT COUNT(*) FROM students")
    fun getStudentCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM students")
    suspend fun getStudentCountDirect(): Int
}
