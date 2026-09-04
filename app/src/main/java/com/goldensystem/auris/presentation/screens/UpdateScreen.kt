package com.goldensystem.auris.presentation.screens

import android.Manifest
import android.app.DownloadManager
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
    var downloadError by remember { mutableStateOf<String?>(null) }
    var isInstalling by remember { mutableStateOf(false) }

    // Launcher para permissão de instalação (Android 8+)
    val installPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Verifica se a permissão foi concedida
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (context.packageManager.canRequestPackageInstalls()) {
                // Tenta instalar novamente
                downloadId?.let { id ->
                    checkDownloadStatus(context, id) { filePath ->
                        if (filePath != null) {
                            installApk(context, filePath)
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

    // Launcher para permissão de armazenamento (Android 6-9)
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startDownload(context, updateInfo) { id ->
                downloadId = id
                isDownloading = true
                downloadError = null
            }
        } else {
            Toast.makeText(
                context,
                "Permissão de armazenamento necessária para baixar a atualização",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    LaunchedEffect(downloadId) {
        val id = downloadId ?: return@LaunchedEffect
        isDownloading = true
        
        val result = checkDownloadStatus(context, id) { filePath ->
            if (filePath != null) {
                isDownloading = false
                isInstalling = true
                
                // Verifica permissão de instalação no Android 8+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (!context.packageManager.canRequestPackageInstalls()) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:${context.packageName}")
                        )
                        installPermissionLauncher.launch(intent)
                        return@checkDownloadStatus
                    }
                }
                
                installApk(context, filePath)
                isInstalling = false
            } else {
                isDownloading = false
                downloadError = "Falha ao baixar atualização"
            }
        }
        
        if (result == null) {
            isDownloading = false
            downloadError = "Erro ao verificar download"
        }
    }

    Dialog(onDismissRequest = { if (!updateInfo.isRequired && !isDownloading) onCancelClick() }) {
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
                // Header
                Icon(
                    imageVector = Icons.Outlined.SystemUpdate,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(Modifier.height(12.dp))
                
                Text(
                    text = if (isDownloading) "Baixando atualização..." else "Atualização disponível",
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
                
                // Versão
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    shape = MaterialTheme.shapes.large
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
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
                            contentDescription = null
                        )
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Nova", style = MaterialTheme.typography.labelSmall)
                            Text(
                                updateInfo.version,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Changelog
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
                            onClick = { downloadError = null },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Tentar novamente")
                        }
                    }
                    else -> {
                        // Botões de ação
                        Button(
                            onClick = {
                                // Verifica permissões baseado na versão do Android
                                when {
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                                        // Android 13+: Apenas inicia o download (não precisa de permissão de notificação)
                                        startDownload(context, updateInfo) { id ->
                                            downloadId = id
                                            isDownloading = true
                                        }
                                    }
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                                        // Android 10+: Usa MediaStore, sem permissão de armazenamento
                                        startDownload(context, updateInfo) { id ->
                                            downloadId = id
                                            isDownloading = true
                                        }
                                    }
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                                        // Android 6-9: Precisa de permissão de armazenamento
                                        storagePermissionLauncher.launch(
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                                        )
                                    }
                                    else -> {
                                        // Android 5-
                                        startDownload(context, updateInfo) { id ->
                                            downloadId = id
                                            isDownloading = true
                                        }
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
                                    Icon(Icons.Outlined.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
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

// Função melhorada para iniciar download
private fun startDownload(
    context: Context,
    updateInfo: AppVersionInfo,
    onIdReceived: (Long) -> Unit
) {
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
            
            // Usa MediaStore para Android 10+ ou fallback para versões anteriores
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/vnd.android.package-archive")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                
                val uri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                
                if (uri != null) {
                    setDestinationUri(uri)
                } else {
                    // Fallback para método antigo
                    setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        fileName
                    )
                }
            } else {
                // Versões anteriores ao Android 10
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    fileName
                )
            }
        }

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val id = manager.enqueue(request)
        onIdReceived(id)
        
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
    }
}

// Função para verificar status do download
private suspend fun checkDownloadStatus(
    context: Context,
    downloadId: Long,
    onComplete: (String?) -> Unit
) = withContext(Dispatchers.IO) {
    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    var attempts = 0
    val maxAttempts = 600 // 5 minutos (500ms * 600)
    
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
                    return@withContext true
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
                        DownloadManager.ERROR_NETWORK_FAILED -> 
                            "Erro de rede"
                        DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> 
                            "Erro HTTP não tratado"
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
                    return@withContext true
                }
                DownloadManager.STATUS_PAUSED -> {
                    // Ainda baixando, continua esperando
                    delay(500)
                    attempts++
                }
                DownloadManager.STATUS_RUNNING -> {
                    // Atualiza progresso se necessário
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
    
    // Timeout
    withContext(Dispatchers.Main) {
        Toast.makeText(
            context,
            "Download demorou muito. Tente novamente.",
            Toast.LENGTH_LONG
        ).show()
        onComplete(null)
    }
    return@withContext false
}

// Função para obter caminho do arquivo baixado
private fun getDownloadedFilePath(
    context: Context,
    manager: DownloadManager,
    downloadId: Long,
    cursor: android.database.Cursor
): String? {
    return try {
        // Tenta obter URI do download
        val uri = manager.getUriForDownloadedFile(downloadId)
        
        if (uri != null && uri.scheme == "content") {
            // Para Android 10+, tenta obter o caminho real
            context.contentResolver.openFileDescriptor(uri, "r")?.use { fd ->
                val file = File(context.cacheDir, "auris_update_temp.apk")
                fd.fileDescriptor?.let { fd2 ->
                    java.io.FileInputStream(fd2).use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    return file.absolutePath
                }
            }
            return null
        }
        
        // Fallback: obtém o caminho do cursor
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

// Função melhorada para instalação
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

        // Verifica permissão de instalação (Android 8+)
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

        // Obtém URI para instalação
        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        // Concede permissão temporária
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