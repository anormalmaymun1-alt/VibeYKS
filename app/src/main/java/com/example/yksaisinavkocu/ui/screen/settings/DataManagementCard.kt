package com.example.yksaisinavkocu.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.yksaisinavkocu.theme.AeroSkyBlue
import com.example.yksaisinavkocu.theme.ErrorRed
import com.example.yksaisinavkocu.ui.components.GlassCard
import com.example.yksaisinavkocu.ui.components.SectionHeader

@Composable
fun DataManagementCard(
    onLoadSampleClick: () -> Unit,
    onClearDataClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(cornerRadius = 20.dp, alpha = 0.1f, modifier = modifier) {
        SectionHeader(
            icon = Icons.Rounded.Storage,
            title = "Veri Yönetimi",
            tint = AeroSkyBlue
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onLoadSampleClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Rounded.FileDownload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Örnek Veri Yükle (Test)")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onClearDataClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ErrorRed
            )
        ) {
            Icon(Icons.Rounded.DeleteForever, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tüm Verileri Sil")
        }
    }
}
