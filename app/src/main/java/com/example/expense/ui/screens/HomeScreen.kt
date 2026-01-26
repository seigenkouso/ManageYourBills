package com.example.expense.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expense.data.database.Transaction
import com.example.expense.viewmodel.TransactionViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: TransactionViewModel = viewModel(),
    onNavigateToAdd: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val transactionList by viewModel.allTransactions.collectAsState(initial = emptyList())

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var isYearView by remember { mutableStateOf(false) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingDeleteTransaction by remember { mutableStateOf<Transaction?>(null) }

    val displayedTransactions = remember(transactionList, selectedDate, isYearView) {
        transactionList.filter {
            if (isYearView) it.date.year == selectedDate.year
            else it.date.year == selectedDate.year && it.date.month == selectedDate.month
        }
    }

    val currentBalance = remember(displayedTransactions) {
        displayedTransactions.sumOf { if (it.type == 1) it.amount else -it.amount }
    }

    val groupedTransactions = remember(displayedTransactions) {
        displayedTransactions
            .sortedByDescending { it.date }
            .groupBy { it.date.toLocalDate() }
    }

    if (showDeleteDialog && pendingDeleteTransaction != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Outlined.Warning, contentDescription = null) },
            title = { Text(text = "确认删除") },
            text = { Text(text = "确定要删除这笔账单吗？删除后无法恢复。") },
            confirmButton = {
                Button(
                    onClick = {
                        pendingDeleteTransaction?.let { viewModel.deleteTransaction(it) }
                        showDeleteDialog = false
                        pendingDeleteTransaction = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        pendingDeleteTransaction = null
                    }
                ) {
                    Text("取消")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = Color(0xFF006C5B),
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "记一笔")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("记账", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.width(12.dp))
                    ViewToggle(isYearView = isYearView, onToggle = { isYearView = it })
                }
                IconButton(onClick = onNavigateToAbout, modifier = Modifier.size(40.dp).clip(CircleShape)) {
                    Icon(Icons.Default.Info, contentDescription = "关于", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            DateSelector(
                currentDate = selectedDate,
                isYearView = isYearView,
                onPrevious = { selectedDate = if (isYearView) selectedDate.minusYears(1) else selectedDate.minusMonths(1) },
                onNext = { selectedDate = if (isYearView) selectedDate.plusYears(1) else selectedDate.plusMonths(1) }
            )

            ExpenseSummaryCard(balance = currentBalance, label = if (isYearView) "年度结余" else "本月结余")

            Spacer(modifier = Modifier.height(16.dp))

            if (displayedTransactions.isEmpty()) {
                val emptyText = if (isYearView) "${selectedDate.year}年暂无账单" else "${selectedDate.monthValue}月暂无账单"
                EmptyStateView(text = emptyText)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    groupedTransactions.forEach { (date, transactionsForDate) ->
                        stickyHeader { DateHeader(date) }
                        items(items = transactionsForDate, key = { it.id }) { transaction ->
                            Box(modifier = Modifier.animateItemPlacement()) {
                                SwipeToDeleteContainer(
                                    item = transaction,
                                    onDeleteRequest = {
                                        pendingDeleteTransaction = transaction
                                        showDeleteDialog = true
                                    }
                                ) {
                                    TransactionItem(transaction = transaction)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteContainer(
    item: Transaction,
    onDeleteRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDeleteRequest()
                false
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) Color(0xFFFF5252) else Color.Transparent, label = "color"
            )
            val scale by animateFloatAsState(
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 1.2f else 0.8f, label = "scale"
            )

            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).clip(RoundedCornerShape(20.dp)).background(color),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = "删除", tint = Color.White, modifier = Modifier.padding(end = 24.dp).scale(scale))
            }
        },
        content = { content() }
    )
}

@Composable
fun ViewToggle(isYearView: Boolean, onToggle: (Boolean) -> Unit) {
    Row(modifier = Modifier.clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.surfaceContainerHighest).padding(4.dp)) {
        ToggleButton("月", !isYearView) { onToggle(false) }
        ToggleButton("年", isYearView) { onToggle(true) }
    }
}

@Composable
fun ToggleButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor by animateColorAsState(if (isSelected) Color(0xFF006C5B) else Color.Transparent, label = "bg")
    val textColor by animateColorAsState(if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, label = "txt")
    Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(backgroundColor).clickable { onClick() }.padding(horizontal = 16.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = textColor)
    }
}

@Composable
fun DateSelector(currentDate: LocalDate, isYearView: Boolean, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrevious) { Icon(Icons.Default.ChevronLeft, contentDescription = "Prev") }
        AnimatedContent(targetState = currentDate, transitionSpec = {
            if (targetState.isAfter(initialState)) slideInHorizontally { width -> width } + fadeIn() togetherWith slideOutHorizontally { width -> -width } + fadeOut()
            else slideInHorizontally { width -> -width } + fadeIn() togetherWith slideOutHorizontally { width -> width } + fadeOut()
        }, label = "DateAnim") { target ->
            val text = if (isYearView) "${target.year}年" else "${target.year}年 ${target.monthValue}月"
            Text(text, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 16.dp))
        }
        IconButton(onClick = onNext) { Icon(Icons.Default.ChevronRight, contentDescription = "Next") }
    }
}

@Composable
fun ExpenseSummaryCard(balance: Double, label: String) {
    val gradientBrush = Brush.horizontalGradient(colors = listOf(Color(0xFF43CEA2), Color(0xFF185A9D)))
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).height(160.dp), shape = RoundedCornerShape(28.dp), elevation = CardDefaults.cardElevation(10.dp)) {
        Box(modifier = Modifier.fillMaxSize().background(gradientBrush)) {
            Box(modifier = Modifier.offset(x = 200.dp, y = (-50).dp).size(200.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)))
            Column(modifier = Modifier.padding(24.dp).align(Alignment.BottomStart)) {
                Text(label, style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(8.dp))
                val sign = if (balance >= 0) "+" else ""
                Text("$sign${String.format("%.2f", balance)}", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
            }
            Icon(Icons.Default.AccountBalanceWallet, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.align(Alignment.TopEnd).padding(24.dp).size(48.dp))
        }
    }
}

@Composable
fun EmptyStateView(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Savings, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(text, color = Color.Gray)
        }
    }
}

@Composable
fun DateHeader(date: LocalDate) {
    val today = LocalDate.now()
    val dateStr = when (date) {
        today -> "今天"
        today.minusDays(1) -> "昨天"
        else -> "${date.monthValue}月${date.dayOfMonth}日 ${date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINA)}"
    }
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
        Text(dateStr, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) {
            Icon(getCategoryIcon(transaction.category), null, tint = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(transaction.category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            val time = transaction.date.format(DateTimeFormatter.ofPattern("HH:mm"))
            val note = transaction.note
            Text(if (note.isNotEmpty()) "$time · $note" else time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
        Column(horizontalAlignment = Alignment.End) {
            val color = if (transaction.type == 0) Color(0xFFD32F2F) else Color(0xFF388E3C)
            val prefix = if (transaction.type == 0) "-" else "+"
            Text("$prefix${transaction.amount}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = color)
        }
    }
}

fun getCategoryIcon(category: String): ImageVector {
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