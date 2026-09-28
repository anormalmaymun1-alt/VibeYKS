package com.example.yksaisinavkocu.ui.screen.settings

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.yksaisinavkocu.theme.AeroDeepBlue
import com.example.yksaisinavkocu.theme.ErrorRed

@Composable
fun ClearDataDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tüm Verileri Sil") },
        text = { Text("Tüm sınav kayıtları silinecek. Bu işlem geri alınamaz.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Sil", color = ErrorRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
fun LoadSampleDataDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Örnek Veri Yükle") },
        text = { Text("8 TYT + 8 AYT deneme sınavı örnek verisi yüklenecek.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Yükle", color = AeroDeepBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
