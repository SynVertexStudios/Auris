package com.goldensystem.auris.presentation.screens

import android.Manifest
import android.app.DownloadManager
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.goldensystem.auris.BuildConfig
import com.goldensystem.auris.R
import com.goldensystem.auris.data.model.AppVersionInfo
import kotlinx.coroutines.*
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
    var downloadError by remember { mutableStateOf<String?>(null) }
    var isInstalling by remember { mutableStateOf(false) }
    var existingApkPath by remember { mutableStateOf<String?>(null) }
    var isCheckingExisting by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    val installPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (context.packageManager.canRequestPackageInstalls()) {
                existingApkPath?.let { path ->
                    installApk(context, path)
                } ?: run {
                    downloadId?.let { id ->
                        coroutineScope.launch {
                            checkDownloadStatus(context, id) { filePath ->
                                if (filePath != null) {
                                    installApk(context, filePath)
                                }
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(
                    context,
                    "Permissão necessária para instalar atualizações",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        startDownloadWithCheck(context, updateInfo) { id, existingPath ->
            downloadId = id
            isDownloading = true
            downloadError = null
            existingApkPath = existingPath
        }
    }

    LaunchedEffect(updateInfo) {
        isCheckingExisting = true
        existingApkPath = findExistingApk(context, updateInfo.version)
        isCheckingExisting = false
    }

    LaunchedEffect(downloadId) {
        val id = downloadId ?: return@LaunchedEffect
        isDownloading = true
        
        checkDownloadStatus(context, id) { filePath ->
            isDownloading = false
            
            if (filePath != null) {
                existingApkPath = filePath
                isInstalling = true
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (!context.packageManager.canRequestPackageInstalls()) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:${context.packageName}")
                        )
                        installPermissionLauncher.launch(intent)
                        isInstalling = false
                        return@checkDownloadStatus
                    }
                }
                
                installApk(context, filePath)
                isInstalling = false
            } else {
                downloadError = "Falha ao baixar atualização"
            }
        }
    }

    Dialog(
        onDismissRequest = { 
            if (!updateInfo.isRequired && !isDownloading && !isCheckingExisting) {
                onCancelClick()
            }
        }
    ) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = if (existingApkPath != null) 
                        MaterialTheme.colorScheme.tertiaryContainer 
                    else 
                        MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (existingApkPath != null) 
                                Icons.Outlined.CheckCircle 
                            else 
                                Icons.Outlined.SystemUpdate,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (existingApkPath != null)
                                MaterialTheme.colorScheme.onTertiaryContainer
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                
                Text(
                    text = when {
                        isCheckingExisting -> "Verificando atualização..."
                        isDownloading -> "Baixando atualização..."
                        isInstalling -> "Preparando instalação..."
                        existingApkPath != null -> "Atualização já baixada!"
                        else -> "Atualização disponível"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                if (updateInfo.isRequired) {
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = "Atualização obrigatória",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                
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
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Atual", style = MaterialTheme.typography.labelSmall)
                            Text(
                                BuildConfig.VERSION_NAME,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.Outlined.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Nova", style = MaterialTheme.typography.labelSmall)
                            Text(
                                updateInfo.version,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (existingApkPath != null)
                                    MaterialTheme.colorScheme.tertiary
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                updateInfo.changelog?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Novidades",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(20.dp))

                when {
                    isCheckingExisting -> {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Verificando arquivos...")
                    }
                    isInstalling -> {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Preparando instalação...")
                    }
                    isDownloading -> {
                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (progress > 0) "Baixando... $progress%" else "Iniciando download...",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    downloadError != null -> {
                        Text(
                            text = downloadError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { 
                                downloadError = null
                                startDownloadWithCheck(context, updateInfo) { id, existingPath ->
                                    downloadId = id
                                    isDownloading = true
                                    existingApkPath = existingPath
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Tentar novamente")
                        }
                    }
                    existingApkPath != null -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Arquivo já baixado anteriormente",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(Modifier.height(12.dp))
                            
                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        if (!context.packageManager.canRequestPackageInstalls()) {
                                            val intent = Intent(
                                                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                                Uri.parse("package:${context.packageName}")
                                            )
                                            installPermissionLauncher.launch(intent)
                                            return@Button
                                        }
                                    }
                                    existingApkPath?.let { installApk(context, it) }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                Icon(Icons.Outlined.SystemUpdate, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Instalar agora")
                            }
                            
                            Spacer(Modifier.height(8.dp))
                            
                            OutlinedButton(
                                onClick = {
                                    startDownloadWithCheck(context, updateInfo) { id, existingPath ->
                                        downloadId = id
                                        isDownloading = true
                                        existingApkPath = existingPath
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Outlined.Download, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Baixar novamente")
                            }
                            
                            if (!updateInfo.isRequired) {
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TextButton(onClick = onRemindLaterClick) {
                                        Icon(
                                            Icons.Outlined.Schedule, 
                                            contentDescription = null, 
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text("Lembrar depois")
                                    }
                                    TextButton(onClick = onCancelClick) {
                                        Text("Fechar")
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(
                                        Manifest.permission.POST_NOTIFICATIONS
                                    )
                                } else {
                                    startDownloadWithCheck(context, updateInfo) { id, existingPath ->
                                        downloadId = id
                                        isDownloading = true
                                        existingApkPath = existingPath
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.Download, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Baixar atualização")
                        }

                        Spacer(Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                val websiteUrl = "https://synvertexstudios.github.io/Auris-website/download"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl))
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.Language, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Baixar pelo site")
                        }

                        if (!updateInfo.isRequired) {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TextButton(onClick = onRemindLaterClick) {
                                    Icon(
                                        Icons.Outlined.Schedule, 
                                        contentDescription = null, 
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Lembrar depois")
                                }
                                TextButton(onClick = onCancelClick) {
                                    Text("Fechar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun findExistingApk(context: Context, version: String): String? {
    return try {
        val fileName = "auris_update_${version.replace(".", "_")}.apk"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Downloads._ID,
                MediaStore.Downloads.DISPLAY_NAME,
                MediaStore.Downloads.RELATIVE_PATH
            )
            val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ?"
            val selectionArgs = arrayOf(fileName)
            
            context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(
                        cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
                    )
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        id
                    )
                    context.contentResolver.openFileDescriptor(contentUri, "r")?.use {
                        return contentUri.toString()
                    }
                }
            }
        }
        
        val downloadsDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )
        
        if (downloadsDir != null && downloadsDir.exists()) {
            val file = File(downloadsDir, fileName)
            if (file.exists()) {
                return file.absolutePath
            }
            
            val pattern = Regex("auris_update_${version.replace(".", "_")}(?:\\\\((\\d+)\\))?\\\\.apk")
            downloadsDir.listFiles()?.forEach { f ->
                if (f.isFile && pattern.matches(f.name)) {
                    return f.absolutePath
                }
            }
        }
        
        val cacheFile = File(context.cacheDir, fileName)
        if (cacheFile.exists()) {
            return cacheFile.absolutePath
        }
        
        null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun startDownloadWithCheck(
    context: Context,
    updateInfo: AppVersionInfo,
    onResult: (Long, String?) -> Unit
) {
    val existingPath = findExistingApk(context, updateInfo.version)
    
    if (existingPath != null) {
        onResult(-1L, existingPath)
        return
    }
    
    try {
        val downloadUri = Uri.parse(updateInfo.downloadUrl)
        val fileName = "auris_update_${updateInfo.version.replace(".", "_")}.apk"
        
        val request = DownloadManager.Request(downloadUri).apply {
            setTitle("Atualização Auris")
            setDescription("Baixando versão ${updateInfo.version}")
            setMimeType("application/vnd.android.package-archive")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/vnd.android.package-archive")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        put(MediaStore.Downloads.IS_PENDING, 0)
                    }
                }
                
                val uri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                
                if (uri != null) {
                    setDestinationUri(uri)
                } else {
                    setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        fileName
                    )
                }
            } else {
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    fileName
                )
            }
        }

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val id = manager.enqueue(request)
        onResult(id, null)
        
        Toast.makeText(
            context,
            "Download iniciado em segundo plano",
            Toast.LENGTH_SHORT
        ).show()
        
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Erro ao iniciar download: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
        onResult(-1L, null)
    }
}

private suspend fun checkDownloadStatus(
    context: Context,
    downloadId: Long,
    onComplete: (String?) -> Unit
) {
    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    var attempts = 0
    val maxAttempts = 600
    
    while (attempts < maxAttempts) {
        val query = DownloadManager.Query().setFilterById(downloadId)
        
        manager.query(query).use { cursor ->
            if (!cursor.moveToFirst()) {
                delay(500)
                attempts++
                continue
            }
            
            val status = cursor.getInt(
                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
            )
            
            when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    val filePath = getDownloadedFilePath(context, manager, downloadId, cursor)
                    withContext(Dispatchers.Main) {
                        onComplete(filePath)
                    }
                    return
                }
                DownloadManager.STATUS_FAILED -> {
                    val reason = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON)
                    )
                    val errorMessage = when (reason) {
                        DownloadManager.ERROR_FILE_ALREADY_EXISTS -> 
                            "Arquivo já existe"
                        DownloadManager.ERROR_INSUFFICIENT_SPACE -> 
                            "Espaço insuficiente"
                        DownloadManager.ERROR_HTTP_DATA_ERROR -> 
                            "Erro ao baixar dados"
                        DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> 
                            "Erro HTTP não tratado"
                       DownloadManager.ERROR_NETWORK_FAILED -> "Erro de rede"
                        else -> "Erro desconhecido ($reason)"
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Falha no download: $errorMessage",
                            Toast.LENGTH_LONG
                        ).show()
                        onComplete(null)
                    }
                    return
                }
                DownloadManager.STATUS_PAUSED,
                DownloadManager.STATUS_RUNNING -> {
                    delay(500)
                    attempts++
                }
                else -> {
                    delay(500)
                    attempts++
                }
            }
        }
    }
    
    withContext(Dispatchers.Main) {
        Toast.makeText(
            context,
            "Download demorou muito. Tente novamente.",
            Toast.LENGTH_LONG
        ).show()
        onComplete(null)
    }
}

private fun getDownloadedFilePath(
    context: Context,
    manager: DownloadManager,
    downloadId: Long,
    cursor: android.database.Cursor
): String? {
    return try {
        val uri = manager.getUriForDownloadedFile(downloadId)
        
        if (uri != null && uri.scheme == "content") {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { fd ->
                val file = File(context.cacheDir, "auris_update_${System.currentTimeMillis()}.apk")
                fd.fileDescriptor?.let { fd2 ->
                    java.io.FileInputStream(fd2).use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    file.deleteOnExit()
                    return file.absolutePath
                }
            }
            return null
        }
        
        val localUri = cursor.getString(
            cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI)
        )
        
        if (!localUri.isNullOrBlank()) {
            return Uri.parse(localUri).path
        }
        
        null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun installApk(context: Context, filePath: String) {
    try {
        val file = File(filePath)
        
        if (!file.exists()) {
            Toast.makeText(
                context,
                "Arquivo de instalação não encontrado",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                Toast.makeText(
                    context,
                    "Permita a instalação de apps desconhecidos nas configurações",
                    Toast.LENGTH_LONG
                ).show()
                
                val intent = Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return
            }
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.grantUriPermission(
                "com.android.packageinstaller",
                apkUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(
                context,
                "Nenhum instalador encontrado",
                Toast.LENGTH_LONG
            ).show()
        }

    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Erro ao instalar: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
        e.printStackTrace()
    }
}