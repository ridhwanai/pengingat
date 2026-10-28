package id.pengingat.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import id.pengingat.app.MainActivity
import id.pengingat.app.PengingatApp
import id.pengingat.app.R
import id.pengingat.app.data.FajrCalculator
import id.pengingat.app.data.StreakCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Widget utama 4x2 (PRD §4.4): streak besar, kutipan refleksi harian,
 * Subuh besok, dan tombol check-in cepat.
 */
class PengingatWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = withContext(Dispatchers.IO) {
            val container = PengingatApp.instance.container
            val entries = container.progress.entriesFlow().first()
            val settings = container.settings.state.first()
            val today = LocalDate.now()
            Triple(
                StreakCalculator.currentStreak(entries, today),
                container.quotes.dailyQuote(today, "widget"),
                FajrCalculator.fajrDateTime(today.plusDays(1), settings.fajrOffsetMin)
            )
        }
        val (streak, quote, fajrTomorrow) = data
        val fajrText = fajrTomorrow.format(DateTimeFormatter.ofPattern("HH:mm"))
        val openApp = Intent(context, MainActivity::class.java)
        val openCheckin = Intent(context, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_SCREEN, MainActivity.SCREEN_CHECKIN)
        }

        provideContent {
            Box(modifier = GlanceModifier.fillMaxSize()) {
                Image(
                    provider = ImageProvider(R.drawable.bg_widget),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = GlanceModifier.fillMaxSize()
                )
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(Color(0x8C0B1026))
                ) {}
              Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(14.dp)
                    .clickable(actionStartActivity(openApp))
            ) {
                Text(
                    text = "$streak hari bersih",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFD4AF37)),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.height(6.dp))
                Text(
                    text = quote.translation,
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFF5EFE0)),
                        fontSize = 13.sp
                    ),
                    maxLines = 3
                )
                Text(
                    text = quote.source,
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFE8C874)),
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = GlanceModifier.height(8.dp))
                Text(
                    text = "Subuh besok $fajrText WIB  •  Ketuk untuk check-in",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFB9BED6)),
                        fontSize = 12.sp
                    ),
                    modifier = GlanceModifier.clickable(actionStartActivity(openCheckin))
                )
              }
            }
        }
    }
}

class PengingatWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PengingatWidget()
}
