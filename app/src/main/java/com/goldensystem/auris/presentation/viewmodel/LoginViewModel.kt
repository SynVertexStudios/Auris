package com.goldensystem.auris.presentation.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    data class Success(val user: FirebaseUser) : AuthUiState()
    data class PhoneCodeSent(
        val verificationId: String,
        val token: PhoneAuthProvider.ForceResendingToken
    ) : AuthUiState()
}

enum class AuthProvider(val displayName: String) {
    GOOGLE("Google"),
    GITHUB("GitHub"),
    EMAIL("E-mail"),
    PHONE("Telefone")
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUser: FirebaseUser? get() = auth.currentUser

    fun isLoggedIn(): Boolean = auth.currentUser != null

    // ============ GOOGLE ============
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val user = result.user
                if (user != null) {
                    _uiState.value = AuthUiState.Success(user)
                } else {
                    _uiState.value = AuthUiState.Error("Falha ao obter usuário do Google")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Erro no login Google")
            }
        }
    }

    // ============ GITHUB (via OAuthProvider) ============
    fun signInWithGitHub(activity: Activity) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val provider = OAuthProvider.newBuilder("github.com")
                    .setScopes(listOf("user:email"))
                    .build()

                val pendingResult = auth
                    .startActivityForSignInWithProvider(activity, provider)
                    .await()

                val user = pendingResult.user
                if (user != null) {
                    _uiState.value = AuthUiState.Success(user)
                } else {
                    _uiState.value = AuthUiState.Error("Falha ao obter usuário do GitHub")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Erro no login GitHub")
            }
        }
    }

    // ============ EMAIL ============
    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user
                if (user != null) {
                    _uiState.value = AuthUiState.Success(user)
                } else {
                    _uiState.value = AuthUiState.Error("Falha ao obter usuário")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Erro no login com e-mail")
            }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user
                if (user != null) {
                    _uiState.value = AuthUiState.Success(user)
                } else {
                    _uiState.value = AuthUiState.Error("Falha ao criar conta")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Erro no cadastro")
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                auth.sendPasswordResetEmail(email).await()
                _uiState.value = AuthUiState.Idle
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Erro ao enviar reset")
            }
        }
    }

    // ============ PHONE ============
    fun startPhoneVerification(
        activity: Activity,
        phoneNumber: String,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        _uiState.value = AuthUiState.Loading
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60L,
            TimeUnit.SECONDS,
            activity,
            callbacks
        )
    }

    fun verifyPhoneCode(verificationId: String, code: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val credential = PhoneAuthProvider.getCredential(verificationId, code)
                val result = auth.signInWithCredential(credential).await()
                val user = result.user
                if (user != null) {
                    _uiState.value = AuthUiState.Success(user)
                } else {
                    _uiState.value = AuthUiState.Error("Falha na verificação do telefone")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Código inválido")
            }
        }
    }

    /**
     * Auto-resolução quando o Firebase detecta o SMS sozinho (sem o usuário digitar).
     * Neste caso, já temos um PhoneAuthCredential pronto — basta logar direto.
     */
    fun signInWithPhoneCredential(credential: com.google.firebase.auth.PhoneAuthCredential) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val result = auth.signInWithCredential(credential).await()
                val user = result.user
                if (user != null) {
                    _uiState.value = AuthUiState.Success(user)
                } else {
                    _uiState.value = AuthUiState.Error("Falha na verificação automática")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Erro na verificação")
            }
        }
    }

    fun notifyPhoneCodeSent(
        verificationId: String,
        token: PhoneAuthProvider.ForceResendingToken
    ) {
        _uiState.value = AuthUiState.PhoneCodeSent(verificationId, token)
    }

    fun notifyPhoneError(message: String) {
        _uiState.value = AuthUiState.Error(message)
    }

    // ============ SIGN OUT ============
    fun signOut() {
        auth.signOut()
        _uiState.value = AuthUiState.Idle
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}