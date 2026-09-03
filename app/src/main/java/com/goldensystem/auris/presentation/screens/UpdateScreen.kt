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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.goldensystem.auris.BuildConfig
import com.goldensystem.auris.R
import com.goldensystem.auris.data.model.AppVersionInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

private enum class UpdateState {
    IDLE,
    DOWNLOADING,
    READY_TO_INSTALL,
    FAILED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateScreen(
    updateInfo: AppVersionInfo,
    onCancelClick: () -> Unit,
    onRemindLaterClick: () -> Unit
) {
    val context = LocalContext.current

    /*
     * O nome do arquivo funciona como identificador da atualização.
     *
     * Exemplo:
     * auris_update_1_2_3.apk
     */
    val updateFileName = remember(updateInfo.version) {
        getUpdateFileName(updateInfo.version)
    }

    var downloadId by remember { mutableStateOf<Long?>(null) }

    var progress by remember {
        mutableIntStateOf(0)
    }

    var downloadedBytes by remember {
        mutableLongStateOf(0L)
    }

    var totalBytes by remember {
        mutableLongStateOf(0L)
    }

    var updateState by remember {
        mutableStateOf(
            if (findExistingUpdateFile(context, updateFileName) != null) {
                UpdateState.READY_TO_INSTALL
            } else {
                UpdateState.IDLE
            }
        )
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    /*
     * Instala diretamente o arquivo já existente.
     */
    fun installExistingApk() {
        val file = findExistingUpdateFile(
            context,
            updateFileName
        )

        if (file != null && file.exists() && file.length() > 0L) {
            installApk(
                context = context,
                apkFile = file
            )
        } else {
            updateState = UpdateState.IDLE
        }
    }

    /*
     * Inicia um novo download.
     */
    fun startDownload() {
        errorMessage = null
        progress = 0
        downloadedBytes = 0L
        totalBytes = 0L

        /*
         * Antes de baixar, verifica novamente se o arquivo já existe.
         */
        val existingFile = findExistingUpdateFile(
            context,
            updateFileName
        )

        if (
            existingFile != null &&
            existingFile.exists() &&
            existingFile.length() > 0L
        ) {
            updateState = UpdateState.READY_TO_INSTALL
            return
        }

        try {
            startDownloadUnique(
                context = context,
                updateInfo = updateInfo
            ) { id ->

                downloadId = id
                updateState = UpdateState.DOWNLOADING
            }
        } catch (e: Exception) {
            updateState = UpdateState.FAILED
            errorMessage = e.message
        }
    }

    /*
     * Permissão de notificações.
     */
    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            startDownload()
        }

    /*
     * Monitora o DownloadManager.
     */
    LaunchedEffect(downloadId, updateState) {
        val id = downloadId
            ?: return@LaunchedEffect

        if (updateState != UpdateState.DOWNLOADING) {
            return@LaunchedEffect
        }

        val manager =
            context.getSystemService(
                Context.DOWNLOAD_SERVICE
            ) as DownloadManager

        while (true) {

            val result = withContext(Dispatchers.IO) {
                queryDownload(
                    manager = manager,
                    id = id
                )
            }

            if (result == null) {
                /*
                 * O download pode ter desaparecido do DownloadManager.
                 * Antes de declarar erro, verificamos se o arquivo existe.
                 */
                val existingFile =
                    findExistingUpdateFile(
                        context,
                        updateFileName
                    )

                if (
                    existingFile != null &&
                    existingFile.exists() &&
                    existingFile.length() > 0L
                ) {
                    progress = 100
                    updateState =
                        UpdateState.READY_TO_INSTALL
                } else {
                    updateState =
                        UpdateState.FAILED

                    errorMessage =
                        context.getString(
                            R.string.update_toast_download_failed
                        )
                }

                break
            }

            downloadedBytes = result.downloaded
            totalBytes = result.total

            if (result.total > 0L) {
                progress = (
                    result.downloaded
                        .coerceAtLeast(0L)
                        .coerceAtMost(result.total)
                        .times(100L)
                        .div(result.total)
                    ).toInt()
            }

            when (result.status) {

                DownloadManager.STATUS_SUCCESSFUL -> {
                    progress = 100

                    /*
                     * O arquivo agora deve existir no Downloads.
                     */
                    val file =
                        findExistingUpdateFile(
                            context,
                            updateFileName
                        )

                    if (
                        file != null &&
                        file.exists() &&
                        file.length() > 0L
                    ) {
                        updateState =
                            UpdateState.READY_TO_INSTALL
                    } else {
                        updateState =
                            UpdateState.FAILED

                        errorMessage =
                            context.getString(
                                R.string.update_toast_no_installer
                            )
                    }

                    break
                }

                DownloadManager.STATUS_FAILED -> {
                    /*
                     * Verificação final.
                     */
                    val file =
                        findExistingUpdateFile(
                            context,
                            updateFileName
                        )

                    if (
                        file != null &&
                        file.exists() &&
                        file.length() > 0L
                    ) {
                        progress = 100
                        updateState =
                            UpdateState.READY_TO_INSTALL
                    } else {
                        updateState =
                            UpdateState.FAILED

                        errorMessage =
                            context.getString(
                                R.string.update_toast_download_failed
                            )
                    }

                    break
                }
            }

            /*
             * 750 ms é suficiente para uma barra fluida
             * sem consultar o DownloadManager milhares de vezes.
             */
            delay(750)
        }
    }

    Dialog(
        onDismissRequest = {
            if (
                !updateInfo.isRequired &&
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
                .animateContentSize(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surfaceContainerHigh
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

                Spacer(
                    Modifier.height(20.dp)
                )

                VersionCard(
                    currentVersion =
                        BuildConfig.VERSION_NAME,
                    newVersion =
                        updateInfo.version
                )

                updateInfo.changelog
                    ?.takeIf { it.isNotBlank() }
                    ?.let { changelog ->

                        Spacer(
                            Modifier.height(16.dp)
                        )

                        ChangelogCard(
                            changelog = changelog
                        )
                    }

                Spacer(
                    Modifier.height(20.dp)
                )

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
                                isRequired =
                                    updateInfo.isRequired,

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

                                onRemindLater =
                                    onRemindLaterClick,

                                onClose =
                                    onCancelClick
                            )
                        }

                        UpdateState.DOWNLOADING -> {
                            DownloadProgress(
                                progress = progress,
                                downloadedBytes =
                                    downloadedBytes,
                                totalBytes =
                                    totalBytes
                            )
                        }

                        UpdateState.READY_TO_INSTALL -> {
                            ReadyToInstall(
                                onInstall = {
                                    installExistingApk()
                                },
                                onWebsite = {
                                    openWebsite(context)
                                }
                            )
                        }

                        UpdateState.FAILED -> {
                            DownloadFailed(
                                errorMessage =
                                    errorMessage,

                                onRetry = {
                                    startDownload()
                                },

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
        UpdateState.DOWNLOADING ->
            Icons.Outlined.SystemUpdate

        UpdateState.READY_TO_INSTALL ->
            Icons.Outlined.CheckCircle

        UpdateState.FAILED ->
            Icons.Outlined.ErrorOutline
    }

    val title = when (state) {
        UpdateState.IDLE ->
            stringResourceSafe(
                R.string.update_title
            )

        UpdateState.DOWNLOADING ->
            "Baixando atualização"

        UpdateState.READY_TO_INSTALL ->
            "Atualização pronta"

        UpdateState.FAILED ->
            "Falha no download"
    }

    Column(
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Surface(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape),
            color =
                if (state == UpdateState.FAILED) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
        ) {
            Box(
                contentAlignment =
                    Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint =
                        if (
                            state ==
                            UpdateState.FAILED
                        ) {
                            MaterialTheme.colorScheme
                                .onErrorContainer
                        } else {
                            MaterialTheme.colorScheme
                                .onPrimaryContainer
                        }
                )
            }
        }

        Spacer(
            Modifier.height(14.dp)
        )

        Text(
            text = title,
            style =
                MaterialTheme.typography.headlineSmall,
            fontWeight =
                FontWeight.Bold
        )

        if (
            isRequired &&
            state == UpdateState.IDLE
        ) {
            Spacer(
                Modifier.height(6.dp)
            )

            Surface(
                shape =
                    MaterialTheme.shapes.small,
                color =
                    MaterialTheme.colorScheme
                        .errorContainer
            ) {
                Text(
                    text =
                        "Atualização obrigatória",
                    modifier =
                        Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                    style =
                        MaterialTheme.typography
                            .labelMedium,
                    color =
                        MaterialTheme.colorScheme
                            .onErrorContainer,
                    fontWeight =
                        FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun VersionCard(
    currentVersion: String,
    newVersion: String
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme
                        .surfaceContainerLow
            ),
        shape =
            MaterialTheme.shapes.large
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            VersionItem(
                label = "Atual",
                version = currentVersion,
                modifier =
                    Modifier.weight(1f)
            )

            Icon(
                imageVector =
                    Icons.Outlined.SystemUpdate,
                contentDescription = null,
                tint =
                    MaterialTheme.colorScheme.primary
            )

            VersionItem(
                label = "Nova",
                version = newVersion,
                modifier =
                    Modifier.weight(1f),
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
            if (alignEnd) {
                Alignment.End
            } else {
                Alignment.Start
            }
    ) {
        Text(
            text = label,
            style =
                MaterialTheme.typography.labelMedium,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant
        )

        Spacer(
            Modifier.height(3.dp)
        )

        Text(
            text = version,
            style =
                MaterialTheme.typography.titleMedium,
            fontWeight =
                FontWeight.Bold
        )
    }
}

@Composable
private fun ChangelogCard(
    changelog: String
) {
    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Icon(
                imageVector =
                    Icons.Outlined.CloudDownload,
                contentDescription = null,
                modifier =
                    Modifier.size(20.dp),
                tint =
                    MaterialTheme.colorScheme.primary
            )

            Spacer(
                Modifier.width(8.dp)
            )

            Text(
                text =
                    stringResourceSafe(
                        R.string.update_changelog_title
                    ),
                style =
                    MaterialTheme.typography
                        .titleSmall,
                fontWeight =
                    FontWeight.SemiBold
            )
        }

        Spacer(
            Modifier.height(8.dp)
        )

        HorizontalDivider()

        Spacer(
            Modifier.height(8.dp)
        )

        Text(
            text = changelog,
            style =
                MaterialTheme.typography.bodyMedium,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant
        )
    }
}

@Composable
private fun DownloadActions(
    isRequired: Boolean,
    onDownload: () -> Unit,
    onWebsite: () -> Unit,
    onRemindLater: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Button(
            onClick = onDownload,
            modifier =
                Modifier.fillMaxWidth(),
            shape =
                MaterialTheme.shapes.large,
            contentPadding =
                PaddingValues(vertical = 14.dp)
        ) {
            Icon(
                imageVector =
                    Icons.Outlined.Download,
                contentDescription = null
            )

            Spacer(
                Modifier.width(8.dp)
            )

            Text(
                text =
                    stringResourceSafe(
                        R.string.update_button_download
                    ),
                fontWeight =
                    FontWeight.SemiBold
            )
        }

        Spacer(
            Modifier.height(10.dp)
        )

        OutlinedButton(
            onClick = onWebsite,
            modifier =
                Modifier.fillMaxWidth(),
            shape =
                MaterialTheme.shapes.large,
            contentPadding =
                PaddingValues(vertical = 13.dp)
        ) {
            Icon(
                imageVector =
                    Icons.Outlined.Language,
                contentDescription = null
            )

            Spacer(
                Modifier.width(8.dp)
            )

            Text(
                text =
                    stringResourceSafe(
                        R.string.update_button_website
                    )
            )
        }

        if (!isRequired) {
            Spacer(
                Modifier.height(8.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                TextButton(
                    onClick = onRemindLater
                ) {
                    Icon(
                        imageVector =
                            Icons.Outlined.Schedule,
                        contentDescription = null,
                        modifier =
                            Modifier.size(18.dp)
                    )

                    Spacer(
                        Modifier.width(6.dp)
                    )

                    Text(
                        text =
                            stringResourceSafe(
                                R.string.update_button_remind_later
                            )
                    )
                }

                TextButton(
                    onClick = onClose
                ) {
                    Text(
                        text =
                            stringResourceSafe(
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
    val animatedProgress by
        animateFloatAsState(
            targetValue =
                progress / 100f,
            label = "download_progress"
        )

    Column(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Box(
            contentAlignment =
                Alignment.Center
        ) {

            CircularProgressIndicator(
                progress = {
                    animatedProgress
                },
                modifier =
                    Modifier.size(88.dp),
                strokeWidth = 7.dp
            )

            Text(
                text = "$progress%",
                style =
                    MaterialTheme.typography
                        .titleMedium,
                fontWeight =
                    FontWeight.Bold
            )
        }

        Spacer(
            Modifier.height(18.dp)
        )

        Text(
            text =
                "Baixando atualização…",
            style =
                MaterialTheme.typography
                    .titleMedium,
            fontWeight =
                FontWeight.SemiBold
        )

        Spacer(
            Modifier.height(8.dp)
        )

        LinearProgressIndicator(
            progress = {
                animatedProgress
            },
            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            Modifier.height(8.dp)
        )

        if (totalBytes > 0L) {
            Text(
                text =
                    "${formatBytes(downloadedBytes)} / " +
                    formatBytes(totalBytes),
                style =
                    MaterialTheme.typography
                        .bodySmall,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReadyToInstall(
    onInstall: () -> Unit,
    onWebsite: () -> Unit
) {
    Column(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector =
                Icons.Outlined.CheckCircle,
            contentDescription = null,
            modifier =
                Modifier.size(64.dp),
            tint =
                MaterialTheme.colorScheme.primary
        )

        Spacer(
            Modifier.height(12.dp)
        )

        Text(
            text =
                "Atualização pronta",
            style =
                MaterialTheme.typography.titleLarge,
            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            Modifier.height(6.dp)
        )

        Text(
            text =
                "O APK já está baixado neste dispositivo.",
            style =
                MaterialTheme.typography.bodyMedium,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant
        )

        Spacer(
            Modifier.height(18.dp)
        )

        Button(
            onClick = onInstall,
            modifier =
                Modifier.fillMaxWidth(),
            shape =
                MaterialTheme.shapes.large,
            contentPadding =
                PaddingValues(vertical = 14.dp)
        ) {
            Icon(
                imageVector =
                    Icons.Outlined.SystemUpdate,
                contentDescription = null
            )

            Spacer(
                Modifier.width(8.dp)
            )

            Text(
                text =
                    "Instalar atualização",
                fontWeight =
                    FontWeight.SemiBold
            )
        }

        Spacer(
            Modifier.height(8.dp)
        )

        OutlinedButton(
            onClick = onWebsite,
            modifier =
                Modifier.fillMaxWidth(),
            shape =
                MaterialTheme.shapes.large
        ) {
            Icon(
                imageVector =
                    Icons.Outlined.OpenInNew,
                contentDescription = null
            )

            Spacer(
                Modifier.width(8.dp)
            )

            Text(
                text =
                    "Abrir site"
            )
        }
    }
}

@Composable
private fun DownloadFailed(
    errorMessage: String?,
    onRetry: () -> Unit,
    onWebsite: () -> Unit
) {
    Column(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector =
                Icons.Outlined.ErrorOutline,
            contentDescription = null,
            modifier =
                Modifier.size(58.dp),
            tint =
                MaterialTheme.colorScheme.error
        )

        Spacer(
            Modifier.height(12.dp)
        )

        Text(
            text =
                "Não foi possível baixar",
            style =
                MaterialTheme.typography.titleLarge,
            fontWeight =
                FontWeight.Bold
        )

        if (!errorMessage.isNullOrBlank()) {
            Spacer(
                Modifier.height(6.dp)
            )

            Text(
                text = errorMessage,
                style =
                    MaterialTheme.typography.bodySmall,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )
        }

        Spacer(
            Modifier.height(18.dp)
        )

        Button(
            onClick = onRetry,
            modifier =
                Modifier.fillMaxWidth(),
            shape =
                MaterialTheme.shapes.large
        ) {
            Icon(
                imageVector =
                    Icons.Outlined.Refresh,
                contentDescription = null
            )

            Spacer(
                Modifier.width(8.dp)
            )

            Text(
                text =
                    "Tentar novamente"
            )
        }

        Spacer(
            Modifier.height(8.dp)
        )

        OutlinedButton(
            onClick = onWebsite,
            modifier =
                Modifier.fillMaxWidth(),
            shape =
                MaterialTheme.shapes.large
        ) {
            Icon(
                imageVector =
                    Icons.Outlined.OpenInNew,
                contentDescription = null
            )

            Spacer(
                Modifier.width(8.dp)
            )

            Text(
                text =
                    "Baixar pelo site"
            )
        }
    }
}

/**
 * Identificador único da atualização.
 *
 * A mesma versão sempre terá o mesmo nome:
 *
 * auris_update_1_2_3.apk
 */
private fun getUpdateFileName(
    version: String
): String {
    val cleanVersion =
        version
            .trim()
            .replace(
                Regex("[^a-zA-Z0-9._-]"),
                "_"
            )
            .replace(
                ".",
                "_"
            )

    return "auris_update_${cleanVersion}.apk"
}

/**
 * Local onde o APK é salvo.
 */
private fun getUpdateFile(
    version: String
): File {
    val downloadsDir =
        Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )

    return File(
        downloadsDir,
        getUpdateFileName(version)
    )
}

/**
 * Procura o APK da versão atual.
 */
private fun findExistingUpdateFile(
    context: Context,
    fileName: String
): File? {
    val downloadsDir =
        Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )

    val file =
        File(
            downloadsDir,
            fileName
        )

    return if (
        file.exists() &&
        file.isFile &&
        file.length() > 0L
    ) {
        file
    } else {
        null
    }
}

/**
 * Inicia o download usando exatamente o mesmo nome
 * da atualização.
 */
private fun startDownloadUnique(
    context: Context,
    updateInfo: AppVersionInfo,
    onIdReceived: (Long) -> Unit
) {
    require(
        updateInfo.downloadUrl.isNotBlank()
    ) {
        "URL de download inválida."
    }

    val downloadsDir =
        Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )

    if (!downloadsDir.exists()) {
        downloadsDir.mkdirs()
    }

    val destinationFile =
        getUpdateFile(
            updateInfo.version
        )

    /*
     * Se já existe, não baixa novamente.
     */
    if (
        destinationFile.exists() &&
        destinationFile.length() > 0L
    ) {
        return
    }

    val manager =
        context.getSystemService(
            Context.DOWNLOAD_SERVICE
        ) as DownloadManager

    /*
     * Verifica se já existe um download para
     * esse mesmo arquivo.
     */
    val existingDownloadId =
        findExistingDownload(
            manager = manager,
            file = destinationFile
        )

    if (existingDownloadId != null) {
        onIdReceived(
            existingDownloadId
        )
        return
    }

    val request =
        DownloadManager.Request(
            Uri.parse(
                updateInfo.downloadUrl
            )
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
            .setMimeType(
                "application/vnd.android.package-archive"
            )
            .setNotificationVisibility(
                DownloadManager.Request
                    .VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationUri(
                Uri.fromFile(
                    destinationFile
                )
            )

    val id =
        manager.enqueue(request)

    onIdReceived(id)
}

/**
 * Procura um download existente que esteja usando
 * exatamente o mesmo arquivo.
 */
private fun findExistingDownload(
    manager: DownloadManager,
    file: File
): Long? {
    return try {

        val query =
            DownloadManager.Query()

        manager.query(query).use { cursor ->

            val idColumn =
                cursor.getColumnIndex(
                    DownloadManager.COLUMN_ID
                )

            val localUriColumn =
                cursor.getColumnIndex(
                    DownloadManager.COLUMN_LOCAL_URI
                )

            if (
                idColumn < 0 ||
                localUriColumn < 0
            ) {
                return null
            }

            while (cursor.moveToNext()) {

                val localUri =
                    cursor.getString(
                        localUriColumn
                    )

                if (
                    localUri == null
                ) {
                    continue
                }

                val path =
                    when {
                        localUri.startsWith(
                            "file://"
                        ) -> {
                            Uri.parse(
                                localUri
                            ).path
                        }

                        else -> {
                            localUri
                        }
                    }

                if (
                    path == file.absolutePath
                ) {
                    return cursor.getLong(
                        idColumn
                    )
                }
            }

            null
        }

    } catch (_: Exception) {
        null
    }
}

/**
 * Consulta o estado do DownloadManager.
 */
private fun queryDownload(
    manager: DownloadManager,
    id: Long
): DownloadResult? {
    return try {

        val query =
            DownloadManager.Query()
                .setFilterById(id)

        manager.query(query).use { cursor ->

            if (!cursor.moveToFirst()) {
                return null
            }

            val downloaded =
                cursor.getLong(
                    cursor.getColumnIndexOrThrow(
                        DownloadManager
                            .COLUMN_BYTES_DOWNLOADED_SO_FAR
                    )
                )

            val total =
                cursor.getLong(
                    cursor.getColumnIndexOrThrow(
                        DownloadManager
                            .COLUMN_TOTAL_SIZE_BYTES
                    )
                )

            val status =
                cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                        DownloadManager
                            .COLUMN_STATUS
                    )
                )

            DownloadResult(
                downloaded = downloaded,
                total = total,
                status = status
            )
        }

    } catch (_: Exception) {
        null
    }
}

private data class DownloadResult(
    val downloaded: Long,
    val total: Long,
    val status: Int
)

/**
 * Instala o APK.
 */
private fun installApk(
    context: Context,
    apkFile: File
) {
    try {

        if (
            !apkFile.exists() ||
            apkFile.length() <= 0L
        ) {
            Toast.makeText(
                context,
                R.string.update_toast_no_installer,
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val apkUri =
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O &&
            !context.packageManager
                .canRequestPackageInstalls()
        ) {

            val settingsIntent =
                Intent(
                    Settings
                        .ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse(
                        "package:${context.packageName}"
                    )
                ).apply {
                    flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK
                }

            context.startActivity(
                settingsIntent
            )

            Toast.makeText(
                context,
                "Permita a instalação de fontes desconhecidas e tente instalar novamente.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val intent =
            Intent(
                Intent.ACTION_VIEW
            ).apply {

                setDataAndType(
                    apkUri,
                    "application/vnd.android.package-archive"
                )

                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

        if (
            intent.resolveActivity(
                context.packageManager
            ) != null
        ) {

            context.startActivity(
                intent
            )

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
                e.message
                    ?: "Erro desconhecido"
            ),
            Toast.LENGTH_LONG
        ).show()
    }
}

private fun openWebsite(
    context: Context
) {
    val websiteUrl =
        "https://synvertexstudios.github.io/Auris-website/"

    try {

        val intent =
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(websiteUrl)
            ).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK
            }

        context.startActivity(
            intent
        )

    } catch (_: Exception) {

        Toast.makeText(
            context,
            "Não foi possível abrir o site.",
            Toast.LENGTH_SHORT
        ).show()
    }
}

private fun formatBytes(
    bytes: Long
): String {

    if (bytes <= 0L) {
        return "0 B"
    }

    val units =
        arrayOf(
            "B",
            "KB",
            "MB",
            "GB"
        )

    var value =
        bytes.toDouble()

    var index = 0

    while (
        value >= 1024.0 &&
        index < units.lastIndex
    ) {
        value /= 1024.0
        index++
    }

    return if (index == 0) {

        "${value.toInt()} ${units[index]}"

    } else {

        String.format(
            Locale.getDefault(),
            "%.1f %s",
            value,
            units[index]
        )
    }
}

@Composable
private fun stringResourceSafe(
    id: Int
): String {
    val context =
        LocalContext.current

    return context.getString(id)
}