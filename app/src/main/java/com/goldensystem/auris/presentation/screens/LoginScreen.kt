package com.goldensystem.auris.presentation.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.goldensystem.auris.R
import com.goldensystem.auris.presentation.viewmodel.AuthUiState
import com.goldensystem.auris.presentation.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Já logado? Vai direto
    LaunchedEffect(Unit) {
        if (viewModel.isLoggedIn()) {
            onLoginSuccess()
        }
    }

    // Observa mudanças de estado
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.Success -> onLoginSuccess()
            is AuthUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    // Launcher Google Sign-In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { viewModel.signInWithGoogle(it) }
                    ?: Toast.makeText(context, "Token Google nulo", Toast.LENGTH_LONG).show()
            } catch (e: ApiException) {
                Toast.makeText(context, "Erro Google: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Estados locais
    var showEmailForm by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }

    var showPhoneForm by remember { mutableStateOf(false) }
    var phoneNumber by remember { mutableStateOf("") }
    var smsCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }

    val isLoading = uiState is AuthUiState.Loading

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Voltar"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))

                // Logo
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(96.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.rounded_account_circle_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Bem-vindo ao Auris",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Entre para sincronizar suas músicas, playlists e preferências",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(40.dp))

                // Botões principais
                if (!showEmailForm && !showPhoneForm) {
                    AuthProviderButton(
                        text = "Continuar com Google",
                        iconRes = R.drawable.ic_google,
                        enabled = !isLoading,
                        onClick = {
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(context.getString(R.string.default_web_client_id))
                                .requestEmail()
                                .build()
                            val client = GoogleSignIn.getClient(context, gso)
                            googleSignInLauncher.launch(client.signInIntent)
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    AuthProviderButton(
                        text = "Continuar com GitHub",
                        iconRes = R.drawable.ic_github,
                        enabled = !isLoading,
                        onClick = {
                            activity?.let { viewModel.signInWithGitHub(it) }
                                ?: Toast.makeText(
                                    context,
                                    "Activity indisponível",
                                    Toast.LENGTH_SHORT
                                ).show()
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    AuthProviderButton(
                        text = "Continuar com E-mail",
                        icon = {
                            Icon(
                                Icons.Rounded.Email,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        enabled = !isLoading,
                        onClick = { showEmailForm = true }
                    )

                    Spacer(Modifier.height(12.dp))

                    AuthProviderButton(
                        text = "Continuar com Telefone",
                        icon = {
                            Icon(
                                Icons.Rounded.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        enabled = !isLoading,
                        onClick = { showPhoneForm = true }
                    )
                }

                // Form E-mail
                AnimatedVisibility(
                    visible = showEmailForm,
                    enter = fadeIn() + expandVertically(spring(stiffness = Spring.StiffnessMedium)),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("E-mail") },
                            singleLine = true,
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Senha") },
                            singleLine = true,
                            enabled = !isLoading,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    showEmailForm = false
                                    email = ""
                                    password = ""
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text("Voltar") }

                            Button(
                                onClick = {
                                    if (isSignUp) viewModel.signUpWithEmail(email, password)
                                    else viewModel.signInWithEmail(email, password)
                                },
                                enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                                modifier = Modifier.weight(1f)
                            ) { Text(if (isSignUp) "Cadastrar" else "Entrar") }
                        }
                        TextButton(
                            onClick = { isSignUp = !isSignUp },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(if (isSignUp) "Já tem conta? Entrar" else "Não tem conta? Cadastrar")
                        }
                    }
                }

                // Form Telefone
                AnimatedVisibility(
                    visible = showPhoneForm,
                    enter = fadeIn() + expandVertically(spring(stiffness = Spring.StiffnessMedium)),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (verificationId == null) {
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                label = { Text("Telefone (+55...)") },
                                singleLine = true,
                                enabled = !isLoading,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            OutlinedTextField(
                                value = smsCode,
                                onValueChange = { smsCode = it },
                                label = { Text("Código SMS") },
                                singleLine = true,
                                enabled = !isLoading,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    showPhoneForm = false
                                    verificationId = null
                                    smsCode = ""
                                    phoneNumber = ""
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text("Voltar") }

                            Button(
                                onClick = {
                                    if (verificationId == null) {
                                        activity?.let { act ->
                                            viewModel.startPhoneVerification(
                                                activity = act,
                                                phoneNumber = phoneNumber,
                                                callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                                                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                                        // Firebase detectou o SMS sozinho — loga direto
                                                        viewModel.signInWithPhoneCredential(credential)
                                                    }

                                                    override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                                                        viewModel.notifyPhoneError(e.message ?: "Falha na verificação")
                                                    }

                                                    override fun onCodeSent(
                                                        id: String,
                                                        token: PhoneAuthProvider.ForceResendingToken
                                                    ) {
                                                        verificationId = id
                                                        viewModel.notifyPhoneCodeSent(id, token)
                                                    }
                                                }
                                            )
                                        }
                                    } else {
                                        viewModel.verifyPhoneCode(verificationId!!, smsCode)
                                    }
                                },
                                enabled = !isLoading && (
                                    if (verificationId == null) phoneNumber.isNotBlank()
                                    else smsCode.isNotBlank()
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (verificationId == null) "Enviar código" else "Verificar")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "Ao continuar, você aceita nossos Termos de Uso e Política de Privacidade",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))
            }
        }

        // Overlay de loading
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Entrando...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthProviderButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    iconRes: Int? = null,
    icon: (@Composable () -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (iconRes != null) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    icon?.invoke()
                }
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
        }
    }
}