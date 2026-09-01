// presentation/viewmodel/SupportViewModel.kt
package com.goldensystem.auris.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldensystem.auris.data.preferences.CustomThemeConfig
import com.goldensystem.auris.data.preferences.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.json.JSONObject

@HiltViewModel
class SupportViewModel @Inject constructor(
    private val themePreferences: ThemePreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _config = MutableStateFlow(CustomThemeConfig())
    val customThemeConfig: StateFlow<CustomThemeConfig> = _config.asStateFlow()

    init {
        viewModelScope.launch {
            themePreferences.customThemeConfig.collect { config ->
                _config.value = config
            }
        }
    }

    suspend fun sendSupportMessage(
        name: String,
        email: String,
        category: String,
        customSubject: String,
        appVersion: String,
        androidVersion: String,
        device: String,
        message: String,
        imageUri: Uri?
    ): Boolean {
        return try {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            val multipartBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("name", name)
                .addFormDataPart("email", email)
                .addFormDataPart("category", category)
                .addFormDataPart("custom_subject", customSubject)
                .addFormDataPart("app_version", appVersion)
                .addFormDataPart("android_version", androidVersion)
                .addFormDataPart("device", device)
                .addFormDataPart("message", message)

            // Adiciona anexo se existir
            imageUri?.let { uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.use { stream ->
                    val tempFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
                    FileOutputStream(tempFile).use { output ->
                        stream.copyTo(output)
                    }
                    val requestBody = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    multipartBuilder.addFormDataPart(
                        "attachment",
                        tempFile.name,
                        requestBody
                    )
                }
            }

            val requestBody = multipartBuilder.build()

            val request = Request.Builder()
                .url("https://auris-website-api.vercel.app/api/send-email")
                .post(requestBody)
                .addHeader("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            response.isSuccessful && responseBody?.let {
                val json = JSONObject(it)
                json.optBoolean("success", false)
            } ?: false

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}