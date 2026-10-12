
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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
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

private enum class LoginMethod {
    NONE, EMAIL, PHONE
}

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

    var loginMethod by remember { mutableStateOf(LoginMethod.NONE) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }

    var phoneNumber by remember { mutableStateOf("") }
    var smsCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }

    val isLoading = uiState is AuthUiState.Loading

    LaunchedEffect(Unit) {
        if (viewModel.isLoggedIn()) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.Success -> onLoginSuccess()

            is AuthUiState.Error -> {
                Toast.makeText(
                    context,
                    state.message,
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetState()
            }

            else -> Unit
        }
    }

    
val googleLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.StartActivityForResult()
) { result ->
    if (result.resultCode != Activity.RESULT_OK) {
        Toast.makeText(
            context,
            "O Google cancelou ou não concluiu o login. Código: ${result.resultCode}",
            Toast.LENGTH_LONG
        ).show()
        return@rememberLauncherForActivityResult
    }

    try {
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        val account = task.getResult(ApiException::class.java)

        val idToken = account.idToken

        if (idToken.isNullOrBlank()) {
            Toast.makeText(
                context,
                "Erro: o Google não forneceu o token. Verifique o OAuth e o Firebase.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            viewModel.signInWithGoogle(idToken)
        }
    } catch (e: ApiException) {
        Toast.makeText(
            context,
            "Erro do Google (${e.statusCode}): ${e.localizedMessage}",
            Toast.LENGTH_LONG
        ).show()
    }
}


    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colors.primary.copy(alpha = 0.09f),
                        colors.surface,
                        colors.surface
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Sua conta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (loginMethod != LoginMethod.NONE) {
                                    loginMethod = LoginMethod.NONE
                                } else {
                                    onBackClick()
                                }
                            },
                            enabled = !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Voltar"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))

                // Identidade visual
                Surface(
                    modifier = Modifier.size(88.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = colors.primaryContainer,
                    tonalElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.AccountCircle,
                            contentDescription = "Conta Auris",
                            tint = colors.onPrimaryContainer,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = when (loginMethod) {
                        LoginMethod.EMAIL ->
                            if (isSignUp) "Crie sua conta" else "Bem-vindo de volta"

                        LoginMethod.PHONE ->
                            if (verificationId == null) {
                                "Entre com seu telefone"
                            } else {
                                "Verifique seu número"
                            }

                        LoginMethod.NONE -> "Bem-vindo ao Auris"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = colors.onSurface
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = when (loginMethod) {
                        LoginMethod.EMAIL ->
                            if (isSignUp) {
                                "Crie sua conta para manter sua experiência sincronizada."
                            } else {
                                "Entre para acessar suas músicas e preferências."
                            }

                        LoginMethod.PHONE ->
                            if (verificationId == null) {
                                "Você receberá um código por SMS para confirmar seu número."
                            } else {
                                "Digite o código enviado para o seu telefone."
                            }

                        LoginMethod.NONE ->
                            "Suas músicas, playlists e preferências em um só lugar."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))

                AnimatedVisibility(
                    visible = loginMethod == LoginMethod.NONE,
                    enter = fadeIn() + expandVertically(
                        spring(stiffness = Spring.StiffnessMedium)
                    ),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "CONTINUE COM",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(
                                start = 4.dp,
                                bottom = 4.dp
                            )
                        )

                        AuthProviderButton(
                            text = "Continuar com Google",
                            iconRes = R.drawable.ic_google,
                            enabled = !isLoading
                        ) {
                            val options = GoogleSignInOptions.Builder(
                                GoogleSignInOptions.DEFAULT_SIGN_IN
                            )
                                .requestIdToken(
                                    context.getString(
                                        R.string.default_web_client_id
                                    )
                                )
                                .requestEmail()
                                .build()

                            googleLauncher.launch(
                                GoogleSignIn.getClient(context, options).signInIntent
                            )
                        }

                        AuthProviderButton(
                            text = "Continuar com GitHub",
                            iconRes = R.drawable.ic_github,
                            enabled = !isLoading
                        ) {
                            activity?.let {
                                viewModel.signInWithGitHub(it)
                            } ?: Toast.makeText(
                                context,
                                "Activity indisponível.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        AuthProviderButton(
                            text = "Continuar com E-mail",
                            icon = {
                                Icon(
                                    Icons.Rounded.Email,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            enabled = !isLoading
                        ) {
                            loginMethod = LoginMethod.EMAIL
                        }

                        AuthProviderButton(
                            text = "Continuar com Telefone",
                            icon = {
                                Icon(
                                    Icons.Rounded.Phone,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            enabled = !isLoading
                        ) {
                            loginMethod = LoginMethod.PHONE
                        }
                    }
                }

                // Login e cadastro por e-mail
                AnimatedVisibility(
                    visible = loginMethod == LoginMethod.EMAIL,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("E-mail") },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Email,
                                    contentDescription = null
                                )
                            },
                            placeholder = { Text("voce@exemplo.com") },
                            singleLine = true,
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Senha") },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Lock,
                                    contentDescription = null
                                )
                            },
                            singleLine = true,
                            enabled = !isLoading,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Button(
                            onClick = {
                                if (isSignUp) {
                                    viewModel.signUpWithEmail(email.trim(), password)
                                } else {
                                    viewModel.signInWithEmail(email.trim(), password)
                                }
                            },
                            enabled = !isLoading &&
                                email.isNotBlank() &&
                                password.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                if (isSignUp) "Criar minha conta" else "Entrar",
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                Icons.Rounded.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        TextButton(
                            onClick = { isSignUp = !isSignUp },
                            enabled = !isLoading,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                if (isSignUp) {
                                    "Já tem uma conta? Entrar"
                                } else {
                                    "Ainda não tem conta? Cadastre-se"
                                }
                            )
                        }
                    }
                }

                // Autenticação por telefone
                AnimatedVisibility(
                    visible = loginMethod == LoginMethod.PHONE,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (verificationId == null) {
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Número de telefone") },
                                placeholder = { Text("+5511999999999") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.Phone,
                                        contentDescription = null
                                    )
                                },
                                singleLine = true,
                                enabled = !isLoading,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                        } else {
                            OutlinedTextField(
                                value = smsCode,
                                onValueChange = {
                                    smsCode = it.filter(Char::isDigit)
                                        .take(6)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Código de verificação") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.VerifiedUser,
                                        contentDescription = null
                                    )
                                },
                                singleLine = true,
                                enabled = !isLoading,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }

                        Button(
                            onClick = {
                                if (verificationId == null) {
                                    activity?.let { act ->
                                        viewModel.startPhoneVerification(
                                            activity = act,
                                            phoneNumber = phoneNumber.trim(),
                                            callbacks = object :
                                                PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                                                override fun onVerificationCompleted(
                                                    credential: PhoneAuthCredential
                                                ) {
                                                    viewModel.signInWithPhoneCredential(
                                                        credential
                                                    )
                                                }

                                                override fun onVerificationFailed(
                                                    e: com.google.firebase.FirebaseException
                                                ) {
                                                    viewModel.notifyPhoneError(
                                                        e.message
                                                            ?: "Falha na verificação."
                                                    )
                                                }

                                                override fun onCodeSent(
                                                    id: String,
                                                    token: PhoneAuthProvider.ForceResendingToken
                                                ) {
                                                    verificationId = id
                                                    viewModel.notifyPhoneCodeSent(
                                                        id,
                                                        token
                                                    )
                                                }
                                            }
                                        )
                                    } ?: Toast.makeText(
                                        context,
                                        "Activity indisponível.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    viewModel.verifyPhoneCode(
                                        verificationId!!,
                                        smsCode
                                    )
                                }
                            },
                            enabled = !isLoading && (
                                if (verificationId == null) {
                                    phoneNumber.isNotBlank()
                                } else {
                                    smsCode.length == 6
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                if (verificationId == null) {
                                    "Enviar código SMS"
                                } else {
                                    "Verificar código"
                                },
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                // Indicador de segurança
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceContainer)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = "Sua conta e suas preferências em segurança.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Ao continuar, você concorda com os Termos de Uso e a Política de Privacidade do Auris.",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(24.dp))
            }
        }

        // Bloqueio visual durante a autenticação
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.36f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = colors.surfaceContainerHigh,
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = 36.dp,
                            vertical = 28.dp
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            strokeWidth = 3.dp
                        )

                        Text(
                            text = "Conectando ao Auris...",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "Aguarde um instante",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
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
    iconRes: Int? = null,
    icon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        contentColor = colors.onSurface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = colors.outlineVariant.copy(alpha = 0.75f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(28.dp),
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

            Spacer(Modifier.width(14.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Rounded.ArrowForward,
                contentDescription = null,
                tint = colors.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
