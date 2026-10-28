package id.pengingat.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.pengingat.app.data.Quote
import id.pengingat.app.ui.theme.Gold
import id.pengingat.app.ui.theme.NavySurface

@Composable
fun GoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Gold,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun OutlineGoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        border = BorderStroke(1.dp, Gold),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(text = text, color = Gold, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Composable
fun ElegantCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Gold.copy(alpha = 0.45f)),
        colors = CardDefaults.cardColors(containerColor = NavySurface.copy(alpha = 0.88f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) { content() }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        color = Gold,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

/** Kartu kutipan: Arab besar rata kanan, terjemahan, sumber emas, lalu refleksi. */
@Composable
fun QuoteCard(quote: Quote, modifier: Modifier = Modifier) {
    ElegantCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = quote.arab,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
            fontFamily = FontFamily.Serif,
            fontSize = 22.sp,
            lineHeight = 34.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "\u201C${quote.translation}\u201D",
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = quote.source,
            color = Gold,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (quote.reflection.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = quote.reflection,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Tampilan streak: angka besar emas + ringkasan terbaik & total. */
@Composable
fun StreakDisplay(streak: Int, best: Int, totalAman: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = streak.toString(),
            color = Gold,
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "hari bersih beruntun",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Terbaik: $best hari  •  Total hari aman: $totalAman",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
    }
}
