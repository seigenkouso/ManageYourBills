package com.example.expense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expense.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: TransactionViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    var type by remember { mutableIntStateOf(0) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var isAmountFocused by remember { mutableStateOf(false) }

    val primaryColor = Color(0xFF006C5B)
    val incomeColor = Color(0xFF388E3C)
    val activeColor = if (type == 0) primaryColor else incomeColor

    val expenseCategories = listOf("餐饮", "交通", "购物", "娱乐", "居住", "医疗", "教育", "人情", "旅行", "数码", "美容", "其他")
    val incomeCategories = listOf("工资", "奖金", "兼职", "理财", "礼金", "退款", "报销", "其他")

    val currentCategories = if (type == 0) expenseCategories else incomeCategories
    var selectedCategory by remember { mutableStateOf(currentCategories.first()) }

    LaunchedEffect(type) { selectedCategory = currentCategories.first() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "记一笔",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
            ) {
                listOf("支出", "收入").forEachIndexed { index, text ->
                    val isSelected = type == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(if (isSelected) activeColor else Color.Transparent)
                            .clickable { type = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = text,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "金额",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TextField(
                value = amount,
                onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amount = it },
                textStyle = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),

                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        isAmountFocused = focusState.isFocused
                    },

                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = activeColor,
                    selectionColors = TextSelectionColors(
                        handleColor = activeColor,
                        backgroundColor = activeColor.copy(alpha = 0.4f)
                    )
                ),

                placeholder = {
                    if (!isAmountFocused) {
                        Text(
                            "0.00",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.LightGray
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "选择分类",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(currentCategories) { category ->
                    val isSelected = selectedCategory == category

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { selectedCategory = category }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = getIconForAddScreen(category),
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("备注...") },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        cursorColor = activeColor,
                        selectionColors = TextSelectionColors(
                            handleColor = activeColor,
                            backgroundColor = activeColor.copy(alpha = 0.4f)
                        ),
                        focusedBorderColor = activeColor
                    ),
                    singleLine = true
                )

                Button(
                    onClick = {
                        val amountDouble = amount.toDoubleOrNull()
                        if (amountDouble != null) {
                            viewModel.addTransaction(amountDouble, type, selectedCategory, note)
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = activeColor),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text("保存", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun getIconForAddScreen(category: String): ImageVector {
    return when (category) {
        "餐饮" -> Icons.Default.Restaurant
        "交通" -> Icons.Default.DirectionsBus
        "购物" -> Icons.Default.ShoppingBag
        "娱乐" -> Icons.Default.Movie
        "居住" -> Icons.Default.Home
        "医疗" -> Icons.Default.LocalHospital
        "教育" -> Icons.Default.School
        "人情" -> Icons.Default.CardGiftcard
        "旅行" -> Icons.Default.Flight
        "数码" -> Icons.Default.PhoneAndroid
        "美容" -> Icons.Default.Face
        "工资" -> Icons.Default.AccountBalanceWallet
        "奖金" -> Icons.Default.EmojiEvents
        "兼职" -> Icons.Default.Work
        "理财" -> Icons.Default.TrendingUp
        "礼金" -> Icons.Default.Redeem
        "退款" -> Icons.Default.Restore
        "报销" -> Icons.Default.Receipt
        else -> Icons.Default.MoreHoriz
    }
}