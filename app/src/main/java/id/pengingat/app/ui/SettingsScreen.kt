package id.pengingat.app.ui

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import id.pengingat.app.ui.theme.Gold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class InstalledApp(val pkg: String, val label: String, val icon: Drawable?)

private fun loadInstalledApps(context: Context): List<InstalledApp> {
    val pm = context.packageManager
    return pm.getInstalledApplications(PackageManager.GET_META_DATA)
        .filter { it.packageName != context.packageName }
        .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || (it.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0 }
        .map {
            InstalledApp(
                pkg = it.packageName,
                label = pm.getApplicationLabel(it).toString(),
                icon = runCatching { pm.getApplicationIcon(it) }.getOrNull()
            )
        }
        .sortedBy { it.label.lowercase(Locale.getDefault()) }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: AppViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val settings by vm.settings.collectAsState()
    val status by vm.systemStatus.collectAsState()
    val exportPath by vm.exportResult.collectAsState()
    val rootGrant by vm.rootGrantResult.collectAsState()
    var showPicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        SectionTitle("Pengaturan")
        Spacer(Modifier.height(16.dp))

        // --- Waktu Subuh ---
        Text("Waktu Subuh (Kulon Progo)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            "Koreksi jadwal (menit): ${settings.fajrOffsetMin}",
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(-10, -5, -2, 0, 2, 5, 10).forEach { v ->
                FilterChip(
                    selected = settings.fajrOffsetMin == v,
                    onClick = { vm.setFajrOffset(v) },
                    label = { Text(if (v > 0) "+$v" else "$v") }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Tunda check-in setelah tombol \"Sholat Dulu\": ${settings.prayerDelayMin} menit",
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(15, 30, 45, 60).forEach { v ->
                FilterChip(
                    selected = settings.prayerDelayMin == v,
                    onClick = { vm.setPrayerDelay(v) },
                    label = { Text("$v mnt") }
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        // --- Popup ---
        Text("Popup Peringatan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            "Hitung mundur tombol \"Lanjutkan\": ${settings.popupCountdownSec} detik",
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(30, 60, 120, 300).forEach { v ->
                FilterChip(
                    selected = settings.popupCountdownSec == v,
                    onClick = {
                        if (v < settings.popupCountdownSec) {
                            vm.requestWeakening(
                                desc = "Hitung mundur popup menjadi $v detik",
                                kind = "countdown", payload = v.toString()
                            )
                        } else {
                            vm.setPopupCountdownDirect(v)
                        }
                    },
                    label = { Text("$v dtk") }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Jeda anti-spam per aplikasi: ${settings.popupCooldownMin} menit",
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(5, 10, 20, 30).forEach { v ->
                FilterChip(
                    selected = settings.popupCooldownMin == v,
                    onClick = {
                        if (v > settings.popupCooldownMin) {
                            vm.requestWeakening(
                                desc = "Jeda anti-spam popup menjadi $v menit",
                                kind = "cooldown", payload = v.toString()
                            )
                        } else {
                            vm.setPopupCooldownDirect(v)
                        }
                    },
                    label = { Text("$v mnt") }
                )
            }
        }
        Text(
            "Menurunkan perlindungan masuk antrean Mode Komitmen (berlaku 24 jam lagi). Menaikkan langsung berlaku.",
            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))
        // --- Daftar pantauan ---
        Text("Aplikasi Pantauan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(6.dp))
        if (settings.watchedApps.isEmpty()) {
            Text(
                "Belum ada aplikasi yang dipantau.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp
            )
        } else {
            settings.watchedApps.sorted().forEach { pkg ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = pkg, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = {
                        vm.requestWeakening(
                            desc = "Berhenti memantau $pkg",
                            kind = "unwatch", payload = pkg
                        )
                    }) { Text("Lepas (24 jam)", color = Gold) }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        GoldButton(
            text = "Pilih Aplikasi Pantauan",
            onClick = { showPicker = true },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))
        // --- Mode Komitmen ---
        Text("Mode Komitmen — Antrean Perubahan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(6.dp))
        if (settings.pendingChanges.isEmpty()) {
            Text(
                "Tidak ada perubahan yang menunggu.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp
            )
        } else {
            val fmt = remember { SimpleDateFormat("dd MMM HH:mm", Locale("id", "ID")) }
            settings.pendingChanges.forEach { change ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(change.desc, fontSize = 14.sp)
                        Text(
                            "Berlaku: ${fmt.format(Date(change.applyAt))}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { vm.cancelPending(change.id) }) {
                        Text("Batalkan", color = Gold)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        // --- Kesehatan sistem ---
        Text("Kesehatan Sistem", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(6.dp))
        StatusLine("Root (Mode Paksa Level 3)", status.root)
        StatusLine("Accessibility (deteksi aplikasi)", status.accessibility)
        StatusLine("Izin overlay (popup)", status.overlay)
        StatusLine("Akses penggunaan", status.usageAccess)
        StatusLine("Notifikasi", status.notifications)
        StatusLine("Dikecualikan dari optimasi baterai", status.batteryWhitelisted)
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlineGoldButton(text = "Cek Ulang", onClick = { vm.refreshSystemStatus() })
            OutlineGoldButton(text = "Aktifkan via Root", onClick = { vm.grantViaRoot() })
        }
        rootGrant?.let {
            Text(
                if (it) "Perintah root dijalankan. Cek ulang status di atas."
                else "Root tidak tersedia / ditolak. Aplikasi berjalan di Level 2.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }) { Text("Buka Accessibility") }
            TextButton(onClick = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }) { Text("Buka Izin Overlay") }
            TextButton(onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }) { Text("Buka Akses Penggunaan") }
        }

        Spacer(Modifier.height(20.dp))
        // --- Data ---
        Text("Data", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(6.dp))
        OutlineGoldButton(
            text = "Ekspor Data (JSON)",
            onClick = { vm.exportData() },
            modifier = Modifier.fillMaxWidth()
        )
        exportPath?.let {
            Text("Tersimpan di: $it", fontSize = 12.sp, color = Gold)
        }
        Spacer(Modifier.height(24.dp))
    }

    if (showPicker) {
        AppPickerDialog(
            watched = settings.watchedApps,
            onToggle = { pkg, watch ->
                if (watch) vm.watchApp(pkg)
                else vm.requestWeakening(
                    desc = "Berhenti memantau $pkg", kind = "unwatch", payload = pkg
                )
            },
            onDismiss = { showPicker = false }
        )
    }
}

@Composable
private fun StatusLine(label: String, ok: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (ok) "✓" else "✗",
            color = if (ok) Gold else MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(text = label, fontSize = 14.sp)
    }
}

@Composable
private fun AppPickerDialog(
    watched: Set<String>,
    onToggle: (String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val apps = remember { loadInstalledApps(context) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Selesai") }
        },
        title = { Text("Pilih Aplikasi Pantauan") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                items(apps, key = { it.pkg }) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        app.icon?.let { icon ->
                            Image(
                                bitmap = icon.toBitmap(96, 96).asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp)
                        ) {
                            Text(app.label, fontSize = 14.sp)
                            Text(
                                app.pkg,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Checkbox(
                            checked = app.pkg in watched,
                            onCheckedChange = { checked -> onToggle(app.pkg, checked) }
                        )
                    }
                }
            }
        }
    )
}
