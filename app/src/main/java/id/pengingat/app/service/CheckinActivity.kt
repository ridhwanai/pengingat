package id.pengingat.app.service

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
import androidx.compose.runtime.getValue
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
import id.pengingat.app.PengingatApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Layar check-in paksa (tahap 2): versi ringkas & mandiri dari check-in utama,
 * memakai repository langsung. Back dinonaktifkan sampai check-in terisi.
 * Selesai -> Subuh berikutnya dijadwalkan ulang & eskalasi berhenti.
 */
class CheckinActivity : ComponentActivity() {

    @Volatile
    private var filled = false

    @Deprecated("Back dinonaktifkan sampai check-in terisi")
    override fun onBackPressed() {
        if (filled) {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
        // Belum terisi: abaikan Back (paksaan Level 3).
    }

    override fun onResume() {
        super.onResume()
        GuardService.checkinShowing = true
    }

    override fun onPause() {
        super.onPause()
        GuardService.checkinShowing = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = PengingatApp.instance.container
        val yesterday = LocalDate.now().minusDays(1)

        setContent {
            ServiceTheme {
              ServiceBackground(id.pengingat.app.R.drawable.bg_home) {
                val scope = rememberCoroutineScope()
                var stage by remember { mutableStateOf(0) } // 0=tanya, 1=bangkit, 2=selesai
                val gold = Color(0xFFD4AF37)
                val bangkitQuote = remember {
                    container.quotes.random("bangkit", System.nanoTime())
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    Spacer(Modifier.height(32.dp))
                    when (stage) {
                        0 -> {
                            Text(
                                text = "Kemarin aman kan?",
                                color = gold,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Jawablah dengan jujur. Alarm ini berhenti hanya setelah kamu menjawab.",
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(32.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        val attempt = withContext(Dispatchers.IO) {
                                            container.settings.state.first().attemptNo
                                        }
                                        container.progress.recordCheckin(
                                            yesterday.toString(), true, null, null, attempt
                                        )
                                        filled = true
                                        stage = 2
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = gold,
                                    contentColor = Color(0xFF0B1026)
                                )
                            ) {
                                Text("Aman, Alhamdulillah", fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { stage = 1 },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Tidak Aman", color = gold)
                            }
                        }

                        1 -> {
                            Text(
                                text = "Tidak apa-apa. Yang penting kamu kembali.",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = bangkitQuote.arab,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End,
                                fontFamily = FontFamily.Serif,
                                fontSize = 21.sp,
                                lineHeight = 34.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "\u201C${bangkitQuote.translation}\u201D",
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = bangkitQuote.source,
                                color = gold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(28.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        val attempt = withContext(Dispatchers.IO) {
                                            container.settings.state.first().attemptNo
                                        }
                                        container.progress.recordCheckin(
                                            yesterday.toString(), false, null, null, attempt
                                        )
                                        container.settings.setAttemptNo(attempt + 1)
                                        filled = true
                                        stage = 2
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = gold,
                                    contentColor = Color(0xFF0B1026)
                                )
                            ) {
                                Text(
                                    "Saya Sudah Bertaubat, Mulai Lagi",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "Refleksi lengkap (pemicu & catatan) bisa kamu isi dari aplikasi setelah ini.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        else -> {
                            Text(
                                text = "Tercatat. Barakallahu fiik.",
                                color = gold,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Alarm selesai untuk hari ini. Subuh berikutnya sudah dijadwalkan.",
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(28.dp))
                            Button(
                                onClick = { finishAndSchedule() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = gold,
                                    contentColor = Color(0xFF0B1026)
                                )
                            ) {
                                Text("Selesai", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
              }
            }
        }
    }

    private fun finishAndSchedule() {
        NotifHelper.cancel(this, NotifHelper.NOTIF_ID_CHECKIN)
        AlarmScheduler.scheduleNextFajr(this)
        finish()
    }
}
