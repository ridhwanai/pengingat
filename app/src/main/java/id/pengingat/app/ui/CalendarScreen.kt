package id.pengingat.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.pengingat.app.data.DayEntry
import id.pengingat.app.data.StreakCalculator
import id.pengingat.app.ui.theme.Emerald
import id.pengingat.app.ui.theme.Gold
import id.pengingat.app.ui.theme.NavySurfaceVar
import id.pengingat.app.ui.theme.SoftRed
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarScreen(vm: AppViewModel, modifier: Modifier = Modifier) {
    val entries by vm.entries.collectAsState()
    val today = remember { LocalDate.now() }
    val month = remember { YearMonth.now() }
    var selected by remember { mutableStateOf<DayEntry?>(null) }

    val streak = remember(entries) { StreakCalculator.currentStreak(entries, today) }
    val best = remember(entries) { StreakCalculator.bestStreak(entries) }
    val total = remember(entries) { StreakCalculator.totalAman(entries) }
    val pct = remember(entries) { StreakCalculator.percentAmanInMonth(entries, month) }
    val byDate = remember(entries) { entries.associateBy { it.date } }

    val monthFmt = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale("id", "ID")) }
    val firstDay = month.atDay(1)
    // Senin = 1 .. Minggu = 7 (DayOfWeek.value); kalender dimulai Senin.
    val startOffset = firstDay.dayOfWeek.value - 1
    val daysInMonth = month.lengthOfMonth()
    val cells = startOffset + daysInMonth
    val rows = (cells + 6) / 7

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        SectionTitle("Progres")
        Spacer(Modifier.height(12.dp))
        ElegantCard(modifier = Modifier.fillMaxWidth()) {
            Text("Streak berjalan: $streak hari", fontSize = 15.sp)
            Text("Streak terbaik: $best hari", fontSize = 15.sp)
            Text("Total hari aman: $total", fontSize = 15.sp)
            Text("Bulan ini: $pct% hari terisi berakhir aman", fontSize = 15.sp, color = Gold)
        }

        Spacer(Modifier.height(20.dp))
        Text(
            text = month.format(monthFmt),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min").forEach { d ->
                Text(
                    text = d,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        for (r in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (c in 0 until 7) {
                    val idx = r * 7 + c
                    val dayNum = idx - startOffset + 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dayNum in 1..daysInMonth) {
                            val date = month.atDay(dayNum)
                            val entry = byDate[date.toString()]
                            val bg: Color = when (entry?.aman) {
                                true -> Emerald.copy(alpha = 0.75f)
                                false -> SoftRed.copy(alpha = 0.65f)
                                else -> NavySurfaceVar
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(bg)
                                    .then(
                                        if (date == today)
                                            Modifier.border(2.dp, Gold, RoundedCornerShape(10.dp))
                                        else Modifier
                                    )
                                    .clickable(enabled = entry != null) { selected = entry },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNum.toString(),
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            text = "Hijau = aman • Merah lembut = tidak aman • Gelap = belum terisi. Ketuk tanggal terisi untuk detail.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
    }

    selected?.let { e ->
        AlertDialog(
            onDismissRequest = { selected = null },
            confirmButton = {
                TextButton(onClick = { selected = null }) { Text("Tutup") }
            },
            title = { Text(e.date) },
            text = {
                Column {
                    Text(if (e.aman == true) "Status: Aman" else "Status: Tidak aman")
                    e.trigger?.let { Text("Pemicu: $it") }
                    e.note?.let { if (it.isNotBlank()) Text("Catatan: $it") }
                    Text("Percobaan ke-${e.attempt}")
                    if (e.edited) Text("(pernah dikoreksi)")
                }
            }
        )
    }
}
