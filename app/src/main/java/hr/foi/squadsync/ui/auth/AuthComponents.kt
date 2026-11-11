package hr.foi.squadsync.ui.auth

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.foi.squadsync.R
import hr.foi.squadsync.domain.auth.AuthError
import hr.foi.squadsync.ui.theme.SquadBlue
import hr.foi.squadsync.ui.theme.SquadCyan
import hr.foi.squadsync.ui.theme.SquadNavy

@Composable
internal fun AuthLayout(
    registration: Boolean,
    onLogin: () -> Unit,
    onRegistration: () -> Unit,
    modifier: Modifier = Modifier,
    navigationEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    BoxWithConstraints(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).imePadding()) {
        val wide = maxWidth >= 840.dp
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally) {
            if (wide) {
                Row(Modifier.widthIn(max = 1120.dp).fillMaxWidth().statusBarsPadding().padding(32.dp),
                    horizontalArrangement = Arrangement.spacedBy(28.dp), verticalAlignment = Alignment.Top) {
                    AuthHero(registration, Modifier.weight(1f).clip(RoundedCornerShape(28.dp)), wide = true)
                    AuthFormPanel(registration, onLogin, onRegistration, navigationEnabled,
                        Modifier.weight(1f), content)
                }
            } else {
                AuthHero(registration, Modifier.fillMaxWidth())
                AuthFormPanel(registration, onLogin, onRegistration, navigationEnabled,
                    Modifier.fillMaxWidth().offset(y = (-24).dp), content)
            }
        }
    }
}

@Composable
private fun AuthFormPanel(
    registration: Boolean,
    onLogin: () -> Unit,
    onRegistration: () -> Unit,
    navigationEnabled: Boolean,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(modifier, shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            AuthTabs(registration, onLogin, onRegistration, navigationEnabled)
            content()
        }
    }
}

@Composable
private fun AuthHero(registration: Boolean, modifier: Modifier = Modifier, wide: Boolean = false) {
    Box(modifier.background(Brush.linearGradient(listOf(SquadNavy, Color(0xFF0E3670))))) {
        CourtArtwork(Modifier.matchParentSize())
        Column(Modifier.then(if (wide) Modifier else Modifier.statusBarsPadding())
            .padding(start = 24.dp, top = 20.dp, end = 24.dp, bottom = 48.dp)) {
            SquadBrand()
            Spacer(Modifier.height(if (wide) 88.dp else 24.dp))
            Text(stringResource(if (registration) R.string.register_eyebrow else R.string.login_eyebrow),
                style = MaterialTheme.typography.labelSmall, color = SquadCyan)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(if (registration) R.string.register_hero_title else R.string.login_hero_title),
                style = if (registration) MaterialTheme.typography.headlineMedium else
                    MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp, lineHeight = 36.sp),
                color = Color.White)
            if (wide) {
                Spacer(Modifier.height(12.dp))
                Text(stringResource(if (registration) R.string.register_hero_description else R.string.login_hero_description),
                    style = MaterialTheme.typography.bodyMedium, color = Color(0xFFBECEE6),
                    modifier = Modifier.widthIn(max = 300.dp))
            }
            if (!registration && wide) {
                Spacer(Modifier.height(64.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf(R.string.sport_training, R.string.sport_connect, R.string.sport_progress).forEach { text ->
                        Text(stringResource(text), color = Color(0xFFD8E9FF),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 7.dp))
                    }
                }
            }
        }
    }
}

