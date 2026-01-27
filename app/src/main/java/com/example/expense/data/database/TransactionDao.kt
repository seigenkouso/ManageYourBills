package com.example.expense.data.database

import androidx.room.*
import com.example.expense.data.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Delete
    suspend fun delete(transaction: Transaction): Int

    @Update
    suspend fun update(transaction: Transaction): Int
}