package id.pengingat.app.service

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Tema lokal sederhana untuk layar-layar service (alarm, check-in, popup),
 * agar tidak tergantung pada file tema UI utama. Warna sama dengan PRD §6.
 */
private val ServiceColors = darkColorScheme(
    primary = Color(0xFFD4AF37),
    onPrimary = Color(0xFF0B1026),
    secondary = Color(0xFF2E9E77),
    background = Color(0xFF0B1026),
    onBackground = Color(0xFFF5EFE0),
    surface = Color(0xFF141B3D),
    onSurface = Color(0xFFF5EFE0),
    surfaceVariant = Color(0xFF1C2650),
    onSurfaceVariant = Color(0xFFB9BED6),
    error = Color(0xFFE07A7A)
)

@Composable
fun ServiceTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ServiceColors, content = content)
}

/** Latar pemandangan untuk layar service + tirai gelap agar teks terbaca. */
@Composable
fun ServiceBackground(
    @androidx.annotation.DrawableRes resId: Int,
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize()
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(resId),
            contentDescription = null,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = androidx.compose.ui.Modifier.fillMaxSize()
        )
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(
                            Color(0xFF0B1026).copy(alpha = 0.45f),
                            Color(0xFF0B1026).copy(alpha = 0.78f),
                            Color(0xFF0B1026).copy(alpha = 0.93f)
                        )
                    )
                )
        )
        content()
    }
}
