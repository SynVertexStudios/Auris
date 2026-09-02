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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    ): Boolean = withContext(Dispatchers.IO) {

        var tempFile: File? = null

        try {
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

            // ---------------------------------------------------------
            // Anexo opcional
            // ---------------------------------------------------------

            imageUri?.let { uri ->

                context.contentResolver.openInputStream(uri)?.use { inputStream ->

                    tempFile = File(
                        context.cacheDir,
                        "temp_image_${System.currentTimeMillis()}.jpg"
                    )

                    FileOutputStream(tempFile!!).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }

                    val requestBody = tempFile!!.asRequestBody(
                        "image/jpeg".toMediaTypeOrNull()
                    )

                    multipartBuilder.addFormDataPart(
                        "attachment",
                        tempFile!!.name,
                        requestBody
                    )
                }
            }

            // ---------------------------------------------------------
            // Requisição
            // ---------------------------------------------------------

            val requestBody = multipartBuilder.build()

            val request = Request.Builder()
                .url("https://auris-website-api.vercel.app/api/send-email")
                .post(requestBody)
                .addHeader("Accept", "application/json")
                .build()

            client.newCall(request).execute().use { response ->

                val responseBody = response.body?.string().orEmpty()

                // Sucesso HTTP + {"success":true}
                if (!response.isSuccessful) {
                    return@withContext false
                }

                if (responseBody.isBlank()) {
                    return@withContext false
                }

                val json = JSONObject(responseBody)

                return@withContext json.optBoolean(
                    "success",
                    false
                )
            }

        } catch (e: Exception) {

            e.printStackTrace()

            false

        } finally {

            // Remove o arquivo temporário depois do envio
            try {
                tempFile?.delete()
            } catch (_: Exception) {
            }
        }
    }
}