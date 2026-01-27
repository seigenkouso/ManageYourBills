package com.example.expense.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object AiParsing {

    private const val API_KEY = " "
    private const val API_URL = "https://ark.cn-beijing.volces.com/api/v3/chat/completions"
    private const val MODEL_ID = "doubao-1-5-lite-32k-250115"

    data class ParsedBill(
        val amount: Double = 0.0,
        val merchant: String = "",
        val date: String = "",
        val category: String = ""
    )

    suspend fun parseByAI(ocrText: String): ParsedBill {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AiParsing", "收到 OCR 文本长度: ${ocrText.length}")
                if (ocrText.isBlank()) {
                    Log.e("AiParsing", "OCR 文本为空，取消请求")
                    return@withContext ParsedBill()
                }

                val client = OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build()

                val prompt = """
                    你是一个记账助手。请从下面的OCR文本中提取账单信息。
                    OCR文本：
                    ---
                    $ocrText
                    ---
                    
                    要求：
                    1. 金额(amount): 必须是纯数字。
                    2. 商户(merchant): 提取店名。
                    3. 日期(date): YYYY-MM-DD。
                    4. 分类(category): 选一个最准确的：["餐饮", "交通", "购物", "娱乐", "居住", "医疗", "教育", "人情", "旅行", "数码", "美容", "其他"]。
                    
                    只返回纯 JSON，不要Markdown，不要废话。
                    示例：{"amount": 38.00, "merchant": "xx", "date": "2026-01-26", "category": "购物"}
                """.trimIndent()

                val systemMsg = JSONObject().apply {
                    put("role", "system")
                    put("content", "你是人工智能助手。")
                }

                val userMsg = JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                }

                val messagesArray = org.json.JSONArray().apply {
                    put(systemMsg)
                    put(userMsg)
                }

                val jsonBody = JSONObject().apply {
                    put("model", MODEL_ID)
                    put("messages", messagesArray)
                    put("stream", false)
                    put("temperature", 0.1)
                }

                Log.d("AiParsing", "JSON Body Corrected: $jsonBody")

                Log.d("AiParsing", "正在发送请求 Body: \n$jsonBody")

                val request = Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer $API_KEY")
                    .addHeader("Content-Type", "application/json")
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                Log.d("AiParsing", "开始网络请求...")
                val response = client.newCall(request).execute()
                val responseStr = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    Log.e("AiParsing", "请求失败: Code=${response.code}, Body=$responseStr")
                    return@withContext ParsedBill()
                }

                Log.d("AiParsing", "AI 原始响应: $responseStr")

                val rootJson = JSONObject(responseStr)
                if (!rootJson.has("choices")) return@withContext ParsedBill()

                val rawContent = rootJson.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                Log.d("AiParsing", "AI 回复内容: $rawContent")

                val jsonString = extractJsonString(rawContent)
                if (jsonString.isEmpty()) return@withContext ParsedBill()

                val resultJson = JSONObject(jsonString)
                val bill = ParsedBill(
                    amount = resultJson.optDouble("amount", 0.0),
                    merchant = resultJson.optString("merchant", ""),
                    date = resultJson.optString("date", ""),
                    category = resultJson.optString("category", "")
                )

                Log.d("AiParsing", "最终解析结果: $bill")
                return@withContext bill

            } catch (e: Exception) {
                Log.e("AiParsing", "崩溃", e)
                return@withContext ParsedBill()
            }
        }
    }

    private fun extractJsonString(input: String): String {
        try {
            val p = Pattern.compile("\\{.*\\}", Pattern.DOTALL)
            val m = p.matcher(input)
            if (m.find()) return m.group() ?: ""
        } catch (e: Exception) { e.printStackTrace() }
        return ""
    }
}