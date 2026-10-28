package id.pengingat.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.pengingat.app.ui.theme.Gold
import id.pengingat.app.ui.theme.SoftRed
import androidx.compose.foundation.layout.ExperimentalLayoutApi

private val TAG_LABELS = listOf(
    null to "Semua",
    "subuh" to "Subuh",
    "checkin" to "Check-in",
    "godaan" to "Godaan",
    "bangkit" to "Bangkit",
    "syukur" to "Syukur",
    "widget" to "Widget",
    "acak" to "Acak"
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(vm: AppViewModel, modifier: Modifier = Modifier) {
    val settings by vm.settings.collectAsState()
    var activeTag by remember { mutableStateOf<String?>(null) }

    val filtered = remember(activeTag, vm.quotes) {
        if (activeTag == null) vm.quotes else vm.quotes.filter { activeTag in it.tags }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        SectionTitle("Perpustakaan Refleksi")
        Spacer(Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TAG_LABELS.forEach { (tag, label) ->
                FilterChip(
                    selected = activeTag == tag,
                    onClick = { activeTag = tag },
                    label = { Text(label) }
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filtered, key = { it.id }) { quote ->
                Column {
                    QuoteCard(quote = quote)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = quote.tags.joinToString(" • "),
                            modifier = Modifier.weight(1f),
                            color = Gold,
                            fontSize = 12.sp
                        )
                        val isFav = quote.source in settings.favorites
                        IconButton(onClick = { vm.toggleFavorite(quote.source) }) {
                            Icon(
                                if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorit",
                                tint = if (isFav) SoftRed else Gold
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
