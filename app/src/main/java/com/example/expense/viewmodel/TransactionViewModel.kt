package com.example.expense.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.expense.data.database.AppDatabase
import com.example.expense.data.database.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).transactionDao()

    val allTransactions: Flow<List<Transaction>> = dao.getAllTransactions()

    // 🆕 新增：自动计算本月的起止时间，并获取支出总额
    val currentMonthExpense: Flow<Double> = dao.getMonthlyExpense(
        start = getStartOfMonth(),
        end = getEndOfMonth()
    ).map { it ?: 0.0 } // 如果数据库返回 null (没记账)，就转成 0.0

    fun addTransaction(amount: Double, type: Int, category: String, note: String) {
        viewModelScope.launch {
            val transaction = Transaction(
                amount = amount,
                type = type,
                category = category,
                note = note
            )
            dao.insertTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            dao.deleteTransaction(transaction)
        }
    }

    // --- 日期计算辅助函数 ---
    private fun getStartOfMonth(): Long {
        return YearMonth.now().atDay(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000
    }

    private fun getEndOfMonth(): Long {
        return YearMonth.now().atEndOfMonth().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toEpochSecond() * 1000
    }
}