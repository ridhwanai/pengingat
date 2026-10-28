package id.pengingat.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.pengingat.app.R
import id.pengingat.app.data.FajrCalculator
import id.pengingat.app.data.StreakCalculator
import id.pengingat.app.ui.theme.Gold
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    vm: AppViewModel,
    modifier: Modifier = Modifier,
    onOpenCheckin: () -> Unit,
    onOpenLibrary: () -> Unit
) {
    val entries by vm.entries.collectAsState()
    val settings by vm.settings.collectAsState()
    val today = remember { LocalDate.now() }
    val yesterday = today.minusDays(1)

    val streak = remember(entries) { StreakCalculator.currentStreak(entries, today) }
    val best = remember(entries) { StreakCalculator.bestStreak(entries) }
    val total = remember(entries) { StreakCalculator.totalAman(entries) }
    val yesterdayEntry = entries.firstOrNull { it.date == yesterday.toString() }
    val yesterdayFilled = yesterdayEntry?.aman != null

    val dailyQuote = remember(today) { vm.container.quotes.dailyQuote(today, "widget") }
    val fajrToday = remember(settings.fajrOffsetMin) {
        FajrCalculator.fajrDateTime(today, settings.fajrOffsetMin)
    }
    val fajrTomorrow = remember(settings.fajrOffsetMin) {
        FajrCalculator.fajrDateTime(today.plusDays(1), settings.fajrOffsetMin)
    }
    val timeFmt = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val dateFmt = remember {
        DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("id", "ID"))
    }

    SceneryBackground(
        resId = R.drawable.bg_home,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Pengingat",
                color = Gold,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = today.format(dateFmt),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                StreakRing(streak = streak)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Terbaik: $best hari  •  Total hari aman: $total",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))
            ElegantCard(modifier = Modifier.fillMaxWidth()) {
                if (yesterdayFilled) {
                    Text(
                        text = if (yesterdayEntry?.aman == true)
                            "Kemarin: Aman. Alhamdulillah, pertahankan."
                        else
                            "Kemarin sudah tercatat. Hari ini lembaran baru — mulai lagi.",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Text(
                        text = "Check-in kemarin belum diisi.",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(10.dp))
                    GoldButton(
                        text = "Isi Check-in: Kemarin Aman Kan?",
                        onClick = onOpenCheckin,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionTitle("Refleksi Hari Ini")
            Spacer(Modifier.height(8.dp))
            QuoteCard(quote = dailyQuote)
            Spacer(Modifier.height(8.dp))
            OutlineGoldButton(
                text = "Buka Perpustakaan Refleksi",
                onClick = onOpenLibrary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            ElegantCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Subuh hari ini: ${fajrToday.format(timeFmt)} WIB",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Subuh besok: ${fajrTomorrow.format(timeFmt)} WIB — alarm akan berbunyi mengikuti waktu ini.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
