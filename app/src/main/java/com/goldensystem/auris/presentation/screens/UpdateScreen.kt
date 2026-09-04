package com.goldensystem.auris.presentation.screens

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.goldensystem.auris.BuildConfig
import com.goldensystem.auris.R
import com.goldensystem.auris.data.model.AppVersionInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateScreen(
    updateInfo: AppVersionInfo,
    onCancelClick: () -> Unit,
    onRemindLaterClick: () -> Unit
) {
    val context = LocalContext.current
    var downloadId by remember { mutableStateOf<Long?>(null) }
    var progress by remember { mutableIntStateOf(0) }
    var isDownloading by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        startDownloadUnique(context, updateInfo) { id ->
            downloadId = id
            isDownloading = true
        }
    }

    LaunchedEffect(downloadId) {
        val id = downloadId ?: return@LaunchedEffect
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        while (true) {
            val done = withContext(Dispatchers.IO) {
                val query = DownloadManager.Query().setFilterById(id)
                manager.query(query).use { cursor ->
                    if (!cursor.moveToFirst()) return@use null

                    val downloaded = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    )
                    val total = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    )
                    val status = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
                    )
                    Triple(downloaded, total, status)
                }
            }

            if (done == null) break

            val (downloaded, total, status) = done

            if (total > 0) {
                progress = (downloaded * 100L / total).toInt()
            }

            when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    isDownloading = false
                    val filePath = withContext(Dispatchers.IO) {
                        val query = DownloadManager.Query().setFilterById(id)
                        manager.query(query).use { cursor ->
                            if (cursor.moveToFirst()) {
                                val localUri = cursor.getString(
                                    cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI)
                                )
                                if (localUri != null) {
                                    if (localUri.startsWith("file://")) Uri.parse(localUri).path
                                    else manager.getUriForDownloadedFile(id)?.path ?: localUri
                                } else null
                            } else null
                        }
                    }
                    if (filePath != null) {
                        installApk(context, filePath)
                    }
                    break
                }
                DownloadManager.STATUS_FAILED,
                DownloadManager.STATUS_PAUSED -> {
                    isDownloading = false
                    Toast.makeText(context, R.string.update_toast_download_failed, Toast.LENGTH_SHORT).show()
                    break
                }
            }

            delay(500)
        }
    }

    Dialog(onDismissRequest = { if (!updateInfo.isRequired) onCancelClick() }) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = context.getString(R.string.update_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = context.getString(R.string.update_current_version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = context.getString(R.string.update_new_version, updateInfo.version),
                    style = MaterialTheme.typography.bodyMedium
                )

                updateInfo.changelog?.let {
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = context.getString(R.string.update_changelog_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(20.dp))

                if (isDownloading) {
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = context.getString(R.string.update_download_progress, progress),
                        style = MaterialTheme.typography.labelMedium
                    )
                } else {
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                startDownloadUnique(context, updateInfo) { id ->
                                    downloadId = id
                                    isDownloading = true
                                }
                            }
                        },
                        enabled = !isDownloading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(context.getString(R.string.update_button_download))
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            val websiteUrl = "https://synvertexstudios.github.io/Auris-website/"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl))
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(context.getString(R.string.update_button_website))
                    }

                    Spacer(Modifier.height(12.dp))

                    if (!updateInfo.isRequired) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = onRemindLaterClick) {
                                Text(context.getString(R.string.update_button_remind_later))
                            }
                            TextButton(onClick = onCancelClick) {
                                Text(context.getString(R.string.update_button_close))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun startDownloadUnique(context: Context, updateInfo: AppVersionInfo, onIdReceived: (Long) -> Unit) {
    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val baseFileName = "auris_update_${updateInfo.version.replace(".", "_")}"
    
    var counter = 1
    var finalFile: File
    var fileName: String
    
    do {
        fileName = if (counter == 1) {
            "$baseFileName.apk"
        } else {
            "$baseFileName($counter).apk"
        }
        finalFile = File(downloadsDir, fileName)
        counter++
    } while (finalFile.exists())
    
    val request = DownloadManager.Request(Uri.parse(updateInfo.downloadUrl))
        .setTitle(context.getString(R.string.update_download_title))
        .setDescription(context.getString(R.string.update_download_description))
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        .setAllowedOverMetered(true)
        .setDestinationUri(Uri.fromFile(finalFile))

    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val id = manager.enqueue(request)
    onIdReceived(id)
}

private fun installApk(context: Context, filePath: String) {
    try {
        Toast.makeText(context, R.string.update_toast_download_complete, Toast.LENGTH_SHORT).show()

        val uri = Uri.parse(filePath)
        val apkUri = if (uri.scheme == "content") {
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File(context.cacheDir, "temp_auris_update.apk")
            inputStream?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            }
            inputStream?.close()
            FileProvider.getUriForFile(context, "${context.packageName}.provider", tempFile)
        } else {
            val file = File(uri.path ?: return)
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        }

        context.grantUriPermission(
            "com.android.packageinstaller",
            apkUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, R.string.update_toast_no_installer, Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(
            context,
            context.getString(R.string.update_toast_install_error, e.message),
            Toast.LENGTH_LONG
        ).show()
    }
}