@Composable
internal fun SquadBrand(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
        Canvas(Modifier.size(43.dp).background(SquadBlue, RoundedCornerShape(13.dp)).padding(10.dp)) {
            val w = size.width
            val h = size.height
            listOf(0.1f to 0.38f, 0.38f to 0.66f, 0.66f to 0.94f).forEachIndexed { index, (start, end) ->
                drawLine(if (index == 2) SquadCyan else Color.White,
                    Offset(w * start, h * 0.82f), Offset(w * end, h * 0.18f),
                    strokeWidth = w * 0.15f, cap = StrokeCap.Round)
            }
        }
        Column {
            Text(stringResource(R.string.app_name), color = Color.White,
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text(stringResource(R.string.brand_tagline), color = Color(0xFF9BB3D6), fontSize = 8.sp,
                letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun CourtArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier.clip(RoundedCornerShape(0.dp))) {
        val center = Offset(size.width * 0.91f, size.height * 0.6f)
        drawCircle(SquadBlue.copy(alpha = 0.14f), size.width * 0.58f, center)
        drawCircle(SquadBlue.copy(alpha = 0.13f), size.width * 0.4f, center)
        rotate(-19f, center) {
            val left = size.width * 0.66f
            val top = size.height * 0.14f
            val width = size.width * 0.55f
            val height = size.height * 0.78f
            val ink = SquadCyan.copy(alpha = 0.16f)
            drawRoundRect(ink, Offset(left, top), Size(width, height), CornerRadius(12.dp.toPx()),
                style = Stroke(1.dp.toPx()))
            drawLine(ink, Offset(left, top + height / 2), Offset(left + width, top + height / 2), 1.dp.toPx())
            drawCircle(ink, width * 0.19f, Offset(left + width / 2, top + height / 2),
                style = Stroke(1.dp.toPx()))
            drawRect(ink, Offset(left + width * 0.23f, top), Size(width * 0.54f, height * 0.18f),
                style = Stroke(1.dp.toPx()))
            drawRect(ink, Offset(left + width * 0.23f, top + height * 0.82f), Size(width * 0.54f, height * 0.18f),
                style = Stroke(1.dp.toPx()))
            val points = listOf(Offset(left + width * 0.18f, top + height * 0.76f),
                Offset(left + width * 0.66f, top + height * 0.56f),
                Offset(left + width * 0.43f, top + height * 0.28f))
            drawPath(Path().apply { moveTo(points[0].x, points[0].y); lineTo(points[1].x, points[1].y)
                lineTo(points[2].x, points[2].y) }, SquadCyan.copy(alpha = 0.25f), style = Stroke(2.dp.toPx()))
            points.forEach {
                drawCircle(SquadCyan.copy(alpha = 0.12f), 10.dp.toPx(), it)
                drawCircle(SquadCyan.copy(alpha = 0.55f), 3.dp.toPx(), it)
            }
        }
    }
}

@Composable
private fun AuthTabs(registration: Boolean, onLogin: () -> Unit, onRegistration: () -> Unit, enabled: Boolean) {
    Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
        .padding(4.dp).selectableGroup()) {
        listOf(false to R.string.login_tab, true to R.string.registration_tab).forEach { (isRegistration, label) ->
            val selected = registration == isRegistration
            Surface(Modifier.weight(1f).clip(RoundedCornerShape(11.dp)).selectable(
                selected = selected, enabled = enabled, role = Role.Tab,
                onClick = if (isRegistration) onRegistration else onLogin),
                shape = RoundedCornerShape(11.dp), shadowElevation = if (selected) 2.dp else 0.dp,
                color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent) {
                Box(Modifier.heightIn(min = 44.dp).padding(8.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(label), style = MaterialTheme.typography.labelLarge,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
internal fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    error: String? = null,
    password: Boolean = false,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onDone: () -> Unit = {}
) {
    var visible by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    Column(modifier.semantics(mergeDescendants = true) {}, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        OutlinedTextField(value = value, onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = label },
            singleLine = true, enabled = enabled, isError = error != null,
            textStyle = MaterialTheme.typography.bodyLarge,
            shape = RoundedCornerShape(14.dp),
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { AuthIcon(icon, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = if (password) {{
                IconButton(onClick = { visible = !visible }, enabled = enabled) {
                    Icon(painterResource(if (visible) R.drawable.ic_eye_off else R.drawable.ic_eye),
                        contentDescription = stringResource(if (visible) R.string.hide_password else R.string.show_password),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(21.dp))
                }
            }} else null,
            visualTransformation = if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                capitalization = if (password || keyboardType == KeyboardType.Email) KeyboardCapitalization.None
                    else KeyboardCapitalization.Words,
                autoCorrectEnabled = !password && keyboardType != KeyboardType.Email,
                keyboardType = if (password) KeyboardType.Password else keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(
                onNext = { focus.moveFocus(FocusDirection.Next) },
                onDone = { keyboard?.hide(); focus.clearFocus(); onDone() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant))
        error?.let { FieldError(it) }
    }
}

@Composable
internal fun AuthIcon(@DrawableRes icon: Int, modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Icon(painterResource(icon), contentDescription = null, modifier = modifier.size(21.dp), tint = tint)
}

@Composable
internal fun FieldError(message: String) {
    Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
}

@Composable
internal fun AuthErrorMessage(error: AuthError) {
    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp)) {
        Text(stringResource(when (error) {
            AuthError.INVALID_CREDENTIALS -> R.string.invalid_credentials
            AuthError.EMAIL_ALREADY_USED -> R.string.email_already_used
            AuthError.SESSION_EXPIRED -> R.string.session_expired
            AuthError.STORAGE_UNAVAILABLE -> R.string.storage_unavailable
            AuthError.SERVICE_UNAVAILABLE -> R.string.service_unavailable
        }), color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth().padding(14.dp).semantics { liveRegion = LiveRegionMode.Polite })
    }
}

@Composable
internal fun AuthSubmitButton(text: String, loadingText: String, loading: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick, modifier.fillMaxWidth().heightIn(min = 56.dp), enabled = !loading,
        shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(19.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
            Spacer(Modifier.width(10.dp))
        }
        Text(if (loading) loadingText else text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (!loading) AuthIcon(R.drawable.ic_arrow)
    }
}

@Composable
internal fun SessionHint() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        AuthIcon(R.drawable.ic_shield, tint = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.session_hint), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
