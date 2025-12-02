package ca.qolt.ui.home

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import ca.qolt.ui.theme.Orange
import ca.qolt.ui.theme.SuccessGreen
import ca.qolt.ui.theme.SurfaceElevated
import ca.qolt.util.BatteryOptimizationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.nio.charset.Charset
import kotlin.math.min
import androidx.core.content.edit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


private fun readNdefMessage(tag: Tag): String? {
    try {
        val ndef = Ndef.get(tag) ?: return null
        ndef.connect()

        val ndefMessage = ndef.ndefMessage
        ndef.close()

        if (ndefMessage == null) {
            return null
        }

        val messages = mutableListOf<String>()
        for (record in ndefMessage.records) {
            val text = parseNdefRecord(record)
            if (text != null) {
                messages.add(text)
            }
        }

        return if (messages.isNotEmpty()) {
            messages.joinToString("\n")
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

private fun parseNdefRecord(record: NdefRecord): String? {
    return when {
        record.tnf == NdefRecord.TNF_WELL_KNOWN && record.type.contentEquals(NdefRecord.RTD_TEXT) -> {
            parseTextRecord(record)
        }

        record.tnf == NdefRecord.TNF_WELL_KNOWN && record.type.contentEquals(NdefRecord.RTD_URI) -> {
            parseUriRecord(record)
        }

        record.tnf == NdefRecord.TNF_ABSOLUTE_URI -> {
            String(record.type, Charset.forName("UTF-8"))
        }

        record.tnf == NdefRecord.TNF_MIME_MEDIA -> {
            String(record.payload, Charset.forName("UTF-8"))
        }

        else -> null
    }
}

private fun parseTextRecord(record: NdefRecord): String? {
    return try {
        val payload = record.payload
        val languageCodeLength = (payload[0].toInt() and 0x3F)
        val text = String(
            payload,
            languageCodeLength + 1,
            payload.size - languageCodeLength - 1,
            Charset.forName("UTF-8")
        )
        text
    } catch (_: Exception) {
        null
    }
}

private fun parseUriRecord(record: NdefRecord): String? {
    return try {
        val payload = record.payload
        val uriPrefixes = arrayOf(
            "", "http://www.", "https://www.", "http://", "https://",
            "tel:", "mailto:", "ftp://anonymous:anonymous@", "ftp://ftp.",
            "ftps://", "sftp://", "smb://", "nfs://", "ftp://", "dav://",
            "news:", "telnet://", "imap:", "rtsp://", "urn:", "pop:",
            "sip:", "sips:", "tftp:", "btspp://", "btl2cap://", "btgoep://",
            "tcpobex://", "irdaobex://", "file://", "urn:epc:id:", "urn:epc:tag:",
            "urn:epc:pat:", "urn:epc:raw:", "urn:epc:", "urn:nfc:"
        )

        val prefixIndex = payload[0].toInt() and 0xFF
        if (prefixIndex < uriPrefixes.size) {
            val prefix = uriPrefixes[prefixIndex]
            val uri = String(payload, 1, payload.size - 1, Charset.forName("UTF-8"))
            prefix + uri
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}

@Composable
fun Home(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    onNavigateToPresets: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            ContextCompat.getSystemService(context, Vibrator::class.java)
        }
    }

    var isNfcReading by remember { mutableStateOf(false) }
    var showScanningDialog by remember { mutableStateOf(false) }

    val prefs = remember { context.getSharedPreferences("qolt_prefs", Context.MODE_PRIVATE) }
    val lastEmergencyDate = prefs.getString("last_emergency_date", "") ?: ""
    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        .format(Date())
    var emergencyUsedToday by remember {
        mutableStateOf(lastEmergencyDate == currentDate)
    }
    val emergencyUnlockEnabled by viewModel.emergencyUnlockEnabled.collectAsState(initial = false)

    val currentStreak by viewModel.currentStreak.collectAsState()
    val currentPreset by viewModel.currentPreset.collectAsState()
    val isBlockingActive by viewModel.isBlockingActive.collectAsState()

    var showNoPresetDialog by remember { mutableStateOf(false) }

    var showScanSuccess by remember { mutableStateOf(false) }
    var lastCheckedAction by remember { mutableStateOf<String?>(null) }

    var holdProgress by remember { mutableStateOf(0f) }
    var isHolding by remember { mutableStateOf(false) }

    var isVisible by remember { mutableStateOf(false) }
    var emergencyShake by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    LaunchedEffect(isHolding) {
        if (isHolding && !isBlockingActive && !showScanSuccess && !showScanningDialog) {

            delay(500)
            if (!isHolding) {
                holdProgress = 0f
                return@LaunchedEffect
            }

            val startTime = System.currentTimeMillis()
            while (isHolding && holdProgress < 1f) {
                val elapsed = System.currentTimeMillis() - startTime
                holdProgress = min((elapsed / 3000f), 1f)

                if (holdProgress >= 1f) {
                    // Validate preset exists
                    if (currentPreset == null) {
                        showNoPresetDialog = true
                        Toast.makeText(
                            context,
                            "Please select a preset first",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (currentPreset!!.blockedApps.isEmpty()) {
                        Toast.makeText(
                            context,
                            "This preset has no apps to block",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Start blocking with current preset
                        scope.launch {
                            val result = viewModel.startBlockingCurrentPreset()

                            result.onSuccess {
                                showScanSuccess = true
                                viewModel.refreshStreak()

                                vibrator?.vibrate(
                                    VibrationEffect.createOneShot(
                                        200,
                                        VibrationEffect.DEFAULT_AMPLITUDE
                                    )
                                )

                                Toast.makeText(
                                    context,
                                    "${currentPreset!!.blockedApps.size} Apps Blocked",
                                    Toast.LENGTH_LONG
                                ).show()
                            }.onFailure { error ->
                                when (error.message) {
                                    "USAGE_STATS_PERMISSION_REQUIRED" -> {
                                        Toast.makeText(
                                            context,
                                            "Please grant Usage Access permission",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        viewModel.requestUsageStatsPermission()
                                    }

                                    "OVERLAY_PERMISSION_REQUIRED" -> {
                                        Toast.makeText(
                                            context,
                                            "Please grant Display Over Other Apps permission",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        viewModel.requestOverlayPermission()
                                    }

                                    else -> {
                                        Toast.makeText(
                                            context,
                                            "Failed to start blocking: ${error.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    }
                    holdProgress = 0f
                    isHolding = false
                }
                delay(16)
            }
        } else {
            holdProgress = 0f
        }
    }

    // Refresh preset when returning from Presets page
    LaunchedEffect(isVisible) {
        if (isVisible) {
            viewModel.refreshCurrentPreset()
        }
    }

    LaunchedEffect(context) {
        while (true) {
            activity?.intent?.let { intent ->
                val action = intent.action

                if (isNfcReading && action != null && action != lastCheckedAction) {
                    lastCheckedAction = action

                    if (action == NfcAdapter.ACTION_TAG_DISCOVERED ||
                        action == NfcAdapter.ACTION_NDEF_DISCOVERED ||
                        action == NfcAdapter.ACTION_TECH_DISCOVERED
                    ) {

                        val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                        }
                        if (tag != null) {
                            val textContent = readNdefMessage(tag)

                            if (textContent != null && textContent.contains(
                                    "KillSwitch",
                                    ignoreCase = true
                                )
                            ) {
                                showScanSuccess = true
                                showScanningDialog = false
                                isNfcReading = false

                                if (!isBlockingActive) {
                                    if (currentPreset != null && currentPreset!!.blockedApps.isNotEmpty()) {
                                        scope.launch {
                                            val result = viewModel.startBlockingCurrentPreset()

                                            result.onSuccess {
                                                viewModel.refreshStreak()
                                                Toast.makeText(
                                                    context,
                                                    "${currentPreset!!.blockedApps.size} Apps Blocked",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }.onFailure { error ->
                                                when (error.message) {
                                                    "USAGE_STATS_PERMISSION_REQUIRED" -> {
                                                        Toast.makeText(
                                                            context,
                                                            "Please grant Usage Access permission",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                        viewModel.requestUsageStatsPermission()
                                                    }

                                                    "OVERLAY_PERMISSION_REQUIRED" -> {
                                                        Toast.makeText(
                                                            context,
                                                            "Please grant Display Over Other Apps permission",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                        viewModel.requestOverlayPermission()
                                                    }

                                                    else -> {
                                                        Toast.makeText(
                                                            context,
                                                            "Failed to start blocking",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        showNoPresetDialog = true
                                        Toast.makeText(
                                            context,
                                            "Please select a preset first",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    scope.launch {
                                        viewModel.stopBlocking()
                                    }
                                    Toast.makeText(
                                        context,
                                        "Apps Unblocked",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else if (textContent != null) {
                                showScanningDialog = false
                                isNfcReading = false
                                Toast.makeText(context, "Wrong tag", Toast.LENGTH_LONG).show()
                            } else {
                                showScanningDialog = false
                                isNfcReading = false
                                Toast.makeText(context, "Wrong tag", Toast.LENGTH_LONG).show()
                            }

                            activity.intent.action = ""
                            lastCheckedAction = ""
                        }
                    }
                }
            }
            delay(100)
        }
    }

    LaunchedEffect(showScanSuccess) {
        if (showScanSuccess) {
            delay(2000)
            showScanSuccess = false
        }
    }

    LaunchedEffect(emergencyShake) {
        if (emergencyShake) {
            delay(500)
            emergencyShake = false
        }
    }

    val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }

    LaunchedEffect(isNfcReading, activity, nfcAdapter) {
        if (isNfcReading && activity != null && nfcAdapter != null) {
            val intent = Intent(activity, activity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            val pendingIntent = PendingIntent.getActivity(
                activity,
                0,
                intent,
                PendingIntent.FLAG_MUTABLE
            )

            val intentFilters = arrayOf(
                IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
                IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
                IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
            )

            try {
                nfcAdapter.enableForegroundDispatch(activity, pendingIntent, intentFilters, null)
            } catch (e: Exception) {
                Toast.makeText(context, "Error activating NFC: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
        } else if (!isNfcReading && activity != null && nfcAdapter != null) {
            try {
                nfcAdapter.disableForegroundDispatch(activity)
            } catch (_: Exception) {
            }
        }
    }

    DisposableEffect(activity, nfcAdapter) {
        onDispose {
            if (activity != null && nfcAdapter != null) {
                try {
                    nfcAdapter.disableForegroundDispatch(activity)
                } catch (_: Exception) {
                }
            }
        }
    }

    val topBarOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else (-50).dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "topBarOffset"
    )

    val fabScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "fabScale"
    )

    val bottomBarOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 100.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bottomBarOffset"
    )

    val fabColor by animateColorAsState(
        targetValue = when {
            showScanSuccess -> SuccessGreen
            isBlockingActive -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.primary
        },
        animationSpec = spring<Color>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fabColor"
    )

    val emergencyRotation by animateFloatAsState(
        targetValue = if (emergencyShake) 10f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "emergencyRotation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 150.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = topBarOffset)
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Streak Badge
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "$currentStreak",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isBlockingActive) {
                Surface(
                    color = if (emergencyUsedToday)
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(20.dp),
                    tonalElevation = if (emergencyUsedToday) 0.dp else 2.dp,
                    modifier = Modifier
                        .rotate(emergencyRotation)
                        .clickable(enabled = !emergencyUsedToday && emergencyUnlockEnabled) @RequiresPermission(
                            Manifest.permission.VIBRATE
                        ) {
                            if (!emergencyUsedToday) {
                                scope.launch @RequiresPermission(Manifest.permission.VIBRATE) {
                                    viewModel.stopBlocking()
                                    emergencyUsedToday = true

                                    prefs.edit {
                                        putString("last_emergency_date", currentDate)
                                    }

                                    vibrator?.vibrate(
                                        VibrationEffect.createWaveform(
                                            longArrayOf(0, 100, 50, 100),
                                            -1
                                        )
                                    )

                                    Toast.makeText(
                                        context,
                                        "Emergency unlock activated! Available again tomorrow.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                emergencyShake = true

                                // Error haptic
                                vibrator?.vibrate(
                                    VibrationEffect.createOneShot(
                                        50,
                                        VibrationEffect.DEFAULT_AMPLITUDE
                                    )
                                )

                                Toast.makeText(
                                    context,
                                    "Emergency unlock already used today",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Emergency Unblock",
                            tint = if (emergencyUsedToday)
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Center - Main FAB
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .scale(fabScale)
                    .size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Enhanced progress rings
                if (holdProgress > 0f) {
                    // Pulsing outer ring animation
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseScale"
                    )

                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 0.1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseAlpha"
                    )

                    androidx.compose.foundation.Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val centerX = size.width / 2
                        val centerY = size.height / 2
                        val baseRadius = size.minDimension / 2

                        // Outer pulsing ring
                        drawCircle(
                            color = Orange.copy(alpha = pulseAlpha),
                            radius = baseRadius * pulseScale,
                            center = Offset(centerX, centerY),
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Background track (subtle)
                        val trackStrokeWidth = 4.dp.toPx()
                        val trackRadius = baseRadius - 12.dp.toPx()
                        drawArc(
                            color = Orange.copy(alpha = 0.15f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = Offset(
                                centerX - trackRadius,
                                centerY - trackRadius
                            ),
                            size = Size(trackRadius * 2, trackRadius * 2),
                            style = Stroke(
                                width = trackStrokeWidth,
                                cap = StrokeCap.Round
                            )
                        )

                        // Main progress arc with gradient-like effect (multiple layers)
                        val mainStrokeWidth = 8.dp.toPx()
                        val mainRadius = baseRadius - 12.dp.toPx()

                        // Glow layer (slightly larger, more transparent)
                        drawArc(
                            color = Orange.copy(alpha = 0.3f),
                            startAngle = -90f,
                            sweepAngle = 360f * holdProgress,
                            useCenter = false,
                            topLeft = Offset(
                                centerX - mainRadius - 2.dp.toPx(),
                                centerY - mainRadius - 2.dp.toPx()
                            ),
                            size = Size(
                                (mainRadius + 2.dp.toPx()) * 2,
                                (mainRadius + 2.dp.toPx()) * 2
                            ),
                            style = Stroke(
                                width = mainStrokeWidth + 4.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )

                        // Main solid arc
                        drawArc(
                            color = Orange,
                            startAngle = -90f,
                            sweepAngle = 360f * holdProgress,
                            useCenter = false,
                            topLeft = Offset(
                                centerX - mainRadius,
                                centerY - mainRadius
                            ),
                            size = Size(mainRadius * 2, mainRadius * 2),
                            style = Stroke(
                                width = mainStrokeWidth,
                                cap = StrokeCap.Round
                            )
                        )

                        // Leading edge glow dot
                        if (holdProgress > 0.05f) {
                            val angle = (-90f + 360f * holdProgress) * (PI / 180f)
                            val dotX = centerX + mainRadius * cos(angle).toFloat()
                            val dotY = centerY + mainRadius * sin(angle).toFloat()

                            // Outer glow
                            drawCircle(
                                color = Orange.copy(alpha = 0.3f),
                                radius = 12.dp.toPx(),
                                center = Offset(dotX, dotY)
                            )

                            // Inner bright dot
                            drawCircle(
                                color = Orange,
                                radius = 6.dp.toPx(),
                                center = Offset(dotX, dotY)
                            )

                            // Center highlight
                            drawCircle(
                                color = Color.White,
                                radius = 3.dp.toPx(),
                                center = Offset(dotX, dotY)
                            )
                        }
                    }
                }

                // Main FAB
                val icon: ImageVector = when {
                    showScanSuccess -> Icons.Default.Check
                    isBlockingActive -> Icons.Default.Lock
                    else -> Icons.Default.LockOpen
                }

                val iconRotation by animateFloatAsState(
                    targetValue = if (showScanSuccess) 360f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "iconRotation"
                )

                Surface(
                    modifier = Modifier
                        .size(180.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    // Short tap - show NFC dialog
                                    if (!showScanSuccess && !showScanningDialog) {
                                        if (nfcAdapter == null) {
                                            Toast
                                                .makeText(
                                                    context,
                                                    "NFC not available on this device",
                                                    Toast.LENGTH_LONG
                                                )
                                                .show()
                                        } else if (!nfcAdapter.isEnabled) {
                                            Toast
                                                .makeText(
                                                    context,
                                                    "Please enable NFC in Settings",
                                                    Toast.LENGTH_LONG
                                                )
                                                .show()
                                        } else {
                                            showScanningDialog = true
                                            isNfcReading = true
                                        }
                                    }
                                },
                                onPress = @RequiresPermission(Manifest.permission.VIBRATE) {
                                    // Long press - hold to block
                                    if (!isBlockingActive && !showScanSuccess && !showScanningDialog) {
                                        isHolding = true

                                        // Start haptic
                                        vibrator?.vibrate(
                                            VibrationEffect.createOneShot(
                                                50,
                                                VibrationEffect.DEFAULT_AMPLITUDE
                                            )
                                        )
                                    }

                                    tryAwaitRelease()
                                    isHolding = false
                                }
                            )
                        },
                    shape = CircleShape,
                    color = fabColor,
                    shadowElevation = 8.dp,
                    tonalElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = if (isBlockingActive) "Unlock apps" else "Lock apps",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .size(80.dp)
                                .rotate(iconRotation)
                        )
                    }
                }
            }
        }

        if (isBlockingActive &&
            !BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BatteryAlert,
                        contentDescription = "Battery optimization warning",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Improve Reliability",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "Disable battery optimization for better app blocking",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )

                        BatteryOptimizationHelper.getManufacturerInstructions()
                            ?.let { instructions ->
                                Text(
                                    text = instructions,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp),
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                )
                            }
                    }
                    TextButton(
                        onClick = {
                            BatteryOptimizationHelper.openBatteryOptimizationSettings(context)
                        }
                    ) {
                        Text(
                            "Settings",
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }

        // Bottom - Preset Display Bar
        Surface(
            color = if (isBlockingActive)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = bottomBarOffset)
                .clickable(enabled = !isBlockingActive) {
                    onNavigateToPresets()
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentPreset != null) {
                    // Show emoji
                    Text(
                        text = currentPreset!!.emoji,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    // Show preset name
                    Text(
                        text = currentPreset!!.name,
                        color = if (isBlockingActive)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Show app count
                    Text(
                        text = "(${currentPreset!!.blockedApps.size})",
                        color = if (isBlockingActive)
                            MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 16.sp
                    )
                } else {
                    // No preset selected
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "No Preset Selected",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }

    // No Preset Selected Dialog
    if (showNoPresetDialog) {
        AlertDialog(
            onDismissRequest = { showNoPresetDialog = false },
            title = {
                Text(
                    text = "No Preset Selected",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = "Please select a preset from the Presets page before starting app blocking.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        showNoPresetDialog = false
                        onNavigateToPresets()
                    }
                ) {
                    Text("Go to Presets")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoPresetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showScanningDialog) {
        AlertDialog(
            onDismissRequest = {
                showScanningDialog = false
                isNfcReading = false
            },
            title = {
                Text(
                    text = "Scanning for NFC Tag",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Pulsing NFC icon
                    val pulseScale by rememberInfiniteTransition(label = "pulse").animateFloat(
                        initialValue = 1f,
                        targetValue = 1.1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseScale"
                    )

                    Surface(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(pulseScale),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        tonalElevation = 4.dp,
                        border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Nfc,
                                contentDescription = "NFC",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    }

                    Text(
                        text = "Hold your phone near the NFC tag",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Looking for your tag...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showScanningDialog = false
                        isNfcReading = false
                    }
                ) {
                    Text("Cancel")
                }
            },
            containerColor = SurfaceElevated
        )
    }
}
