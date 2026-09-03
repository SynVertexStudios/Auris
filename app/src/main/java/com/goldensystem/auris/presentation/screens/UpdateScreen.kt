package com.goldensystem.auris.presentation.screens

import android.Manifest
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.window.Dialog
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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
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

private enum class UpdateState {
    IDLE,
    DOWNLOADING,
    COMPLETED,
    FAILED,
    ALREADY_DOWNLOADED
}

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
    var downloadedBytes by remember { mutableLongStateOf(0L) }
    var totalBytes by remember { mutableLongStateOf(0L) }
    var updateState by remember { mutableStateOf(UpdateState.IDLE) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var existingFileUri by remember { mutableStateOf<Uri?>(null) }

    // Verifica se o arquivo já existe ao iniciar
    LaunchedEffect(updateInfo) {
        val file = getExistingApkFile(context, updateInfo.version)
        if (file != null && file.exists()) {
            existingFileUri = Uri.fromFile(file)
            updateState = UpdateState.ALREADY_DOWNLOADED
        }
    }

    val startDownload: () -> Unit = {
        errorMessage = null
        progress = 0
        downloadedBytes = 0L
        totalBytes = 0L
        existingFileUri = null

        try {
            startDownloadUnique(context, updateInfo) { id ->
                downloadId = id
                updateState = UpdateState.DOWNLOADING
            }
        } catch (e: Exception) {
            updateState = UpdateState.FAILED
            errorMessage = e.message
        }
    }

    val installExistingApk: () -> Unit = {
        existingFileUri?.let { uri ->
            installApk(context, uri)
        } ?: run {
            // Se por algum motivo o URI for nulo, tenta baixar novamente
            startDownload()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        startDownload()
    }

    LaunchedEffect(downloadId) {
        val id = downloadId ?: return@LaunchedEffect

        val manager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        while (updateState == UpdateState.DOWNLOADING) {
            val result = withContext(Dispatchers.IO) {
                try {
                    val query = DownloadManager.Query().setFilterById(id)

                    manager.query(query).use { cursor ->
                        if (!cursor.moveToFirst()) {
                            null
                        } else {
                            val downloaded = cursor.getLong(
                                cursor.getColumnIndexOrThrow(
                                    DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR
                                )
                            )

                            val total = cursor.getLong(
                                cursor.getColumnIndexOrThrow(
                                    DownloadManager.COLUMN_TOTAL_SIZE_BYTES
                                )
                            )

                            val status = cursor.getInt(
                                cursor.getColumnIndexOrThrow(
                                    DownloadManager.COLUMN_STATUS
                                )
                            )

                            val reason = cursor.getInt(
                                cursor.getColumnIndexOrThrow(
                                    DownloadManager.COLUMN_REASON
                                )
                            )

                            DownloadResult(
                                downloaded = downloaded,
                                total = total,
                                status = status,
                                reason = reason
                            )
                        }
                    }
                } catch (_: Exception) {
                    null
                }
            }

            if (result == null) {
                updateState = UpdateState.FAILED
                errorMessage =
                    context.getString(R.string.update_toast_download_failed)
                break
            }

            downloadedBytes = result.downloaded
            totalBytes = result.total

            if (result.total > 0L) {
                progress = (
                    result.downloaded
                        .coerceAtLeast(0L)
                        .coerceAtMost(result.total) * 100L / result.total
                    ).toInt()
            }

            when (result.status) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    updateState = UpdateState.COMPLETED
                    progress = 100

                    val fileUri = withContext(Dispatchers.IO) {
                        getDownloadedFileUri(context, manager, id)
                    }

                    if (fileUri != null) {
                        installApk(context, fileUri)
                    } else {
                        updateState = UpdateState.FAILED
                        errorMessage =
                            context.getString(R.string.update_toast_no_installer)
                    }

                    break
                }

                DownloadManager.STATUS_FAILED -> {
                    updateState = UpdateState.FAILED

                    errorMessage = context.getString(
                        R.string.update_toast_download_failed
                    )

                    break
                }

                DownloadManager.STATUS_PAUSED -> {
                    delay(500)
                }
            }

            delay(400)
        }
    }

    Dialog(
        onDismissRequest = {
            if (!updateInfo.isRequired &&
                updateState != UpdateState.DOWNLOADING
            ) {
                onCancelClick()
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                UpdateHeader(
                    state = updateState,
                    isRequired = updateInfo.isRequired
                )

                Spacer(Modifier.height(20.dp))

                VersionCard(
                    currentVersion = BuildConfig.VERSION_NAME,
                    newVersion = updateInfo.version
                )

                updateInfo.changelog
                    ?.takeIf { it.isNotBlank() }
                    ?.let { changelog ->

                        Spacer(Modifier.height(16.dp))

                        ChangelogCard(changelog)
                    }

                Spacer(Modifier.height(20.dp))

                AnimatedContent(
                    targetState = updateState,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "update_state"
                ) { state ->

                    when (state) {
                        UpdateState.IDLE -> {
                            DownloadActions(
                                context = context,
                                isRequired = updateInfo.isRequired,
                                onDownload = {
                                    if (
                                        Build.VERSION.SDK_INT >=
                                        Build.VERSION_CODES.TIRAMISU
                                    ) {
                                        notificationPermissionLauncher.launch(
                                            Manifest.permission.POST_NOTIFICATIONS
                                        )
                                    } else {
                                        startDownload()
                                    }
                                },
                                onWebsite = {
                                    openWebsite(context)
                                },
                                onRemindLater = onRemindLaterClick,
                                onClose = onCancelClick
                            )
                        }

                        UpdateState.ALREADY_DOWNLOADED -> {
                            AlreadyDownloadedActions(
                                onInstall = installExistingApk,
                                onDownload = {
                                    if (
                                        Build.VERSION.SDK_INT >=
                                        Build.VERSION_CODES.TIRAMISU
                                    ) {
                                        notificationPermissionLauncher.launch(
                                            Manifest.permission.POST_NOTIFICATIONS
                                        )
                                    } else {
                                        startDownload()
                                    }
                                },
                                onWebsite = {
                                    openWebsite(context)
                                },
                                onClose = if (!updateInfo.isRequired) onCancelClick else null
                            )
                        }

                        UpdateState.DOWNLOADING -> {
                            DownloadProgress(
                                progress = progress,
                                downloadedBytes = downloadedBytes,
                                totalBytes = totalBytes
                            )
                        }

                        UpdateState.COMPLETED -> {
                            DownloadCompleted()
                        }

                        UpdateState.FAILED -> {
                            DownloadFailed(
                                errorMessage = errorMessage,
                                onRetry = startDownload,
                                onWebsite = {
                                    openWebsite(context)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateHeader(
    state: UpdateState,
    isRequired: Boolean
) {
    val icon = when (state) {
        UpdateState.IDLE,
        UpdateState.DOWNLOADING -> Icons.Outlined.SystemUpdate

        UpdateState.COMPLETED,
        UpdateState.ALREADY_DOWNLOADED -> Icons.Outlined.CheckCircle

        UpdateState.FAILED -> Icons.Outlined.ErrorOutline
    }

    val title = when (state) {
        UpdateState.IDLE -> stringResourceSafe(R.string.update_title)

        UpdateState.DOWNLOADING ->
            stringResourceSafe(R.string.update_download_title)

        UpdateState.COMPLETED ->
            stringResourceSafe(R.string.update_toast_download_complete)

        UpdateState.ALREADY_DOWNLOADED ->
            "Atualização já baixada"

        UpdateState.FAILED ->
            stringResourceSafe(R.string.update_toast_download_failed)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape),
            color = when (state) {
                UpdateState.ALREADY_DOWNLOADED -> 
                    MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.primaryContainer
            }
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = when (state) {
                        UpdateState.ALREADY_DOWNLOADED -> 
                            MaterialTheme.colorScheme.onTertiaryContainer
                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        if (isRequired && state == UpdateState.IDLE) {
            Spacer(Modifier.height(6.dp))

            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    text = "Atualização obrigatória",
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AlreadyDownloadedActions(
    onInstall: () -> Unit,
    onDownload: () -> Unit,
    onWebsite: () -> Unit,
    onClose: (() -> Unit)?
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onInstall,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(vertical = 14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.SystemUpdate,
                contentDescription = null
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = "Instalar agora",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = onDownload,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(vertical = 13.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Download,
                contentDescription = null
            )

            Spacer(Modifier.width(8.dp))

            Text("Baixar novamente")
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = onWebsite,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(vertical = 13.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.OpenInNew,
                contentDescription = null
            )

            Spacer(Modifier.width(8.dp))

            Text("Baixar pelo site")
        }

        if (onClose != null) {
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = onClose) {
                    Text("Fechar")
                }
            }
        }
    }
}

// Função para verificar se o APK já existe
private fun getExistingApkFile(
    context: Context,
    version: String
): File? {
    try {
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )

        if (!downloadsDir.exists()) {
            return null
        }

        val baseFileName = "auris_update_${version.replace(".", "_")}"
        
        // Verifica se existe algum arquivo com esse nome (com ou sem sufixo)
        val files = downloadsDir.listFiles { _, name ->
            name.startsWith(baseFileName) && name.endsWith(".apk")
        }

        return files?.firstOrNull()
    } catch (_: Exception) {
        return null
    }
}

@Composable
private fun VersionCard(
    currentVersion: String,
    newVersion: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VersionItem(
                label = "Atual",
                version = currentVersion,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Outlined.SystemUpdate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            VersionItem(
                label = "Nova",
                version = newVersion,
                modifier = Modifier.weight(1f),
                alignEnd = true
            )
        }
    }
}

@Composable
private fun VersionItem(
    label: String,
    version: String,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment =
            if (alignEnd) Alignment.End
            else Alignment.Start
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(3.dp))

        Text(
            text = version,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ChangelogCard(
    changelog: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.CloudDownload,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = stringResourceSafe(R.string.update_changelog_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(8.dp))

        HorizontalDivider()

        Spacer(Modifier.height(8.dp))

        Text(
            text = changelog,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DownloadActions(
    context: Context,
    isRequired: Boolean,
    onDownload: () -> Unit,
    onWebsite: () -> Unit,
    onRemindLater: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onDownload,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Download,
                contentDescription = null
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = stringResourceSafe(
                    R.string.update_button_download
                ),
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = onWebsite,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(vertical = 13.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Language,
                contentDescription = null
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = stringResourceSafe(
                    R.string.update_button_website
                )
            )
        }

        if (!isRequired) {
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onRemindLater) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = stringResourceSafe(
                            R.string.update_button_remind_later
                        )
                    )
                }

                TextButton(onClick = onClose) {
                    Text(
                        text = stringResourceSafe(
                            R.string.update_button_close
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadProgress(
    progress: Int,
    downloadedBytes: Long,
    totalBytes: Long
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress / 100f,
        label = "download_progress"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.size(88.dp),
                strokeWidth = 7.dp
            )

            Text(
                text = "$progress%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(18.dp))

        Text(
            text = "Baixando atualização…",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        if (totalBytes > 0L) {
            Text(
                text = formatBytes(downloadedBytes) +
                    " / " +
                    formatBytes(totalBytes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DownloadCompleted() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Download concluído",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Abrindo o instalador…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DownloadFailed(
    errorMessage: String?,
    onRetry: () -> Unit,
    onWebsite: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(58.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Não foi possível baixar",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        if (!errorMessage.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))

            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(18.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null
            )

            Spacer(Modifier.width(8.dp))

            Text("Tentar novamente")
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = onWebsite,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Icon(
                imageVector = Icons.Outlined.OpenInNew,
                contentDescription = null
            )

            Spacer(Modifier.width(8.dp))

            Text("Baixar pelo site")
        }
    }
}

private data class DownloadResult(
    val downloaded: Long,
    val total: Long,
    val status: Int,
    val reason: Int
)

private fun startDownloadUnique(
    context: Context,
    updateInfo: AppVersionInfo,
    onIdReceived: (Long) -> Unit
) {
    require(updateInfo.downloadUrl.isNotBlank()) {
        "URL de download inválida."
    }

    val downloadsDir =
        Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )

    if (!downloadsDir.exists()) {
        downloadsDir.mkdirs()
    }

    val baseFileName =
        "auris_update_${updateInfo.version.replace(".", "_")}"

    var counter = 1
    lateinit var finalFile: File

    do {
        val fileName =
            if (counter == 1) {
                "$baseFileName.apk"
            } else {
                "$baseFileName($counter).apk"
            }

        finalFile = File(downloadsDir, fileName)
        counter++
    } while (finalFile.exists())

    val request = DownloadManager.Request(
        Uri.parse(updateInfo.downloadUrl)
    )
        .setTitle(
            context.getString(
                R.string.update_download_title
            )
        )
        .setDescription(
            context.getString(
                R.string.update_download_description
            )
        )
        .setMimeType("application/vnd.android.package-archive")
        .setNotificationVisibility(
            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
        )
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)
        .setDestinationUri(Uri.fromFile(finalFile))

    val manager =
        context.getSystemService(
            Context.DOWNLOAD_SERVICE
        ) as DownloadManager

    val id = manager.enqueue(request)

    onIdReceived(id)
}

private fun getDownloadedFileUri(
    context: Context,
    manager: DownloadManager,
    id: Long
): Uri? {
    return try {
        val uri = manager.getUriForDownloadedFile(id)

        if (uri != null) {
            uri
        } else {
            val query = DownloadManager.Query()
                .setFilterById(id)

            manager.query(query).use { cursor ->
                if (!cursor.moveToFirst()) {
                    null
                } else {
                    val localUri = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                            DownloadManager.COLUMN_LOCAL_URI
                        )
                    )

                    if (localUri.isNullOrBlank()) {
                        null
                    } else {
                        Uri.parse(localUri)
                    }
                }
            }
        }
    } catch (_: Exception) {
        null
    }
}

private fun installApk(
    context: Context,
    downloadedUri: Uri
) {
    try {
        Toast.makeText(
            context,
            R.string.update_toast_download_complete,
            Toast.LENGTH_SHORT
        ).show()

        val apkUri: Uri = when {
            downloadedUri.scheme == "content" -> {
                downloadedUri
            }

            downloadedUri.scheme == "file" -> {
                val path = downloadedUri.path
                    ?: throw IllegalStateException(
                        "Caminho do APK inválido."
                    )

                val file = File(path)

                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
            }

            else -> {
                val file = File(
                    downloadedUri.path
                        ?: throw IllegalStateException(
                            "Arquivo de atualização inválido."
                        )
                )

                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
            }
        }

        val intent = Intent(
            Intent.ACTION_VIEW
        ).apply {
            setDataAndType(
                apkUri,
                "application/vnd.android.package-archive"
            )

            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_GRANT_READ_URI_PERMISSION

            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            val settingsIntent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(settingsIntent)

            Toast.makeText(
                context,
                "Permita a instalação de fontes desconhecidas e abra o APK novamente.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        if (
            intent.resolveActivity(
                context.packageManager
            ) != null
        ) {
            context.startActivity(intent)
        } else {
            Toast.makeText(
                context,
                R.string.update_toast_no_installer,
                Toast.LENGTH_LONG
            ).show()
        }

    } catch (e: Exception) {
        Toast.makeText(
            context,
            context.getString(
                R.string.update_toast_install_error,
                e.message ?: "Erro desconhecido"
            ),
            Toast.LENGTH_LONG
        ).show()
    }
}

private fun openWebsite(context: Context) {
    val websiteUrl =
        "https://synvertexstudios.github.io/Auris-website/download"

    try {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(websiteUrl)
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Não foi possível abrir o site.",
            Toast.LENGTH_SHORT
        ).show()
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 B"

    val units = arrayOf(
        "B",
        "KB",
        "MB",
        "GB"
    )

    var value = bytes.toDouble()
    var index = 0

    while (
        value >= 1024 &&
        index < units.lastIndex
    ) {
        value /= 1024
        index++
    }

    return if (index == 0) {
        "${value.toInt()} ${units[index]}"
    } else {
        String.format(
            java.util.Locale.getDefault(),
            "%.1f %s",
            value,
            units[index]
        )
    }
}

/*
 * Helper para manter as strings centralizadas no strings.xml
 * sem precisar de Context em todos os componentes.
 */
@Composable
private fun stringResourceSafe(
    id: Int
): String {
    val context = LocalContext.current
    return context.getString(id)
}