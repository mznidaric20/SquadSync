package hr.foi.squadsync.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SquadBlue,
    onPrimary = Color.White,
    primaryContainer = SquadIce,
    onPrimaryContainer = Color(0xFF1649A8),
    secondary = Color(0xFF47627E),
    background = Color(0xFFF2F5FB),
    onBackground = SquadNavy,
    surface = Color.White,
    onSurface = SquadNavy,
    surfaceVariant = Color(0xFFF3F6FC),
    onSurfaceVariant = Color(0xFF5D6D83),
    outline = Color(0xFF8190A6),
    outlineVariant = Color(0xFFDCE4F0),
    error = Color(0xFFBA2944),
    errorContainer = Color(0xFFFFEDF0),
    onErrorContainer = Color(0xFF8A1930)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF99BFFF),
    onPrimary = Color(0xFF062657),
    primaryContainer = Color(0xFF19375D),
    onPrimaryContainer = Color(0xFFCFE3FF),
    secondary = SquadCyan,
    background = Color(0xFF091426),
    onBackground = Color(0xFFE8F0FF),
    surface = Color(0xFF111F36),
    onSurface = Color(0xFFE8F0FF),
    surfaceVariant = Color(0xFF182941),
    onSurfaceVariant = Color(0xFFA6B6CD),
    outline = Color(0xFF8195B0),
    outlineVariant = Color(0xFF34455F),
    error = Color(0xFFFFA5B3),
    errorContainer = Color(0xFF4E2030),
    onErrorContainer = Color(0xFFFFD9DF)
)

@Composable
fun SquadSyncTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}