package com.example.expense.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val amount: Double,
    val type: Int,
    val category: String,
    val note: String,

    val date: Long = System.currentTimeMillis()
)