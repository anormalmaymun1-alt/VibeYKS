package com.example.yksaisinavkocu.ui.screen.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yksaisinavkocu.theme.*
import com.example.yksaisinavkocu.ui.components.GlassCard
import com.example.yksaisinavkocu.ui.components.SectionHeader

@Composable
fun UploadSourceGrid(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onPdfClick: () -> Unit,
    onManualClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SectionHeader(
            icon = Icons.Rounded.CloudUpload,
            title = "Veri Yükleme Seçenekleri",
            tint = AeroSkyBlue
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 1. Kamera
        ScanActionCard(
            title = "Kamera ile Anında Çek",
            subtitle = "Fiziksel deneme karnenizin fotoğrafını çekin",
            icon = Icons.Rounded.PhotoCamera,
            gradient = Brush.linearGradient(listOf(AeroSkyBlue, AeroDeepBlue)),
            onClick = onCameraClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Galeri
        ScanActionCard(
            title = "Galeriden Karne Seç",
            subtitle = "Önceden çektiğiniz net ve aydınlık bir görsel seçin",
            icon = Icons.Rounded.PhotoLibrary,
            gradient = Brush.linearGradient(listOf(Color(0xFF26A69A), Color(0xFF00695C))),
            onClick = onGalleryClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. PDF
        ScanActionCard(
            title = "PDF Sonuç Belgesi Yükle",
            subtitle = "PdfRenderer ile 300 DPI işlenerek çoklu sayfa taranır",
            icon = Icons.Rounded.PictureAsPdf,
            gradient = Brush.linearGradient(listOf(AeroPurple, Color(0xFF4A148C))),
            onClick = onPdfClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Manuel Giriş
        ScanActionCard(
            title = "Manuel Sınav Girişi",
            subtitle = "Karneniz yoksa ders doğru/yanlışlarını elle girin",
            icon = Icons.Rounded.EditNote,
            gradient = Brush.linearGradient(listOf(AeroSunset, AeroCoral)),
            onClick = onManualClick
        )
    }
}

@Composable
fun ScanActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        cornerRadius = 20.dp,
        alpha = 0.12f,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(gradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
