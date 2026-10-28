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
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import id.pengingat.app.MainActivity
import id.pengingat.app.PengingatApp
import id.pengingat.app.R
import id.pengingat.app.data.StreakCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** Widget ringkas 2x2: angka streak besar + label. Ketuk membuka aplikasi. */
class PengingatWidgetSmall : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val streak = withContext(Dispatchers.IO) {
            val entries = PengingatApp.instance.container.progress.entriesFlow().first()
            StreakCalculator.currentStreak(entries, LocalDate.now())
        }
        val openApp = Intent(context, MainActivity::class.java)

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
                    .padding(12.dp)
                    .clickable(actionStartActivity(openApp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = streak.toString(),
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFD4AF37)),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "hari bersih",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFF5EFE0)),
                        fontSize = 13.sp
                    )
                )
              }
            }
        }
    }
}

class PengingatWidgetSmallReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PengingatWidgetSmall()
}
