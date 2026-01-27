package com.example.expense.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.expense.ExpenseApplication
import com.example.expense.viewmodel.TransactionViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ExpenseApplication)

            TransactionViewModel(application.repository)
        }
    }
}