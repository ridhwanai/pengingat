package id.pengingat.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta Pengingat: navy pekat + emas + emerald + krem (PRD §6)
val NavyBg = Color(0xFF0B1026)
val NavySurface = Color(0xFF141B3D)
val NavySurfaceVar = Color(0xFF1C2650)
val Gold = Color(0xFFD4AF37)
val GoldLight = Color(0xFFE8C874)
val Emerald = Color(0xFF2E9E77)
val Cream = Color(0xFFF5EFE0)
val SoftRed = Color(0xFFE07A7A)
val MutedText = Color(0xFFB9BED6)

private val DarkColors = darkColorScheme(
    primary = Gold,
    onPrimary = NavyBg,
    secondary = Emerald,
    onSecondary = Cream,
    background = NavyBg,
    onBackground = Cream,
    surface = NavySurface,
    onSurface = Cream,
    surfaceVariant = NavySurfaceVar,
    onSurfaceVariant = MutedText,
    error = SoftRed,
    onError = NavyBg
)

// Aplikasi pada dasarnya bertema gelap; skema terang disamakan agar konsisten.
private val LightColors = lightColorScheme(
    primary = Gold,
    onPrimary = NavyBg,
    secondary = Emerald,
    background = NavyBg,
    onBackground = Cream,
    surface = NavySurface,
    onSurface = Cream,
    surfaceVariant = NavySurfaceVar,
    onSurfaceVariant = MutedText,
    error = SoftRed
)

@Composable
fun PengingatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
