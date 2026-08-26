// ui/theme/Theme.kt
package com.goldensystem.auris.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.goldensystem.auris.data.preferences.WallpaperType
import com.goldensystem.auris.presentation.viewmodel.ColorSchemePair
import com.goldensystem.auris.presentation.viewmodel.CustomThemeViewModel
import java.io.File

val LocalAurisDarkTheme = staticCompositionLocalOf { false }

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun AurisStatusBarStyle(
    color: Color,
    useDarkIcons: Boolean = ColorUtils.calculateLuminance(color.toArgb()) > 0.55
) {
    val view = LocalView.current
    if (view.isInEditMode) return

    val colorArgb = color.toArgb()
    SideEffect {
        val window = view.context.findActivity()?.window ?: return@SideEffect
        @Suppress("DEPRECATION")
        window.statusBarColor = colorArgb
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = useDarkIcons
    }
}

val DarkColorScheme = darkColorScheme(
    primary = AurisPurplePrimary,
    secondary = AurisPink,
    tertiary = AurisOrange,
    background = AurisPurpleDark,
    surface = AurisSurface,
    onPrimary = AurisWhite,
    onSecondary = AurisWhite,
    onTertiary = AurisWhite,
    onBackground = AurisWhite,
    onSurface = AurisLightPurple,
    error = Color(0xFFFF5252),
    onError = AurisWhite
)

val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color(0xFF000000),
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightPrimary,
    onSecondary = Color(0xFF000000),
    secondaryContainer = LightPrimary.copy(alpha = 0.2f),
    onSecondaryContainer = Color(0xFF000000),
    tertiary = Color(0xFF4D3E00),
    onTertiary = Color(0xFFFFC107),
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutline.copy(alpha = 0.6f),
    surfaceTint = LightPrimary,
    error = Color(0xFFD32F2F),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun AurisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorSchemePairOverride: ColorSchemePair? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val viewModel: CustomThemeViewModel = hiltViewModel()
    val config by viewModel.customThemeConfig.collectAsStateWithLifecycle()

    // UM ÚNICO colorScheme
    val finalColorScheme = if (config.isEnabled) {
        customColorScheme(config, isDark = true)
    } else {
        if (darkTheme) DarkColorScheme else LightColorScheme
    }

    val statusBarElevation = if (darkTheme) 4.dp else 12.dp
    val elevatedSurface = finalColorScheme.surfaceColorAtElevation(statusBarElevation)
    val defaultStatusBarColor = Color(
        ColorUtils.blendARGB(
            finalColorScheme.background.toArgb(),
            elevatedSurface.toArgb(),
            0.35f
        )
    )

    AurisStatusBarStyle(color = defaultStatusBarColor)

    CompositionLocalProvider(LocalAurisDarkTheme provides darkTheme) {
        LaunchedEffect(config.wallpaperUri) {
            if (config.wallpaperType == WallpaperType.GALLERY && config.wallpaperUri != null) {
                val file = File(config.wallpaperUri!!)
                if (!file.exists()) {
                    viewModel.resetWallpaper()
                    Log.d("Theme", "Wallpaper não encontrado, resetando para cor sólida")
                }
            }
        }

        // UM ÚNICO MaterialTheme
        MaterialTheme(
    colorScheme = finalColorScheme,
    typography = Typography,
    shapes = Shapes
) {
    // SEMPRE TEM UM BOX, MESMO SE DESABILITADO
    Box(modifier = Modifier.fillMaxSize()) {
        // Wallpaper só se habilitado
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
                    val uri = config.wallpaperUri
                    if (uri != null) {
                        val file = File(uri)
                        if (file.exists()) {
                            val blurRadius = config.wallpaperBlur * 18f
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(file)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Wallpaper da galeria",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            Modifier.graphicsLayer {
                                                renderEffect = RenderEffect
                                                    .createBlurEffect(
                                                        blurRadius,
                                                        blurRadius,
                                                        Shader.TileMode.MIRROR
                                                    )
                                                    .asComposeRenderEffect()
                                            }
                                        } else {
                                            Modifier.blur(radius = blurRadius.dp)
                                        }
                                    ),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(config.backgroundColor))
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(config.backgroundColor))
                        )
                    }
                }
                WallpaperType.SERVER -> {
                    config.wallpaperUrl?.let { url ->
                        val blurRadius = config.wallpaperBlur * 18f
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(url)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Wallpaper do servidor",
                            modifier = Modifier
                                .fillMaxSize()
                                .then(
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        Modifier.graphicsLayer {
                                            renderEffect = RenderEffect
                                                .createBlurEffect(
                                                    blurRadius,
                                                    blurRadius,
                                                    Shader.TileMode.MIRROR
                                                )
                                                .asComposeRenderEffect()
                                        }
                                    } else {
                                        Modifier.blur(radius = blurRadius.dp)
                                    }
                                ),
                            contentScale = ContentScale.Crop
                        )
                    } ?: run {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(config.backgroundColor))
                        )
                    }
                }
            }

            if (config.wallpaperType != WallpaperType.SOLID && config.wallpaperDim > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = config.wallpaperDim))
                )
            }
        }
        
        // SEMPRE RENDERIZA O CONTEÚDO, NA MESMA POSIÇÃO
        content()
    }
}
    }
}