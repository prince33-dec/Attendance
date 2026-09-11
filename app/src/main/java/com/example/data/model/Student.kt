package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val dob: String = "", // Format: YYYY-MM-DD
    val gender: String = "", // Male, Female, Other
    val phone1: String = "",
    val phone2: String = "",
    val collegeOrSchool: String = "",
    val standardOrDegree: String = "",
    val studentClass: String = "",
    val grNoRollNo: String = "",
    val gameCategory: String = "",
    val address: String = "",
    val photoUri: String? = null,
    val aadhaarNumber: String = "",
    val bankAccountNumber: String = "",
    val bankName: String = "",
    val bankBranch: String = "",
    val ifscCode: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
