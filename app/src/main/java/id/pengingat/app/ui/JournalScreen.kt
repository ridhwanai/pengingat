package id.pengingat.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

@Composable
fun JournalScreen(vm: AppViewModel, modifier: Modifier = Modifier) {
    val journal by vm.journal.collectAsState()
    var draft by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        SectionTitle("Jurnal Refleksi")
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Tulis apa pun yang ingin kamu ingat dari perjuanganmu hari ini.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Refleksi baru...") },
            minLines = 3
        )
        Spacer(Modifier.height(8.dp))
        GoldButton(
            text = "Simpan Jurnal",
            onClick = {
                vm.addJournal(draft)
                draft = ""
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = draft.isNotBlank()
        )
        Spacer(Modifier.height(16.dp))

        if (journal.isEmpty()) {
            Text(
                text = "Belum ada jurnal. Mulai dari satu kalimat jujur hari ini.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(journal, key = { it.id }) { entry ->
                ElegantCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.date,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { vm.deleteJournal(entry.id) }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Hapus jurnal",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(text = entry.text, fontSize = 15.sp, lineHeight = 22.sp)
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}
