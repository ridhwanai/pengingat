package id.pengingat.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.pengingat.app.ui.theme.Gold
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class CheckinStage { Ask, SlipForm, DoneAman, DoneSlip }

private val TRIGGERS = listOf("Bosan", "Sendiri", "Lelah", "Stres", "Emosi", "Aplikasi", "Lainnya")

/**
 * Layar check-in "Kemarin aman kan?" — dipakai UI utama.
 * Alur: tanya -> (Aman: apresiasi) / (Tidak aman: form refleksi lembut) -> selesai.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckinScreen(vm: AppViewModel, onDone: () -> Unit) {
    val settings by vm.settings.collectAsState()
    var stage by remember { mutableStateOf(CheckinStage.Ask) }
    var trigger by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf("") }

    val yesterday = remember { vm.yesterday() }
    val dateFmt = remember {
        DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("id", "ID"))
    }
    val syukurQuote = remember { vm.container.quotes.random("syukur", System.nanoTime()) }
    val bangkitQuote = remember { vm.container.quotes.random("bangkit", System.nanoTime()) }

  SceneryBackground(resId = id.pengingat.app.R.drawable.bg_home) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Check-in Pagi",
            color = Gold,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Untuk ${yesterday.format(dateFmt)}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(20.dp))

        when (stage) {
            CheckinStage.Ask -> {
                Text(
                    text = "Kemarin aman kan?",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Jawablah dengan jujur — catatan ini hanya untukmu dan Allah.",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))
                GoldButton(
                    text = "Aman, Alhamdulillah",
                    onClick = {
                        vm.doCheckin(aman = true, trigger = null, note = null)
                        stage = CheckinStage.DoneAman
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlineGoldButton(
                    text = "Tidak Aman",
                    onClick = { stage = CheckinStage.SlipForm },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                OutlineGoldButton(
                    text = "Nanti Dulu",
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            CheckinStage.SlipForm -> {
                Text(
                    text = "Tidak apa-apa. Yang penting kamu kembali.",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Allah Maha Pengampun. Catat sebentar apa pemicunya, agar besok kamu lebih siap — bukan untuk menghukum diri.",
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Text(text = "Pemicunya apa?", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TRIGGERS.forEach { t ->
                        FilterChip(
                            selected = trigger == t,
                            onClick = { trigger = t },
                            label = { Text(t) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Catatan (opsional)") },
                    minLines = 2
                )
                Spacer(Modifier.height(16.dp))
                QuoteCard(quote = bangkitQuote)
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Percobaan ke-${settings.attemptNo + 1} dimulai hari ini. Satu langkah lagi.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(10.dp))
                GoldButton(
                    text = "Saya Sudah Bertaubat, Mulai Lagi",
                    onClick = {
                        vm.doCheckin(aman = false, trigger = trigger, note = note.ifBlank { null })
                        stage = CheckinStage.DoneSlip
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            CheckinStage.DoneAman -> {
                Text(
                    text = "Alhamdulillah. Satu hari lagi terjaga.",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))
                QuoteCard(quote = syukurQuote)
                Spacer(Modifier.height(20.dp))
                GoldButton(
                    text = "Selesai",
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            CheckinStage.DoneSlip -> {
                Text(
                    text = "Tercatat. Hari ini hari baru.",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Jangan berputus asa dari rahmat Allah. Bangkit, wudhu, sholat — dan lanjutkan perjuanganmu.",
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))
                GoldButton(
                    text = "Selesai",
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
  }
}
