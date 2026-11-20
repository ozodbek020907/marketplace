package com.example.ministore.data

import com.example.ministore.model.LoginRecord
import com.example.ministore.model.SignUpRecord
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

object SupabaseRepository {
    private const val SUPABASE_URL = "https://semybpbsbknvslhybcqx.supabase.co"
    private const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNlbXlicGJzYmtudnNsaHliY3F4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTUxNTQyOTIsImV4cCI6MjExMDczMDI5Mn0.ME3dPpIHuKeD2kyLfS0uVGodye37CcS6_epKonDZFA4"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Ro'yxatdan o'tish (public.signup jadvali).
     * Agar login avval mavjud bo'lsa xatolik qaytaradi.
     */
    suspend fun register(username: String, login: String, pass: String): Result<SignUpRecord> =
        withContext(Dispatchers.IO) {
            try {
                // 1. Avval mavjud loginni tekshirish
                val checkUrl = "$SUPABASE_URL/rest/v1/signup?login=eq.${login.trim()}&select=*"
                val checkRequest = Request.Builder()
                    .url(checkUrl)
                    .addHeader("apikey", ANON_KEY)
                    .addHeader("Authorization", "Bearer $ANON_KEY")
                    .get()
                    .build()

                val checkResponse = client.newCall(checkRequest).execute()
                val checkBody = checkResponse.body?.string() ?: ""

                if (!checkResponse.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Supabase tekshirishda xatolik: HTTP ${checkResponse.code} - $checkBody")
                    )
                }

                val existingListType = object : TypeToken<List<SignUpRecord>>() {}.type
                val existingUsers: List<SignUpRecord> = gson.fromJson(checkBody, existingListType) ?: emptyList()

                if (existingUsers.isNotEmpty()) {
                    return@withContext Result.failure(Exception("Ushbu login band! Boshqa login tanlang."))
                }

                // 2. Yangi foydalanuvchini signup jadvaliga yozish
                val insertUrl = "$SUPABASE_URL/rest/v1/signup"
                val newRecord = mapOf(
                    "username" to username.trim(),
                    "login" to login.trim(),
                    "password" to pass
                )
                val jsonPayload = gson.toJson(newRecord)

                val insertRequest = Request.Builder()
                    .url(insertUrl)
                    .addHeader("apikey", ANON_KEY)
                    .addHeader("Authorization", "Bearer $ANON_KEY")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .post(jsonPayload.toRequestBody(jsonMediaType))
                    .build()

                val insertResponse = client.newCall(insertRequest).execute()
                val insertBody = insertResponse.body?.string() ?: ""

                if (insertResponse.isSuccessful) {
                    val returnList: List<SignUpRecord> = gson.fromJson(insertBody, existingListType) ?: emptyList()
                    val created = returnList.firstOrNull() ?: SignUpRecord(username = username, login = login, password = pass)
                    Result.success(created)
                } else {
                    Result.failure(Exception("Ro'yxatdan o'tishda xatolik: HTTP ${insertResponse.code} - $insertBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Tizimga kirish (public.signup tekshiriladi, natijasi public.login jadvaliga log qilinadi).
     */
    suspend fun login(login: String, pass: String): Result<SignUpRecord> =
        withContext(Dispatchers.IO) {
            val trimmedLogin = login.trim()
            try {
                // 1. signup jadvalidan foydalanuvchini qidirish
                val searchUrl = "$SUPABASE_URL/rest/v1/signup?login=eq.$trimmedLogin&select=*"
                val searchRequest = Request.Builder()
                    .url(searchUrl)
                    .addHeader("apikey", ANON_KEY)
                    .addHeader("Authorization", "Bearer $ANON_KEY")
                    .get()
                    .build()

                val searchResponse = client.newCall(searchRequest).execute()
                val searchBody = searchResponse.body?.string() ?: ""

                if (!searchResponse.isSuccessful) {
                    val logStatus = "XATO: Server javobi ${searchResponse.code}"
                    recordLoginAttempt(trimmedLogin, pass, logStatus)
                    return@withContext Result.failure(Exception("Tarmoq xatosi: $searchBody"))
                }

                val listType = object : TypeToken<List<SignUpRecord>>() {}.type
                val foundUsers: List<SignUpRecord> = gson.fromJson(searchBody, listType) ?: emptyList()

                if (foundUsers.isEmpty()) {
                    val logStatus = "XATO: Login topilmadi"
                    recordLoginAttempt(trimmedLogin, pass, logStatus)
                    return@withContext Result.failure(Exception("XATO: Login topilmadi"))
                }

                val user = foundUsers.first()
                if (user.password != pass) {
                    val logStatus = "XATO: Parol noto'g'ri"
                    recordLoginAttempt(trimmedLogin, pass, logStatus)
                    return@withContext Result.failure(Exception("XATO: Parol noto'g'ri"))
                }

                // Kirish muvaffaqiyatli
                val logStatus = "SUCCESS"
                recordLoginAttempt(trimmedLogin, pass, logStatus)
                Result.success(user)
            } catch (e: Exception) {
                recordLoginAttempt(trimmedLogin, pass, "XATO: ${e.localizedMessage ?: "Aloqa uzildi"}")
                Result.failure(e)
            }
        }

    /**
     * Har bir login urinishini public.login jadvaliga yozadi.
     */
    private fun recordLoginAttempt(login: String, pass: String, status: String) {
        try {
            val logUrl = "$SUPABASE_URL/rest/v1/login"
            val logData = mapOf(
                "login" to login,
                "password" to pass,
                "status" to status
            )
            val jsonPayload = gson.toJson(logData)

            val logRequest = Request.Builder()
                .url(logUrl)
                .addHeader("apikey", ANON_KEY)
                .addHeader("Authorization", "Bearer $ANON_KEY")
                .addHeader("Content-Type", "application/json")
                .post(jsonPayload.toRequestBody(jsonMediaType))
                .build()

            client.newCall(logRequest).execute().close()
        } catch (_: Exception) {
            // Log yozishdagi xatolik asosiy jarayonni to'xtatmasligi kerak
        }
    }

    /**
     * public.signup jadvalidagi barcha ro'yxatdan o'tgan foydalanuvchilar.
     */
    suspend fun fetchRegisteredUsers(): Result<List<SignUpRecord>> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$SUPABASE_URL/rest/v1/signup?select=*&order=id.desc"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", ANON_KEY)
                    .addHeader("Authorization", "Bearer $ANON_KEY")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val listType = object : TypeToken<List<SignUpRecord>>() {}.type
                    val list: List<SignUpRecord> = gson.fromJson(body, listType) ?: emptyList()
                    Result.success(list)
                } else {
                    Result.failure(Exception("HTTP ${response.code}: $body"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * public.login jadvalidagi barcha urinishlar loglari.
     */
    suspend fun fetchLoginLogs(): Result<List<LoginRecord>> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$SUPABASE_URL/rest/v1/login?select=*&order=id.desc"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", ANON_KEY)
                    .addHeader("Authorization", "Bearer $ANON_KEY")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val listType = object : TypeToken<List<LoginRecord>>() {}.type
                    val list: List<LoginRecord> = gson.fromJson(body, listType) ?: emptyList()
                    Result.success(list)
                } else {
                    Result.failure(Exception("HTTP ${response.code}: $body"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
