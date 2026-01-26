package com.example.expense.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: Int,
    val category: String,
    val note: String = "",
    val date: LocalDateTime = LocalDateTime.now()
)