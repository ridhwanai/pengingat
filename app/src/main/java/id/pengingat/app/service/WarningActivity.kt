package id.pengingat.app.service

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.pengingat.app.MainActivity
import id.pengingat.app.PengingatApp
import id.pengingat.app.data.PendingWarning
import id.pengingat.app.data.StreakCalculator
import id.pengingat.app.root.RootHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * POPUP PERINGATAN PERSISTEN (PRD §4.3).
 * - Back dinonaktifkan total.
 * - Status aktif tersimpan sebagai baris PendingWarning di database; layar ini
 *   hanya hilang bila pengguna menekan salah satu dari dua pilihan.
 * - Bila proses mati / HP reboot, GuardService & BootReceiver memunculkannya lagi.
 */
class WarningActivity : ComponentActivity() {

    @Deprecated("Popup tidak bisa ditutup dengan Back")
    override fun onBackPressed() {
        // Sengaja kosong total: hanya dua tombol pilihan yang menyelesaikan popup.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = PengingatApp.instance.container
        val popupDao = container.db.popupDao()

        val pending: PendingWarning? = runBlocking { popupDao.getPending() }
        if (pending == null) {
            finish()
            return
        }
        val targetPkg = pending.packageName
        val settings = runBlocking { container.settings.state.first() }
        val entries = runBlocking { container.progress.entriesFlow().first() }
        val streak = StreakCalculator.currentStreak(entries, LocalDate.now())
        val quote = container.quotes.random("godaan", pending.since)
        val appLabel = loadAppLabel(targetPkg)

        setContent {
            ServiceTheme {
              ServiceBackground(id.pengingat.app.R.drawable.bg_warning) {
                val scope = rememberCoroutineScope()
                var secondsLeft by remember { mutableIntStateOf(settings.popupCountdownSec) }
                var resolving by remember { mutableStateOf(false) }
                val gold = Color(0xFFD4AF37)

                LaunchedEffect(Unit) {
                    while (secondsLeft > 0) {
                        delay(1_000)
                        secondsLeft--
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    Spacer(Modifier.height(28.dp))
                    Text(
                        text = "Berhenti sejenak.",
                        color = gold,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Allah selalu melihatmu.",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Kamu membuka $appLabel. Kamu sudah bersih $streak hari — jangan rusak perjuanganmu untuk sesaat.",
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = quote.arab,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        fontFamily = FontFamily.Serif,
                        fontSize = 22.sp,
                        lineHeight = 36.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "\u201C${quote.translation}\u201D",
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = quote.source,
                        color = gold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (quote.reflection.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = quote.reflection,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(36.dp))
                    Button(
                        onClick = {
                            if (resolving) return@Button
                            resolving = true
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    RootHelper.forceStop(targetPkg)
                                }
                                resolveAndFinish(popupDao, targetPkg, "tutup") {
                                    val i = Intent(
                                        this@WarningActivity,
                                        MainActivity::class.java
                                    ).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        putExtra(MainActivity.EXTRA_SCREEN, MainActivity.SCREEN_HOME)
                                    }
                                    startActivity(i)
                                }
                            }
                        },
                        enabled = !resolving,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = gold,
                            contentColor = Color(0xFF0B1026)
                        )
                    ) {
                        Text("Tutup & Kembali", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            if (resolving) return@OutlinedButton
                            resolving = true
                            scope.launch {
                                resolveAndFinish(popupDao, targetPkg, "lanjut") {
                                    // Tanpa startActivity: finish mengembalikan pengguna
                                    // ke aplikasi yang tadi ia buka.
                                }
                            }
                        },
                        enabled = !resolving && secondsLeft <= 0,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (secondsLeft > 0)
                                "Saya Sadar, Lanjutkan ($secondsLeft)"
                            else
                                "Saya Sadar, Lanjutkan",
                            color = gold
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Popup ini tidak akan hilang sebelum kamu memilih — termasuk bila HP dimatikan dan dinyalakan lagi.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(24.dp))
                }
              }
            }
        }
    }

    private suspend fun resolveAndFinish(
        popupDao: id.pengingat.app.data.PopupDao,
        pkg: String,
        choice: String,
        beforeFinish: () -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val open = popupDao.latestOpenLog(pkg)
            if (open != null) {
                popupDao.updateResolution(open.id, choice, System.currentTimeMillis())
            }
            popupDao.deletePending()
        }
        beforeFinish()
        finish()
    }

    private fun loadAppLabel(pkg: String): String = try {
        val appInfo = packageManager.getApplicationInfo(pkg, 0)
        packageManager.getApplicationLabel(appInfo).toString()
    } catch (e: Exception) {
        pkg
    }
}
