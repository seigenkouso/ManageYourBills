package com.example.expense.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import java.util.regex.Pattern

object BillRecognition {

    data class BillResult(
        val amount: String = "",
        val date: String = "",
        val merchant: String = "",
        val matchedCategory: String = ""
    )

    fun analyze(context: Context, uri: Uri, onResult: (BillResult) -> Unit) {
        try {
            val image = InputImage.fromFilePath(context, uri)
            val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    Log.d("OCR_DEBUG", "=== OCR 原始文本开始 ===")
                    Log.d("OCR_DEBUG", visionText.text)
                    Log.d("OCR_DEBUG", "=== OCR 原始文本结束 ===")

                    val result = parseBillText(visionText.text)
                    onResult(result)
                }
                .addOnFailureListener { e ->
                    Log.e("OCR_DEBUG", "识别失败", e)
                    onResult(BillResult())
                }
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(BillResult())
        }
    }

    private fun parseBillText(text: String): BillResult {
        val lines = text.split("\n").map { it.trim() }.filter { it.isNotBlank() }

        var note = findValueForLabel(lines, listOf("商品", "商品名称"))

        if (note.isEmpty()) {
            note = findValueForLabel(lines, listOf("商户全称", "商户", "收款方"))
        }

        if (note.isEmpty() && lines.isNotEmpty()) {
            val firstLine = lines[0]
            if (!firstLine.contains("账单") && !firstLine.contains("详情") && !firstLine.contains("成功")) {
                note = firstLine
            }
        }

        var amount = ""
        val amountPattern = Pattern.compile("([-+]?\\s*\\d+\\.\\d{2})")
        for (line in lines) {
            if (line.contains("余额") || line.contains("积分") || line.contains("优惠") || line.contains("红包")) continue

            val matcher = amountPattern.matcher(line)
            if (matcher.find()) {
                val raw = matcher.group(1) ?: ""
                val clean = raw.replace(" ", "").replace("+", "").replace("-", "")
                if (!clean.startsWith("202")) {
                    if (amount.isEmpty()) amount = clean
                }
            }
        }

        var date = ""
        val datePattern = Pattern.compile("(\\d{4})[年/-](\\d{1,2})[月/-](\\d{1,2})")
        for (line in lines) {
            val matcher = datePattern.matcher(line)
            if (matcher.find()) {
                val y = matcher.group(1)
                val m = matcher.group(2)?.padStart(2, '0')
                val d = matcher.group(3)?.padStart(2, '0')
                date = "$y-$m-$d"
                break
            }
        }

        val category = determineCategory(note, text)

        Log.d("OCR_DEBUG", "最终解析: 金额=$amount, 备注=$note, 分类=$category")
        return BillResult(amount, date, note, category)
    }

    private fun findValueForLabel(lines: List<String>, labels: List<String>): String {
        for (i in lines.indices) {
            val line = lines[i]

            for (label in labels) {
                if (line.contains(label)) {
                    var content = line.replace(label, "")
                        .replace("名称", "")
                        .replace("全称", "")
                        .replace(":", "")
                        .replace("：", "")
                        .trim()

                    if (content.isEmpty() || isBlacklisted(content)) {
                        if (i + 1 < lines.size) {
                            val nextLine = lines[i + 1].trim()
                            if (!isLabelLine(nextLine)) {
                                return nextLine
                            }
                        }
                    } else {
                        return content
                    }
                }
            }
        }
        return ""
    }

    private fun isBlacklisted(text: String): Boolean {
        return text == "商户" || text == "名称" || text == "全称" ||
                text == "商户全称" || text == "商品名称" || text == "收款方"
    }

    private fun isLabelLine(text: String): Boolean {
        return text.startsWith("商户") || text.startsWith("商品") ||
                text.startsWith("当前") || text.startsWith("支付") || text.startsWith("交易")
    }

    private fun determineCategory(note: String, fullText: String): String {
        val info = "$note $fullText"

        if (note.contains("-") && note.length > 2 && !note.startsWith("202")) {
            return "交通"
        }

        return when {
            info.contains("怀运") || info.contains("公路") || info.contains("运输") ||
                    info.contains("客运") || info.contains("车") || info.contains("票") ||
                    info.contains("交通") || info.contains("铁路") || info.contains("出行") -> "交通"

            info.contains("餐饮") || info.contains("美食") || info.contains("面") ||
                    info.contains("粉") || info.contains("饭") -> "餐饮"

            info.contains("超市") || info.contains("便利") || info.contains("百货") -> "购物"

            else -> ""
        }
    }
}