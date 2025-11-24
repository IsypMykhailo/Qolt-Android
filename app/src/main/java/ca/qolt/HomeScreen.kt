package ca.qolt

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.widget.Toast
import kotlinx.coroutines.delay
import java.nio.charset.Charset
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding


private fun loadInstalledApps(context: Context): List<InstalledApp> {
    val pm = context.packageManager
    return pm.getInstalledApplications(PackageManager.GET_META_DATA)
        .filter { appInfo ->
            pm.getLaunchIntentForPackage(appInfo.packageName) != null
        }
        .map { appInfo ->
            InstalledApp(
                packageName = appInfo.packageName,
                appName = pm.getApplicationLabel(appInfo).toString(),
                icon = pm.getApplicationIcon(appInfo)
            )
        }
        .sortedBy { it.appName.lowercase() }
}

@Composable
private fun rememberAppIcon(drawable: Drawable): ImageBitmap {
    return remember(drawable) {
        val w = drawable.intrinsicWidth.coerceAtLeast(1)
        val h = drawable.intrinsicHeight.coerceAtLeast(1)
        val bmp = createBitmap(w, h)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bmp.asImageBitmap()
    }
}

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
    } catch (e: Exception) {
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
        if (prefixIndex >= 0 && prefixIndex < uriPrefixes.size) {
            val prefix = uriPrefixes[prefixIndex]
            val uri = String(payload, 1, payload.size - 1, Charset.forName("UTF-8"))
            prefix + uri
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val activity = context as? Activity
    val orange = Color(0xFFFF6A1A)

    var isNfcReading by remember { mutableStateOf(false) }
    var showScanningDialog by remember { mutableStateOf(false) }
    var emergencyUsedToday by remember { mutableStateOf(false) }
    var currentStreak by remember { mutableStateOf(7) }
    val selectedPreset = "Study Focus"
    val selectedPresetEmoji = "📖"

    var showAppSelector by remember { mutableStateOf(false) }
    var allApps by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    var selectedApps by remember { mutableStateOf(setOf<String>()) }

    var lastScannedTag by remember { mutableStateOf<String?>(null) }
    var showScanSuccess by remember { mutableStateOf(false) }
    var lastCheckedAction by remember { mutableStateOf<String?>(null) }
    var appsBlocked by remember { mutableStateOf(AppBlockingManager.isBlockingActive(context)) }

    LaunchedEffect(Unit) {
        val savedApps = AppBlockingManager.getBlockedApps(context)
        if (savedApps.isNotEmpty()) {
            selectedApps = savedApps
        }
    }

    LaunchedEffect(showAppSelector) {
        if (showAppSelector && allApps.isEmpty()) {
            allApps = loadInstalledApps(context)
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
                        action == NfcAdapter.ACTION_TECH_DISCOVERED) {

                        val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                        if (tag != null) {
                            val textContent = readNdefMessage(tag)

                            if (textContent != null && textContent.contains("KillSwitch", ignoreCase = true)) {
                                lastScannedTag = textContent
                                showScanSuccess = true
                                showScanningDialog = false
                                isNfcReading = false

                                if (!appsBlocked) {
                                    if (selectedApps.isNotEmpty()) {
                                        if (!AppBlockingManager.hasUsageStatsPermission(context)) {
                                            Toast.makeText(
                                                context,
                                                "Please grant Usage Access permission",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            AppBlockingManager.requestUsageStatsPermission(context)
                                        }
                                        else if (!AppBlockingManager.hasOverlayPermission(context)) {
                                            Toast.makeText(
                                                context,
                                                "Please grant Display Over Other Apps permission",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            AppBlockingManager.requestOverlayPermission(context)
                                        }
                                        else {
                                            AppBlockingManager.saveBlockedApps(context, selectedApps)

                                            AppBlockingManager.blockApps(context, selectedApps)
                                            appsBlocked = true
                                            Toast.makeText(
                                                context,
                                                "✅ ${selectedApps.size} Apps Blocked!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "No apps selected to block",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    AppBlockingManager.unblockApps(context)
                                    appsBlocked = false
                                    Toast.makeText(
                                        context,
                                        "✅ Apps Unblocked!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else if (textContent != null) {
                                showScanningDialog = false
                                isNfcReading = false
                                Toast.makeText(context, "❌ Wrong tag!", Toast.LENGTH_LONG).show()
                            } else {
                                showScanningDialog = false
                                isNfcReading = false
                                Toast.makeText(context, "❌ Wrong tag!", Toast.LENGTH_LONG).show()
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
            delay(3000)
            showScanSuccess = false
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
                Toast.makeText(context, "Error activating NFC: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else if (!isNfcReading && activity != null && nfcAdapter != null) {
            try {
                nfcAdapter.disableForegroundDispatch(activity)
            } catch (e: Exception) {
            }
        }
    }

    DisposableEffect(activity, nfcAdapter) {
        onDispose {
            if (activity != null && nfcAdapter != null) {
                try {
                    nfcAdapter.disableForegroundDispatch(activity)
                } catch (e: Exception) {
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E))
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .background(orange.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔥",
                        fontSize = 20.sp
                    )
                    Text(
                        text = "$currentStreak",
                        color = orange,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (emergencyUsedToday) Color.Gray.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable(enabled = !emergencyUsedToday) {
                            if (!emergencyUsedToday) {
                                emergencyUsedToday = true
                                Toast.makeText(context, "Emergency unlock activated! Available again tomorrow.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Emergency unlock already used today", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "🚨",
                        fontSize = 20.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                FloatingActionButton(
                    onClick = {
                        if (!showScanSuccess && !showScanningDialog) {
                            if (nfcAdapter == null) {
                                Toast.makeText(context, "NFC not available on this device", Toast.LENGTH_LONG).show()
                            } else if (!nfcAdapter.isEnabled) {
                                Toast.makeText(context, "Please enable NFC in Settings", Toast.LENGTH_LONG).show()
                            } else {
                                showScanningDialog = true
                                isNfcReading = true
                            }
                        }
                    },
                    modifier = Modifier.size(180.dp),
                    shape = CircleShape,
                    containerColor = if (showScanSuccess) Color(0xFF4CAF50) else if (appsBlocked) Color.Red.copy(alpha = 0.8f) else orange,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = if (appsBlocked) "Unlock apps" else "Lock apps",
                        tint = Color.White,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (appsBlocked) Color.Red.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f)
                    )
                    .clickable(enabled = !appsBlocked) {
                        showAppSelector = true
                    }
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (appsBlocked) {
                    Text(
                        text = "🔒",
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${selectedApps.size} Apps Locked",
                        color = Color.Red,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = selectedPresetEmoji,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (selectedApps.isEmpty()) "Select Apps to Block" else "${selectedApps.size} Apps Selected",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    if (showAppSelector) {
        AlertDialog(
            onDismissRequest = { showAppSelector = false },
            title = {
                Text(
                    text = "Select Apps to Block",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {
                    if (allApps.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Loading apps...",
                                style = TextStyle(fontSize = 13.sp, color = Color.Gray)
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(allApps) { app ->
                                val isChecked = app.packageName in selectedApps
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val iconBitmap = rememberAppIcon(app.icon)
                                    Image(
                                        bitmap = iconBitmap,
                                        contentDescription = app.appName,
                                        modifier = Modifier
                                            .height(32.dp)
                                            .padding(end = 8.dp)
                                    )

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = app.appName,
                                            style = TextStyle(fontSize = 14.sp)
                                        )
                                        Text(
                                            text = app.packageName,
                                            style = TextStyle(
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        )
                                    }

                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            selectedApps = if (checked) {
                                                selectedApps + app.packageName
                                            } else {
                                                selectedApps - app.packageName
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        AppBlockingManager.saveBlockedApps(context, selectedApps)
                        Toast.makeText(context, "${selectedApps.size} apps selected", Toast.LENGTH_SHORT).show()
                        showAppSelector = false
                    }
                ) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAppSelector = false }) {
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
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
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
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(orange.copy(alpha = 0.2f), CircleShape)
                            .border(3.dp, orange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📱",
                            fontSize = 48.sp
                        )
                    }

                    Text(
                        text = "Hold your phone near the NFC tag",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = Color.White,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    )

                    Text(
                        text = "Looking for your tag...",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = orange,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isNfcReading = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = orange
                    )
                ) {
                    Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            },
            containerColor = Color(0xFF2C2C2E),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }
}
