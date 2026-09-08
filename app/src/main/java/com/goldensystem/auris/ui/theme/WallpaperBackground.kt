package com.goldensystem.auris.ui.theme

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.goldensystem.auris.data.preferences.WallpaperType
import com.goldensystem.auris.presentation.viewmodel.CustomThemeViewModel
import com.goldensystem.auris.utils.toBlurRadius
@Composable
fun WallpaperBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val viewModel: CustomThemeViewModel = hiltViewModel()
    val config by viewModel.customThemeConfig.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // ← CALCULA O RAIO DO BLUR AQUI
    val blurRadius = config.wallpaperBlur.toBlurRadius()

    Box(modifier = modifier.fillMaxSize()) {
        // Só mostra o wallpaper se o tema personalizado estiver ativo
        if (config.isEnabled) {
            when (config.wallpaperType) {
                WallpaperType.SOLID -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(config.backgroundColor))
                    )
                }

                WallpaperType.GALLERY -> {
                    config.wallpaperUri?.let { uri ->
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(Uri.parse(uri))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Wallpaper",
                            modifier = Modifier
                                .fillMaxSize()
                                .blur(radius = blurRadius.dp), // ← ADICIONA O BLUR AQUI
                            contentScale = ContentScale.Crop
                        )
                    } ?: run {
                        // Fallback se não tiver URI - usa a cor de fundo
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(config.backgroundColor))
                        )
                    }
                }

                WallpaperType.SERVER -> {
                    config.wallpaperUrl?.let { url ->
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(url)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Wallpaper",
                            modifier = Modifier
                                .fillMaxSize()
                                .blur(radius = blurRadius.dp), // ← ADICIONA O BLUR AQUI
                            contentScale = ContentScale.Crop
                        )
                    } ?: run {
                        // Fallback se não tiver URL - usa a cor de fundo
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(config.backgroundColor))
                        )
                    }
                }
            }

            // Dim overlay (escurecimento) - só para imagens
            if (config.wallpaperType != WallpaperType.SOLID && config.wallpaperDim > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = config.wallpaperDim))
                )
            }
        }

        // Conteúdo da tela (se não tiver wallpaper, o fundo do MaterialTheme aparece)
        content()
    }
}