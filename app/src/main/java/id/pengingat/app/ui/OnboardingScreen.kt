package id.pengingat.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import id.pengingat.app.service.AlarmScheduler
import id.pengingat.app.service.GuardService
import id.pengingat.app.ui.theme.Gold

@Composable
fun OnboardingScreen(vm: AppViewModel, onDone: () -> Unit) {
    val context = LocalContext.current
    val status by vm.systemStatus.collectAsState()
    val rootGrant by vm.rootGrantResult.collectAsState()

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { vm.refreshSystemStatus() }

    LaunchedEffect(Unit) { vm.refreshSystemStatus() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Selamat datang di Pengingat",
            color = Gold,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Aplikasi ini membantumu bangun Subuh, mencatat progresmu, dan menahanmu di momen godaan. Agar bisa bekerja penuh (Mode Paksa Level 3), aktifkan izin-izin berikut satu per satu. Semua data tetap di HP ini — aplikasi ini bahkan tidak punya izin internet.",
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))

        StepCard(
            title = "1. Notifikasi",
            desc = "Untuk alarm Subuh & pengingat check-in.",
            done = status.notifications
        ) {
            OutlineGoldButton(
                text = "Izinkan Notifikasi",
                onClick = {
                    if (Build.VERSION.SDK_INT >= 33 &&
                        ContextCompat.checkSelfPermission(
                            context, Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        context.startActivity(
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        StepCard(
            title = "2. Alarm Tepat & Baterai",
            desc = "Agar alarm Subuh berbunyi tepat waktu dan aplikasi tidak dibunuh sistem.",
            done = status.batteryWhitelisted
        ) {
            OutlineGoldButton(
                text = "Kecualikan dari Optimasi Baterai",
                onClick = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                            Uri.parse("package:${context.packageName}")
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        StepCard(
            title = "3. Tampil di Atas Aplikasi Lain (Overlay)",
            desc = "Untuk popup peringatan layar penuh.",
            done = status.overlay
        ) {
            OutlineGoldButton(
                text = "Buka Izin Overlay",
                onClick = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        StepCard(
            title = "4. Akses Penggunaan",
            desc = "Cadangan deteksi aplikasi yang sedang dibuka.",
            done = status.usageAccess
        ) {
            OutlineGoldButton(
                text = "Buka Akses Penggunaan",
                onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        StepCard(
            title = "5. Accessibility — Deteksi Aplikasi Pantauan",
            desc = "Jujur apa adanya: layanan ini memberi tahu Pengingat aplikasi apa yang sedang di depan layar, hanya untuk mencocokkannya dengan daftar pantauanmu. Isi layar tidak dibaca dan tidak ada data yang dikirim ke mana pun.",
            done = status.accessibility
        ) {
            OutlineGoldButton(
                text = "Buka Pengaturan Accessibility",
                onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        StepCard(
            title = "6. Mode Paksa (Root) — Opsional tapi Disarankan",
            desc = "Dengan root, aplikasi memberi izin-izin di atas ke dirinya sendiri, menutup paksa aplikasi pantauan, dan menjaga dirinya tetap hidup.",
            done = status.root
        ) {
            OutlineGoldButton(
                text = "Aktifkan via Root",
                onClick = { vm.grantViaRoot() },
                modifier = Modifier.fillMaxWidth()
            )
            rootGrant?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    if (it) "Perintah root dijalankan. Tekan \"Cek Ulang\" di langkah ini setelah beberapa detik."
                    else "Root tidak terdeteksi / ditolak. Tanpa root, aplikasi tetap jalan di Level 2 (notifikasi memaksa tanpa penjaga root).",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        OutlineGoldButton(
            text = "Cek Ulang Semua Status",
            onClick = { vm.refreshSystemStatus() },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        GoldButton(
            text = "Selesai — Mulai Pakai Pengingat",
            onClick = {
                GuardService.start(context)
                AlarmScheduler.scheduleNextFajr(context)
                onDone()
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StepCard(
    title: String,
    desc: String,
    done: Boolean,
    content: @Composable () -> Unit
) {
    ElegantCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = (if (done) "✓ " else "") + title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = if (done) Gold else MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = desc,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        content()
    }
    Spacer(Modifier.height(12.dp))
}
