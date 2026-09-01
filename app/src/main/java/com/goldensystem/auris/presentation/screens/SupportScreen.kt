// presentation/screens/SupportScreen.kt
package com.goldensystem.auris.presentation.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.goldensystem.auris.R
import com.goldensystem.auris.presentation.viewmodel.SupportViewModel
import com.goldensystem.auris.ui.theme.customColorScheme
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.TextFieldValue
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    navController: NavController,
    viewModel: SupportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val config by viewModel.customThemeConfig.collectAsStateWithLifecycle()
    val colorScheme = remember(config) { customColorScheme(config, true) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var customSubject by remember { mutableStateOf("") }
    var appVersion by remember { mutableStateOf("") }
    var androidVersion by remember { mutableStateOf("") }
    var device by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isSending by remember { mutableStateOf(false) }
    var sendStatus by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    val categories = listOf(
        "Problema / Bug",
        "Dúvida",
        "Sugestão",
        "Problema com reprodução",
        "Compatibilidade",
        "Tradução / Idioma",
        "Download / Instalação",
        "Outro"
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Suporte",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Fechar",
                            tint = colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Headset,
                            contentDescription = null,
                            tint = colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Central de Suporte",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        "Envie sua dúvida, sugestão ou problema. Responderemos o mais rápido possível.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            // Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surfaceContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Nome
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome") },
                        placeholder = { Text("Seu nome") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            focusedLabelColor = colorScheme.primary
                        )
                    )

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-mail") },
                        placeholder = { Text("seu@email.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            focusedLabelColor = colorScheme.primary
                        )
                    )

                    // Categoria
                    ExposedDropdownMenuBox(
                        expanded = false,
                        onExpandedChange = { /* handled by menu */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoria") },
                            placeholder = { Text("Selecione uma categoria") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorScheme.primary,
                                focusedLabelColor = colorScheme.primary
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = false,
                            onDismissRequest = {},
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categories.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = { category = option }
                                )
                            }
                        }
                    }

                    // Mensagem
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Mensagem") },
                        placeholder = { Text("Descreva sua dúvida, sugestão ou problema...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            focusedLabelColor = colorScheme.primary
                        )
                    )

                    // Anexo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Rounded.AttachFile,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Anexar imagem")
                        }
                        if (imageUri != null) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = "Imagem anexada",
                                tint = colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Status
                    if (sendStatus != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSuccess) 
                                colorScheme.primaryContainer 
                            else 
                                colorScheme.errorContainer
                        ) {
                            Text(
                                text = sendStatus!!,
                                modifier = Modifier.padding(12.dp),
                                color = if (isSuccess) 
                                    colorScheme.onPrimaryContainer 
                                else 
                                    colorScheme.onErrorContainer
                            )
                        }
                    }

                    // Botão Enviar
                    Button(
                        onClick = {
                            if (name.isBlank() || email.isBlank() || category.isBlank() || message.isBlank()) {
                                sendStatus = "Preencha todos os campos obrigatórios"
                                isSuccess = false
                                return@Button
                            }
                            
                            isSending = true
                            sendStatus = "Enviando..."
                            isSuccess = false
                            
                            scope.launch {
                                try {
                                    val success = viewModel.sendSupportMessage(
                                        name = name,
                                        email = email,
                                        category = category,
                                        customSubject = customSubject,
                                        appVersion = appVersion,
                                        androidVersion = androidVersion,
                                        device = device,
                                        message = message,
                                        imageUri = imageUri
                                    )
                                    if (success) {
                                        sendStatus = "Mensagem enviada com sucesso! Responderemos em breve."
                                        isSuccess = true
                                        name = ""
                                        email = ""
                                        category = ""
                                        customSubject = ""
                                        appVersion = ""
                                        androidVersion = ""
                                        device = ""
                                        message = ""
                                        imageUri = null
                                    } else {
                                        sendStatus = "Erro ao enviar mensagem. Tente novamente."
                                        isSuccess = false
                                    }
                                } catch (e: Exception) {
                                    sendStatus = "Erro: ${e.message}"
                                    isSuccess = false
                                }
                                isSending = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSending,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enviando...")
                        } else {
                            Icon(
                                Icons.Rounded.Send,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enviar mensagem")
                        }
                    }
                }
            }
        }
    }
}