package com.example.expense
import android.app.Application
import com.example.expense.data.database.AppDatabase
import com.example.expense.data.database.TransactionRepository

class ExpenseApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { TransactionRepository(database.transactionDao()) }
}