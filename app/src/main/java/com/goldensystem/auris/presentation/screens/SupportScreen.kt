// presentation/screens/SupportScreen.kt
package com.goldensystem.auris.presentation.screens

import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.goldensystem.auris.R
import com.goldensystem.auris.presentation.viewmodel.SupportViewModel
import com.goldensystem.auris.ui.theme.customColorScheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    navController: NavController,
    viewModel: SupportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val config by viewModel.customThemeConfig.collectAsStateWithLifecycle()
    val colorScheme = remember(config) {
        customColorScheme(config, true)
    }

    // ---------------------------------------------------------
    // Estado do formulário
    // ---------------------------------------------------------

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    var imageUri by remember { mutableStateOf<android.net.Uri?>(null) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }

    var sendStatus by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    // ---------------------------------------------------------
    // Informações automáticas do dispositivo
    // ---------------------------------------------------------

    val appVersion = remember {
        try {
            context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionName ?: "Desconhecida"
        } catch (_: Exception) {
            "Desconhecida"
        }
    }

    val androidVersion = remember {
        "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    }

    val device = remember {
        "${Build.MANUFACTURER} ${Build.MODEL}"
    }

    // ---------------------------------------------------------
    // Categorias
    // ---------------------------------------------------------

    val categories = remember {
        listOf(
            "Problema / Bug",
            "Dúvida",
            "Sugestão",
            "Problema com reprodução",
            "Compatibilidade",
            "Tradução / Idioma",
            "Download / Instalação",
            "Conta / Sincronização",
            "Desempenho",
            "Outro"
        )
    }

    // ---------------------------------------------------------
    // Seletor de imagem
    // ---------------------------------------------------------

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
    }

    // ---------------------------------------------------------
    // Cores auxiliares
    // ---------------------------------------------------------

    val backgroundColor = colorScheme.background
    val surfaceColor = colorScheme.surfaceContainer
    val primaryColor = colorScheme.primary

    Scaffold(
        containerColor = backgroundColor,

        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Suporte",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )

                        Text(
                            text = "Estamos aqui para ajudar",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                },

                navigationIcon = {
                    IconButton(
                        onClick = {
                            keyboardController?.hide()
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Fechar",
                            tint = colorScheme.onSurface
                        )
                    }
                },

                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = backgroundColor,
                    scrolledContainerColor = backgroundColor
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),

            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // =================================================
            // HEADER
            // =================================================

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Surface(
                            modifier = Modifier.size(52.dp),
                            shape = CircleShape,
                            color = colorScheme.primary
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.HeadsetMic,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = colorScheme.onPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Central de Suporte",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = "Fale diretamente com a equipe do Auris.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colorScheme.onPrimaryContainer.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 10.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Quanto mais detalhes você fornecer, mais fácil será resolver o problema.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // =================================================
            // FORMULÁRIO
            // =================================================

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = surfaceColor
                )
            ) {

                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // Cabeçalho do formulário

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Enviar mensagem",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )

                        Text(
                            text = "Preencha os campos abaixo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // -------------------------------------------------
                    // Nome
                    // -------------------------------------------------

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (sendStatus != null) {
                                sendStatus = null
                            }
                        },
                        label = {
                            Text("Nome")
                        },
                        placeholder = {
                            Text("Como podemos chamar você?")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Smartphone,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isSending,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor,
                            cursorColor = primaryColor
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    // -------------------------------------------------
                    // Email
                    // -------------------------------------------------

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (sendStatus != null) {
                                sendStatus = null
                            }
                        },
                        label = {
                            Text("E-mail")
                        },
                        placeholder = {
                            Text("seu@email.com")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Email,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isSending,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor,
                            cursorColor = primaryColor
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    // -------------------------------------------------
                    // Categoria
                    // -------------------------------------------------

                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = {
                            if (!isSending) {
                                categoryExpanded = !categoryExpanded
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,

                            label = {
                                Text("Categoria")
                            },

                            placeholder = {
                                Text("Selecione o assunto")
                            },

                            leadingIcon = {
                                Icon(
                                    imageVector = when {
                                        category.contains("Bug", true) ->
                                            Icons.Rounded.BugReport

                                        category.contains("Sugestão", true) ->
                                            Icons.Rounded.Lightbulb

                                        category.contains("Idioma", true) ->
                                            Icons.Rounded.Language

                                        else ->
                                            Icons.Rounded.Info
                                    },
                                    contentDescription = null
                                )
                            },

                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = categoryExpanded
                                )
                            },

                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),

                            enabled = !isSending,

                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                focusedLabelColor = primaryColor
                            ),

                            shape = RoundedCornerShape(16.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = {
                                categoryExpanded = false
                            }
                        ) {

                            categories.forEach { option ->

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option,
                                            fontWeight = if (category == option) {
                                                FontWeight.SemiBold
                                            } else {
                                                FontWeight.Normal
                                            }
                                        )
                                    },

                                    leadingIcon = {
                                        Icon(
                                            imageVector = when {
                                                option.contains("Bug", true) ->
                                                    Icons.Rounded.BugReport

                                                option.contains("Sugestão", true) ->
                                                    Icons.Rounded.Lightbulb

                                                option.contains("Idioma", true) ->
                                                    Icons.Rounded.Language

                                                option.contains("Compatibilidade", true) ->
                                                    Icons.Rounded.Devices

                                                else ->
                                                    Icons.Rounded.Info
                                            },
                                            contentDescription = null
                                        )
                                    },

                                    trailingIcon = {
                                        if (category == option) {
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircle,
                                                contentDescription = null,
                                                tint = primaryColor
                                            )
                                        }
                                    },

                                    onClick = {
                                        category = option
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // -------------------------------------------------
                    // Mensagem
                    // -------------------------------------------------

                    OutlinedTextField(
                        value = message,
                        onValueChange = {
                            if (it.length <= 3000) {
                                message = it
                            }

                            if (sendStatus != null) {
                                sendStatus = null
                            }
                        },

                        label = {
                            Text("Mensagem")
                        },

                        placeholder = {
                            Text(
                                "Descreva o problema, dúvida ou sugestão com o máximo de detalhes possível..."
                            )
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp),

                        enabled = !isSending,

                        minLines = 6,
                        maxLines = 8,

                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor,
                            cursorColor = primaryColor
                        ),

                        shape = RoundedCornerShape(16.dp),

                        supportingText = {
                            Text(
                                text = "${message.length}/3000",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        }
                    )

                    // =================================================
                    // ANEXO
                    // =================================================

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector = Icons.Rounded.AttachFile,
                                contentDescription = null,
                                tint = colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Text(
                                    text = "Anexo",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorScheme.onSurface
                                )

                                Text(
                                    text = "Opcional • JPG, PNG ou imagem equivalente",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (imageUri == null) {

                            OutlinedButton(
                                onClick = {
                                    imagePickerLauncher.launch("image/*")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isSending,
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Upload,
                                    contentDescription = null
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text("Selecionar imagem")
                            }

                        } else {

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = colorScheme.surface
                                )
                            ) {

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(imageUri)
                                            .crossfade(true)
                                            .build(),

                                        contentDescription = "Imagem anexada",

                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(12.dp)),

                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "Imagem anexada",
                                            fontWeight = FontWeight.SemiBold,
                                            color = colorScheme.onSurface
                                        )

                                        Text(
                                            text = "A imagem será enviada junto com sua mensagem.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            imageUri = null
                                        },
                                        enabled = !isSending
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Remover imagem"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // =================================================
                    // INFORMAÇÕES TÉCNICAS
                    // =================================================

                    Divider(
                        color = colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = colorScheme.surface
                    ) {

                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Devices,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = primaryColor
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Informações técnicas",
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorScheme.onSurface
                                )
                            }

                            Text(
                                text = "Dispositivo: $device",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "Android: $androidVersion",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "Versão do Auris: $appVersion",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // =================================================
                    // STATUS
                    // =================================================

                    if (sendStatus != null) {

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),

                            color = if (isSuccess) {
                                colorScheme.primaryContainer
                            } else {
                                colorScheme.errorContainer
                            }
                        ) {

                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Icon(
                                    imageVector = if (isSuccess) {
                                        Icons.Rounded.CheckCircle
                                    } else {
                                        Icons.Rounded.Info
                                    },

                                    contentDescription = null,

                                    tint = if (isSuccess) {
                                        colorScheme.onPrimaryContainer
                                    } else {
                                        colorScheme.onErrorContainer
                                    },

                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = sendStatus!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSuccess) {
                                        colorScheme.onPrimaryContainer
                                    } else {
                                        colorScheme.onErrorContainer
                                    }
                                )
                            }
                        }
                    }

                    // =================================================
                    // BOTÃO ENVIAR
                    // =================================================

                    Button(
                        onClick = {

                            keyboardController?.hide()

                            // Validação
                            when {
                                name.isBlank() -> {
                                    sendStatus = "Informe seu nome."
                                    isSuccess = false
                                    return@Button
                                }

                                email.isBlank() -> {
                                    sendStatus = "Informe seu e-mail."
                                    isSuccess = false
                                    return@Button
                                }

                                !android.util.Patterns.EMAIL_ADDRESS
                                    .matcher(email.trim())
                                    .matches() -> {

                                    sendStatus = "Informe um e-mail válido."
                                    isSuccess = false
                                    return@Button
                                }

                                category.isBlank() -> {
                                    sendStatus = "Selecione uma categoria."
                                    isSuccess = false
                                    return@Button
                                }

                                message.isBlank() -> {
                                    sendStatus = "Escreva uma mensagem."
                                    isSuccess = false
                                    return@Button
                                }

                                message.trim().length < 10 -> {
                                    sendStatus =
                                        "Descreva um pouco mais o problema para podermos ajudar."
                                    isSuccess = false
                                    return@Button
                                }
                            }

                            isSending = true
                            sendStatus = "Enviando sua mensagem..."
                            isSuccess = false

                            scope.launch {

                                try {

                                    val success = viewModel.sendSupportMessage(
                                        name = name.trim(),
                                        email = email.trim(),
                                        category = category,
                                        customSubject = "",
                                        appVersion = appVersion,
                                        androidVersion = androidVersion,
                                        device = device,
                                        message = message.trim(),
                                        imageUri = imageUri
                                    )

                                    if (success) {

                                        sendStatus =
                                            "Mensagem enviada com sucesso! Responderemos em breve."

                                        isSuccess = true

                                        name = ""
                                        email = ""
                                        category = ""
                                        message = ""
                                        imageUri = null

                                    } else {

                                        sendStatus =
                                            "Não foi possível enviar sua mensagem. Tente novamente."

                                        isSuccess = false
                                    }

                                } catch (e: Exception) {

                                    sendStatus =
                                        "Ocorreu um erro ao enviar. Verifique sua conexão e tente novamente."

                                    isSuccess = false
                                }

                                isSending = false
                            }
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),

                        enabled = !isSending,

                        shape = RoundedCornerShape(16.dp),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = colorScheme.onPrimary
                        )
                    ) {

                        if (isSending) {

                            CircularProgressIndicator(
                                modifier = Modifier.size(21.dp),
                                color = colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = "Enviando...",
                                fontWeight = FontWeight.SemiBold
                            )

                        } else {

                            Icon(
                                imageVector = Icons.Rounded.Send,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(9.dp))

                            Text(
                                text = "Enviar mensagem",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // =================================================
                    // RODAPÉ
                    // =================================================

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.width(5.dp))

                        Text(
                            text = "Obrigado por ajudar a melhorar o Auris.",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}