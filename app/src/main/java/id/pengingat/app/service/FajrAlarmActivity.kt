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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.pengingat.app.PengingatApp
import id.pengingat.app.data.FajrCalculator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Layar alarm Subuh (tahap 1): membangunkan untuk sholat.
 * Pilihan: "Sholat Dulu" (check-in ditunda) atau "Isi Check-in Sekarang".
 */
class FajrAlarmActivity : ComponentActivity() {

    @Deprecated("Back sengaja dinonaktifkan di layar alarm")
    override fun onBackPressed() {
        // Sengaja kosong: alarm tidak bisa dibatalkan dengan Back.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GuardService.checkinShowing = false

        val container = PengingatApp.instance.container
        val settings = runBlocking { container.settings.state.first() }
        val fajrToday = FajrCalculator.fajrDateTime(LocalDate.now(), settings.fajrOffsetMin)
        val timeText = fajrToday.format(DateTimeFormatter.ofPattern("HH:mm"))

        setContent {
            ServiceTheme {
              ServiceBackground(id.pengingat.app.R.drawable.bg_fajr) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    val gold = Color(0xFFD4AF37)
                    Spacer(Modifier.height(32.dp))
                    Text(
                        text = "Subuh telah tiba",
                        color = gold,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pukul $timeText WIB — waktunya sholat Subuh.",
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(28.dp))
                    Text(
                        text = "أَقِمِ الصَّلَاةَ لِدُلُوكِ الشَّمْسِ إِلَىٰ غَسَقِ اللَّيْلِ وَقُرْآنَ الْفَجْرِ ۖ إِنَّ قُرْآنَ الْفَجْرِ كَانَ مَشْهُودًا",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        fontFamily = FontFamily.Serif,
                        fontSize = 22.sp,
                        lineHeight = 36.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "\u201CDirikanlah sholat sejak matahari tergelincir hingga gelapnya malam, dan (dirikanlah pula) sholat Subuh. Sesungguhnya sholat Subuh itu disaksikan (oleh malaikat).\u201D",
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "QS. Al-Isra [17]: 78",
                        color = gold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(36.dp))
                    Button(
                        onClick = {
                            AlarmScheduler.scheduleCheckinIn(
                                this@FajrAlarmActivity,
                                settings.prayerDelayMin
                            )
                            NotifHelper.cancel(
                                this@FajrAlarmActivity,
                                NotifHelper.NOTIF_ID_FAJR
                            )
                            finish()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = gold,
                            contentColor = Color(0xFF0B1026)
                        )
                    ) {
                        Text(
                            "Sholat Dulu (check-in ${settings.prayerDelayMin} menit lagi)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            NotifHelper.cancel(
                                this@FajrAlarmActivity,
                                NotifHelper.NOTIF_ID_FAJR
                            )
                            startActivity(
                                Intent(
                                    this@FajrAlarmActivity,
                                    CheckinActivity::class.java
                                )
                            )
                            finish()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Isi Check-in Sekarang", color = gold)
                    }
                    Spacer(Modifier.height(24.dp))
                }
              }
            }
        }
    }
}
